package icu.takeneko.omms.client.data.system

import com.google.gson.annotations.SerializedName

data class FileSystemInfo(
    @SerializedName("filesystems")
    val fileSystemList: List<FileSystem>,
)

data class FileSystem(
    var free: Long,
    var total: Long,
    var volume: String,
    var montPoint: String,
    var fillSystemType: String,
)
