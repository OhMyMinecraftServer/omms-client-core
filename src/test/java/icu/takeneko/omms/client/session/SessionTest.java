package icu.takeneko.omms.client.session;

import icu.takeneko.omms.client.exception.ConnectionFailException;
import icu.takeneko.omms.client.exception.PermissionDeniedException;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;

class SessionTest {
    ClientSession session;

    @Test
    void testSession() throws Throwable {
        try {
            ClientInitialSession initialSession = new ClientInitialSession(InetAddress.getByName("localhost"), 50000);
            String token = ClientInitialSession.generateToken("o6n1ywzk");
            System.out.println("token = " + token);
            session = initialSession.init(token);
            session.close((s) -> System.out.println("ServerName = " + s));
            session.join();
        }catch (ConnectionFailException e){
            System.out.println("e.getResponse() = " + e.getResponse());
            throw e;
        }
    }
}