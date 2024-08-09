package icu.takeneko.omms.client.exception;

import icu.takeneko.omms.client.session.response.Response;

public class ConnectionFailException extends Exception {
    private final Response response;

    public ConnectionFailException(Response resp) {
        super(String.format("Server returned error message:%s", resp.getResponseCode()));
        this.response = resp;
    }

    public Response getResponse() {
        return response;
    }
}
