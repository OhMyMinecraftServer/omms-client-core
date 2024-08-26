package icu.takeneko.omms.client.data.chatbridge

data class MessageCache(
    val maxCapacity: Int,
    val messages: List<Broadcast>
)
