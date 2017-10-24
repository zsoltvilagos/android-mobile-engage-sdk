package com.emarsys.mobileengage;

import com.emarsys.core.response.ResponseModel;

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

    public MobileEngageException(ResponseModel requestModel) {
        this(requestModel.getStatusCode(), requestModel.getMessage(), requestModel.getBody());
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

    @Override
    public String toString() {
        return "MobileEngageException{" +
                "statusCode=" + statusCode +
                ", statusMessage='" + statusMessage + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
