package com.emarsys.mobileengage.responsehandler;

import com.emarsys.core.response.ResponseModel;

public class InAppMessageResponseHandler extends AbstractResponseHandler {

    @Override
    protected boolean shouldHandleResponse(ResponseModel responseModel) {
        return false;
    }

    @Override
    protected void handleResponse(ResponseModel responseModel) {

    }
}
