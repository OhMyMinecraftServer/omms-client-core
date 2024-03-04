package icu.takeneko.omms.client.request;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class Request {
    public Request(String req) {
        this.request = req;
    }

    public Request() {

    }

    @SerializedName("request")
    String request;

    @SerializedName("content")
    HashMap<String, String> content = new HashMap<>();

    public String getContent(String key) {
        return content.get(key);
    }

    public void setContent(HashMap<String, String> content) {
        this.content = content;
    }

    public String getRequest() {
        return request;
    }

    public Request setRequest(String request) {
        this.request = request;
        return this;
    }


    public Request withContentKeyPair(String key, String pair) {
        content.put(key, pair);
        return this;
    }

    @Override
    public String toString() {
        return "Request{" +
                "request='" + request + '\'' +
                ", content=" + content +
                '}';
    }
}
