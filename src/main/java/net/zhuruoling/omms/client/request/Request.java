package net.zhuruoling.omms.client.request;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class Request {
    public Request(String cmd){
        this.cmd = cmd;
    }

    public Request(String cmd, HashMap<String, String> content) {
        this.cmd = cmd;
        this.content = content;
    }

    @SerializedName("cmd")
    String cmd = "";
    @SerializedName("content")
    HashMap<String, String> content = new HashMap<>();

    public String getCmd() {
        return cmd;
    }


    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getContent(String key) {
        return content.get(key);
    }

    public Request withContentKeyPair(String key, String pair){
        content.put(key,pair);
        return this;
    }

}
