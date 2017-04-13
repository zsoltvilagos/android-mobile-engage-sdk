package com.emarsys.mobileengage;

public class MobileEngageException extends Exception {
    private final int statusCode;
    private final String statusMessage;
    private final String body;

    public MobileEngageException(int statusCode, String statusMessage, String body) {
        super(statusMessage);
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getBody() {
        return body;
    }
}
