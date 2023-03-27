package net.zhuruoling.omms.client.testmain;

import net.zhuruoling.omms.client.session.ClientInitialSession;
import net.zhuruoling.omms.client.session.ClientSession;

import java.net.InetAddress;
import java.util.Arrays;

public class ClientTestMain {
    public static void main(String[] args) throws Exception {
        ClientInitialSession initialSession = new ClientInitialSession(InetAddress.getByName("localhost"), 50000);
        ClientSession session = initialSession.init(114514);

        System.out.println("======BEGIN======");
        session.fetchWhitelistFromServer((map) -> map.forEach((s, strings) -> System.out.printf("%s %s", s, Arrays.toString(strings.toArray()))));
        session.fetchAnnouncementFromServer((map) -> map.forEach((s, announcement) -> System.out.printf("%s %s", s, announcement.toString())));
        session.fetchControllersFromServer((map) -> map.forEach((s, controller) -> System.out.printf("%s %s", s,controller.toString())));

    }
}
