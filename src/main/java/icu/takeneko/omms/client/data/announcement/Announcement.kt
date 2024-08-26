package icu.takeneko.omms.client.data.announcement

import icu.takeneko.omms.client.util.Util

data class Announcement(
    var id: String = Util.randomStringGen(16),
    var timeMillis: Long = System.currentTimeMillis(),
    var title: String,
    var content: List<String>,
) {
    override fun toString() =
        "Announcement { id='$id', timeMillis=$timeMillis, title='$title', content=$content}"
}
