package net.zhuruoling.omms.client.server.session;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.org.apache.bcel.internal.generic.RET;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import net.zhuruoling.omms.client.message.Message;
import net.zhuruoling.omms.client.util.ConnectionFailException;
import net.zhuruoling.omms.client.util.EncryptedConnector;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ClientInitialSession {
    InetAddress inetAddress;
    int port;
    public ClientInitialSession(InetAddress inetAddress, int port){
        this.port = port;
        this.inetAddress = inetAddress;
    }

    public ClientSession init(int code) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, ConnectionFailException {
        Socket socket = new Socket(this.inetAddress,this.port);
        LocalDateTime date = LocalDateTime.now();
        String key = date.format(DateTimeFormatter.ofPattern("yyyyMMddhhmm"));
        EncryptedConnector connector = new EncryptedConnector(
                new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                ),
                new PrintWriter(new OutputStreamWriter(socket.getOutputStream())),
                key
        );
        long timeCode = Long.parseLong(key);
        String connCode = String.valueOf(timeCode ^ code);
        Gson gson = new GsonBuilder().serializeNulls().create();
        connector.send(gson.toJson(new Object(){
            final String command = "PING";
            final String[] load = {connCode};
        }));
        String line = connector.readLine();
        Message message = gson.fromJson(line, Message.class);
        if (Objects.equals(message.getMsg(), "OK")){
            String newKey = message.getLoad()[0];
            EncryptedConnector newConnector = new EncryptedConnector(
                    new BufferedReader(
                            new InputStreamReader(socket.getInputStream())
                    ),
                    new PrintWriter(new OutputStreamWriter(socket.getOutputStream())),
                    newKey
            );
            return new ClientSession(newConnector);
        }
        else {
            throw new ConnectionFailException();
        }

    }
}
