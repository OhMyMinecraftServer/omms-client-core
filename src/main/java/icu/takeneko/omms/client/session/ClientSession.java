package icu.takeneko.omms.client.session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import icu.takeneko.omms.client.data.chatbridge.ChatMessage;
import icu.takeneko.omms.client.data.chatbridge.ChatbridgeImplementation;
import icu.takeneko.omms.client.data.chatbridge.MessageCache;
import icu.takeneko.omms.client.data.controller.Controller;
import icu.takeneko.omms.client.data.controller.Status;
import icu.takeneko.omms.client.data.permission.PermissionOperation;
import icu.takeneko.omms.client.data.system.SystemInfo;
import icu.takeneko.omms.client.session.callback.*;
import icu.takeneko.omms.client.session.data.FailureReasons;
import icu.takeneko.omms.client.session.data.SessionContext;
import icu.takeneko.omms.client.session.data.StatusEvent;
import icu.takeneko.omms.client.session.handler.CallbackHandle;
import icu.takeneko.omms.client.session.handler.EventSubscription;
import icu.takeneko.omms.client.session.handler.ResponseHandlerDelegate;
import icu.takeneko.omms.client.session.handler.ResponseHandlerDelegateImpl;
import icu.takeneko.omms.client.session.data.Request;
import icu.takeneko.omms.client.session.data.Response;
import icu.takeneko.omms.client.util.EncryptedConnector;
import icu.takeneko.omms.client.util.Util;
import lombok.Getter;
import lombok.Setter;

import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Session API
 */
@SuppressWarnings("unused")
@Getter
public class ClientSession extends Thread {
    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private final HashMap<String, List<String>> whitelistMap = new HashMap<>();
    private final HashMap<String, Controller> controllerMap = new HashMap<>();
    private final HashMap<String, CallbackHandle<SessionContext>> controllerConsoleAssocMap = new HashMap<>();
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private final Socket socket;
    private final String serverName;
    EncryptedConnector connector;
    private SystemInfo systemInfo = null;
    private PermissionOperation lastPermissionOperation;
    private final ResponseHandlerDelegate<SessionContext, CallbackHandle<SessionContext>> delegate;
    private final PermissionDeniedCallbackHandle<SessionContext> onPermissionDeniedCallback = new PermissionDeniedCallbackHandle<>();
    @Setter
    private Callback<Response> onServerInternalExceptionCallback;
    @Setter
    private Callback2<Thread, Throwable> onAnyExceptionCallback = (t, e) -> {
        System.out.printf("Exception in thread %s%n", t);
        e.printStackTrace();
    };
    private Callback<Response> onResponseRecievedCallback;
    @Setter
    private Callback0 onDisconnectedCallback;
    @Setter
    private Callback<ChatMessage> onNewChatMessageReceivedCallback;
    @Getter
    @Setter
    private String sessionName;

    boolean chatMessagePassthroughEnabled = true;

    public ClientSession(EncryptedConnector connector, Socket socket, String serverName) {
        super("ClientSessionThread");
        this.serverName = serverName;
        this.connector = connector;
        this.socket = socket;
        delegate = new ResponseHandlerDelegateImpl<>();
        delegate.setExceptionHandler(e -> {
            if (onAnyExceptionCallback != null) {
                onAnyExceptionCallback.accept(Thread.currentThread(), e);
            } else {
                throw new RuntimeException(e);
            }
        });
        sessionName = Integer.toString(Math.abs((serverName + ":" + new String(connector.getKey())).hashCode()));
    }

    public EventSubscription<SessionContext> subscribe(String requestId) {
        return delegate.subscribe(requestId)
            .subscribe(StatusEvent.PERMISSION_DENIED, onPermissionDeniedCallback);
    }

    public EventSubscription<SessionContext> subscribe() {
        return subscribe(newRequestId());
    }

