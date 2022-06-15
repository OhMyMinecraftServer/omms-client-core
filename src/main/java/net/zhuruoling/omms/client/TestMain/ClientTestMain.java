package net.zhuruoling.omms.client.TestMain;

import net.zhuruoling.omms.client.ConsoleLogger;
import net.zhuruoling.omms.client.server.session.ClientInitialSession;
import net.zhuruoling.omms.client.server.session.ClientSession;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientTestMain {
    public static void main(String[] args) throws Exception {
        ConsoleLogger.info("getServerAddress");
        Scanner scanner = new Scanner(System.in);
        String addr = scanner.nextLine();
        Object obj = InetAddress.getByName(addr);
        ConsoleLogger.info("getServerPort");
        int port = scanner.nextInt();
        ClientInitialSession initialSession = new ClientInitialSession(InetAddress.getByName(addr),port);
        ConsoleLogger.info("getPermissionCode");
        int code = scanner.nextInt();
        ClientSession session = initialSession.init(code);
    }
}
