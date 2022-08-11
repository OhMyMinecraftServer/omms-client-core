package net.zhuruoling.omms.client.server.session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.zhuruoling.omms.client.command.Command;
import net.zhuruoling.omms.client.message.Message;
import net.zhuruoling.omms.client.util.EncryptedConnector;
import net.zhuruoling.omms.client.util.WhitelistResult;

import java.net.Socket;
import java.util.*;

public class ClientSession {
    EncryptedConnector connector;
    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private Socket socket;

    private HashMap<String, ArrayList<String>> whitelistMap = new HashMap<>();
    public ClientSession(EncryptedConnector connector, Socket socket) {
        this.connector = connector;
        this.socket = socket;
    }

    public Message send(Command command) throws Exception {
        String content = gson.toJson(command);
        connector.println(content);
        Message message = gson.fromJson(connector.readLine(),Message.class);
        return message;
    }

    public void close() throws Exception {
        Message message = send(new Command("END", new String[]{}));
        if (message.getMsg() == "OK"){
            socket.close();
        }

    }

    public void fetchWhitelistFromServer() throws Exception{
        Message message = send(new Command("WHITELIST_LIST",new String[]{}));
        if (Objects.equals(message.getMsg(), "OK")){
            String[] whitelistNames = message.getLoad();
            for (String whitelistName : whitelistNames) {
                message = send(new Command("WHITELIST_GET",new String[]{whitelistName}));
                if (Objects.equals(message.getMsg(), "OK")){
                    whitelistMap.put(whitelistName, new ArrayList<String>(Arrays.asList(message.getLoad())));
                }
            }
        }

    }

    public HashMap<String, ArrayList<String>> getWhitelistMap() {
        return whitelistMap;
    }

    public WhitelistResult queryWhitelist(String whitelistName, String playerName){
        if (whitelistMap.isEmpty()){
            return WhitelistResult.NO_WHITELIST;
        }
        if (!whitelistMap.containsKey(whitelistName)){
            return WhitelistResult.WHITELIST_NOT_EXIST;
        }
        if (whitelistMap.get(whitelistName).contains(playerName)){
            return WhitelistResult.OK;
        }
        return WhitelistResult.NO_SUCH_PLAYER;
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
    public WhitelistResult addToWhitelist(String whitelistName, String player) throws Exception {
        Message message = this.send(new Command("WHITELIST_ADD",new String[]{whitelistName, player}));
        return WhitelistResult.valueOf(message.getMsg());
    }

    public WhitelistResult removeFromWhitelist(String whitelistName, String player) throws Exception{
        Message message = this.send(new Command("WHITELIST_REMOVE",new String[]{whitelistName, player}));
        return WhitelistResult.valueOf(message.getMsg());
    }


}
