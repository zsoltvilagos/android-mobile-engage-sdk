package com.emarsys.mobileengage.responsehandler;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;

import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.iam.dialog.IamDialog;
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider;
import com.emarsys.mobileengage.iam.dialog.action.OnDialogShownAction;
import com.emarsys.mobileengage.iam.dialog.action.SaveDisplayedIamAction;
import com.emarsys.mobileengage.iam.dialog.action.SendDisplayedIamAction;
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge;
import com.emarsys.mobileengage.iam.jsbridge.InAppMessageHandlerProvider;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;
import com.emarsys.mobileengage.iam.webview.DefaultMessageLoadedListener;
import com.emarsys.mobileengage.iam.webview.IamWebViewProvider;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Map;

public class InAppMessageResponseHandler extends AbstractResponseHandler {

    private Handler coreSdkHandler;
    private IamWebViewProvider webViewProvider;
    private InAppMessageHandlerProvider messageHandlerProvider;
    private IamDialogProvider dialogProvider;
    private Repository<ButtonClicked, SqlSpecification> buttonClickedRepository;
    private Repository<DisplayedIam, SqlSpecification> displayedIamRepository;
    private Repository<Map<String, Object>, SqlSpecification> logRepository;
    private TimestampProvider timestampProvider;
    private MobileEngageInternal mobileEngageInternal;

    public InAppMessageResponseHandler(
            Handler coreSdkHandler,
            IamWebViewProvider webViewProvider,
            InAppMessageHandlerProvider messageHandlerProvider,
            IamDialogProvider dialogProvider,
            Repository<ButtonClicked, SqlSpecification> buttonClickedRepository,
            Repository<DisplayedIam, SqlSpecification> displayedIamRepository,
            Repository<Map<String, Object>, SqlSpecification> logRepository,
            TimestampProvider timestampProvider,
            MobileEngageInternal mobileEngageInternal) {
        Assert.notNull(webViewProvider, "WebViewProvider must not be null!");
        Assert.notNull(messageHandlerProvider, "MessageHandlerProvider must not be null!");
        Assert.notNull(dialogProvider, "DialogProvider must not be null!");
        Assert.notNull(coreSdkHandler, "CoreSdkHandler must not be null!");
        Assert.notNull(buttonClickedRepository, "ButtonClickRepository must not be null!");
        Assert.notNull(displayedIamRepository, "DisplayedIamRepository must not be null!");
        Assert.notNull(logRepository, "LogRepository must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        Assert.notNull(mobileEngageInternal, "MobileEngageInternal must not be null!");
        this.webViewProvider = webViewProvider;
        this.messageHandlerProvider = messageHandlerProvider;
        this.dialogProvider = dialogProvider;
        this.coreSdkHandler = coreSdkHandler;
        this.buttonClickedRepository = buttonClickedRepository;
        this.displayedIamRepository = displayedIamRepository;
        this.logRepository = logRepository;
        this.timestampProvider = timestampProvider;
        this.mobileEngageInternal = mobileEngageInternal;
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
            setupDialogWithActions(iamDialog);

            DefaultMessageLoadedListener listener = new DefaultMessageLoadedListener(iamDialog, logRepository, responseModel.getTimestamp(), timestampProvider);
            IamJsBridge jsBridge = new IamJsBridge(
                    messageHandlerProvider,
                    buttonClickedRepository,
                    id,
                    coreSdkHandler,
                    mobileEngageInternal);
            webViewProvider.loadMessageAsync(html, jsBridge, listener);
        } catch (JSONException je) {
            EMSLogger.log(MobileEngageTopic.IN_APP_MESSAGE, "Exception occurred, exception: %s json: %s", je, responseBody);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setupDialogWithActions(IamDialog iamDialog) {
        OnDialogShownAction saveDisplayedIamAction = new SaveDisplayedIamAction(
                coreSdkHandler,
                displayedIamRepository,
                timestampProvider);

        OnDialogShownAction sendDisplayedIamAction = new SendDisplayedIamAction(
                coreSdkHandler,
                mobileEngageInternal);

        iamDialog.setActions(Arrays.asList(
                saveDisplayedIamAction,
                sendDisplayedIamAction));
    }
}
