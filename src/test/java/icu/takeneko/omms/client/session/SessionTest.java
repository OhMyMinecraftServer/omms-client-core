package icu.takeneko.omms.client.session;

import icu.takeneko.omms.client.util.Util;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;

class SessionTest {
    ClientSession session;

    @Test
    void testSession() throws Throwable {
        ClientInitialSession initialSession = new ClientInitialSession(InetAddress.getByName("localhost"), 50000);
        session = initialSession.init(114514);
        session.fetchSystemInfoFromServer(si -> System.out.println("systemInfo = " + si.toString()));
        session.fetchWhitelistFromServer(map -> map.forEach((s, strings) -> System.out.printf("whitelist: %s -> %s\n", s, Util.joinToString(strings))));
        session.fetchControllersFromServer(map -> map.forEach((s, controller) -> {
            System.out.printf("controller %s -> %s", s, controller.toString());
            session.fetchControllerStatus(controller.getId(),
                    (status) -> System.out.printf("controller status %s -> %s", s, status.toString()),
                    (cid) -> System.out.printf("controller not found %s", s)
            );
        }));
        session.fetchAnnouncementFromServer(map -> map.forEach((s, announcement) -> System.out.printf("announcement %s -> %s", s, announcement)));

        //session.close((s) -> System.out.println("ServerName = " + s));
        session.join();
    }
}