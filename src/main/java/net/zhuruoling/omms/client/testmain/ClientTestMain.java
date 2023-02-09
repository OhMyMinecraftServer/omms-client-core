package net.zhuruoling.omms.client.testmain;

import net.zhuruoling.omms.client.session.ClientInitialSession;
import net.zhuruoling.omms.client.session.ClientSession;
import net.zhuruoling.omms.client.util.Pair;
import net.zhuruoling.omms.client.util.Result;

import java.net.InetAddress;
import java.util.Objects;
import java.util.Scanner;

public class ClientTestMain {
    public static void main(String[] args) throws Exception {
        ClientInitialSession initialSession = new ClientInitialSession(InetAddress.getByName("localhost"),50000);
        ClientSession session = initialSession.init(114514);

        System.out.println("======BEGIN======");
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        while (!Objects.equals(line, "end")) {
            Pair<Result, String> resultStringPair = session.sendCommandToController("out_survival", line);
            System.out.println(resultStringPair.getA());
            System.out.println(resultStringPair.getB());
            line = scanner.nextLine();
        }

        session.close();
    }
}
