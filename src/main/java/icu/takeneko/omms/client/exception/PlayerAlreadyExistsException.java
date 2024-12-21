package icu.takeneko.omms.client.exception;

public class PlayerAlreadyExistsException extends Exception {
    String player;
    String whitelist;

    public PlayerAlreadyExistsException(String whitelist, String player) {
        super(String.format("Player %s already exists in %s.",player, whitelist));
        this.player = player;
        this.whitelist = whitelist;
    }

    public String getWhitelist() {
        return whitelist;
    }

    public String getPlayer() {
        return player;
    }
}
