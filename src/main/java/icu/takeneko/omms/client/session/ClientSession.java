package icu.takeneko.omms.client.session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import icu.takeneko.omms.client.announcement.Announcement;
import icu.takeneko.omms.client.controller.Controller;
import icu.takeneko.omms.client.controller.Status;
import icu.takeneko.omms.client.request.Request;
import icu.takeneko.omms.client.response.Response;
import icu.takeneko.omms.client.session.callback.Callback;
import icu.takeneko.omms.client.session.callback.Callback2;
import icu.takeneko.omms.client.session.handler.CallbackHandle;
import icu.takeneko.omms.client.session.handler.ResponseHandlerDelegate;
import icu.takeneko.omms.client.session.handler.ResponseHandlerDelegateImpl;
import icu.takeneko.omms.client.system.SystemInfo;
import icu.takeneko.omms.client.util.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.net.Socket;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientSession extends Thread {
    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private final HashMap<String, List<String>> whitelistMap = new HashMap<>();
    private final HashMap<String, Controller> controllerMap = new HashMap<>();
    private final HashMap<String, Announcement> announcementMap = new HashMap<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Socket socket;
    private final String serverName;
    EncryptedConnector connector;
    private SystemInfo systemInfo = null;
    private Object lastPermissionOperation;
    private final ResponseHandlerDelegate<Result, SessionContext, CallbackHandle<SessionContext>> delegate;

    private Callback<ClientSession> onPermissionDeniedCallback;
    private Callback<Response> onServerInternalExeptionCallback;
    private Callback<Object> onInvalidOperationCallback;
    private Callback2<Thread, Throwable> onAnyExceptionCallback;
    //    private Callback<Map<String, Announcement>> onAnnouncementReceivedCallback;
//    private Callback<Pair<String,List<String>>> onControllerCommandLogReceivedCallback;
    private Callback<Response> onResponseRecievedCallback;
    //    private Callback<HashMap<String, ArrayList<String>>> onWhitelistReceivedCallback;
//    private Callback<Map<String, Controller>> onControllerListedCallback;
    private Callback<String> onDisconnectedCallback;


    public ClientSession(EncryptedConnector connector, Socket socket, String serverName) {
        super("ClientSessionThread");
        this.serverName = serverName;
        this.connector = connector;
        this.socket = socket;
        delegate = new ResponseHandlerDelegateImpl<>();
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
                if (onServerInternalExeptionCallback == null)
                    throw new ServerInternalErrorException("Got FAIL from server.");
                else {
                    onServerInternalExeptionCallback.accept(response);
                }
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
        }
        delegate.handle(response.getResponseCode(), response);

    }

    public void send(Request request) {
        executorService.submit(() -> {
            String content = gson.toJson(request);
            try {
                connector.println(content);
            } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                     BadPaddingException | InvalidKeyException e) {
                onAnyExceptionCallback.accept(new Pair<>(currentThread(), e));
            }
        });
    }

    public Response sendBlocking(Request request) throws Exception {
        String content = gson.toJson(request);
        connector.println(content);
        String s = connector.readLine();
        Response response = gson.fromJson(s, Response.class);
        if (onResponseRecievedCallback != null) {
            onResponseRecievedCallback.accept(response);
        }
        if (response.getResponseCode() == Result.RATE_LIMIT_EXCEEDED) {
            this.socket.close();
            throw new RateExceedException("Connection closed because request rate exceeded.");
        } else {
            return response;
        }
    }

    public void close(Callback<String> onDisconnectedCallback) throws Exception {
        setOnDisconnectedCallback(onDisconnectedCallback);
        send(new Request("END"));
    }

    public void fetchWhitelistFromServer(Callback<HashMap<String, ArrayList<String>>> callback) throws Exception {
        fetchWhitelistFromServer(callback, null);
    }

    public void fetchWhitelistFromServer(Callback<HashMap<String, ArrayList<String>>> callback,
                                         Callback<String> onWhitelistNotExistCallback
    ) throws Exception {
        setOnWhitelistReceivedCallback(callback);
        setOnWhitelistNotExistCallback(onWhitelistNotExistCallback);
        send(new Request("WHITELIST_LIST"));
    }

    public void fetchControllersFromServer(Callback<Map<String, Controller>> callback) throws Exception {
        fetchControllersFromServer(callback, null);
    }

    public void fetchControllersFromServer(Callback<Map<String, Controller>> callback,
                                           Callback<String> onControllerNotExistCallback
    ) throws Exception {
        setOnControllerListedCallback(callback);
        setOnControllerNotExistCallback(onControllerNotExistCallback);
        send(new Request("CONTROLLER_LIST"));
    }

    public void fetchSystemInfoFromServer(Callback<SystemInfo> onSystemInfoGotCallback) throws Exception {
        setOnSystemInfoGotCallback(onSystemInfoGotCallback);
        send(new Request("SYSTEM_GET_INFO"));
    }

    public void fetchAnnouncementFromServer(Callback<Map<String, Announcement>> onAnnouncementReceivedCallback
    ) throws Exception {
        fetchAnnouncementFromServer(onAnnouncementReceivedCallback, null);
    }

    public void fetchAnnouncementFromServer(Callback<Map<String, Announcement>> onAnnouncementReceivedCallback,
                                            Callback<String> onAnnouncementNotExistCallback
    ) throws Exception {
        setOnAnnouncementReceivedCallback(onAnnouncementReceivedCallback);
        setOnAnnouncementNotExistCallback(onAnnouncementNotExistCallback);
        send(new Request().setRequest("ANNOUNCEMENT_LIST"));
    }

    public void fetchControllerStatus(String controllerId,
                                      Callback<Status> onStatusReceivedCallback,
                                      Callback<String> onControllerNotExistCallback
    ) {
        setOnStatusReceivedCallback(onStatusReceivedCallback);
        setOnControllerNotExistCallback(onControllerNotExistCallback);
        send(new Request().setRequest("CONTROLLER_GET_STATUS").withContentKeyPair("id", controllerId));
    }


    public void removeFromWhitelist(String whitelistName,
                                    String player,
                                    Callback<Pair<String, String>> onPlayerRemovedCallback,
                                    Callback<Pair<String, String>> onPlayerNotExistCallback
    ) {
        this.setOnPlayerRemovedCallback(onPlayerRemovedCallback);
        this.setOnPlayerNotExistCallback(onPlayerNotExistCallback);
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
                                       Callback<Pair<String, String>> onControllerConsoleLaunchedCallback,
                                       Callback<Pair<String, String>> onControllerConsoleLogRecievedCallback,
                                       Callback<String> onControllerNotExistCallback,
                                       Callback<String> onControllerConsoleAlreadyExistsCallback
    ) {
        setOnControllerConsoleLaunchedCallback(onControllerConsoleLaunchedCallback);
        setOnControllerConsoleLogReceivedCallback(onControllerConsoleLogRecievedCallback);
        setOnControllerNotExistCallback(onControllerNotExistCallback);
        setOnControllerConsoleAlreadyExistsCallback(onControllerConsoleAlreadyExistsCallback);
        send(new Request().setRequest("CONTROLLER_LAUNCH_CONSOLE").withContentKeyPair("controller", controller));
    }

    public void stopControllerConsole(String consoleId,
                                      Callback<String> onConsoleStoppedCallback,
                                      Callback<String> onConsoleNotFoundCallback
    ) {
        setOnConsoleStoppedCallback(onConsoleStoppedCallback);
        setOnConsoleNotFoundCallback(onConsoleNotFoundCallback);
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
        setOnControllerConsoleInputSendCallback(onControllerConsoleInputSendCallback);
        setOnConsoleNotFoundCallback(onConsoleNotFoundCallback);
        send(new Request().setRequest("CONTROLLER_INPUT_CONSOLE")
                .withContentKeyPair("consoleId", consoleId)
                .withContentKeyPair("command", line)
        );
    }

    public void addToWhitelist(String whitelistName,
                               String player,
                               Callback<Pair<String, String>> resultCallback,
                               Callback<Pair<String, String>> onPlayerAlreadyExistsCallback
    ) {
        this.setOnPlayerAddedCallback(resultCallback);
        setOnPlayerAlreadyExistsCallback(onPlayerAlreadyExistsCallback);
        this.send(new Request("WHITELIST_ADD")
                .withContentKeyPair("whitelist", whitelistName)
                .withContentKeyPair("player", player)
        );
    }


    public void sendCommandToController(String controller,
                                        String command,
                                        Callback<Pair<String, List<String>>> callback//this list will never equal null
    ) {
        sendCommandToController(controller, command, callback, null, null);
    }

    public void sendCommandToController(String controller,
                                        String command,
                                        Callback<Pair<String, List<String>>> callback,//this list will never equal null
                                        Callback<String> onControllerNotExistCallback,
                                        Callback<String> onControllerAuthFailedCallback
    ) {
        Request request = new Request()
                .setRequest("CONTROLLER_EXECUTE_COMMAND")
                .withContentKeyPair("controller", controller)
                .withContentKeyPair("command", command);
        setOnControllerCommandLogReceivedCallback(callback);
        setOnControllerNotExistCallback(onControllerNotExistCallback);
        setOnControllerAuthFailedCallback(onControllerAuthFailedCallback);
        send(request);
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

    public void setOnServerInternalExeptionCallback(Callback<Response> onServerInternalExeptionCallback) {
        this.onServerInternalExeptionCallback = onServerInternalExeptionCallback;
    }

    public void setOnInvalidOperationCallback(Callback<Object> onInvalidOperationCallback) {
        this.onInvalidOperationCallback = onInvalidOperationCallback;
    }

    public void setOnAnyExceptionCallback(Callback<Pair<Thread, Throwable>> onAnyExceptionCallback) {
        this.onAnyExceptionCallback = onAnyExceptionCallback;
    }

    public void setOnAnnouncementReceivedCallback(Callback<Map<String, Announcement>> onAnnouncementReceivedCallback) {
        this.onAnnouncementReceivedCallback = onAnnouncementReceivedCallback;
    }


    public void setOnResponseReceivedCallback(Callback<Response> onResponseRecievedCallback) {
        this.onResponseRecievedCallback = onResponseRecievedCallback;
    }

    public void setOnWhitelistReceivedCallback(Callback<HashMap<String, ArrayList<String>>> onWhitelistReceivedCallback) {
        this.onWhitelistReceivedCallback = onWhitelistReceivedCallback;
    }

    public void setOnConsoleNotFoundCallback(Callback<String> onConsoleNotFoundCallback) {
        this.onConsoleNotFoundCallback = onConsoleNotFoundCallback;
    }

    public void setOnStatusReceivedCallback(Callback<Status> onStatusReceivedCallback) {
        this.onStatusReceivedCallback = onStatusReceivedCallback;
    }

    public void setOnControllerCommandLogReceivedCallback(Callback<Pair<String, List<String>>> onControllerCommandLogReceivedCallback) {
        this.onControllerCommandLogReceivedCallback = onControllerCommandLogReceivedCallback;
    }

    public void setOnWhitelistNotExistCallback(Callback<String> onWhitelistNotExistCallback) {
        this.onWhitelistNotExistCallback = onWhitelistNotExistCallback;
    }

    public void setOnControllerListedCallback(Callback<Map<String, Controller>> onControllerListedCallback) {
        this.onControllerListedCallback = onControllerListedCallback;
    }


    public void setOnSystemInfoGotCallback(Callback<SystemInfo> onSystemInfoGotCallback) {
        this.onSystemInfoGotCallback = onSystemInfoGotCallback;
    }

    public void setOnPlayerAddedCallback(Callback<Pair<String, String>> onPlayerAddedCallback) {
        this.onPlayerAddedCallback = onPlayerAddedCallback;
    }

    public void setOnPlayerRemovedCallback(Callback<Pair<String, String>> onPlayerRemovedCallback) {
        this.onPlayerRemovedCallback = onPlayerRemovedCallback;
    }

    public void setOnArgumentInvalidCallback(Callback<List<String>> onArgumentInvalidCallback) {
        this.onArgumentInvalidCallback = onArgumentInvalidCallback;
    }

    public void setOnAnnouncementNotExistCallback(Callback<String> onAnnouncementNotExistCallback) {
        this.onAnnouncementNotExistCallback = onAnnouncementNotExistCallback;
    }

    public void setOnPlayerAlreadyExistsCallback(Callback<Pair<String, String>> onPlayerAlreadyExistsCallback) {
        this.onPlayerAlreadyExistsCallback = onPlayerAlreadyExistsCallback;
    }

    public void setOnConsoleStoppedCallback(Callback<String> onConsoleStoppedCallback) {
        this.onConsoleStoppedCallback = onConsoleStoppedCallback;
    }

    public void setOnControllerNotExistCallback(Callback<String> onControllerNotExistCallback) {
        this.onControllerNotExistCallback = onControllerNotExistCallback;
    }

    public void setOnDisconnectedCallback(Callback<String> onDisconnectedCallback) {
        this.onDisconnectedCallback = onDisconnectedCallback;
    }

    public void setOnControllerConsoleAlreadyExistsCallback(Callback<String> onControllerConsoleAlreadyExistsCallback) {
        this.onControllerConsoleAlreadyExistsCallback = onControllerConsoleAlreadyExistsCallback;
    }

    public void setOnControllerConsoleLaunchedCallback(Callback<Pair<String, String>> onControllerConsoleLaunchedCallback) {
        this.onControllerConsoleLaunchedCallback = onControllerConsoleLaunchedCallback;
    }

    public void setOnControllerConsoleInputSendCallback(Callback<String> onControllerConsoleInputSendCallback) {
        this.onControllerConsoleInputSendCallback = onControllerConsoleInputSendCallback;
    }

    public void setOnControllerConsoleLogReceivedCallback(Callback<Pair<String, String>> onControllerConsoleLogReceievedCallback) {
        this.onControllerConsoleLogReceievedCallback = onControllerConsoleLogReceievedCallback;
    }

    public void setOnPlayerNotExistCallback(Callback<Pair<String, String>> onPlayerNotExistCallback) {
        this.onPlayerNotExistCallback = onPlayerNotExistCallback;
    }

    public void setOnControllerAuthFailedCallback(Callback<String> onControllerAuthFailedCallback) {
        this.onControllerAuthFailedCallback = onControllerAuthFailedCallback;
    }
}
