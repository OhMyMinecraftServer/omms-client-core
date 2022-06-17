package net.zhuruoling.omms.client.server.session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jdk.nashorn.internal.runtime.Scope;
import net.zhuruoling.omms.client.ConsoleLogger;
import net.zhuruoling.omms.client.command.Command;
import net.zhuruoling.omms.client.message.Message;
import net.zhuruoling.omms.client.util.EncryptedConnector;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class ClientSession {
    EncryptedConnector connector;
    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private Socket socket;
    public ClientSession(EncryptedConnector connector, Socket socket) throws InterruptedException {
        ConsoleLogger.info("awa");
        this.connector = connector;
        this.socket = socket;
    }

    public Message send(Command command) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException {
        String content = gson.toJson(command);
        ConsoleLogger.info(content);
        connector.println(content);
        Message message = gson.fromJson(connector.readLine(),Message.class);
        return message;
    }

    public void close() throws IOException {
        socket.close();
    }
}

