package icu.takeneko.omms.client.session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import icu.takeneko.omms.client.data.chatbridge.ChatMessage;
import icu.takeneko.omms.client.data.chatbridge.ChatbridgeImplementation;
import icu.takeneko.omms.client.data.chatbridge.MessageCache;
import icu.takeneko.omms.client.data.controller.Controller;
import icu.takeneko.omms.client.data.controller.Status;
import icu.takeneko.omms.client.data.permission.PermissionOperation;
import icu.takeneko.omms.client.data.system.SystemInfo;
import icu.takeneko.omms.client.exception.ConsoleExistsException;
import icu.takeneko.omms.client.exception.ConsoleNotFoundException;
import icu.takeneko.omms.client.exception.ControllerNotFoundException;
import icu.takeneko.omms.client.exception.PlayerAlreadyExistsException;
import icu.takeneko.omms.client.exception.PlayerNotFoundException;
import icu.takeneko.omms.client.exception.RequestUnauthorisedException;
import icu.takeneko.omms.client.exception.WhitelistNotFoundException;
import icu.takeneko.omms.client.session.callback.*;
import icu.takeneko.omms.client.session.data.FailureReasons;
import icu.takeneko.omms.client.session.data.SessionContext;
import icu.takeneko.omms.client.session.data.StatusEvent;
import icu.takeneko.omms.client.session.handler.CallbackHandle;
import icu.takeneko.omms.client.session.handler.EventSubscription;
import icu.takeneko.omms.client.session.handler.ResponseHandlerDelegate;
import icu.takeneko.omms.client.session.handler.ResponseHandlerDelegateImpl;
import icu.takeneko.omms.client.session.data.Request;
import icu.takeneko.omms.client.session.data.Response;
import icu.takeneko.omms.client.util.EncryptedConnector;
import icu.takeneko.omms.client.util.Util;
import lombok.Getter;
import lombok.Setter;

import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Session API
 */
@SuppressWarnings("unused")
@Getter
public class ClientSession extends Thread {
    private final Gson gson = new GsonBuilder().serializeNulls().create();
    private final HashMap<String, List<String>> whitelistMap = new HashMap<>();
    private final HashMap<String, Controller> controllerMap = new HashMap<>();
    private final HashMap<String, EventSubscription<SessionContext>> controllerConsoleSubscriptions = new HashMap<>();
    private final HashMap<String, ControllerConsoleClient> controllerConsoleClients = new HashMap<>();
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private final Socket socket;
    private final String serverName;
    EncryptedConnector connector;
    private SystemInfo systemInfo = null;
    private PermissionOperation lastPermissionOperation;
    private final ResponseHandlerDelegate<SessionContext, CallbackHandle<SessionContext>> delegate;
    @Setter
    private PermissionDeniedCallbackHandle<SessionContext> onPermissionDeniedCallback = new PermissionDeniedCallbackHandle<>();
    @Setter
    private Callback<Response> onServerInternalExceptionCallback;
    @Setter
    private Callback2<Thread, Throwable> onAnyExceptionCallback = (t, e) -> {
        System.out.printf("Exception in thread %s%n", t);
        e.printStackTrace();
    };
    private Callback<Response> onResponseRecievedCallback;
    @Setter
    private Callback0 onDisconnectedCallback;
    @Setter
    private Callback<ChatMessage> onNewChatMessageReceivedCallback;
    @Setter
    private String sessionName;

    boolean chatMessagePassthroughEnabled = true;

    public ClientSession(EncryptedConnector connector, Socket socket, String serverName) {
        super("ClientSessionThread");
        this.serverName = serverName;
        this.connector = connector;
        this.socket = socket;
        delegate = new ResponseHandlerDelegateImpl<>();
        delegate.setExceptionHandler(e -> {
            if (onAnyExceptionCallback != null) {
                onAnyExceptionCallback.accept(Thread.currentThread(), e);
            } else {
                throw new RuntimeException(e);
            }
        });
        sessionName = Integer.toString(Math.abs((serverName + ":" + new String(connector.getKey())).hashCode()));
    }

    public EventSubscription<SessionContext> subscribe(String requestId) {
        return delegate.subscribe(requestId)
            .subscribe(StatusEvent.PERMISSION_DENIED, onPermissionDeniedCallback);
    }

    public EventSubscription<SessionContext> subscribe() {
        return subscribe(newRequestId());
    }

    public String newRequestId() {
        return Util.generateNonce(16) + ":" + this;
    }

    public boolean isActive() {
        return !socket.isClosed() && this.isAlive();
    }

