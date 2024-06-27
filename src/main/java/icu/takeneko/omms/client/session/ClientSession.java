package icu.takeneko.omms.client.session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import icu.takeneko.omms.client.data.announcement.Announcement;
import icu.takeneko.omms.client.data.broadcast.Broadcast;
import icu.takeneko.omms.client.data.broadcast.MessageCache;
import icu.takeneko.omms.client.data.controller.Controller;
import icu.takeneko.omms.client.data.controller.Status;
import icu.takeneko.omms.client.data.permission.PermissionOperation;
import icu.takeneko.omms.client.data.system.SystemInfo;
import icu.takeneko.omms.client.exception.PermissionDeniedException;
import icu.takeneko.omms.client.exception.ServerInternalErrorException;
import icu.takeneko.omms.client.session.callback.*;
import icu.takeneko.omms.client.session.handler.CallbackHandle;
import icu.takeneko.omms.client.session.handler.ResponseHandlerDelegate;
import icu.takeneko.omms.client.session.handler.ResponseHandlerDelegateImpl;
import icu.takeneko.omms.client.session.request.Request;
import icu.takeneko.omms.client.session.response.Response;
import icu.takeneko.omms.client.util.EncryptedConnector;
import icu.takeneko.omms.client.util.Result;

import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@SuppressWarnings("unused")
public class ClientSession extends Thread {
    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private final HashMap<String, List<String>> whitelistMap = new HashMap<>();
    private final HashMap<String, Controller> controllerMap = new HashMap<>();
    private final HashMap<String, Announcement> announcementMap = new HashMap<>();
    private final HashMap<String, CallbackHandle<SessionContext>> controllerConsoleAssocMap = new HashMap<>();
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private final Socket socket;
    private final String serverName;
    EncryptedConnector connector;
    @SuppressWarnings("FieldMayBeFinal")
    private SystemInfo systemInfo = null;
    private PermissionOperation lastPermissionOperation;
    private final ResponseHandlerDelegate<Result, SessionContext, CallbackHandle<SessionContext>> delegate;
    private Callback<ClientSession> onPermissionDeniedCallback;
    private Callback<Response> onServerInternalExceptionCallback;
    private Callback<PermissionOperation> onInvalidOperationCallback;
    private Callback2<Thread, Throwable> onAnyExceptionCallback = (t, e) -> {
        System.out.printf("Exception in thread %s%n", t);
        e.printStackTrace();
    };
    private Callback<Response> onResponseRecievedCallback;
    private Callback<String> onDisconnectedCallback;
    private Callback<Broadcast> onNewBroadcastReceivedCallback;

    boolean chatMessagePassthroughEnabled = true;

    public ClientSession(EncryptedConnector connector, Socket socket, String serverName) {
        super("ClientSessionThread");
        this.serverName = serverName;
        this.connector = connector;
        this.socket = socket;
        delegate = new ResponseHandlerDelegateImpl<>();
        delegate.register(Result.BROADCAST_MESSAGE, new JsonObjectCallbackHandle<Broadcast>("broadcast", (broadcast) -> {
            if (onNewBroadcastReceivedCallback != null) {
                onNewBroadcastReceivedCallback.accept(broadcast);
            }
        }) {
            @Override
            protected TypeToken<Broadcast> getObjectType() {
                return TypeToken.get(Broadcast.class);
            }
        }, false);
        delegate.setOnExceptionThrownHandler(e -> {
            if (onAnyExceptionCallback != null) {
                onAnyExceptionCallback.accept(Thread.currentThread(), e);
            }
            else throw new RuntimeException(e);
        });
    }

    public boolean isActive() {
        return !socket.isClosed() && this.isAlive();
    }

