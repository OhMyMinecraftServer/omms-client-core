package icu.takeneko.omms.client.session;

import icu.takeneko.omms.client.util.Util;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;

class SessionTest {
    ClientSession session;

    //@Test
    void testSession() throws Throwable {
        ClientInitialSession initialSession = new ClientInitialSession(InetAddress.getByName("localhost"), 50000);
        session = initialSession.init(114514);
        CountDownLatch latch = new CountDownLatch(4);
        session.fetchSystemInfoFromServer(si -> {
            System.out.println("systemInfo = " + si.toString());
            System.out.flush();
            latch.countDown();
        });
        session.fetchWhitelistFromServer(map -> {
            map.forEach((s, strings) -> System.out.printf("whitelist: %s -> %s\n", s, Util.joinToString(strings)));
            System.out.flush();
            latch.countDown();
        });
        session.fetchControllersFromServer(map -> {
            CountDownLatch cl1 = new CountDownLatch(map.size());
            map.forEach((s, controller) -> {
                System.out.printf("controller %s -> %s", s, controller.toString());
                session.fetchControllerStatus(controller.getId(),
                        (status) -> {
                            System.out.printf("controller status %s -> %s", s, status.toString());
                            System.out.flush();
                            cl1.countDown();
                        },
                        (cid) -> {
                            System.out.printf("controller not found %s", s);
                            System.out.flush();
                            cl1.countDown();
                        }
                );
                System.out.flush();
            });
            try {
                cl1.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            latch.countDown();
        });
        session.fetchAnnouncementFromServer(map -> {
            map.forEach((s, announcement) -> System.out.printf("announcement %s -> %s", s, announcement));
            System.out.flush();
            latch.countDown();
        });
        System.out.flush();
        latch.await();
        session.close((s) -> System.out.println("ServerName = " + s));
        session.join();
    }
}