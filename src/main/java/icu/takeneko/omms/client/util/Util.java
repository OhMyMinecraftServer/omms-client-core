package icu.takeneko.omms.client.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import icu.takeneko.omms.client.session.request.InitRequest;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class Util {
    public static Gson gson = new GsonBuilder().serializeNulls().create();

    public static String base64Encode(String content) {
        return Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
    }

    public static final long PROTOCOL_VERSION = InitRequest.VERSION_BASE + 0x10;

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

    public static String joinToString(Collection<String> cl) {
        StringBuilder sb = new StringBuilder("[");
        List<String> tl = new ArrayList<>(cl);
        for (int i = 0; i < tl.size(); i++) {
            if (i == tl.size() - 1) {
                sb.append(tl.get(i));
            } else {
                sb.append(tl.get(i)).append(", ");
            }
        }
        return sb.append("]").toString();
    }
}
