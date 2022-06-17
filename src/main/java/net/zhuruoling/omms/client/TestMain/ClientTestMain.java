package net.zhuruoling.omms.client.TestMain;

import net.zhuruoling.omms.client.command.Command;
import net.zhuruoling.omms.client.message.Message;
import net.zhuruoling.omms.client.server.session.ClientInitialSession;
import net.zhuruoling.omms.client.server.session.ClientSession;

import java.net.InetAddress;

public class ClientTestMain {
    public static void main(String[] args) throws Exception {
        ClientInitialSession initialSession = new ClientInitialSession(InetAddress.getByName("localhost"),50000);
        ClientSession session = initialSession.init(114514);
        Message message = session.send(new Command("TEST1",new String[]{}));
        System.out.println(message.toString());
        message = session.send(new Command("TEST2",new String[]{}));
        System.out.println(message.toString());
    }
}
