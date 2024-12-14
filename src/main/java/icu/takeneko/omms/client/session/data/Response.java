package icu.takeneko.omms.client.session.data;

import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;

@Getter
@ToString
public class Response {
    private String requestId;
    private StatusEvent event;
    private HashMap<String, String> content = new HashMap<>();

    public static Response deserialize(String x) {
        return new GsonBuilder().serializeNulls().create().fromJson(x, Response.class);
    }

    public String getContent(String key) {
        return content.get(key);
    }
}
