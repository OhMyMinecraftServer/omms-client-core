package net.zhuruoling.omms.client.server.session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.zhuruoling.omms.client.controller.Instruction;
import net.zhuruoling.omms.client.request.Request;
import net.zhuruoling.omms.client.controller.Controller;
import net.zhuruoling.omms.client.message.Message;
import net.zhuruoling.omms.client.util.EncryptedConnector;
import net.zhuruoling.omms.client.util.Result;

import java.net.Socket;
import java.util.*;

public class ClientSession {
    EncryptedConnector connector;
    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private Socket socket;

    private HashMap<String, ArrayList<String>> whitelistMap = new HashMap<>();
    private HashMap<String, Controller> controllerMap = new HashMap<>();
    public ClientSession(EncryptedConnector connector, Socket socket) {
        this.connector = connector;
        this.socket = socket;
    }

    public Message send(Request request) throws Exception {
        String content = gson.toJson(request);
        connector.println(content);
        Message message = gson.fromJson(connector.readLine(),Message.class);
        return message;
    }

    public void close() throws Exception {
        Message message = send(new Request("END", new String[]{}));
        if (message.getMsg() == "OK"){
            socket.close();
        }

    }

    public void fetchWhitelistFromServer() throws Exception{
        Message message = send(new Request("WHITELIST_LIST",new String[]{}));
        if (Objects.equals(message.getMsg(), "OK")){
            String[] whitelistNames = message.getLoad();
            for (String whitelistName : whitelistNames) {
                message = send(new Request("WHITELIST_GET",new String[]{whitelistName}));
                if (Objects.equals(message.getMsg(), "OK")){
                    whitelistMap.put(whitelistName, new ArrayList<String>(Arrays.asList(message.getLoad())));
                }
            }
        }
    }

    public HashMap<String, ArrayList<String>> getWhitelistMap() {
        return whitelistMap;
    }

    public Result queryWhitelist(String whitelistName, String playerName){
        if (whitelistMap.isEmpty()){
            return Result.NO_WHITELIST;
        }
        if (!whitelistMap.containsKey(whitelistName)){
            return Result.WHITELIST_NOT_EXIST;
        }
        if (whitelistMap.get(whitelistName).contains(playerName)){
            return Result.OK;
        }
        return Result.NO_SUCH_PLAYER;
    }

    public ArrayList<String> queryInAllWhitelist(String playerName){
        ArrayList<String> whitelists = new ArrayList<>();
        if (whitelistMap.isEmpty()){
            return null;
        }
        whitelistMap.forEach((k,v) -> {
            if (v.contains(playerName)){
                whitelists.add(k);
            }
        });
        if (whitelists.isEmpty()){
            return null;
        }
        return whitelists;
    }
    public Result addToWhitelist(String whitelistName, String player) throws Exception {
        Message message = this.send(new Request("WHITELIST_ADD",new String[]{whitelistName, player}));
        return Result.valueOf(message.getMsg());
    }

    public Result removeFromWhitelist(String whitelistName, String player) throws Exception{
        Message message = this.send(new Request("WHITELIST_REMOVE",new String[]{whitelistName, player}));
        return Result.valueOf(message.getMsg());
    }

    public Result fetchCotrollersFromServer() throws Exception {
        Message message = send(new Request("CONTROLLERS_LIST",new String[]{}));
        if (Objects.equals(message.getMsg(), "OK")){
            String[] controllerNames = message.getLoad();
            for (String controllerName : controllerNames) {
                Message message_ = send(new Request("CONTROLLERS_GET",new String[]{controllerName}));
                if (Objects.equals(message_.getMsg(), "OK")){
                    controllerMap.put(controllerName, gson.fromJson(message_.getLoad()[0], Controller.class));
                }
                else return Result.valueOf(message_.getMsg());
            }
            return Result.OK;
        }
        else {
            return Result.valueOf(message.getMsg());
        }
    }

    public Result executeControllerCommand(Controller controller, Instruction instruction) throws Exception {
        Message message = send(new Request("CONTROLLERS_EXECUTE",new String[]{}));
        return Result.valueOf(message.getMsg());
    }


}
