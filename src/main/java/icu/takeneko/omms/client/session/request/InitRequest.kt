package icu.takeneko.omms.client.session.request

class InitRequest(
    var version: Long
): Request("PING") {
    companion object {
        const val VERSION_BASE: Long = 0x000_0000L
    }
    constructor(version: Long, request: Request): this(version) {
        this.request = request.request
        this.content = request.content
    }

}
