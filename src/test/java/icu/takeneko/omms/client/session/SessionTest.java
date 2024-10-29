package icu.takeneko.omms.client.session;

import icu.takeneko.omms.client.exception.ConnectionFailedException;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

class SessionTest {
    ClientSession session;

    @Test
    void testSession() throws Throwable {
        try {
            ClientInitialSession initialSession = new ClientInitialSession(InetAddress.getByName("localhost"), 50000);
            String token = ClientInitialSession.generateToken("o6n1ywzk");
            System.out.println("token = " + token);
            session = initialSession.init(token);
            CountDownLatch latch = new CountDownLatch(3);
            session.fetchSystemInfoFromServer(info -> {
                System.out.println("info = " + info);
                latch.countDown();
            });
            session.fetchWhitelistFromServer(it -> {
                System.out.println("it.toString() = " + it.toString());
                latch.countDown();
            });
            while (latch.getCount() != 1) {
                LockSupport.parkNanos(1);
            }
            session.close((s) -> {
                System.out.println("ServerName = " + s);
                latch.countDown();
            });
            latch.await();
            session.join();
        } catch (ConnectionFailedException e) {
            System.out.println("e.getResponse() = " + e.getResponse());
            throw e;
        }
    }
}