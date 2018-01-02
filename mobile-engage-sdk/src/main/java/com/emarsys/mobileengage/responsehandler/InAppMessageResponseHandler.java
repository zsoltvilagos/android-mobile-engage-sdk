package com.emarsys.mobileengage.responsehandler;

import com.emarsys.core.response.ResponseModel;
import com.emarsys.mobileengage.iam.IamDialog;
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge;
import com.emarsys.mobileengage.iam.jsbridge.InAppMessageHandlerProvider;
import com.emarsys.mobileengage.iam.webview.DefaultMessageLoadedListener;
import com.emarsys.mobileengage.iam.webview.IamWebViewProvider;

import org.json.JSONException;
import org.json.JSONObject;

public class InAppMessageResponseHandler extends AbstractResponseHandler {

    private IamWebViewProvider webViewProvider;
    private InAppMessageHandlerProvider messageHandlerProvider;

    public InAppMessageResponseHandler(IamWebViewProvider webViewProvider, InAppMessageHandlerProvider messageHandlerProvider) {
        this.webViewProvider = webViewProvider;
        this.messageHandlerProvider = messageHandlerProvider;
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
            webViewProvider.loadMessageAsync(html, new IamJsBridge(iamDialog, messageHandlerProvider), listener);

        } catch (JSONException ignore) {
        }
    }
}
