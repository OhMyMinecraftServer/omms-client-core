package icu.takeneko.omms.client.session.response;

import com.google.gson.GsonBuilder;
import icu.takeneko.omms.client.util.Pair;
import icu.takeneko.omms.client.util.Result;

import java.util.HashMap;

public class Response {

    private Result responseCode;
    private HashMap<String, String> content = new HashMap<>();

    public Response(String responseCode, HashMap<String, String> content) {
        this.responseCode = Result.valueOf(responseCode);
        this.content = content;
    }

    public Response(Result responseCode, HashMap<String, String> content) {
        this.responseCode = responseCode;
        this.content = content;
    }

    public Response() {
    }

    public static String serialize(Response response) {
        return new GsonBuilder().serializeNulls().create().toJson(response);
    }

    public static Response deserialize(String x) {
        return new GsonBuilder().serializeNulls().create().fromJson(x, Response.class);
    }

    public Result getResponseCode() {
        return responseCode;
    }

    public String getContent(String key) {
        return content.get(key);
    }

    public Pair<String, String> getPair(String k, String v) {
        return new Pair<>(this.getContent(k), this.getContent(v));
    }


    @Override
    public String toString() {
        return "Response{" +
                "code='" + responseCode + '\'' +
                ", content=" + content +
                '}';
    }
}
