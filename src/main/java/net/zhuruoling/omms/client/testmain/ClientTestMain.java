package net.zhuruoling.omms.client.testmain;

import net.zhuruoling.omms.client.session.ClientInitialSession;
import net.zhuruoling.omms.client.session.ClientSession;

import java.net.InetAddress;
import java.util.Arrays;

public class ClientTestMain {
    public static void main(String[] args) throws Exception {
        ClientInitialSession initialSession = new ClientInitialSession(InetAddress.getByName("localhost"), 50000);
        ClientSession session = initialSession.init(114514);
        session.setOnAnyExceptionCallback((pair) -> pair.getB().printStackTrace());
        session.setOnResponseReceivedCallback((response) -> System.out.println(response.toString()));
        System.out.println("===========BEGIN===========");
        System.out.println("===========FETCH ALL===========");
        //session.fetchWhitelistFromServer((map) -> map.forEach((s, strings) -> System.out.printf("%s %s\n", s, Arrays.toString(strings.toArray()))), null);
        //session.fetchAnnouncementFromServer((map) -> map.forEach((s, announcement) -> System.out.printf("%s %s\n", s, announcement.toString())));
        session.fetchControllersFromServer((map) -> map.forEach((s, controller) -> System.out.printf("%s %s", s,controller.toString())));
//        session.fetchSystemInfoFromServer((info) -> System.out.printf("%s", info.toString()));
        System.out.println("\n===========WHITELIST TEST===========");

        System.out.println("\n===========CONTROLLER TEST===========");
    }
}
