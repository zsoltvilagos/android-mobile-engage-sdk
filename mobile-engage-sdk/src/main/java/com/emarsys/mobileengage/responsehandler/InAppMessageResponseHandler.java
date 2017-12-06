package com.emarsys.mobileengage.responsehandler;

import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.iam.ui.IamWebViewProvider;

import org.json.JSONException;
import org.json.JSONObject;

public class InAppMessageResponseHandler extends AbstractResponseHandler {

    private IamWebViewProvider webViewProvider;

    public InAppMessageResponseHandler(IamWebViewProvider webViewProvider) {
        this.webViewProvider = webViewProvider;
    }

    @Override
    protected boolean shouldHandleResponse(ResponseModel responseModel) {
        Assert.notNull(responseModel, "ResponseModel must not be null!");

        JSONObject responseBody = responseModel.getParsedBody();
        try {
            JSONObject message = responseBody.getJSONObject("message");
            return message.has("html");
        } catch (JSONException je) {
            return false;
        }
    }

    @Override
    protected void handleResponse(ResponseModel responseModel) {

    }
}
