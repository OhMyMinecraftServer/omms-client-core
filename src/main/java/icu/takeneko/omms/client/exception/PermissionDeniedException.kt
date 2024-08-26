package icu.takeneko.omms.client.exception

class PermissionDeniedException(
    message: String = "Permission Denied.",
): RuntimeException(message)
