package net.zhuruoling.omms.client.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Util {
    private static Gson gson = new  GsonBuilder().serializeNulls().create();
    public static String base64Encode(String content){
        return Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
    }

    public static String[] string2Array(String s){
        return gson.fromJson(s, String[].class);
    }
}
