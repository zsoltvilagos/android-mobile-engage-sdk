package com.emarsys.mobileengage.iam.jsbridge;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.iam.IamDialog;
import com.emarsys.mobileengage.iam.InAppMessageHandler;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

import org.json.JSONException;
import org.json.JSONObject;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class IamJsBridge {

    private IamDialog iamDialog;
    private InAppMessageHandlerProvider messageHandlerProvider;
    private WebView webView;
    private Handler uiHandler;

    public IamJsBridge(IamDialog iamDialog, InAppMessageHandlerProvider messageHandlerProvider) {
        Assert.notNull(iamDialog, "IamDialog must not be null!");
        Assert.notNull(messageHandlerProvider, "MessageHandlerProvider must not be null!");
        this.iamDialog = iamDialog;
        this.messageHandlerProvider = messageHandlerProvider;
        this.uiHandler = new Handler(Looper.getMainLooper());
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
    }

    @JavascriptInterface
    public void close(String json) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                iamDialog.dismiss();
            }
        });
    }

    @JavascriptInterface
    public void triggerAppEvent(String json) {
        final InAppMessageHandler inAppMessageHandler = messageHandlerProvider.provideHandler();

        if (inAppMessageHandler != null) {
            try {
                final JSONObject jsonObject = new JSONObject(json);
                final String eventName = jsonObject.getString("name");
                final JSONObject payload = jsonObject.has("payload") ? jsonObject.getJSONObject("payload") : null;

                final JSONObject result = new JSONObject().put("id", jsonObject.getString("id"));

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        inAppMessageHandler.handleApplicationEvent(eventName, payload);
                        sendResult(result);
                    }
                });
            } catch (JSONException je) {
                EMSLogger.log(MobileEngageTopic.IN_APP_MESSAGE, "Exception occurred, exception: %s json: %s", je, json);
            }
        }
    }

    void sendResult(JSONObject payload) {
        Assert.notNull(payload, "Payload must not be null!");
        if (!payload.has("id")) {
            throw new IllegalArgumentException("Payload must have an id!");
        }
        webView.evaluateJavascript(String.format("MEIAM.handleResponse(%s);", payload), null);
    }
}
