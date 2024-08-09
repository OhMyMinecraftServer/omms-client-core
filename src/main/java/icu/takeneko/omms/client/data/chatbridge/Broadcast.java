package icu.takeneko.omms.client.data.chatbridge;

public class Broadcast {
    private String channel;
    private String server;
    private String player;
    private String content;
    private String id;

    public Broadcast(String channel, String server, String player, String content) {
        this.channel = channel;
        this.server = server;
        this.player = player;
        this.content = content;
        this.id = "";
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

    public String getId() {
        return id;
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

    public void setContent(String content) {
        this.content = content;
    }
}