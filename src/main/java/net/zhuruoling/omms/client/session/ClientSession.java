package net.zhuruoling.omms.client.session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.zhuruoling.omms.client.announcement.Announcement;
import net.zhuruoling.omms.client.controller.Status;
import net.zhuruoling.omms.client.request.Request;
import net.zhuruoling.omms.client.controller.Controller;
import net.zhuruoling.omms.client.response.Response;
import net.zhuruoling.omms.client.system.SystemInfo;
import net.zhuruoling.omms.client.util.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ClientSession extends Thread {
    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private final HashMap<String, ArrayList<String>> whitelistMap = new HashMap<>();
    private final HashMap<String, Controller> controllerMap = new HashMap<>();
    private final HashMap<String, Announcement> announcementMap = new HashMap<>();

    private final List<Request> requestCache = new ArrayList<>();
    private final Socket socket;
    EncryptedConnector connector;
    private SystemInfo systemInfo = null;

    public ClientSession(EncryptedConnector connector, Socket socket) {
        super("ClientSessionThread");
        this.connector = connector;
        this.socket = socket;
    }

    @Override
    public void run(){
        Response response;
        while (true){
            try {
                response = gson.fromJson(connector.readLine(), Response.class);
                handleResponse(response);
            } catch (IOException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                     BadPaddingException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleResponse(Response response) throws IOException {
        if (response.getResponseCode() == Result.RATE_LIMIT_EXCEEDED){
            this.socket.close();
            throw new RateExceedException("Connection closed because request rate exceeded.");
        }
        switch (response.getResponseCode()){
            case FAIL:
                throw new ServerInternalErrorException("Got FAIL from server.");
            case OK:
                break;
            case PERMISSION_DENIED:
                break;
            case OPERATION_ALREADY_EXISTS:
                break;
            case CONTROLLER_NOT_EXIST:
                break;
            case CONTROLLER_NO_STATUS:
                break;
            case CONTROLLER_LOG:
                break;
            case CONSOLE_NOT_EXIST:
                break;
            case NO_WHITELIST:
                break;
            case WHITELIST_NOT_EXIST:
                break;
            case RUNNER_NOT_EXIST:
                break;
            case NO_SUCH_PLAYER:
                break;
            case PLAYER_ALREADY_EXISTS:
                break;
            case VERSION_NOT_MATCH:
                break;
            case ANNOUNCEMENT_NOT_EXIST:
                break;
            case INVALID_ARGUMENTS:
                break;
            case NO_RUNNER:
                break;
            case RATE_LIMIT_EXCEEDED:
                break;
            case ANNOUNCEMENT_GOT:
                break;
            case ANNOUNCEMENT_LISTED:
                break;
            case CONTROLLER_CREATED:
                break;
            case CONTROLLER_GOT:
                break;
            case CONTROLLER_STATUS_GOT:
                break;
            case CONSOLE_LAUNCHED:
                break;
            case CONTROLLER_LISTED:
                break;
            case CONTROLLER_COMMAND_SENT:
                break;
            case CONTROLLER_CONSOLE_INPUT_SENT:
                break;
            case PERMISSION_CREATED:
                break;
            case PERMISSION_DELETED:
                break;
            case PERMISSION_REMOVED:
                break;
            case PERMISSION_GRANTED:
                break;
            case PERMISSION_LISTED:
                break;
            case RUNNER_LISTED:
                break;
            case RUNNER_OUTPUT_GOT:
                break;
            case SYSINFO_GOT:
                break;
            case RUNNER_LAUNCHED:
                break;
            case WHITELIST_ADDED:
                break;
            case WHITELIST_CREATED:
                break;
            case WHITELIST_DELETED:
                break;
            case WHITELIST_GOT:
                break;
            case WHITELIST_LISTED:
                break;
            case WHITELIST_REMOVED:
                break;
            case UNDEFINED:
                break;
        }
    }

    public Response send(Request request) throws Exception {
        String content = gson.toJson(request);
        connector.println(content);
        Response response = gson.fromJson(connector.readLine(), Response.class);
        if (response.getResponseCode() == Result.RATE_LIMIT_EXCEEDED){
            this.socket.close();
            throw new RateExceedException("Connection closed because request rate exceeded.");
        }else {
            return response;
        }
    }

    public void close() throws Exception {
        Response response = send(new Request("END"));
        if (response.getResponseCode() == Result.OK) {
            socket.close();
            this.interrupt();
        }
    }

    public void fetchWhitelistFromServer() throws Exception {
        Response response = send(new Request("WHITELIST_LIST"));
        if (response.getResponseCode() == Result.OK) {
            String[] whitelistNames = gson.fromJson(response.getContent("whitelists"), String[].class);
            for (String whitelistName : whitelistNames) {
                response = send(new Request("WHITELIST_GET").withContentKeyPair("whitelist", whitelistName));
                if (response.getResponseCode() == Result.OK) {
                    whitelistMap.put(whitelistName, new ArrayList<>(Arrays.asList(gson.fromJson(response.getContent("players"), String[].class))));
                }
            }
        }
    }

    public Result fetchControllersFromServer() throws Exception {
        Response response = send(new Request("CONTROLLER_LIST"));
        if (response.getResponseCode() == Result.OK) {
            String[] controllerNames = Util.string2Array(response.getContent("names"));
            for (String controllerName : controllerNames) {
                Response response1 = send(new Request("CONTROLLER_GET").withContentKeyPair("controller", controllerName));
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
        Response response = send(new Request("SYSTEM_GET_INFO"));
        if (response.getResponseCode() == Result.OK) {
            this.systemInfo = gson.fromJson(response.getContent("systemInfo"), SystemInfo.class);
            return Result.OK;
        } else {
            return response.getResponseCode();
        }
    }

    public Result fetchAnnouncementFromServer() throws Exception {
        Response response = send(new Request().setRequest("ANNOUNCEMENT_LIST"));
        if (response.getResponseCode() == Result.OK) {
            String[] names = Util.string2Array(response.getContent("announcements"));
            for (String name : names) {
                Response r = send(new Request().setRequest("ANNOUNCEMENT_GET").withContentKeyPair("id", name));
                if (r.getResponseCode() == Result.OK) {
                    Announcement announcement = new Announcement(r.getContent("id"),
                            Long.parseLong(r.getContent("time")),
                            r.getContent("title"),
                            Util.string2Array(r.getContent("content"))
                    );
                    announcementMap.put(name, announcement);
                } else {
                    return r.getResponseCode();
                }
            }
            return Result.OK;
        }
        return response.getResponseCode();
    }

    public Pair<Result, Status> fetchControllerStatus(String controllerId) throws Exception{
        Response response = send(new Request().setRequest("CONTROLLER_GET_STATUS").withContentKeyPair("id", controllerId));
        if (response.getResponseCode() == Result.OK){
            Status status = Util.gson.fromJson(response.getContent("status"), Status.class);
            return new Pair<>(response.getResponseCode(), status);
        }else {
            return new Pair<>(response.getResponseCode(), null);
        }
    }


    public Result removeFromWhitelist(String whitelistName, String player) throws Exception {
        Response response = this.send(new Request("WHITELIST_REMOVE")
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

    public Result addToWhitelist(String whitelistName, String player) throws Exception {
        Response response = this.send(new Request("WHITELIST_ADD")
                .withContentKeyPair("whitelist", whitelistName)
                .withContentKeyPair("player", player)
        );
        return response.getResponseCode();
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

    public Pair<Result, String> sendCommandToController(String controller, String command) throws Exception{
        Request request = new Request().setRequest("CONTROLLER_EXECUTE_COMMAND").withContentKeyPair("controller",controller).withContentKeyPair("command",command);
         Response response = send(request);
         return new Pair<>(response.getResponseCode(), response.getContent("output"));
    }

    public Controller getControllerByName(String name) {
        return controllerMap.get(name);
    }


}
