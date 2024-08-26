package icu.takeneko.omms.client.data.chatbridge

data class Broadcast(
    val channel: String,
    val server: String,
    val player: String,
    var content: String,
) {
    val id = ""

    override fun toString(): String = "Broadcast { channel='$channel', server='$server', player='$player', id='$id' }"
}
