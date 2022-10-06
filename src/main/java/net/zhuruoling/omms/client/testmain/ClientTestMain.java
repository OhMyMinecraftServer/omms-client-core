package net.zhuruoling.omms.client.testmain;

import net.zhuruoling.omms.client.request.Request;
import net.zhuruoling.omms.client.message.Message;
import net.zhuruoling.omms.client.session.ClientInitialSession;
import net.zhuruoling.omms.client.session.ClientSession;

import java.net.InetAddress;

public class ClientTestMain {
    public static void main(String[] args) throws Exception {
        ClientInitialSession initialSession = new ClientInitialSession(InetAddress.getByName("localhost"),50000);
        ClientSession session = initialSession.init(114514);
        session.fetchWhitelistFromServer();
        System.out.println(session.getWhitelistMap().toString());

        session.close();
    }
}
