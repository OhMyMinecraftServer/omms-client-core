package net.zhuruoling.omms.client.testmain;

import com.google.gson.Gson;
import net.zhuruoling.omms.client.session.ClientInitialSession;
import net.zhuruoling.omms.client.session.ClientSession;
import net.zhuruoling.omms.client.util.Pair;
import net.zhuruoling.omms.client.util.Result;

import java.net.InetAddress;

public class ClientTestMain {
    public static void main(String[] args) throws Exception {
        ClientInitialSession initialSession = new ClientInitialSession(InetAddress.getByName("localhost"),50000);
        ClientSession session = initialSession.init(114514);
//        session.fetchWhitelistFromServer();
//        session.fetchControllersFromServer();
//        session.fetchSystemInfoFromServer();
//        session.fetchAnnouncementFromServer();
        System.out.println("======BEGIN======");
//        System.out.println(session.getWhitelistMap().toString());
//        System.out.println("=================");
//        System.out.println(new Gson().toJson(session.getSystemInfo()));
//        System.out.println("=================");
//        System.out.println(session.getControllerMap().toString());
//        System.out.println("=================");
//        System.out.println(session.getAnnouncementMap().toString());
//        System.out.println("=================");
//        System.out.println(session.sendCommandToController("some_controller_name_that_does_not_exist","say Hello World!"));
//        System.out.println(session.sendCommandToController("out_survival","sendToConsole !!qb make"));
        System.out.println(session.fetchControllerStatus("out_survival"));
        Pair<Result, String> resultStringPair = session.sendCommandToController("out_survival", "carpet list");
        System.out.println(resultStringPair.getA());
        System.out.println(resultStringPair.getB());
        session.close();
    }
}
