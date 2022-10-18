package com.bossymr.rapid.robot.network;

public class ResponseStatusException extends RuntimeException {

    private final int statusCode;
    private int responseCode;

    public ResponseStatusException(int statusCode) {
        this.statusCode = statusCode;
    }

    public ResponseStatusException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ResponseStatusException(String message, int statusCode, int responseCode) {
        super(message);
        this.statusCode = statusCode;
        this.responseCode = responseCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
