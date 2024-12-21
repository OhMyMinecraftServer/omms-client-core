package icu.takeneko.omms.client.session;

public interface ControllerConsoleClient {
    void onLaunched(String controllerId, String consoleId);

    void onLogReceived(String consoleId, String log);

    void onStopped();
}
