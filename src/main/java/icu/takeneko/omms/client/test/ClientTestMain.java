package icu.takeneko.omms.client.test;

import icu.takeneko.omms.client.session.ClientInitialSession;
import icu.takeneko.omms.client.session.ClientSession;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ClientTestMain {
    public static void main(String[] args) throws Exception {
        ClientInitialSession initialSession = new ClientInitialSession(InetAddress.getByName("localhost"), 50000);
        ClientSession session = initialSession.init(114514);


    }
}
