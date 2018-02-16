package com.emarsys.mobileengage.responsehandler;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;

import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.iam.dialog.IamDialog;
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider;
import com.emarsys.mobileengage.iam.dialog.action.OnDialogShownAction;
import com.emarsys.mobileengage.iam.dialog.action.SaveDisplayedIamAction;
import com.emarsys.mobileengage.iam.dialog.action.SendDisplayedIamAction;
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge;
import com.emarsys.mobileengage.iam.jsbridge.InAppMessageHandlerProvider;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIamRepository;
import com.emarsys.mobileengage.iam.webview.DefaultMessageLoadedListener;
import com.emarsys.mobileengage.iam.webview.IamWebViewProvider;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class InAppMessageResponseHandler extends AbstractResponseHandler {

    Context context;
    private Handler coreSdkHandler;
    private IamWebViewProvider webViewProvider;
    private InAppMessageHandlerProvider messageHandlerProvider;
    private IamDialogProvider dialogProvider;
    Repository<ButtonClicked, SqlSpecification> buttonClickedRepository;
    private RequestManager requestManager;
    private String applicationCode;
    private MeIdStorage meIdStorage;
    private MeIdSignatureStorage meIdSignatureStorage;
    private TimestampProvider timestampProvider;

    public InAppMessageResponseHandler(
            Context context,
            Handler coreSdkHandler,
            IamWebViewProvider webViewProvider,
            InAppMessageHandlerProvider messageHandlerProvider,
            IamDialogProvider dialogProvider,
            Repository<ButtonClicked, SqlSpecification> buttonClickedRepository,
            RequestManager requestManager,
            String applicationCode,
            MeIdStorage meIdStorage,
            MeIdSignatureStorage meIdSignatureStorage,
            TimestampProvider timestampProvider) {
        Assert.notNull(context, "Context must not be null!");
        Assert.notNull(webViewProvider, "WebViewProvider must not be null!");
        Assert.notNull(messageHandlerProvider, "MessageHandlerProvider must not be null!");
        Assert.notNull(dialogProvider, "DialogProvider must not be null!");
        Assert.notNull(coreSdkHandler, "CoreSdkHandler must not be null!");
        Assert.notNull(buttonClickedRepository, "ButtonClickRepository must not be null!");
        Assert.notNull(requestManager, "RequestManager must not be null!");
        Assert.notNull(applicationCode, "ApplicationCode must not be null!");
        Assert.notNull(meIdStorage, "MeIdStorage must not be null!");
        Assert.notNull(meIdSignatureStorage, "MeIdSignatureStorage must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        this.context = context;
        this.webViewProvider = webViewProvider;
        this.messageHandlerProvider = messageHandlerProvider;
        this.dialogProvider = dialogProvider;
        this.coreSdkHandler = coreSdkHandler;
        this.buttonClickedRepository = buttonClickedRepository;
        this.requestManager = requestManager;
        this.applicationCode = applicationCode;
        this.meIdStorage = meIdStorage;
        this.meIdSignatureStorage = meIdSignatureStorage;
        this.timestampProvider = timestampProvider;
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

            DefaultMessageLoadedListener listener = new DefaultMessageLoadedListener(iamDialog);
            IamJsBridge jsBridge = new IamJsBridge(
                    messageHandlerProvider,
                    requestManager,
                    applicationCode,
                    buttonClickedRepository,
                    id,
                    coreSdkHandler,
                    meIdStorage,
                    meIdSignatureStorage,
                    timestampProvider);
            webViewProvider.loadMessageAsync(html, jsBridge, listener);
        } catch (JSONException je) {
            EMSLogger.log(MobileEngageTopic.IN_APP_MESSAGE, "Exception occurred, exception: %s json: %s", je, responseBody);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setupDialogWithActions(IamDialog iamDialog) {
        OnDialogShownAction saveDisplayedIamAction = new SaveDisplayedIamAction(
                coreSdkHandler,
                new DisplayedIamRepository(context),
                timestampProvider);

        OnDialogShownAction sendDisplayedIamAction = new SendDisplayedIamAction(
                coreSdkHandler,
                requestManager,
                applicationCode,
                meIdStorage,
                meIdSignatureStorage,
                timestampProvider);

        iamDialog.setActions(Arrays.asList(
                saveDisplayedIamAction,
                sendDisplayedIamAction));
    }
}
