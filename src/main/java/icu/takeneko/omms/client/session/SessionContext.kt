package icu.takeneko.omms.client.session

import icu.takeneko.omms.client.session.response.Response
import icu.takeneko.omms.client.util.Result

class SessionContext(
    val response: Response,
    @JvmField val session: ClientSession
) {
    val responseCode: Result
        get() = response.responseCode

    fun getContent(key: String): String? {
        return response.getContent(key)
    }
}
