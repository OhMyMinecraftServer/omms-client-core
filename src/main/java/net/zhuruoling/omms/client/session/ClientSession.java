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

import java.net.Socket;
import java.util.*;

public class ClientSession extends Thread {
    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private final HashMap<String, ArrayList<String>> whitelistMap = new HashMap<>();
    private final HashMap<String, Controller> controllerMap = new HashMap<>();
    private final HashMap<String, Announcement> announcementMap = new HashMap<>();

    private final List<Request> requestCache = new ArrayList<>();
    private final Socket socket;
    private final String serverName;
    EncryptedConnector connector;
    private SystemInfo systemInfo = null;
    private Object lastPermissionOperation;

    private Callback<ClientSession> onPermissionDeniedCallback;
    private Callback<Response> onServerInternalExeptionCallback;
    private Callback<Object> onInvalidOperationCallback;
    private Callback<Throwable> onAnyExceptionCallback;
    private Callback<Map<String,Announcement>> onAnnouncementReceivedCallback;
    private Callback<List<String>> onControllerCommandLogReceivedCallback;
    private Callback<Pair<String, String>> onControllerConsoleLogRecievedCallback;
    private Callback<Response> onResponseRecievedCallback;
    private Callback<HashMap<String, ArrayList<String>>> onWhitelistReceivedCallback;
    private Callback<String> onConsoleNotFoundCallback;
    private Callback<Status> onStatusReceivedCallback;
    private Callback<String> onWhitelistNotExistCallback;
    private Callback<Map<String, Controller>> onControllerListedCallback;
    private Callback<String> onControllerConsoleLaunchedCallback;
    private Callback<SystemInfo> onSystemInfoGotCallback;
    private Callback<Pair<String, String>> onPlayerAddedCallback;
    private Callback<Pair<String, String>> onPlayerRemovedCallback;
    private Callback<List<String>> onArgumentInvalidCallback;
    private Callback<String> onAnnouncementNotExistCallback;
    private Callback<Pair<String, String>> onPlayerAlreadyExistsCallback;
    private Callback<String> onConsoleStoppedCallback;


    public ClientSession(EncryptedConnector connector, Socket socket, String serverName) {
        super("ClientSessionThread");
        this.serverName = serverName;
        this.connector = connector;
        this.socket = socket;
    }

    public boolean isActive() {
        return socket.isClosed() && this.isAlive();
    }

    @Override
    public void run() {
        Response response;
        while (true) {
            try {
                response = gson.fromJson(connector.readLine(), Response.class);
                handleResponse(response);
            } catch (Exception e) {
                onAnyExceptionCallback.accept(e);
            }
        }
    }

