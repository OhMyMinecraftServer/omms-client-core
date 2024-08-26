package icu.takeneko.omms.client.data.system

import com.google.gson.annotations.SerializedName

data class StorageInfo(
    @SerializedName("storages")
    var storageList: List<Storage> = listOf()
)

data class Storage(
    val name: String,
    val model: String,
    val size: Long
)
