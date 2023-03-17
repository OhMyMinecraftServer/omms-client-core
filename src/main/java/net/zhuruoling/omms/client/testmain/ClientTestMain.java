package net.zhuruoling.omms.client.testmain;

import net.zhuruoling.omms.client.session.ClientInitialSession;
import net.zhuruoling.omms.client.session.ClientSession;

import java.net.InetAddress;

public class ClientTestMain {
    public static void main(String[] args) throws Exception {
        ClientInitialSession initialSession = new ClientInitialSession(InetAddress.getByName("localhost"),50000);
        ClientSession session = initialSession.init(114514);

        System.out.println("======BEGIN======");


        session.close((s) -> System.out.println("Disconnected from server."));
    }
}
