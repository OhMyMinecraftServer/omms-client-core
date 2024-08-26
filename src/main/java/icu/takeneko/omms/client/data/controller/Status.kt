package icu.takeneko.omms.client.data.controller

data class Status(
    var type: String = "fabric",
    var playerCount: Int = 0,
    var maxPlayerCount: Int = 0,
    var players: List<String> = listOf(),
    var isAlive: Boolean = false,
    var isQueryable: Boolean = false,
    var name: String? = null,
)
