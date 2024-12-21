package icu.takeneko.omms.client.session;

import icu.takeneko.omms.client.exception.ConnectionFailedException;
import icu.takeneko.omms.client.exception.ControllerNotFoundException;
import icu.takeneko.omms.client.exception.PlayerAlreadyExistsException;
import icu.takeneko.omms.client.exception.PlayerNotFoundException;
import icu.takeneko.omms.client.exception.WhitelistNotFoundException;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

class SessionTest implements ControllerConsoleClient {
    ClientSession session;

    @Test
    void testSession() throws Throwable {
        try {
            ClientInitialSession initialSession = new ClientInitialSession(InetAddress.getByName("localhost"), 50000);
            String token = ClientInitialSession.generateToken("o6n1ywzk");
            System.out.println("token = " + token);
            session = initialSession.init(token);
            CountDownLatch latch = new CountDownLatch(10);
            session.setOnAnyExceptionCallback((th, tr) -> {
                tr.printStackTrace();
            });
            session.fetchWhitelistFromServer()
                .thenAccept(result -> {
                    System.out.println("whitelists = " + result);
                    latch.countDown();
                });
            session.fetchControllersFromServer()
                .thenAccept(result -> {
                    System.out.println("controllers = " + result);
                    latch.countDown();
                });
            session.sendCommandToController("creative", "list")
                .thenAccept(res -> {
                    System.out.println("res = " + res);
                    latch.countDown();
                });
            session.addToWhitelist("some_whitelist", "takeneko")
                .whenComplete((v, ex) -> {
                    System.out.println("Added takeneko to some_whitelist");
                    session.addToWhitelist("some_whitelist", "takeneko")
                        .whenComplete((v1, ex1) -> {
                            System.out.println("ex1 = " + ex1);
                            assert ex1 != null;
                            assert ex1 instanceof PlayerAlreadyExistsException;
                            System.out.println("Assertion Complete");
                            session.removeFromWhitelist("some_whitelist", "takeneko")
                                .whenComplete((v2, ex2) -> {
                                    System.out.println("Player takeneko removed");
                                    session.removeFromWhitelist("some_whitelist", "takeneko")
                                        .whenComplete((v3, ex3) -> {
                                            assert ex3 != null;
                                            assert ex3 instanceof PlayerNotFoundException;
                                            System.out.println("Assertion Complete");
                                            latch.countDown();
                                        });
                                });
                        });
                });
            session.addToWhitelist("something_that_not_exist", "takeneko")
                .whenComplete((v, ex) -> {
                    assert ex != null;
                    assert ex instanceof WhitelistNotFoundException;
                    System.out.println("unknown whitelist 1 Assertion Complete");
                    session.removeFromWhitelist("something_that_not_exist", "takeneko")
                        .whenComplete((v1, ex1) -> {
                            assert ex1 != null;
                            assert ex1 instanceof WhitelistNotFoundException;
                            System.out.println("unknown whitelist 2 Assertion Complete");
                            latch.countDown();
                        });
                });

            session.fetchControllerStatus("something_not_exist")
                .whenComplete((status, throwable) -> {
                    assert throwable != null;
                    assert throwable instanceof ControllerNotFoundException;
                    System.out.println("unknown controller Assertion Complete");
                    session.fetchControllerStatus("creative")
                        .whenComplete((status1, throwable1) -> {
                            assert throwable1 == null;
                            assert status1 != null;
                            System.out.println("status1 = " + status1);
                            System.out.println("existing controller Assertion Complete");
                            latch.countDown();
                        });
                });
            session.fetchSystemInfoFromServer()
                .thenAccept(systemInfo -> {
                    System.out.println("systemInfo = " + systemInfo);
                    latch.countDown();
                });

            session.startControllerConsole("something_not_exist", this)
                .whenComplete((unused, throwable) -> {
                    System.out.println("throwable = " + throwable);
                    assert throwable != null;
                    assert throwable instanceof ControllerNotFoundException;
                    System.out.println("controller console 1 assert complete");
                    latch.countDown();
                });
            while (latch.getCount() != 2) {
                LockSupport.parkNanos(1);
            }
            session.startControllerConsole("creative", this)
                .thenAccept(id -> {
                    LockSupport.parkNanos(TimeUnit.MICROSECONDS.toNanos(500));
                    System.out.println("started controller console id = " + id);
                    String completionString = "execute as @e[";
                    session.controllerConsoleComplete(id, completionString, completionString.length())
                        .thenAccept(list -> {
                            System.out.println("Completion Result list = " + list);
                            LockSupport.parkNanos(TimeUnit.MICROSECONDS.toNanos(500));
                            session.controllerConsoleInput(id, "11111")
                                .thenAccept(unused -> {
                                    session.stopControllerConsole(id)
                                        .thenAccept(it -> {
                                            latch.countDown();
                                        });
                                });
                        });
                });

            while (latch.getCount() != 1) {
                LockSupport.parkNanos(1);
            }
            session.close()
                .thenAccept(unused -> {
                    latch.countDown();
                });
            latch.await();
            session.join();
        } catch (ConnectionFailedException e) {
            System.out.println("e.getResponse() = " + e.getResponse());
            throw e;
        }
    }

    @Override
    public void onLaunched(String controllerId, String consoleId) {
        System.out.println("onLaunched called with controllerId=" + controllerId + ", consoleId=" + consoleId);
    }

    @Override
    public void onLogReceived(String consoleId, String log) {
        for (String s : log.split("\n")) {
            System.out.println(String.format("[%s] %s", consoleId, s));
        }

    }

    @Override
    public void onStopped(String consoleId) {
        System.out.println("onStopped called with consoleId=" + consoleId);
    }
}