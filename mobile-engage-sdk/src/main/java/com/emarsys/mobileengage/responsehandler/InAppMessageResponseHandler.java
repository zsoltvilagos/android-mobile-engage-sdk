package com.emarsys.mobileengage.responsehandler;

import android.os.Build;
import android.os.Handler;

import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.iam.dialog.IamDialog;
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider;
import com.emarsys.mobileengage.iam.dialog.OnDialogShownAction;
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge;
import com.emarsys.mobileengage.iam.jsbridge.InAppMessageHandlerProvider;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIamRepository;
import com.emarsys.mobileengage.iam.webview.DefaultMessageLoadedListener;
import com.emarsys.mobileengage.iam.webview.IamWebViewProvider;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

import org.json.JSONException;
import org.json.JSONObject;

public class InAppMessageResponseHandler extends AbstractResponseHandler {

    private IamWebViewProvider webViewProvider;
    private InAppMessageHandlerProvider messageHandlerProvider;
    private IamDialogProvider dialogProvider;
    private Handler handler;

    public InAppMessageResponseHandler(Handler handler, IamWebViewProvider webViewProvider, InAppMessageHandlerProvider messageHandlerProvider, IamDialogProvider dialogProvider) {
        this.handler = handler;
        this.webViewProvider = webViewProvider;
        this.messageHandlerProvider = messageHandlerProvider;
        this.dialogProvider = dialogProvider;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            JSONObject responseBody = responseModel.getParsedBody();
            try {
                JSONObject message = responseBody.getJSONObject("message");
                String html = message.getString("html");
                String id = message.getString("id");

                IamDialog iamDialog = dialogProvider.provideDialog(id);
                OnDialogShownAction action = new OnDialogShownAction(
                        handler,
                        new DisplayedIamRepository(iamDialog.getActivity()));
                iamDialog.setAction(action);

                DefaultMessageLoadedListener listener = new DefaultMessageLoadedListener(iamDialog);
                webViewProvider.loadMessageAsync(html, new IamJsBridge(iamDialog, messageHandlerProvider), listener);
            } catch (JSONException je) {
                EMSLogger.log(MobileEngageTopic.IN_APP_MESSAGE, "Exception occurred, exception: %s json: %s", je, responseBody);
            }
        }
    }
}