    public String newRequestId() {
        return Util.generateNonce(16) + ":" + this;
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
                if (response.getEvent() == StatusEvent.BROADCAST) {
                    ChatMessage chatMessage = gson.fromJson(response.getContent("message"), ChatMessage.class);
                    onNewChatMessageReceivedCallback.accept(chatMessage);
                    continue;
                }
                if (response.getEvent() == StatusEvent.DISCONNECT) {
                    if (onDisconnectedCallback != null) {
                        onDisconnectedCallback.accept();
                    }
                    break;
                }
                delegate.handle(new SessionContext(response, this));
            } catch (DisconnectedException | SocketException ignored) {
                if (onDisconnectedCallback != null) {
                    onDisconnectedCallback.accept();
                }
                break;
            } catch (Exception e) {
                if (onAnyExceptionCallback != null)
                    onAnyExceptionCallback.accept(currentThread(), e);
            }
        }
    }

    private void syncStatus() {

    }

    public void send(Request request, String requestId) {
        networkExecutor.submit(() -> {
            request.setRequestId(requestId);
            String content = gson.toJson(request);
            try {
                connector.println(content);
            } catch (Throwable e) {
                onAnyExceptionCallback.accept(currentThread(), e);
            }
        });
    }

    public void close(Callback0 onDisconnectedCallback) {
        setOnDisconnectedCallback(onDisconnectedCallback);
        send(
            new Request("END"),
            newRequestId()
        );
        delegate.shutdown();
        networkExecutor.shutdownNow();
    }

    public void fetchWhitelistFromServer(Callback<Map<String, List<String>>> callback) {
        CallbackHandle<SessionContext> cb = new WhitelistListCallbackHandle(callback);
        send(
            new Request("WHITELIST_LIST"),
            subscribe()
                .subscribeSuccess(cb)
                .getRequestId()
        );
    }

    public void fetchControllersFromServer(Callback<Map<String, Controller>> callback) {
        send(
            new Request("CONTROLLER_LIST"),
            subscribe()
                .subscribeSuccess(new ControllerListCallbackHandle(callback))
                .getRequestId()
        );
    }

    public void fetchSystemInfoFromServer(Callback<SystemInfo> fn) {
        send(
            new Request("SYSTEM_GET_INFO"),
            subscribe()
                .subscribeSuccess(new SystemInfoCallbackHandle((si) -> {
                    this.systemInfo = si;
                    fn.accept(si);
                }))
                .getRequestId()
        );
    }

    public void fetchControllerStatus(String controllerId,
                                      Callback<Status> onStatusReceivedCallback,
                                      Callback0 onControllerNotExistCallback
    ) {
        CallbackHandle<SessionContext> c1 = new StatusCallbackHandle(onStatusReceivedCallback);
        EventSubscription<SessionContext> ctx = subscribe()
            .subscribeSuccess(c1);
        if (onControllerNotExistCallback != null) {
            CallbackHandle<SessionContext> c2 = new CallbackHandle0<>(onControllerNotExistCallback);
            ctx.subscribeFailure(c2);
        }
        send(
            new Request()
                .setRequest("CONTROLLER_GET_STATUS")
                .withContentKeyPair("id", controllerId),
            ctx.getRequestId()
        );
    }


    public void removeFromWhitelist(String whitelistName,
                                    String player,
                                    Callback0 onPlayerRemovedCallback,
                                    Callback0 onPlayerNotExistCallback
    ) {
        CallbackHandle<SessionContext> c1 = new CallbackHandle0<>(onPlayerRemovedCallback);
        EventSubscription<SessionContext> s = subscribe()
            .subscribeSuccess(c1);
        if (onPlayerNotExistCallback != null) {
            CallbackHandle<SessionContext> c2 = new CallbackHandle0<>(onPlayerNotExistCallback);
            s.subscribeFailure(c2);
        }
        this.send(
            new Request("WHITELIST_REMOVE")
                .withContentKeyPair("whitelist", whitelistName)
                .withContentKeyPair("player", player),
            s.getRequestId()
        );
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

    public void startControllerConsole(
        String controller,
        Callback2<String, String> onControllerConsoleLaunchedCallback,
        Callback2<String, String> onControllerConsoleLogReceivedCallback,
        Callback<String> onControllerNotExistCallback,
        Callback<String> onControllerConsoleAlreadyExistsCallback
    ) {
        EventSubscription<SessionContext> subscription = subscribe();
        CallbackHandle<SessionContext> logRecv = new BiStringCallbackHandle("consoleId", "content", onControllerConsoleLogReceivedCallback);
        CallbackHandle<SessionContext> consoleLaunched = new BiStringCallbackHandle("controller", "consoleId", (ct, conId) -> {
            controllerConsoleAssocMap.put(conId, logRecv);
            onControllerConsoleLaunchedCallback.accept(ct, conId);
        });
        CallbackHandle<SessionContext> consoleExists = new StringCallbackHandle("controller", onControllerConsoleAlreadyExistsCallback);
        CallbackHandle<SessionContext> controllerNotExist = new StringCallbackHandle("controller", onControllerNotExistCallback);
        subscription.subscribeAlways(StatusEvent.SUCCESS, new RawCallbackHandle<>(ctx -> {
            if (ctx.hasMarker("log")) {
                logRecv.invoke(ctx);
                return;
            }
            if (ctx.hasMarker("launched")) {
                consoleLaunched.invoke(ctx);
            }
        }));
        subscription.subscribeFailure(new RawCallbackHandle<>(ctx -> {
            if (ctx.hasReason(FailureReasons.CONSOLE_EXISTS)) {
                consoleExists.invoke(ctx);
                return;
            }
            if (ctx.hasReason(FailureReasons.CONTROLLER_NOT_FOUND)) {
                controllerNotExist.invoke(ctx);
            }
        }));
        send(
            new Request()
                .setRequest("CONTROLLER_LAUNCH_CONSOLE")
                .withContentKeyPair("controller", controller),
            subscription.getRequestId()
        );
    }

    public void stopControllerConsole(
        String consoleId,
        Callback<String> onConsoleStoppedCallback,
        Callback<String> onConsoleNotFoundCallback
    ) {
        CallbackHandle<SessionContext> conStopped = new StringCallbackHandle("consoleId", onConsoleStoppedCallback);
        CallbackHandle<SessionContext> conNotFound = new StringCallbackHandle("consoleId", onConsoleNotFoundCallback);
        send(
            new Request()
                .setRequest("CONTROLLER_END_CONSOLE")
                .withContentKeyPair("consoleId", consoleId),
            subscribe()
                .subscribeSuccess(conStopped)
                .subscribeFailure(conNotFound)
                .getRequestId()
        );
    }

    public void controllerConsoleInput(
        String consoleId,
        String line,
        Callback<String> onConsoleNotFoundCallback
    ) {
        controllerConsoleInput(
            consoleId,
            line,
            onConsoleNotFoundCallback,
            s -> {
            }
        );
    }

    public void controllerConsoleInput(
        String consoleId,
        String line,
        Callback<String> onConsoleNotFoundCallback,
        Callback<String> onControllerConsoleInputSendCallback
    ) {
        CallbackHandle<SessionContext> conNotFound = new StringCallbackHandle("consoleId", onConsoleNotFoundCallback);
        CallbackHandle<SessionContext> conInput = new StringCallbackHandle("consoleId", onControllerConsoleInputSendCallback);
        send(
            new Request().setRequest("CONTROLLER_INPUT_CONSOLE")
                .withContentKeyPair("consoleId", consoleId)
                .withContentKeyPair("command", line),
            subscribe()
                .subscribeSuccess(conInput)
                .subscribeFailure(conNotFound)
                .getRequestId()
        );
    }

    public void addToWhitelist(String whitelistName,
                               String player,
                               Callback2<String, String> resultCallback,
                               Callback2<String, String> onPlayerAlreadyExistsCallback
    ) {
        CallbackHandle<SessionContext> added = new BiStringCallbackHandle("whitelist", "player", resultCallback);
        CallbackHandle<SessionContext> playerExists = new BiStringCallbackHandle("whitelist", "player", onPlayerAlreadyExistsCallback);
        this.send(
            new Request("WHITELIST_ADD")
                .withContentKeyPair("whitelist", whitelistName)
                .withContentKeyPair("player", player),
            subscribe()
                .subscribeSuccess(added)
                .subscribeFailure(playerExists)
                .getRequestId()
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
        send(
            request,
            subscribe()
                .subscribeSuccess(logCallbackHandle)
                .subscribeFailure(new RawCallbackHandle<>(c -> {
                    if (c.hasReason(FailureReasons.CONTROLLER_UNAUTHORISED)) {
                        authFailed.invoke(c);
                        return;
                    }
                    if (c.hasReason(FailureReasons.CONTROLLER_NOT_FOUND)) {
                        notExist.invoke(c);
                    }
                }))
                .getRequestId()
        );
    }

    public void setChatMessagePassthroughState(boolean state, Callback<Boolean> onStateChangedCallback) {
        Request request = new Request()
            .setRequest("SET_CHAT_PASSTHROUGH_STATE")
            .withContentKeyPair("state", Boolean.toString(state));
        BooleanCallbackHandle cb = new BooleanCallbackHandle("state", onStateChangedCallback);
        send(request, subscribe().subscribeSuccess(cb).getRequestId());
    }

    public void sendChatbridgeMessage(String channel, String message, Callback2<String, String> onMessageSentCallback) {
        Request request = new Request()
            .setRequest("SEND_BROADCAST")
            .withContentKeyPair("channel", channel)
            .withContentKeyPair("message", message);
        BiStringCallbackHandle cb = new BiStringCallbackHandle("channel", "message", onMessageSentCallback);

        send(request, subscribe().subscribeSuccess(cb).getRequestId());
    }

    public void getChatHistory(Callback<MessageCache> onMessageCacheReceivedCallback) {
        JsonObjectCallbackHandle<MessageCache> cb = new JsonObjectCallbackHandle<MessageCache>("content", onMessageCacheReceivedCallback) {
            @Override
            protected TypeToken<MessageCache> getObjectType() {
                return TypeToken.get(MessageCache.class);
            }
        };
        send(new Request("GET_CHAT_HISTORY"), subscribe().subscribeSuccess(cb).getRequestId());
    }

    public void getChatbridgeImplementation(Callback<ChatbridgeImplementation> onResultReceivedCallback) {
        EnumCallbackHandle<ChatbridgeImplementation> handle = new EnumCallbackHandle<>(
            "implementation",
            ChatbridgeImplementation::valueOf,
            onResultReceivedCallback
        );
        send(new Request("GET_CHATBRIDGE_IMPL"), subscribe().subscribeSuccess(handle).getRequestId());
    }

    public void controllerConsoleComplete(
        String consoleId,
        String text,
        int cursorPosition,
        Callback<List<String>> callback
    ) {
        ListCallbackHandle<String> callbackHandle = new ListCallbackHandle<>("result", callback);
        send(new Request("CONTROLLER_CONSOLE_COMPLETE")
                .withContentKeyPair("input", text)
                .withContentKeyPair("cursor", Integer.toString(cursorPosition)),
            subscribe()
                .subscribeSuccess(callbackHandle)
                .getRequestId()
        );
    }

    public Controller getControllerByName(String name) {
        return controllerMap.get(name);
    }

    public void setOnResponseReceivedCallback(Callback<Response> onResponseRecievedCallback) {
        this.onResponseRecievedCallback = onResponseRecievedCallback;
    }

    @Override
    public String toString() {
        return sessionName;
    }
}
