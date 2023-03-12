package net.zhuruoling.omms.client.response;

import com.google.gson.GsonBuilder;
import net.zhuruoling.omms.client.util.Pair;
import net.zhuruoling.omms.client.util.Result;

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

    public void setResponseCode(Result responseCode) {
        this.responseCode = responseCode;
    }

    public String getContent(String key) {
        return content.get(key);
    }

    public Pair<String, String> getPair(String k, String v){
        new Pair<>(this.getContent(k), this.getContent(v));
    }

    public void setContent(HashMap<String, String> content) {
        this.content = content;
    }

    public Response withResponseCode(Result code){
        setResponseCode(code);
        return this;
    }

    public Response withContentPair(String a, String b) {
        content.put(a, b);
        return this;
    }

    @Override
    public String toString() {
        return "Response{" +
                "code='" + responseCode + '\'' +
                ", content=" + content +
                '}';
    }
}
