package icu.takeneko.omms.client.data.chatbridge;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class ChatMessage {
    private String channel;
    private String server;
    private String player;
    private String content;
    private String id;
    private long timeMillis;
}