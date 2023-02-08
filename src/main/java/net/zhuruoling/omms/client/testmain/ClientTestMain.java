package net.zhuruoling.omms.client.testmain;

import net.zhuruoling.omms.client.controller.Status;
import net.zhuruoling.omms.client.session.ClientInitialSession;
import net.zhuruoling.omms.client.session.ClientSession;
import net.zhuruoling.omms.client.util.Pair;
import net.zhuruoling.omms.client.util.Result;

import java.net.InetAddress;

public class ClientTestMain {
    public static void main(String[] args) throws Exception {
        ClientInitialSession initialSession = new ClientInitialSession(InetAddress.getByName("localhost"),50000);
        ClientSession session = initialSession.init(114514);

        System.out.println("======BEGIN======");
        for (int j = 0; j < 50; j++) {
            Thread.sleep(1000);
            for (int i = 0; i < 99; i++) {
                Pair<Result, Status> result = session.fetchControllerStatus("skyblock");
                System.out.printf("%d %d %s %s\n",j, i, result.getA(), result.getB());
            }
        }
        session.close();
    }
}
