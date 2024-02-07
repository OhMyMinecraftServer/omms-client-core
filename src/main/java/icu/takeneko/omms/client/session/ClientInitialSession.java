package icu.takeneko.omms.client.session;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import icu.takeneko.omms.client.request.InitRequest;
import icu.takeneko.omms.client.response.Response;
import icu.takeneko.omms.client.util.ConnectionFailException;
import icu.takeneko.omms.client.util.EncryptedConnector;
import icu.takeneko.omms.client.util.Result;
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

public class ClientInitialSession {
    InetAddress inetAddress;
    int port;
    public ClientInitialSession(InetAddress inetAddress, int port){
        this.port = port;
        this.inetAddress = inetAddress;
    }

    public ClientSession init(int code) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, ConnectionFailException, InterruptedException {
        Socket socket = new Socket(this.inetAddress,this.port);
        socket.setKeepAlive(true);

        LocalDateTime date = LocalDateTime.now();
        String key = date.format(DateTimeFormatter.ofPattern("yyyyMMddhhmm"));
        key = Base64.getEncoder().encodeToString(key.getBytes(StandardCharsets.UTF_8));
        key = Base64.getEncoder().encodeToString(key.getBytes(StandardCharsets.UTF_8));

        EncryptedConnector connector = new EncryptedConnector(
                new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                ),
                new PrintWriter(new OutputStreamWriter(socket.getOutputStream())),
                key
        );

        long timeCode = Long.parseLong(date.format(DateTimeFormatter.ofPattern("yyyyMMddhhmm")));
        String connCode = String.valueOf(timeCode ^ code);
        connCode = Util.base64Encode(connCode);
        connCode = Util.base64Encode(connCode);

        Gson gson = new GsonBuilder().serializeNulls().create();
        String content = gson.toJson(new InitRequest(Util.PROTOCOL_VERSION).withContentKeyPair("token", connCode));
        System.out.println(content);
        connector.send(content);

        String line = connector.readLine();
        Response response = Response.deserialize(line);
        System.out.println(response);
        if (response.getResponseCode() == Result.OK){
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
        }
        else {
            throw new ConnectionFailException(String.format("Server returned ERR_CODE:%s", response.getResponseCode()));
        }

    }
}
