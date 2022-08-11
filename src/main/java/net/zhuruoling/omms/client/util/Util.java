package net.zhuruoling.omms.client.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Util {
    public static String base64Encode(String content){
        return Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
    }

}
