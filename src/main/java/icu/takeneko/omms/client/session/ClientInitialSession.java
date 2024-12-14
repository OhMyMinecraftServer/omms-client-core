package icu.takeneko.omms.client.session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import icu.takeneko.omms.client.Constants;
import icu.takeneko.omms.client.exception.VersionNotMatchException;
import icu.takeneko.omms.client.session.data.LoginRequest;
import icu.takeneko.omms.client.session.data.Response;
import icu.takeneko.omms.client.exception.ConnectionFailedException;
import icu.takeneko.omms.client.session.data.StatusEvent;
import icu.takeneko.omms.client.util.EncryptedConnector;
import icu.takeneko.omms.client.util.Util;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Create a {@link ClientSession} and authenticate with Central Server
 */
public class ClientInitialSession {
    InetAddress inetAddress;
    int port;

    public ClientInitialSession(InetAddress inetAddress, int port) {
        this.port = port;
        this.inetAddress = inetAddress;
    }

    public ClientSession init(String token) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, ConnectionFailedException, InterruptedException {
        Socket socket = new Socket(this.inetAddress, this.port);
        socket.setKeepAlive(true);

        LocalDateTime date = LocalDateTime.now();
        String key = date.format(DateTimeFormatter.ofPattern("yyyyMMddhhmm"));
        key = Base64.getEncoder().encodeToString(key.getBytes(StandardCharsets.UTF_8));
        key = Base64.getEncoder().encodeToString(key.getBytes(StandardCharsets.UTF_8));

        EncryptedConnector connector = new EncryptedConnector(
                new BufferedReader(new InputStreamReader(socket.getInputStream())),
                new PrintWriter(new OutputStreamWriter(socket.getOutputStream())),
                key
        );
        Gson gson = new GsonBuilder().serializeNulls().create();
        String content = gson.toJson(new LoginRequest(Constants.PROTOCOL_VERSION, token));
        connector.send(content);

        String line = connector.readLine();
        Response response = Response.deserialize(line);
        if (response.getEvent() == StatusEvent.SUCCESS) {
            String newKey = response.getContent("key");
            EncryptedConnector newConnector = new EncryptedConnector(
                    new BufferedReader(
                            new InputStreamReader(socket.getInputStream())
                    ),
                    new PrintWriter(new OutputStreamWriter(socket.getOutputStream())),
                    newKey
            );
            ClientSession clientSession = new ClientSession(newConnector, socket, response.getContent("serverName"));
            clientSession.start();
            return clientSession;
        } else {
            if (response.getEvent() == StatusEvent.FAIL){
                String serverVersion = response.getContent("version");
                if (serverVersion == null){
                    throw new VersionNotMatchException();
                }
                throw new VersionNotMatchException(Long.parseLong(serverVersion));
            }
            throw new ConnectionFailedException(response);
        }

    }

    public static String generateTokenFromHashed(String hashed){
        LocalDateTime date = LocalDateTime.now();
        String time = date.format(DateTimeFormatter.ofPattern("yyyyMMddhhmm"));
        return Base64.getEncoder().encodeToString((time + ";" + hashed).getBytes());
    }

    public static String generateToken(String original){
        return generateTokenFromHashed(Util.getChecksumMD5(original));
    }
}
