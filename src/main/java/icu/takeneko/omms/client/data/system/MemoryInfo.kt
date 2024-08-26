package icu.takeneko.omms.client.data.system

data class MemoryInfo(
    var memoryTotal: Long,
    var memoryUsed: Long,
    var swapTotal: Long,
    var swapUsed: Long,
)
