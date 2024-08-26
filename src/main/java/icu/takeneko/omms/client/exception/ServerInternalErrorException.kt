package icu.takeneko.omms.client.exception

class ServerInternalErrorException(
    message: String = "Got FAIL from server.",
): RuntimeException(message)
