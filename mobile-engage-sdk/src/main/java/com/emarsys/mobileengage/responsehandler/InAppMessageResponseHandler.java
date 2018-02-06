package com.emarsys.mobileengage.responsehandler;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;

import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.iam.dialog.IamDialog;
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider;
import com.emarsys.mobileengage.iam.dialog.OnDialogShownAction;
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge;
import com.emarsys.mobileengage.iam.jsbridge.InAppMessageHandlerProvider;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedRepository;
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
    private Handler coreSdkHandler;
    private ButtonClickedRepository repository;

    public InAppMessageResponseHandler(
            Handler coreSdkHandler,
            IamWebViewProvider webViewProvider,
            InAppMessageHandlerProvider messageHandlerProvider,
            IamDialogProvider dialogProvider,
            ButtonClickedRepository repository) {
        this.coreSdkHandler = coreSdkHandler;
        this.webViewProvider = webViewProvider;
        this.messageHandlerProvider = messageHandlerProvider;
        this.dialogProvider = dialogProvider;
        this.repository = repository;
    }

    @Override
    protected boolean shouldHandleResponse(ResponseModel responseModel) {
        JSONObject responseBody = responseModel.getParsedBody();
        boolean responseBodyNotNull = responseBody != null;
        boolean kitkatOrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        boolean shouldHandle = false;

        if (kitkatOrAbove && responseBodyNotNull) {
            try {
                JSONObject message = responseBody.getJSONObject("message");
                shouldHandle = message.has("html");
            } catch (JSONException ignored) {
            }
        }

        return shouldHandle;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected void handleResponse(ResponseModel responseModel) {
        JSONObject responseBody = responseModel.getParsedBody();
        try {
            JSONObject message = responseBody.getJSONObject("message");
            String html = message.getString("html");
            String id = message.getString("id");

            IamDialog iamDialog = dialogProvider.provideDialog(id);
            OnDialogShownAction action = new OnDialogShownAction(
                    coreSdkHandler,
                    new DisplayedIamRepository(iamDialog.getActivity()));
            iamDialog.setAction(action);

            DefaultMessageLoadedListener listener = new DefaultMessageLoadedListener(iamDialog);
            webViewProvider.loadMessageAsync(html, new IamJsBridge(messageHandlerProvider, repository, id, coreSdkHandler), listener);
        } catch (JSONException je) {
            EMSLogger.log(MobileEngageTopic.IN_APP_MESSAGE, "Exception occurred, exception: %s json: %s", je, responseBody);
        }
    }
}
