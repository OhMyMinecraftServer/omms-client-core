package icu.takeneko.omms.client.data.system

data class SystemInfo(
    var osName: String,
    var osVersion: String,
    var osArch: String,
    var fileSystemInfo: FileSystemInfo,
    var memoryInfo: MemoryInfo,
    var networkInfo: NetworkInfo,
    var processorInfo: ProcessorInfo,
    var storageInfo: StorageInfo
)
