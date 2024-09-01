package icu.takeneko.omms.client.data.controller;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Status {
    boolean isAlive = false;

    boolean isQueryable = false;
    String name;
    String type;
    int playerCount;
    int maxPlayerCount;
    List<String> players;

    public Status(String type, int playerCount, int maxPlayerCount, List<String> players) {
        this.type = type;
        this.playerCount = playerCount;
        this.maxPlayerCount = maxPlayerCount;
        this.players = players;
    }

    public boolean isQueryable() {
        return isQueryable;
    }

    public boolean isAlive() {
        return isAlive;
    }

    @Override
    public String toString() {
        return "Status{" +
                "isAlive=" + isAlive +
                ", isQueryable=" + isQueryable +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", playerCount=" + playerCount +
                ", maxPlayerCount=" + maxPlayerCount +
                ", players=" + players +
                '}';
    }
}
