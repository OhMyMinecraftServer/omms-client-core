package icu.takeneko.omms.client.data.broadcast;

import java.util.List;

public class MessageCache {
    int maxCapacity;
    List<Message> messages;

    public MessageCache(int maxCapacity, List<Message> messages) {
        this.maxCapacity = maxCapacity;
        this.messages = messages;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public List<Message> getMessages() {
        return messages;
    }
}
