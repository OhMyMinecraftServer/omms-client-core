package icu.takeneko.omms.client.session.data;

import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;

@ToString
public class Request {
    public Request(String req) {
        this.request = req;
    }

    public Request() {
    }

    private String request;
    private HashMap<String, String> content = new HashMap<>();
    @Setter
    private String requestId;

    public String getContent(String key) {
        return content.get(key);
    }

    public Request setRequest(String request) {
        this.request = request;
        return this;
    }

    public Request withContentKeyPair(String key, String pair) {
        content.put(key, pair);
        return this;
    }
}
