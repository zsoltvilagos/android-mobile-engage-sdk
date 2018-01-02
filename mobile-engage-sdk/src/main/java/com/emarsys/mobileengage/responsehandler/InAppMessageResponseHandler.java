package com.emarsys.mobileengage.responsehandler;

import com.emarsys.core.response.ResponseModel;
import com.emarsys.mobileengage.iam.InAppMessageHandler;
import com.emarsys.mobileengage.iam.ui.DefaultMessageLoadedListener;
import com.emarsys.mobileengage.iam.ui.IamDialog;
import com.emarsys.mobileengage.iam.ui.IamJsBridge;
import com.emarsys.mobileengage.iam.ui.IamWebViewProvider;

import org.json.JSONException;
import org.json.JSONObject;

public class InAppMessageResponseHandler extends AbstractResponseHandler {

    private IamWebViewProvider webViewProvider;
    private InAppMessageHandler inAppMessageHandler;

    public InAppMessageResponseHandler(IamWebViewProvider webViewProvider, InAppMessageHandler inAppMessageHandler) {
        this.webViewProvider = webViewProvider;
        this.inAppMessageHandler = inAppMessageHandler;
    }

    @Override
    protected boolean shouldHandleResponse(ResponseModel responseModel) {
        JSONObject responseBody = responseModel.getParsedBody();
        if (responseBody == null) {
            return false;
        }

        try {
            JSONObject message = responseBody.getJSONObject("message");
            return message.has("html");
        } catch (JSONException je) {
            return false;
        }
    }

    @Override
    protected void handleResponse(ResponseModel responseModel) {

        JSONObject responseBody = responseModel.getParsedBody();
        try {
            JSONObject message = responseBody.getJSONObject("message");
            String html = message.getString("html");

            IamDialog iamDialog = new IamDialog();
            DefaultMessageLoadedListener listener = new DefaultMessageLoadedListener(iamDialog);
            webViewProvider.loadMessageAsync(html, new IamJsBridge(iamDialog, inAppMessageHandler), listener);

        } catch (JSONException ignore) {
        }
    }
}
