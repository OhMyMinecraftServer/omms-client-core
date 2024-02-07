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
        session.setOnAnyExceptionCallback((pair) -> pair.getB().printStackTrace());
        System.out.println("===========BEGIN===========");
        System.out.println("===========FETCH ALL===========");
        fetchWhitelistTest(session);
        fetchAnnouncementTest(session);
        fetchControllerTest(session);
        fetchSystemInfoTest(session);
        System.out.println("\n===========WHITELIST TEST===========");
        String playerName = "AAAAAA";
        for (int i = 0; i < 2; ++i) {
            addToWhitelistTest(session, playerName);
        }
        fetchWhitelistTest(session);
        for (int i = 0; i < 2; i++) {
            removeFromWhitelistTest(session, playerName);
        }
        System.out.println("\n===========CONTROLLER TEST===========");
        session.getControllerMap().forEach((s, controller) -> {
            try {
                controllerTest(session, s);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("\n===========CONTROLLER CONSOLE TEST===========");
        awaitExecute(countDownLatch -> {
            String name;
            Scanner scanner = new Scanner(System.in);
            name = scanner.nextLine();
            String finalName = name;
            AtomicReference<String> id = new AtomicReference<>("");
            session.startControllerConsole(name, s -> {
                        System.out.printf("Controller Console %s to %s started.", s.getA(), s.getB());
                        id.set(s.getA());
                    }, pair -> System.out.printf("[%s(%s)] %s\n", finalName, pair.getA(), pair.getB()),
                    s -> System.out.printf("Controller %s Not Exist.", s),
                    s -> System.out.printf("Controller Console %s already started.", s));
            while (true) {
                String line = scanner.nextLine();
                if (Objects.equals(line, ":q")) {
                    session.stopControllerConsole(id.get(), s -> {
                        System.out.printf("Console %s Closed.", s);
                        countDownLatch.countDown();
                    }, s -> {
                        System.out.printf("Console %s already closed.", s);
                        countDownLatch.countDown();
                    });
                    break;
                }
                session.controllerConsoleInput(id.get(), line, s -> {}, s -> {
                    System.out.printf("Input %s to %s.", s, id.get());
                });
            }
        });
        System.out.println("\n===========DISCONNECT===========");
        disconnectTest(session);
    }

    private static void controllerTest(ClientSession session, String controller) throws InterruptedException {
        System.out.printf("\n===========CONTROLLER %s===========\n", controller);
        awaitExecute(countDownLatch -> session.sendCommandToController(controller, "list", pair -> {
            pair.getB().forEach(System.out::println);
            countDownLatch.countDown();
        }));
        awaitExecute(countDownLatch -> session.fetchControllerStatus(controller, status -> {
            System.out.printf("%s %s", controller, status);
            countDownLatch.countDown();
        }, s1 -> {
            System.out.printf("Controller %s Not Exist", s1);
        }));
    }

    private static void disconnectTest(ClientSession session) throws InterruptedException {
        awaitExecute(countDownLatch -> {
            try {
                session.close((s) -> {
                    countDownLatch.countDown();
                    System.out.println("Disconnected.");
                    System.exit(0);
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void removeFromWhitelistTest(ClientSession session, String playerName) throws InterruptedException {
        awaitExecute(countDownLatch -> {
            try {
                session.removeFromWhitelist("in_survival", playerName, pair -> {
                    System.out.printf("Removed player %s from whitelist", playerName);
                    countDownLatch.countDown();
                }, pair -> {
                    System.out.printf("Player %s Not Exist", playerName);
                    countDownLatch.countDown();
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void addToWhitelistTest(ClientSession session, String playerName) throws InterruptedException {
        awaitExecute(countDownLatch -> {
            try {
                session.addToWhitelist("in_survival", playerName, pair -> {
                    System.out.printf("Added player %s to whitelist", playerName);
                    countDownLatch.countDown();
                }, pair -> {
                    System.out.printf("Player %s already Exists", playerName);
                    countDownLatch.countDown();
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void fetchSystemInfoTest(ClientSession session) throws InterruptedException {
        System.out.println("\n===========FETCH SYSTEM INFO===========");
        awaitExecute(countDownLatch -> {
            try {
                session.fetchSystemInfoFromServer((info) -> {
                    System.out.printf("%s", info.toString());
                    countDownLatch.countDown();
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void fetchControllerTest(ClientSession session) throws InterruptedException {
        System.out.println("\n===========FETCH CONTROLLERS===========");
        awaitExecute(countDownLatch -> {
            try {
                session.fetchControllersFromServer((map) -> {
                    map.forEach((s, controller) -> System.out.printf("%s %s\n", s, controller.toString()));
                    countDownLatch.countDown();
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void fetchAnnouncementTest(ClientSession session) throws InterruptedException {
        System.out.println("\n===========FETCH ANNOUNCEMENT===========");
        awaitExecute(countDownLatch -> {
            try {
                session.fetchAnnouncementFromServer((map) -> {
                    map.forEach((s, announcement) -> System.out.printf("%s %s\n", s, announcement.toString()));
                    countDownLatch.countDown();
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void fetchWhitelistTest(ClientSession session) throws InterruptedException {
        System.out.println("\n===========FETCH WHITELIST===========");
        awaitExecute(countDownLatch -> {
            try {
                session.fetchWhitelistFromServer((map) -> {
                    map.forEach((s, strings) -> System.out.printf("%s %s\n", s, Arrays.toString(strings.toArray())));
                    countDownLatch.countDown();
                }, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void awaitExecute(Consumer<CountDownLatch> consumer) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        consumer.accept(countDownLatch);
        countDownLatch.await();
    }
}