    public static class UncaughtExceptionHandlerImpl implements UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {

        }
    }

    @Override
    public void run() {
        Response response;
        while (true) {
            try {
                response = gson.fromJson(connector.readLine(), Response.class);
                if (onResponseRecievedCallback != null) {
                    onResponseRecievedCallback.accept(response);
                }
                handleResponse(response);
            } catch (DisconnectedException | SocketException ignored) {
                if (onDisconnectedCallback != null) {
                    onDisconnectedCallback.accept(this.serverName);
                }
                break;
            } catch (Exception e) {
                if (onAnyExceptionCallback != null)
                    onAnyExceptionCallback.accept(currentThread(), e);
            }
        }
    }

    private void handleResponse(Response response) throws Exception {
        if (response.getResponseCode() == Result.RATE_LIMIT_EXCEEDED) {
            this.socket.close();
            throw new RateExceedException("Connection closed because request rate exceeded.");
        }
        switch (response.getResponseCode()) {
            case FAIL:
                if (onServerInternalExceptionCallback == null)
                    throw new ServerInternalErrorException("Got FAIL from server.");
                else {
                    onServerInternalExceptionCallback.accept(response);
                }
                break;
            case PERMISSION_DENIED:
                if (onPermissionDeniedCallback == null)
                    throw new PermissionDeniedException("Permission Denied.");
                else
                    onPermissionDeniedCallback.accept(this);
                break;
            case OPERATION_ALREADY_EXISTS:
                if (onInvalidOperationCallback != null) {
                    onInvalidOperationCallback.accept(lastPermissionOperation);
                    onInvalidOperationCallback = null;
                }
                break;
            case DISCONNECT:
                throw new DisconnectedException();
            default:
                delegate.handle(response.getResponseCode(), new SessionContext(response, this));
        }
    }

    private void syncStatus() {

    }

    public void send(Request request) {
        networkExecutor.submit(() -> {
            String content = gson.toJson(request);
            try {
                connector.println(content);
            } catch (Throwable e) {
                onAnyExceptionCallback.accept(currentThread(), e);
            }
        });
    }

    public Response sendBlocking(Request request, Result... expectedValue) throws Exception {
        String content = gson.toJson(request);
        connector.println(content);
        String s = connector.readLine();
        Response response = gson.fromJson(s, Response.class);
        while (true) {// tiny loop
            if (onResponseRecievedCallback != null) {
                onResponseRecievedCallback.accept(response);
            }
            if (response.getResponseCode() == Result.RATE_LIMIT_EXCEEDED) {
                this.socket.close();
                throw new RateExceedException("Connection closed because request rate exceeded.");
            } else {
                Response finalResponse = response;
                if (Arrays.stream(expectedValue).anyMatch(result -> result == finalResponse.getResponseCode())) {
                    break;
                } else {
                    handleResponse(response);
                }
            }
            s = connector.readLine();
            response = gson.fromJson(s, Response.class);
        }
        return response;
    }

    public void close(Callback<String> onDisconnectedCallback) {
        setOnDisconnectedCallback(onDisconnectedCallback);
        send(new Request("END"));
        networkExecutor.shutdownNow();
    }

    public void fetchWhitelistFromServer(Callback<Map<String, List<String>>> callback) {
        CallbackHandle<SessionContext> cb = new WhitelistListCallbackHandle(callback);
        String groupId = Long.toString(System.nanoTime());
        cb.setAssociateGroupId(groupId);
        delegate.registerOnce(Result.WHITELIST_LISTED, cb);
        delegate.registerOnce(Result.NO_WHITELIST, cb);
        send(new Request("WHITELIST_LIST"));
    }

    public void fetchControllersFromServer(Callback<Map<String, Controller>> callback) {
        delegate.registerOnce(Result.CONTROLLER_LISTED, new ControllerListCallbackHandle(callback));
        send(new Request("CONTROLLER_LIST"));
    }

    public void fetchSystemInfoFromServer(Callback<SystemInfo> fn) {
        delegate.registerOnce(Result.SYSINFO_GOT, new SystemInfoCallbackHandle((si) ->{
            this.systemInfo = si;
            fn.accept(si);
        }));
        send(new Request("SYSTEM_GET_INFO"));
    }

    public void fetchAnnouncementFromServer(Callback<Map<String, Announcement>> callback) {
        delegate.registerOnce(Result.ANNOUNCEMENT_LISTED, new AnnouncementListCallbackHandle(callback));
        send(new Request().setRequest("ANNOUNCEMENT_LIST"));
    }

    public void fetchControllerStatus(String controllerId,
                                      Callback<Status> onStatusReceivedCallback,
                                      Callback<String> onControllerNotExistCallback
    ) {
        CallbackHandle<SessionContext> c1 = new StatusCallbackHandle(onStatusReceivedCallback);
        if (onControllerNotExistCallback != null) {
            CallbackHandle<SessionContext> c2 = new StringCallbackHandle("controller", onControllerNotExistCallback);
            String groupId = Long.toString(System.nanoTime());
            c1.setAssociateGroupId(groupId);
            c2.setAssociateGroupId(groupId);
            delegate.registerOnce(Result.CONTROLLER_NOT_EXIST, c2);
        }
        delegate.registerOnce(Result.CONTROLLER_STATUS_GOT, c1);
        send(new Request().setRequest("CONTROLLER_GET_STATUS").withContentKeyPair("id", controllerId));
    }


    public void removeFromWhitelist(String whitelistName,
                                    String player,
                                    Callback2<String, String> onPlayerRemovedCallback,
                                    Callback2<String, String> onPlayerNotExistCallback
    ) {
        CallbackHandle<SessionContext> c1 = new BiStringCallbackHandle("whitelist", "player", onPlayerRemovedCallback);
        if (onPlayerNotExistCallback != null) {
            CallbackHandle<SessionContext> c2 = new BiStringCallbackHandle("whitelist", "player", onPlayerNotExistCallback);
            String groupId = Long.toString(System.nanoTime());
            c1.setAssociateGroupId(groupId);
            c2.setAssociateGroupId(groupId);
            delegate.registerOnce(Result.PLAYER_NOT_EXIST, c2);
        }
        delegate.registerOnce(Result.WHITELIST_REMOVED, c1);
        this.send(new Request("WHITELIST_REMOVE")
                .withContentKeyPair("whitelist", whitelistName)
                .withContentKeyPair("player", player)
        );
    }

    public Result queryWhitelist(String whitelistName, String playerName) {
        if (whitelistMap.isEmpty()) {
            return Result.NO_WHITELIST;
        }
        if (!whitelistMap.containsKey(whitelistName)) {
            return Result.WHITELIST_NOT_EXIST;
        }
        if (whitelistMap.get(whitelistName).contains(playerName)) {
            return Result.OK;
        }
        return Result.NO_SUCH_PLAYER;
    }

    public ArrayList<String> queryInAllWhitelist(String playerName) {
        ArrayList<String> whitelists = new ArrayList<>();
        if (whitelistMap.isEmpty()) {
            return null;
        }
        whitelistMap.forEach((k, v) -> {
            if (v.contains(playerName)) {
                whitelists.add(k);
            }
        });
        if (whitelists.isEmpty()) {
            return null;
        }
        return whitelists;
    }

    public void startControllerConsole(String controller,
                                       Callback2<String, String> onControllerConsoleLaunchedCallback,
                                       Callback2<String, String> onControllerConsoleLogReceivedCallback,
                                       Callback<String> onControllerNotExistCallback,
                                       Callback<String> onControllerConsoleAlreadyExistsCallback
    ) {
        CallbackHandle<SessionContext> logRecv = new BiStringCallbackHandle("consoleId", "content", onControllerConsoleLogReceivedCallback);
        CallbackHandle<SessionContext> consoleLaunched = new BiStringCallbackHandle("controller", "consoleId", (ct, conId) -> {
            controllerConsoleAssocMap.put(conId, logRecv);
            onControllerConsoleLaunchedCallback.accept(ct, conId);
        });
        CallbackHandle<SessionContext> consoleExists = new StringCallbackHandle("controller", onControllerConsoleAlreadyExistsCallback);
        CallbackHandle<SessionContext> controllerNotExist = new StringCallbackHandle("controller", onControllerNotExistCallback);
        String groupId = Long.toString(System.nanoTime());
        consoleLaunched.setAssociateGroupId(groupId);
        consoleExists.setAssociateGroupId(groupId);
        //logRecv.setAssociateGroupId(groupId);
        controllerNotExist.setAssociateGroupId(groupId);
        delegate.registerOnce(Result.CONSOLE_LAUNCHED, consoleLaunched);
        delegate.registerOnce(Result.CONSOLE_ALREADY_EXISTS, consoleExists);
        delegate.registerOnce(Result.CONTROLLER_NOT_EXIST, controllerNotExist);
        delegate.register(Result.CONTROLLER_LOG, logRecv, false);
        send(new Request().setRequest("CONTROLLER_LAUNCH_CONSOLE").withContentKeyPair("controller", controller));
    }

    public void stopControllerConsole(String consoleId,
                                      Callback<String> onConsoleStoppedCallback,
                                      Callback<String> onConsoleNotFoundCallback
    ) {
        String groupId = Long.toString(System.nanoTime());
        CallbackHandle<SessionContext> conStopped = new StringCallbackHandle("consoleId", onConsoleStoppedCallback);
        CallbackHandle<SessionContext> conNotFound = new StringCallbackHandle("consoleId", onConsoleNotFoundCallback);
        conStopped.setAssociateGroupId(groupId);
        conNotFound.setAssociateGroupId(groupId);
        if (controllerConsoleAssocMap.containsKey(consoleId)) {
            controllerConsoleAssocMap.get(consoleId).setAssociateGroupId(groupId);
        }
        delegate.registerOnce(Result.CONSOLE_STOPPED, conStopped);
        delegate.registerOnce(Result.CONSOLE_NOT_EXIST, conNotFound);
        send(new Request().setRequest("CONTROLLER_END_CONSOLE").withContentKeyPair("consoleId", consoleId));
    }

    public void controllerConsoleInput(String consoleId,
                                       String line,
                                       Callback<String> onConsoleNotFoundCallback
    ) {
        controllerConsoleInput(consoleId, line, onConsoleNotFoundCallback, null);
    }

    public void controllerConsoleInput(String consoleId,
                                       String line,
                                       Callback<String> onConsoleNotFoundCallback,
                                       Callback<String> onControllerConsoleInputSendCallback
    ) {
        String groupId = Long.toString(System.nanoTime());
        CallbackHandle<SessionContext> conNotFound = new StringCallbackHandle("consoleId", onConsoleNotFoundCallback);
        CallbackHandle<SessionContext> conInput = new StringCallbackHandle("consoleId", onControllerConsoleInputSendCallback);
        conInput.setAssociateGroupId(groupId);
        conNotFound.setAssociateGroupId(groupId);
        delegate.registerOnce(Result.CONTROLLER_CONSOLE_INPUT_SENT, conInput);
        delegate.registerOnce(Result.CONSOLE_NOT_EXIST, conNotFound);
        send(new Request().setRequest("CONTROLLER_INPUT_CONSOLE")
                .withContentKeyPair("consoleId", consoleId)
                .withContentKeyPair("command", line)
        );
    }

    public void addToWhitelist(String whitelistName,
                               String player,
                               Callback2<String, String> resultCallback,
                               Callback2<String, String> onPlayerAlreadyExistsCallback
    ) {
        String groupId = Long.toString(System.nanoTime());
        CallbackHandle<SessionContext> added = new BiStringCallbackHandle("whitelist", "player", resultCallback);
        CallbackHandle<SessionContext> playerExists = new BiStringCallbackHandle("whitelist", "player", onPlayerAlreadyExistsCallback);
        playerExists.setAssociateGroupId(groupId);
        added.setAssociateGroupId(groupId);
        delegate.registerOnce(Result.WHITELIST_ADDED, added);
        delegate.registerOnce(Result.PLAYER_ALREADY_EXISTS, playerExists);
        this.send(new Request("WHITELIST_ADD")
                .withContentKeyPair("whitelist", whitelistName)
                .withContentKeyPair("player", player)
        );
    }


    public void sendCommandToController(String controller,
                                        String command,
                                        Callback2<String, List<String>> callback
    ) {
        sendCommandToController(controller, command, callback, null, null);
    }

    public void sendCommandToController(String controller,
                                        String command,
                                        Callback2<String, List<String>> callback,
                                        Callback<String> onControllerNotExistCallback,
                                        Callback<String> onControllerAuthFailedCallback
    ) {
        Request request = new Request()
                .setRequest("CONTROLLER_EXECUTE_COMMAND")
                .withContentKeyPair("controller", controller)
                .withContentKeyPair("command", command);
        String groupId = Long.toString(System.nanoTime());
        ControllerCommandLogCallbackHandle logCallbackHandle = new ControllerCommandLogCallbackHandle(callback);
        StringCallbackHandle notExist = new StringCallbackHandle("controllerId", onControllerNotExistCallback);
        StringCallbackHandle authFailed = new StringCallbackHandle("controllerId", onControllerAuthFailedCallback);
        logCallbackHandle.setAssociateGroupId(groupId);
        notExist.setAssociateGroupId(groupId);
        authFailed.setAssociateGroupId(groupId);
        delegate.registerOnce(Result.CONTROLLER_COMMAND_SENT, logCallbackHandle);
        delegate.registerOnce(Result.CONTROLLER_NOT_EXIST, notExist);
        delegate.registerOnce(Result.CONTROLLER_AUTH_FAILED, authFailed);
        send(request);
    }

    public void setChatMessagePassthroughState(boolean state, Callback<Boolean> onStateChangedCallback) {
        Request request = new Request()
                .setRequest("SET_CHAT_PASSTHROUGH_STATE")
                .withContentKeyPair("state", Boolean.toString(state));
        BooleanCallbackHandle cb = new BooleanCallbackHandle("state", onStateChangedCallback);
        delegate.registerOnce(Result.CHAT_PASSTHROUGH_STATE_CHANGED, cb);
        send(request);
    }

    public void sendBroadcastMessage(String channel, String message, Callback2<String, String> onMessageSentCallback) {
        Request request = new Request()
                .setRequest("SEND_BROADCAST")
                .withContentKeyPair("channel", channel)
                .withContentKeyPair("message", message);
        BiStringCallbackHandle cb = new BiStringCallbackHandle("channel", "message", onMessageSentCallback);
        delegate.registerOnce(Result.BROADCAST_SENT, cb);
        send(request);
    }

    public void getChatHistory(Callback<MessageCache> onMessageCacheReceivedCallback) {
        JsonObjectCallbackHandle<MessageCache> cb = new JsonObjectCallbackHandle<MessageCache>("content", onMessageCacheReceivedCallback) {
            @Override
            protected TypeToken<MessageCache> getObjectType() {
                return TypeToken.get(MessageCache.class);
            }
        };
        delegate.registerOnce(Result.GOT_CHAT_HISTORY, cb);
        send(new Request("GET_CHAT_HISTORY"));
    }


    public HashMap<String, List<String>> getWhitelistMap() {
        return whitelistMap;
    }

    public HashMap<String, Announcement> getAnnouncementMap() {
        return announcementMap;
    }

    public SystemInfo getSystemInfo() {
        return systemInfo;
    }

    public HashMap<String, Controller> getControllerMap() {
        return controllerMap;
    }

    public Controller getControllerByName(String name) {
        return controllerMap.get(name);
    }

    public String getServerName() {
        return serverName;
    }

    public void setOnPermissionDeniedCallback(Callback<ClientSession> onPermissionDeniedCallback) {
        this.onPermissionDeniedCallback = onPermissionDeniedCallback;
    }

    public void setOnServerInternalExceptionCallback(Callback<Response> onServerInternalExeptionCallback) {
        this.onServerInternalExceptionCallback = onServerInternalExeptionCallback;
    }

    public void setOnInvalidOperationCallback(Callback<PermissionOperation> onInvalidOperationCallback) {
        this.onInvalidOperationCallback = onInvalidOperationCallback;
    }

    public void setOnResponseReceivedCallback(Callback<Response> onResponseRecievedCallback) {
        this.onResponseRecievedCallback = onResponseRecievedCallback;
    }

    public void setOnDisconnectedCallback(Callback<String> onDisconnectedCallback) {
        this.onDisconnectedCallback = onDisconnectedCallback;
    }

    public void setOnNewBroadcastReceivedCallback(Callback<Broadcast> onNewBroadcastReceivedCallback) {
        this.onNewBroadcastReceivedCallback = onNewBroadcastReceivedCallback;
    }

    public EncryptedConnector getConnector() {
        return connector;
    }

    public ResponseHandlerDelegate<Result, SessionContext, CallbackHandle<SessionContext>> getDelegate() {
        return delegate;
    }
}
