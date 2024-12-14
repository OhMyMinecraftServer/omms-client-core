package icu.takeneko.omms.client.data.chatbridge;

import lombok.Getter;

import java.util.List;

@Getter
public class MessageCache {
    int maxCapacity;
    List<ChatMessage> messages;

    public MessageCache(int maxCapacity, List<ChatMessage> messages) {
        this.maxCapacity = maxCapacity;
        this.messages = messages;
    }

}
