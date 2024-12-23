package icu.takeneko.omms.client.util;

import lombok.Getter;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Getter
public class EncryptedConnector {
    private final BufferedReader in;
    private final PrintWriter out;
    private final byte[] key;

    public EncryptedConnector(BufferedReader in, PrintWriter out, String key) {
        this.in = in;
        this.out = out;
        this.key = key.getBytes(StandardCharsets.UTF_8);
    }

    public void println(String content) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.send(content);
    }

    public void send(String content) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        byte[] data = encryptECB(content.getBytes(StandardCharsets.UTF_8), this.key);
        out.println(new String(data, StandardCharsets.UTF_8));
        out.flush();
    }

    public String readLine() throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String line = in.readLine();
        if (line == null) return null;
        byte[] data = decryptECB(line.getBytes(StandardCharsets.UTF_8), this.key);
        return new String(data, StandardCharsets.UTF_8);
    }

    private static byte[] encryptECB(byte[] data, byte[] key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
        byte[] result = cipher.doFinal(data);
        return Base64.getEncoder().encode(result);
    }

    private static byte[] decryptECB(byte[] data, byte[] key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
        byte[] base64 = Base64.getDecoder().decode(data);
        return cipher.doFinal(base64);
    }

}
