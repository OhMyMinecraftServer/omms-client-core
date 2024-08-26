package icu.takeneko.omms.client.session.response

import com.google.gson.GsonBuilder
import icu.takeneko.omms.client.util.Result

class Response {
    constructor(responseCode: String, content: Map<String, String>) {
        this.content = content
        this.responseCode = Result.valueOf(responseCode)
    }
    constructor(responseCode: Result, content: Map<String, String>) {
        this.content = content
        this.responseCode = responseCode
    }
    lateinit var responseCode: Result
        private set
    lateinit var content: Map<String, String>
        private set

    companion object {
        @JvmStatic
        fun serialize(response: Response): String =
            GsonBuilder().serializeNulls().create().toJson(response)

        @JvmStatic
        fun deserialize(x: String): Response =
            GsonBuilder().serializeNulls().create().fromJson(x, Response::class.java)
    }

    fun getContent(key: String) = content[key]

    fun getPair(first: String, second: String): Pair<String?, String?> =
        getContent(first) to getContent(second)

    override fun toString(): String =
        "Response { responseCode='$responseCode', content=$content } "
}