    private void handleResponse(Response response) throws Exception {
        if (onResponseRecievedCallback != null){
            onResponseRecievedCallback.accept(response);
        }
        if (response.getResponseCode() == Result.RATE_LIMIT_EXCEEDED) {
            this.socket.close();
            throw new RateExceedException("Connection closed because request rate exceeded.");
        }
        switch (response.getResponseCode()) {
            case FAIL:
                if (onServerInternalExeptionCallback == null)
                    throw new ServerInternalErrorException("Got FAIL from server.");
                else onServerInternalExeptionCallback.accept(response);
            case PERMISSION_DENIED:
                if (onPermissionDeniedCallback == null)
                    throw new PermissionDeniedException("Permission Denied.");
                else
                    onPermissionDeniedCallback.accept(this);
                break;
            case OPERATION_ALREADY_EXISTS:
                if (onInvalidOperationCallback != null)
                    onInvalidOperationCallback.accept(lastPermissionOperation);
                break;
            case CONTROLLER_NOT_EXIST:
                throw new ControllerNotExistException("Controller not exist");
            case CONTROLLER_NO_STATUS:
                if (onStatusReceivedCallback != null) onStatusReceivedCallback.accept(null);
                break;
            case CONTROLLER_LOG:
                onControllerConsoleLogRecievedCallback.accept(response.getPair("consoleId", "content"));
                break;
            case CONSOLE_NOT_EXIST:
                if (onConsoleNotFoundCallback != null) {
                    onConsoleNotFoundCallback.accept(response.getContent("console"));
                }
                break;
            case NO_WHITELIST:
                if (onWhitelistReceivedCallback != null) onWhitelistReceivedCallback.accept(null);
                break;
            case WHITELIST_NOT_EXIST:
                if (onWhitelistNotExistCallback != null) {
                    onWhitelistNotExistCallback.accept(response.getContent("whitelist"));
                }
                break;
            case PLAYER_ALREADY_EXISTS:
                if (onPlayerAlreadyExistsCallback != null) {
                    onPlayerAlreadyExistsCallback.accept(response.getPair("whitelist", "player"));
                }
                break;
            case ANNOUNCEMENT_NOT_EXIST:
                if (onArgumentInvalidCallback != null) {
                    onAnnouncementNotExistCallback.accept(response.getContent("announcementId"));
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
                if (onAnnouncementReceivedCallback != null){
                    onAnnouncementReceivedCallback.accept(announcementMap);
                }
                break;
            case CONTROLLER_STATUS_GOT:
                if (onStatusReceivedCallback != null) {
                    onStatusReceivedCallback.accept(gson.fromJson(response.getContent("status"), Status.class));
                }
                break;
            case CONSOLE_LAUNCHED:
                if (onControllerConsoleLaunchedCallback != null) {
                    onControllerConsoleLaunchedCallback.accept(response.getContent("consoleId"));
                }
                break;
            case CONTROLLER_LISTED:
                String[] controllerNames = Util.string2Array(response.getContent("names"));
                for (String controllerName : controllerNames) {
                    Response response1 = sendBlocking(new Request("CONTROLLER_GET").withContentKeyPair("controller", controllerName));
                    if (response1.getResponseCode() == Result.CONTROLLER_GOT) {
                        String jsonString = response1.getContent("controller");
                        Controller controller = gson.fromJson(jsonString, Controller.class);
                        System.out.println(controller.toString());
                        controllerMap.put(controllerName, controller);
                    }
                }
                if (onControllerListedCallback != null) {
                    onControllerListedCallback.accept(controllerMap);
                }
                break;
            case CONTROLLER_COMMAND_SENT:
                if (onControllerCommandLogReceivedCallback != null)
                    onControllerCommandLogReceivedCallback.accept(Arrays.asList(response.getContent("output").split("\n")));
                break;
            case CONTROLLER_CONSOLE_INPUT_SENT:
                //ignore result
                break;
            case SYSINFO_GOT:
                this.systemInfo = gson.fromJson(response.getContent("systemInfo"), SystemInfo.class);
                if (onSystemInfoGotCallback != null) {
                    onSystemInfoGotCallback.accept(systemInfo);
                }
                break;

            case WHITELIST_ADDED:
                if (onPlayerAddedCallback != null) {
                    onPlayerAddedCallback.accept(response.getPair("whitelist", "player"));
                }
                break;
            case WHITELIST_LISTED:
                String[] whitelistNames = gson.fromJson(response.getContent("whitelists"), String[].class);
                for (String whitelistName : whitelistNames) {
                    response = sendBlocking(new Request("WHITELIST_GET").withContentKeyPair("whitelist", whitelistName));
                    if (response.getResponseCode() == Result.OK) {
                        whitelistMap.put(whitelistName, new ArrayList<>(Arrays.asList(gson.fromJson(response.getContent("players"), String[].class))));
                    }
                }
                onWhitelistReceivedCallback.accept(whitelistMap);
                break;
            case WHITELIST_REMOVED:
                if (onPlayerRemovedCallback != null) {
                    onPlayerRemovedCallback.accept(response.getPair("whitelist", "player"));
                }
                break;
            case CONSOLE_STOPPED:
                if (onConsoleStoppedCallback != null) {
                    onConsoleStoppedCallback.accept(response.getContent("consoleId"));
                }
                break;
        }
    }

    public void send(Request request) throws Exception {
        String content = gson.toJson(request);
        connector.println(content);
    }

    private Response sendBlocking(Request request) throws Exception {
        send(request);
        Response response = gson.fromJson(connector.readLine(), Response.class);
        if (response.getResponseCode() == Result.RATE_LIMIT_EXCEEDED) {
            this.socket.close();
            throw new RateExceedException("Connection closed because request rate exceeded.");
        } else {
            return response;
        }
    }

    public void close() throws Exception {
        Response response = sendBlocking(new Request("END"));
        if (response.getResponseCode() == Result.OK) {
            socket.close();
            this.interrupt();
        }
    }

    public void fetchWhitelistFromServer(Callback<HashMap<String, ArrayList<String>>> callback) throws Exception {
        setOnWhitelistReceivedCallback(callback);
        send(new Request("WHITELIST_LIST"));

    }

    public Result fetchControllersFromServer() throws Exception {
        Response response = sendBlocking(new Request("CONTROLLER_LIST"));
        if (response.getResponseCode() == Result.OK) {
            String[] controllerNames = Util.string2Array(response.getContent("names"));
            for (String controllerName : controllerNames) {
                Response response1 = sendBlocking(new Request("CONTROLLER_GET").withContentKeyPair("controller", controllerName));
                if (response1.getResponseCode() == Result.OK) {
                    String jsonString = response1.getContent("controller");
                    Controller controller = gson.fromJson(jsonString, Controller.class);
                    System.out.println(controller.toString());
                    controllerMap.put(controllerName, controller);
                } else return response1.getResponseCode();
            }
            return Result.OK;
        } else {
            return response.getResponseCode();
        }
    }

    public Result fetchSystemInfoFromServer() throws Exception {
        Response response = sendBlocking(new Request("SYSTEM_GET_INFO"));
        if (response.getResponseCode() == Result.OK) {
            this.systemInfo = gson.fromJson(response.getContent("systemInfo"), SystemInfo.class);
            return Result.OK;
        } else {
            return response.getResponseCode();
        }
    }

    public Result fetchAnnouncementFromServer() throws Exception {
        Response response = sendBlocking(new Request().setRequest("ANNOUNCEMENT_LIST"));
        if (response.getResponseCode() == Result.OK) {

            return Result.OK;
        }
        return response.getResponseCode();
    }

    public Pair<Result, Status> fetchControllerStatus(String controllerId) throws Exception {
        Response response = sendBlocking(new Request().setRequest("CONTROLLER_GET_STATUS").withContentKeyPair("id", controllerId));
        if (response.getResponseCode() == Result.OK) {
            Status status = Util.gson.fromJson(response.getContent("status"), Status.class);
            return new Pair<>(response.getResponseCode(), status);
        } else {
            return new Pair<>(response.getResponseCode(), null);
        }
    }


    public Result removeFromWhitelist(String whitelistName, String player) throws Exception {
        Response response = this.sendBlocking(new Request("WHITELIST_REMOVE")
                .withContentKeyPair("whitelist", whitelistName)
                .withContentKeyPair("player", player)
        );
        return response.getResponseCode();
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

    public void addToWhitelist(String whitelistName, String player, Callback<Result> resultCallback) throws Exception {
        this.send(new Request("WHITELIST_ADD")
                .withContentKeyPair("whitelist", whitelistName)
                .withContentKeyPair("player", player)
        );
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

    public Pair<Result, String> sendCommandToController(String controller, String command) throws Exception {
        Request request = new Request().setRequest("CONTROLLER_EXECUTE_COMMAND").withContentKeyPair("controller", controller).withContentKeyPair("command", command);
        Response response = sendBlocking(request);
        return new Pair<>(response.getResponseCode(), response.getContent("output"));
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

    public void setOnControllerCommandLogReceivedCallback(Callback<List<String>> onControllerCommandLogReceivedCallback) {
        this.onControllerCommandLogReceivedCallback = onControllerCommandLogReceivedCallback;
    }

    public void setOnControllerConsoleLogRecievedCallback(Callback<Pair<String, String>> onControllerConsoleLogRecievedCallback) {
        this.onControllerConsoleLogRecievedCallback = onControllerConsoleLogRecievedCallback;
    }

    public void setOnResponseRecievedCallback(Callback<Response> onResponseRecievedCallback) {
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

    public void setOnWhitelistNotExistCallback(Callback<String> onWhitelistNotExistCallback) {
        this.onWhitelistNotExistCallback = onWhitelistNotExistCallback;
    }

    public void setOnControllerListedCallback(Callback<Map<String, Controller>> onControllerListedCallback) {
        this.onControllerListedCallback = onControllerListedCallback;
    }

    public void setOnControllerConsoleLaunchedCallback(Callback<String> onControllerConsoleLaunchedCallback) {
        this.onControllerConsoleLaunchedCallback = onControllerConsoleLaunchedCallback;
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
}
