package net.zhuruoling.omms.client.response;

import com.google.gson.GsonBuilder;

import java.util.HashMap;

public class Response {

    private String code = "";
    private HashMap<String, String> content = new HashMap<>();

    public Response(String code, HashMap<String, String> content) {
        this.code = code;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getContent(String key) {
        return content.get(key);
    }

    public void setContent(HashMap<String, String> content) {
        this.content = content;
    }

    public Response withResponseCode(String code){
        setCode(code);
        return this;
    }

    public Response withContentPair(String a, String b) {
        content.put(a, b);
        return this;
    }

    @Override
    public String toString() {
        return "Response{" +
                "code='" + code + '\'' +
                ", content=" + content +
                '}';
    }
}
