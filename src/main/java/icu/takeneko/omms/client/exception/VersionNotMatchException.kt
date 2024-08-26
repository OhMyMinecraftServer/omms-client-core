package icu.takeneko.omms.client.exception

import icu.takeneko.omms.client.Constants

class VersionNotMatchException(
    val serverVersion: Long? = null,
): Exception(
    "Version mismatch, ${
        if (serverVersion == null) ""
        else "server version: $serverVersion,"
    } current client version: ${Constants.PROTOCOL_VERSION}"
) {
    val clientVersion = Constants.PROTOCOL_VERSION
}