    public static class UncaughtExceptionHandlerImpl implements UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {

        }
    }

    @Override
    public void run() {
        Response response;
        while (true) {
            try {
                response = gson.fromJson(connector.readLine(), Response.class);
                if (onResponseRecievedCallback != null) {
                    onResponseRecievedCallback.accept(response);
                }
                if (response.getEvent() == StatusEvent.BROADCAST) {
                    ChatMessage chatMessage = gson.fromJson(response.getContent("message"), ChatMessage.class);
                    if (onNewChatMessageReceivedCallback != null) {
                        onNewChatMessageReceivedCallback.accept(chatMessage);
                    }
                    continue;
                }
                if (response.getEvent() == StatusEvent.DISCONNECT) {
                    if (onDisconnectedCallback != null) {
                        onDisconnectedCallback.accept();
                    }
                    break;
                }
                delegate.handle(new SessionContext(response, this));
            } catch (DisconnectedException | SocketException ignored) {
                if (onDisconnectedCallback != null) {
                    onDisconnectedCallback.accept();
                }
                break;
            } catch (Exception e) {
                if (onAnyExceptionCallback != null)
                    onAnyExceptionCallback.accept(currentThread(), e);
            }
        }
    }

    private void syncStatus() {

    }

    public void send(Request request, String requestId) {
        networkExecutor.submit(() -> {
            request.setRequestId(requestId);
            String content = gson.toJson(request);
            try {
                connector.println(content);
            } catch (Throwable e) {
                onAnyExceptionCallback.accept(currentThread(), e);
            }
        });
    }

    public CompletableFuture<Void> close() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        setOnDisconnectedCallback(() -> future.complete(null));
        send(
            new Request("END"),
            newRequestId()
        );
        delegate.shutdown();
        networkExecutor.shutdownNow();
        return future;
    }

    public CompletableFuture<Map<String, List<String>>> fetchWhitelistFromServer() {
        CompletableFuture<Map<String, List<String>>> fu = new CompletableFuture<>();
        CallbackHandle<SessionContext> cb = new WhitelistListCallbackHandle(fu::complete);
        send(
            new Request("WHITELIST_LIST"),
            subscribe()
                .subscribeSuccess(cb)
                .getRequestId()
        );
        return fu;
    }

    public CompletableFuture<Map<String, Controller>> fetchControllersFromServer() {
        CompletableFuture<Map<String, Controller>> future = new CompletableFuture<>();
        send(
            new Request("CONTROLLER_LIST"),
            subscribe()
                .subscribeSuccess(new ControllerListCallbackHandle(future::complete))
                .getRequestId()
        );
        return future;
    }

    public CompletableFuture<SystemInfo> fetchSystemInfoFromServer() {
        CompletableFuture<SystemInfo> future = new CompletableFuture<>();
        send(
            new Request("SYSTEM_GET_INFO"),
            subscribe()
                .subscribeSuccess(new SystemInfoCallbackHandle((si) -> {
                    this.systemInfo = si;
                    future.complete(si);
                }))
                .getRequestId()
        );
        return future;
    }

    public CompletableFuture<Status> fetchControllerStatus(String controllerId) {
        CompletableFuture<Status> future = new CompletableFuture<>();
        EventSubscription<SessionContext> ctx = subscribe()
            .subscribeSuccess(
                new StatusCallbackHandle(future::complete)
            ).subscribeFailure(
                new CallbackHandle0<>(
                    () -> future.completeExceptionally(new ControllerNotExistException(controllerId))
                )
            );

        send(
            new Request()
                .setRequest("CONTROLLER_GET_STATUS")
                .withContentKeyPair("id", controllerId),
            ctx.getRequestId()
        );
        return future;
    }


    public CompletableFuture<Void> removeFromWhitelist(
        String whitelistName,
        String player
    ) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        EventSubscription<SessionContext> s = subscribe()
            .subscribeSuccess(new CallbackHandle0<>(() -> future.complete(null)))
            .subscribeFailure(
                new RawCallbackHandle<>(ctx -> {
                    if (ctx.hasReason(FailureReasons.WHITELIST_NOT_FOUND)) {
                        future.completeExceptionally(new WhitelistNotFoundException(whitelistName));
                    } else {
                        if (ctx.hasReason(FailureReasons.PLAYER_NOT_FOUND)) {
                            future.completeExceptionally(new PlayerNotFoundException(whitelistName, player));
                        }
                    }
                })
            );

        this.send(
            new Request("WHITELIST_REMOVE")
                .withContentKeyPair("whitelist", whitelistName)
                .withContentKeyPair("player", player),
            s.getRequestId()
        );
        return future;
    }


    public ArrayList<String> queryInAllWhitelist(String playerName) {
        ArrayList<String> whitelists = new ArrayList<>();
        if (whitelistMap.isEmpty()) {
            return null;
        }
        whitelistMap.forEach((k, v) -> {
            if (v.contains(playerName)) {
                whitelists.add(k);
            }
        });
        if (whitelists.isEmpty()) {
            return null;
        }
        return whitelists;
    }

    public CompletableFuture<Void> startControllerConsole(
        String controller,
        ControllerConsoleClient client
    ) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        EventSubscription<SessionContext> subscription = subscribe();
        CallbackHandle<SessionContext> logRecv = new BiStringCallbackHandle(
            "consoleId",
            "content",
            client::onLogReceived
        );
        CallbackHandle<SessionContext> consoleLaunched = new BiStringCallbackHandle(
            "controller",
            "consoleId",
            (ct, conId) -> {
                controllerConsoleSubscriptions.put(conId, subscription);
                controllerConsoleClients.put(conId, client);
                future.complete(null);
                client.onLaunched(controller, conId);
            }
        );
        CallbackHandle<SessionContext> consoleExists = new StringCallbackHandle(
            "controller",
            s -> future.completeExceptionally(new ConsoleExistsException(controller))
        );
        CallbackHandle<SessionContext> controllerNotFound = new StringCallbackHandle(
            "controller",
            s -> future.completeExceptionally(new ControllerNotFoundException(controller))
        );
        subscription.subscribeAlways(StatusEvent.SUCCESS, new RawCallbackHandle<>(ctx -> {
            if (ctx.hasMarker("log")) {
                logRecv.invoke(ctx);
                return;
            }
            if (ctx.hasMarker("launched")) {
                consoleLaunched.invoke(ctx);
            }
        }));
        subscription.subscribeFailure(new RawCallbackHandle<>(ctx -> {
            if (ctx.hasReason(FailureReasons.CONSOLE_EXISTS)) {
                consoleExists.invoke(ctx);
                return;
            }
            if (ctx.hasReason(FailureReasons.CONTROLLER_NOT_FOUND)) {
                controllerNotFound.invoke(ctx);
            }
        }));
        send(
            new Request()
                .setRequest("CONTROLLER_LAUNCH_CONSOLE")
                .withContentKeyPair("controller", controller),
            subscription.getRequestId()
        );
        return future;
    }

    public CompletableFuture<Void> stopControllerConsole(
        String consoleId
    ) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        CallbackHandle<SessionContext> conStopped = new StringCallbackHandle(
            "consoleId",
            id -> {
                controllerConsoleClients.get(id).onStopped();
                controllerConsoleSubscriptions.get(id).setRemoved();
                controllerConsoleClients.remove(id);
                controllerConsoleSubscriptions.remove(id);
                future.complete(null);
            }
        );
        CallbackHandle<SessionContext> conNotFound = new StringCallbackHandle(
            "consoleId",
            id -> future.completeExceptionally(new ConsoleNotFoundException(id))
        );
        send(
            new Request()
                .setRequest("CONTROLLER_END_CONSOLE")
                .withContentKeyPair("consoleId", consoleId),
            subscribe()
                .subscribeSuccess(conStopped)
                .subscribeFailure(conNotFound)
                .getRequestId()
        );
        return future;
    }

    public CompletableFuture<Void> controllerConsoleInput(
        String consoleId,
        String line
    ) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        CallbackHandle<SessionContext> conNotFound = new CallbackHandle0<>(
            () -> future.completeExceptionally(new ConsoleNotFoundException(consoleId))
        );
        CallbackHandle<SessionContext> conInput = new CallbackHandle0<>(
            () -> future.complete(null)
        );
        send(
            new Request().setRequest("CONTROLLER_INPUT_CONSOLE")
                .withContentKeyPair("consoleId", consoleId)
                .withContentKeyPair("command", line),
            subscribe()
                .subscribeSuccess(conInput)
                .subscribeFailure(conNotFound)
                .getRequestId()
        );
        return future;
    }

    public CompletableFuture<Void> addToWhitelist(
        String whitelistName,
        String player
    ) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        this.send(
            new Request("WHITELIST_ADD")
                .withContentKeyPair("whitelist", whitelistName)
                .withContentKeyPair("player", player),
            subscribe()
                .subscribeSuccess(new CallbackHandle0<>(
                        () -> future.complete(null)
                    )
                ).subscribeFailure(new RawCallbackHandle<>(ctx -> {
                    if (ctx.hasReason(FailureReasons.WHITELIST_NOT_FOUND)) {
                        future.completeExceptionally(new WhitelistNotFoundException(whitelistName));
                    } else {
                        if (ctx.hasReason(FailureReasons.PLAYER_EXISTS)) {
                            future.completeExceptionally(new PlayerAlreadyExistsException(whitelistName, player));
                        }
                    }
                }))
                .getRequestId()
        );
        return future;
    }

    public CompletableFuture<List<String>> sendCommandToController(
        String controller,
        String command
    ) {
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        Request request = new Request()
            .setRequest("CONTROLLER_EXECUTE_COMMAND")
            .withContentKeyPair("controller", controller)
            .withContentKeyPair("command", command);
        String groupId = Long.toString(System.nanoTime());
        ControllerCommandLogCallbackHandle logCallbackHandle = new ControllerCommandLogCallbackHandle(
            (id, c) -> future.complete(c)
        );
        StringCallbackHandle notExist = new StringCallbackHandle(
            "controllerId",
            c -> future.completeExceptionally(new ControllerNotFoundException(c))
        );
        StringCallbackHandle authFailed = new StringCallbackHandle(
            "controllerId",
            c -> future.completeExceptionally(new RequestUnauthorisedException(controller))
        );
        send(
            request,
            subscribe()
                .subscribeSuccess(logCallbackHandle)
                .subscribeFailure(new RawCallbackHandle<>(c -> {
                    if (c.hasReason(FailureReasons.CONTROLLER_UNAUTHORISED)) {
                        authFailed.invoke(c);
                        return;
                    }
                    if (c.hasReason(FailureReasons.CONTROLLER_NOT_FOUND)) {
                        notExist.invoke(c);
                    }
                }))
                .getRequestId()
        );
        return future;
    }

    public CompletableFuture<Boolean> setChatMessagePassthroughState(boolean state) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Request request = new Request()
            .setRequest("SET_CHAT_PASSTHROUGH_STATE")
            .withContentKeyPair("state", Boolean.toString(state));
        BooleanCallbackHandle cb = new BooleanCallbackHandle(
            "state",
            future::complete
        );
        send(request, subscribe().subscribeSuccess(cb).getRequestId());
        return future;
    }

    public CompletableFuture<Void> sendChatbridgeMessage(String channel, String message) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Request request = new Request()
            .setRequest("SEND_BROADCAST")
            .withContentKeyPair("channel", channel)
            .withContentKeyPair("message", message);
        CallbackHandle0<SessionContext> cb = new CallbackHandle0<>(
            () -> future.complete(null)
        );

        send(request, subscribe().subscribeSuccess(cb).getRequestId());
        return future;
    }

    public CompletableFuture<MessageCache> getChatHistory() {
        CompletableFuture<MessageCache> future = new CompletableFuture<>();
        JsonObjectCallbackHandle<MessageCache> cb = new JsonObjectCallbackHandle<MessageCache>("content", future::complete) {
            @Override
            protected TypeToken<MessageCache> getObjectType() {
                return TypeToken.get(MessageCache.class);
            }
        };
        send(new Request("GET_CHAT_HISTORY"), subscribe().subscribeSuccess(cb).getRequestId());
        return future;
    }

    public CompletableFuture<ChatbridgeImplementation> getChatbridgeImplementation() {
        CompletableFuture<ChatbridgeImplementation> future = new CompletableFuture<>();
        EnumCallbackHandle<ChatbridgeImplementation> handle = new EnumCallbackHandle<>(
            "implementation",
            ChatbridgeImplementation::valueOf,
            future::complete
        );
        send(new Request("GET_CHATBRIDGE_IMPL"), subscribe().subscribeSuccess(handle).getRequestId());
        return future;
    }

    public CompletableFuture<List<String>> controllerConsoleComplete(
        String consoleId,
        String text,
        int cursorPosition,
        Callback<List<String>> callback
    ) {
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        ListCallbackHandle<String> callbackHandle = new ListCallbackHandle<>("result", future::complete);
        send(new Request("CONTROLLER_CONSOLE_COMPLETE")
                .withContentKeyPair("input", text)
                .withContentKeyPair("cursor", Integer.toString(cursorPosition)),
            subscribe()
                .subscribeSuccess(callbackHandle)
                .getRequestId()
        );
        return future;
    }

    public Controller getControllerByName(String name) {
        return controllerMap.get(name);
    }

    public void setOnResponseReceivedCallback(Callback<Response> onResponseRecievedCallback) {
        this.onResponseRecievedCallback = onResponseRecievedCallback;
    }

    @Override
    public String toString() {
        return sessionName;
    }
}
