package icu.takeneko.omms.client.data.announcement

import icu.takeneko.omms.client.util.Util

data class Announcement(
    var id: String = Util.randomStringGen(16),
    var timeMillis: Long = System.currentTimeMillis(),
    var title: String,
    var content: List<String>,
) {
    constructor(
        id: String = Util.randomStringGen(16),
        timeMillis: Long = System.currentTimeMillis(),
        title: String,
        content: Array<String>,
    ): this(id, timeMillis, title, content.toList())
    override fun toString() =
        "Announcement { id='$id', timeMillis=$timeMillis, title='$title', content=$content}"
}
