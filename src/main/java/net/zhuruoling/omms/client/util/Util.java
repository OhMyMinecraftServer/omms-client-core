package net.zhuruoling.omms.client.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.zhuruoling.omms.client.request.InitRequest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

public class Util {
    public static Gson gson = new  GsonBuilder().serializeNulls().create();
    public static String base64Encode(String content){
        return Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
    }

    public static String[] string2Array(String s){
        return gson.fromJson(s, String[].class);
    }

    public static final long PROTOCOL_VERSION = InitRequest.VERSION_BASE + 0x04;

    public static String randomStringGen(int len) {
        String ch = "abcdefghijklmnopqrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < len; i++) {
            Random random = new Random(System.nanoTime());
            int num = random.nextInt(62);
            stringBuffer.append(ch.charAt(num));
        }
        return stringBuffer.toString();
    }

}
