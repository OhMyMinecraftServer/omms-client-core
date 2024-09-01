package icu.takeneko.omms.client.data.chatbridge;

import icu.takeneko.omms.client.util.Util;
import lombok.Getter;

@Getter
public class Broadcast {
    private String channel;
    private String server;
    private String player;
    private String content;
    private String id;
    private long timeMillis;

    public Broadcast(String channel, String server, String player, String content) {
        this.channel = channel;
        this.server = server;
        this.player = player;
        this.content = content;
        this.id = Util.generateRandomString(16);
        this.timeMillis = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "Broadcast{" +
                "channel='" + channel + '\'' +
                ", server='" + server + '\'' +
                ", player='" + player + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}