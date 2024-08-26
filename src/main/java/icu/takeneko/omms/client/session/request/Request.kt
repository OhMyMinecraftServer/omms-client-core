package icu.takeneko.omms.client.session.request

open class Request() {
    constructor(request: String) : this() {
        this.request = request
    }
    lateinit var request: String
        protected set

    fun setRequest(request: String): Request {
        this.request = request
        return this
    }

    var content: MutableMap<String, String> = mutableMapOf()

    fun withContentKeyPair(key: String, value: String): Request {
        content[key] = value
        return this
    }

    override fun toString(): String = "Request { request=$request, content=$content }"
}
