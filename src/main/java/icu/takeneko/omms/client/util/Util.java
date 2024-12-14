package icu.takeneko.omms.client.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Util {
    public static Gson gson = new GsonBuilder().serializeNulls().create();

    public static String base64Encode(String content) {
        return Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateRandomString(int len) {
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

    public static String getChecksumMD5(String original){
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException notIgnored) {
            throw new RuntimeException(notIgnored);
        }
        return Base64.getEncoder().encodeToString(digest.digest(original.getBytes()));
    }

    public static <K, V> Collector<Pair<K, V>, ?, Map<K, V>> toMapCollector() {
        return Collectors.toMap(Pair::getA, Pair::getB);
    }
}
