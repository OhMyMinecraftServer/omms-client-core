package net.zhuruoling.omms.client.session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.zhuruoling.omms.client.announcement.Announcement;
import net.zhuruoling.omms.client.controller.Status;
import net.zhuruoling.omms.client.request.Request;
import net.zhuruoling.omms.client.controller.Controller;
import net.zhuruoling.omms.client.response.Callback;
import net.zhuruoling.omms.client.response.Response;
import net.zhuruoling.omms.client.system.SystemInfo;
import net.zhuruoling.omms.client.util.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.net.Socket;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientSession extends Thread {
    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private final HashMap<String, ArrayList<String>> whitelistMap = new HashMap<>();
    private final HashMap<String, Controller> controllerMap = new HashMap<>();
    private final HashMap<String, Announcement> announcementMap = new HashMap<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Socket socket;
    private final String serverName;
    EncryptedConnector connector;
    private SystemInfo systemInfo = null;
    private Object lastPermissionOperation;

    private Callback<ClientSession> onPermissionDeniedCallback;
    private Callback<Response> onServerInternalExeptionCallback;
    private Callback<Object> onInvalidOperationCallback;
    private Callback<Throwable> onAnyExceptionCallback;
    private Callback<Map<String, Announcement>> onAnnouncementReceivedCallback;
    private Callback<Pair<String,List<String>>> onControllerCommandLogReceivedCallback;
    private Callback<Pair<String, String>> onControllerConsoleLogReceievedCallback;
    private Callback<Response> onResponseRecievedCallback;
    private Callback<HashMap<String, ArrayList<String>>> onWhitelistReceivedCallback;
    private Callback<String> onConsoleNotFoundCallback;
    private Callback<Status> onStatusReceivedCallback;
    private Callback<String> onWhitelistNotExistCallback;
    private Callback<Map<String, Controller>> onControllerListedCallback;
    private Callback<Pair<String,String>> onControllerConsoleLaunchedCallback;
    private Callback<SystemInfo> onSystemInfoGotCallback;
    private Callback<Pair<String, String>> onPlayerAddedCallback;
    private Callback<Pair<String, String>> onPlayerRemovedCallback;
    private Callback<List<String>> onArgumentInvalidCallback;
    private Callback<String> onAnnouncementNotExistCallback;
    private Callback<Pair<String, String>> onPlayerAlreadyExistsCallback;
    private Callback<String> onConsoleStoppedCallback;
    private Callback<String> onControllerNotExistCallback;

    private Callback<String> onDisconnectedCallback;

    private Callback<String> onControllerConsoleAlreadyExistsCallback;
    private Callback<String> onControllerConsoleInputSendCallback;
    private Callback<Pair<String, String>> onPlayerNotExistCallback;
    private Callback<String> onControllerAuthFailedCallback;


    public ClientSession(EncryptedConnector connector, Socket socket, String serverName) {
        super("ClientSessionThread");
        this.serverName = serverName;
        this.connector = connector;
        this.socket = socket;
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
                if (onResponseRecievedCallback != null){
                    onResponseRecievedCallback.accept(response);
                }
                handleResponse(response);
            } catch (DisconnectedException | SocketException ignored) {
                if (onDisconnectedCallback != null) {
                    onDisconnectedCallback.accept(this.serverName);
                }
                break;
            } catch (Exception e) {
                onAnyExceptionCallback.accept(e);
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
            case CONTROLLER_NOT_EXIST:
                if (onConsoleNotFoundCallback == null)
                    throw new ControllerNotExistException("Controller not exist");
                else {
                    onControllerNotExistCallback.accept(response.getContent("controllerId"));
                    onControllerNotExistCallback = null;
                }
            case CONTROLLER_NO_STATUS:
                if (onStatusReceivedCallback != null) {
                    onStatusReceivedCallback.accept(null);
                    onStatusReceivedCallback = null;
                }
                break;
            case CONTROLLER_AUTH_FAILED:
                if (onControllerAuthFailedCallback != null){
                    onControllerAuthFailedCallback.accept(response.getContent("controllerId"));
                }
                break;
            case CONTROLLER_LOG:
                if (onControllerConsoleLogReceievedCallback != null) {
                    onControllerConsoleLogReceievedCallback.accept(response.getPair("consoleId", "content"));
                }
                break;
            case CONSOLE_NOT_EXIST:
                if (onConsoleNotFoundCallback != null) {
                    onConsoleNotFoundCallback.accept(response.getContent("console"));
                    onConsoleNotFoundCallback = null;
                }
                break;
            case NO_WHITELIST:
                if (onWhitelistReceivedCallback != null) {
                    onWhitelistReceivedCallback.accept(null);
                    onWhitelistReceivedCallback = null;
                }
                break;
            case WHITELIST_NOT_EXIST:
                if (onWhitelistNotExistCallback != null) {
                    onWhitelistNotExistCallback.accept(response.getContent("whitelist"));
                    onWhitelistNotExistCallback = null;
                }
                break;
            case PLAYER_ALREADY_EXISTS:
                if (onPlayerAlreadyExistsCallback != null) {
                    onPlayerAlreadyExistsCallback.accept(response.getPair("whitelist", "player"));
                    onPlayerAlreadyExistsCallback = null;
                }
                break;
            case PLAYER_NOT_EXIST:
                if (onPlayerNotExistCallback != null){
                    onPlayerNotExistCallback.accept(response.getPair("whitelist","player"));
                }
                break;
            case ANNOUNCEMENT_NOT_EXIST:
                if (onArgumentInvalidCallback != null) {
                    onAnnouncementNotExistCallback.accept(response.getContent("announcementId"));
                    onAnnouncementNotExistCallback = null;
                }
                break;
            case INVALID_ARGUMENTS:
                if (onArgumentInvalidCallback != null) {
                    onArgumentInvalidCallback.accept(Arrays.asList(gson.fromJson(response.getContent("args"), String[].class)));
                }
                break;
            case ANNOUNCEMENT_LISTED:
                String[] names = Util.string2Array(response.getContent("announcements"));
                for (String name : names) {
                    Response r = sendBlocking(new Request().setRequest("ANNOUNCEMENT_GET").withContentKeyPair("id", name));
                    if (r.getResponseCode() == Result.ANNOUNCEMENT_GOT) {
                        Announcement announcement = new Announcement(r.getContent("id"),
                                Long.parseLong(r.getContent("time")),
                                r.getContent("title"),
                                Util.string2Array(r.getContent("content"))
                        );
                        announcementMap.put(name, announcement);
                    }
                }
                if (onAnnouncementReceivedCallback != null) {
                    onAnnouncementReceivedCallback.accept(announcementMap);
                    onAnnouncementReceivedCallback = null;
                }
                break;
            case CONTROLLER_STATUS_GOT:
                if (onStatusReceivedCallback != null) {
                    onStatusReceivedCallback.accept(gson.fromJson(response.getContent("status"), Status.class));
                    onStatusReceivedCallback = null;
                }
                break;
            case CONSOLE_LAUNCHED:
                if (onControllerConsoleLaunchedCallback != null) {
                    onControllerConsoleLaunchedCallback.accept(new Pair<>(response.getContent("consoleId"),response.getContent("consoleId")));
                    onControllerConsoleLaunchedCallback = null;
                }
                break;
            case CONTROLLER_LISTED:
                String[] controllerNames = Util.string2Array(response.getContent("names"));
                for (String controllerName : controllerNames) {
                    System.out.printf("Fetching Controller %s%n",controllerName );
                    Response response1 = sendBlocking(new Request("CONTROLLER_GET").withContentKeyPair("controller", controllerName));
                    System.out.printf("Got Controller %s%n", response1);
                    if (response1.getResponseCode() == Result.CONTROLLER_GOT) {
                        String jsonString = response1.getContent("controller");
                        Controller controller = gson.fromJson(jsonString, Controller.class);
                        System.out.println(controller.toString());
                        controllerMap.put(controllerName, controller);
                    }
                    System.out.print("Next\n");
                }
                if (onControllerListedCallback != null) {
                    onControllerListedCallback.accept(controllerMap);
                    onControllerListedCallback = null;
                }
                break;
            case CONTROLLER_COMMAND_SENT:
                if (onControllerCommandLogReceivedCallback != null) {
                    onControllerCommandLogReceivedCallback.accept(new Pair<>(response.getContent("controllerId"),Arrays.asList(response.getContent("output").split("\n"))));
                    onControllerCommandLogReceivedCallback = null;
                }
                break;
            case CONTROLLER_CONSOLE_INPUT_SENT:
                String id = response.getContent("consoleId");
                if (onControllerConsoleInputSendCallback != null) {
                    onControllerConsoleInputSendCallback.accept(id);
                }
                //can ignore result
                break;
            case SYSINFO_GOT:
                this.systemInfo = gson.fromJson(response.getContent("systemInfo"), SystemInfo.class);
                if (onSystemInfoGotCallback != null) {
                    onSystemInfoGotCallback.accept(systemInfo);
                    onSystemInfoGotCallback = null;
                }
                break;
            case CONSOLE_ALREADY_EXISTS:
                if (onControllerConsoleAlreadyExistsCallback != null) {
                    onControllerConsoleAlreadyExistsCallback.accept(response.getContent("controller"));
                    onControllerConsoleAlreadyExistsCallback = null;
                }
            case WHITELIST_ADDED:
                if (onPlayerAddedCallback != null) {
                    onPlayerAddedCallback.accept(response.getPair("whitelist", "player"));
                    onPlayerAddedCallback = null;
                }
                break;
            case WHITELIST_LISTED:
                String[] whitelistNames = gson.fromJson(response.getContent("whitelists"), String[].class);
                for (String whitelistName : whitelistNames) {
                    response = sendBlocking(new Request("WHITELIST_GET").withContentKeyPair("whitelist", whitelistName));
                    if (response.getResponseCode() == Result.WHITELIST_GOT) {
                        whitelistMap.put(whitelistName, new ArrayList<>(Arrays.asList(gson.fromJson(response.getContent("players"), String[].class))));
                    }
                }
                if (onWhitelistReceivedCallback != null) {
                    onWhitelistReceivedCallback.accept(whitelistMap);
                    onWhitelistReceivedCallback = null;
                }
                break;
            case WHITELIST_REMOVED:
                if (onPlayerRemovedCallback != null) {
                    onPlayerRemovedCallback.accept(response.getPair("whitelist", "player"));
                    onPlayerRemovedCallback = null;
                }
                break;
            case CONSOLE_STOPPED:
                if (onConsoleStoppedCallback != null) {
                    onConsoleStoppedCallback.accept(response.getContent("consoleId"));
                    onConsoleStoppedCallback = null;
                }
                break;
            case DISCONNECT:
                throw new DisconnectedException();
        }
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

    private Response sendBlocking(Request request) throws Exception {
        String content = gson.toJson(request);
        connector.println(content);
        Response response = gson.fromJson(connector.readLine(), Response.class);
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
        sendBlocking(new Request("CONTROLLER_LIST"));
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
                                       Callback<Pair<String,String>> onControllerConsoleLaunchedCallback,
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
                                        Callback<Pair<String,List<String>>> callback//this list will never equal null
    ) {
        sendCommandToController(controller, command, callback, null,null);
    }

    public void sendCommandToController(String controller,
                                        String command,
                                        Callback<Pair<String,List<String>>> callback,//this list will never equal null
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


    public HashMap<String, ArrayList<String>> getWhitelistMap() {
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

    public void setOnAnyExceptionCallback(Callback<Throwable> onAnyExceptionCallback) {
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
