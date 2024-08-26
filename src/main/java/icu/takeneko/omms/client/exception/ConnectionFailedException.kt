package icu.takeneko.omms.client.exception

import icu.takeneko.omms.client.session.response.Response

class ConnectionFailedException(
    val response: Response
): Exception("Server returned error message: ${response.responseCode}")
