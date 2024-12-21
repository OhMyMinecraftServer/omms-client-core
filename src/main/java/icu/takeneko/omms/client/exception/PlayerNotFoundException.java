package icu.takeneko.omms.client.exception;

import lombok.Getter;

@Getter
public class PlayerNotFoundException extends Exception {
    String player;
    String whitelist;

    public PlayerNotFoundException(String whitelist, String player) {
        super(String.format("Cannot find player %s in whitelist %s.", player, whitelist));
        this.player = player;
        this.whitelist = whitelist;
    }

}
