package icu.takeneko.omms.client.session;

import icu.takeneko.omms.client.exception.ConnectionFailedException;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;

class SessionTest {
    ClientSession session;

    @Test
    void testSession() throws Throwable {
        try {
            ClientInitialSession initialSession = new ClientInitialSession(InetAddress.getByName("localhost"), 50000);
            String token = ClientInitialSession.generateToken("o6n1ywzk");
            System.out.println("token = " + token);
            session = initialSession.init(token);
            CountDownLatch latch = new CountDownLatch(1 + 10 + 10);
            session.setOnNewBroadcastReceivedCallback(b -> {
                System.out.println("b = " + b);
                latch.countDown();
            });
            session.setChatMessagePassthroughState(true, state -> {
                System.out.println("state = " + state);
                latch.countDown();
            });
            for (int i = 0; i < 10; i++) {
                session.sendChatbridgeMessage(
                        "GLOBAL",
                        "TEST MESSAGE" + i,
                        (a,b) -> latch.countDown()
                );
            }

            latch.await();
            session.close((s) -> System.out.println("ServerName = " + s));
            session.join();
        }catch (ConnectionFailedException e){
            System.out.println("e.getResponse() = " + e.getResponse());
            throw e;
        }
    }
}