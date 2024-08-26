package icu.takeneko.omms.client.data.chatbridge;

import java.util.List;

public class MessageCache {
    int maxCapacity;
    List<Broadcast> messages;

    public MessageCache(int maxCapacity, List<Broadcast> messages) {
        this.maxCapacity = maxCapacity;
        this.messages = messages;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public List<Broadcast> getMessages() {
        return messages;
    }
}
