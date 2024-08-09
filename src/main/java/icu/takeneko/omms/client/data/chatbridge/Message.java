package icu.takeneko.omms.client.data.chatbridge;

public class Message {
    String channel;
    String server;
    String player;
    String content;
    String id;

    public Message(String channel, String server, String player, String content, String id) {
        this.channel = channel;
        this.server = server;
        this.player = player;
        this.content = content;
        this.id = id;
    }

    public String getChannel() {
        return channel;
    }

    public String getServer() {
        return server;
    }

    public String getPlayer() {
        return player;
    }

    public String getContent() {
        return content;
    }

    public String getId() {
        return id;
    }
}
