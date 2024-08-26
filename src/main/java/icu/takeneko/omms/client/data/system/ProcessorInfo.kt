package icu.takeneko.omms.client.data.system

data class ProcessorInfo(
    var physicalCPUCount: Int = 0,
    var logicalProcessorCount: Int = 0,
    var processorName: String = "",
    var cpuLoadAvg: Double = 0.0,
    var processorId: String = "",
    var cpuTemp: Double = 0.0,
)
