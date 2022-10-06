package net.zhuruoling.omms.client.session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.zhuruoling.omms.client.controller.Instruction;
import net.zhuruoling.omms.client.request.Request;
import net.zhuruoling.omms.client.controller.Controller;
import net.zhuruoling.omms.client.message.Message;
import net.zhuruoling.omms.client.response.Response;
import net.zhuruoling.omms.client.system.SystemInfo;
import net.zhuruoling.omms.client.util.EncryptedConnector;
import net.zhuruoling.omms.client.util.Result;
import net.zhuruoling.omms.client.util.Util;

import java.net.Socket;
import java.util.*;

public class ClientSession {
    private final Gson gson = new GsonBuilder().serializeNulls().create();
    EncryptedConnector connector;
    private Socket socket;

    private HashMap<String, ArrayList<String>> whitelistMap = new HashMap<>();
    private HashMap<String, Controller> controllerMap = new HashMap<>();

    private SystemInfo systemInfo = null;

    public SystemInfo getSystemInfo() {
        return systemInfo;
    }

    public ClientSession(EncryptedConnector connector, Socket socket) {
        this.connector = connector;
        this.socket = socket;
    }

    public HashMap<String, Controller> getControllerMap() {
        return controllerMap;
    }

    public Response send(Request request) throws Exception {
        String content = gson.toJson(request);
        connector.println(content);
        Response response = gson.fromJson(connector.readLine(), Response.class);
        return response;
    }

    public void close() throws Exception {
        Response response = send(new Request("END"));
        if (Objects.equals(response.getCode(), "OK")) {
            socket.close();
        }
    }

    public void fetchWhitelistFromServer() throws Exception {
        Response response = send(new Request("WHITELIST_LIST"));
        if (Objects.equals(response.getCode(), "OK")) {
            String[] whitelistNames = gson.fromJson(response.getContent("whitelists"), String[].class);
            for (String whitelistName : whitelistNames) {
                response = send(new Request("WHITELIST_GET").withContentKeyPair("whitelist", whitelistName));
                if (Objects.equals(response.getCode(), "OK")) {
                    whitelistMap.put(whitelistName, new ArrayList<>(Arrays.asList(gson.fromJson(response.getContent("players"), String[].class))));
                }
            }
        }
    }

    public HashMap<String, ArrayList<String>> getWhitelistMap() {
        return whitelistMap;
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
                .withContentKeyPair("whitelist",whitelistName)
                .withContentKeyPair("player", player)
        );
        return Result.valueOf(response.getCode());
    }

    public Result removeFromWhitelist(String whitelistName, String player) throws Exception {
        Response response = this.send(new Request("WHITELIST_REMOVE")
                .withContentKeyPair("whitelist",whitelistName)
                .withContentKeyPair("player", player)
        );
        return Result.valueOf(response.getCode());
    }

    public Result fetchCotrollersFromServer() throws Exception {
        Response response = send(new Request("CONTROLLERS_LIST"));
        if (Objects.equals(response.getCode(), "OK")) {
            String[] controllerNames = Util.string2Array(response.getContent("names"));
            for (String controllerName : controllerNames) {
                Response response1 = send(new Request("CONTROLLERS_GET").withContentKeyPair("controller",controllerName));
                if (Objects.equals(response1.getCode(), "OK")) {
                    String jsonString = response1.getContent("controller");
                    Controller controller = gson.fromJson(jsonString, Controller.class);
                    System.out.println(controller.toString());
                    controllerMap.put(controllerName, controller);
                } else return Result.valueOf(response1.getCode());
            }
            return Result.OK;
        } else {
            return Result.valueOf(response.getCode());
        }
    }

    public Result fetchSystemInfoFromServer() throws Exception{
        Response response = send(new Request("SYSINFO_GET"));
        if (Objects.equals(response.getCode(), "OK")) {
            this.systemInfo = gson.fromJson(response.getContent("systemInfo"), SystemInfo.class);
            return Result.OK;
        } else {
            return Result.valueOf(response.getCode());
        }
    }

    public Result executeControllerCommand(Controller controller, Instruction instruction) throws Exception {
        Response response = send(new Request("CONTROLLERS_EXECUTE").withContentKeyPair("command", Instruction.asJsonString(instruction)));
        return Result.valueOf(response.getCode());
    }


}
