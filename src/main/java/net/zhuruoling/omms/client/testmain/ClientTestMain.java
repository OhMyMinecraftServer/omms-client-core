package net.zhuruoling.omms.client.testmain;

import net.zhuruoling.omms.client.request.Request;
import net.zhuruoling.omms.client.message.Message;
import net.zhuruoling.omms.client.server.session.ClientInitialSession;
import net.zhuruoling.omms.client.server.session.ClientSession;

import java.net.InetAddress;

public class ClientTestMain {
    public static void main(String[] args) throws Exception {
        ClientInitialSession initialSession = new ClientInitialSession(InetAddress.getByName("localhost"),50000);
        ClientSession session = initialSession.init(114514);
        session.fetchWhitelistFromServer();
        System.out.println(session.getWhitelistMap().toString());
        Message message = session.send(new Request("TEST",new String[]{}));
        System.out.println(message.toString());
        message = session.send(new Request("TEST2",new String[]{}));
        System.out.println(message.toString());
        System.out.println(session.queryWhitelist("my_whitelist", "ZhuRuoLing"));
        System.out.println(session.queryWhitelist("wdnmd", "ZhuRuoLing"));
        System.out.println(session.queryInAllWhitelist("ZhuRuoLing"));
        System.out.println(session.queryInAllWhitelist("Simuoss"));
        System.out.println(session.queryInAllWhitelist("abab"));
        session.close();
    }
}
