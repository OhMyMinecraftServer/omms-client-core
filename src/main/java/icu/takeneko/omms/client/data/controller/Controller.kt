package icu.takeneko.omms.client.data.controller

data class Controller(
    val name: String,
    val type: String,
    val displayName: String,
    val statusQueryable: Boolean,
    val id: String = ""
)
