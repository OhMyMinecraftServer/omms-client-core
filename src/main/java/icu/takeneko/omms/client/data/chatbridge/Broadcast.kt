package icu.takeneko.omms.client.data.chatbridge

data class Broadcast(
    val channel: String,
    val server: String,
    val player: String,
    var content: String,
    val id: String = ""
)
