package com.emarsys.mobileengage.iam.jsbridge;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.iam.IamDialog;
import com.emarsys.mobileengage.iam.InAppMessageHandler;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

import org.json.JSONException;
import org.json.JSONObject;

public class IamJsBridge {

    private IamDialog iamDialog;
    private InAppMessageHandlerProvider messageHandlerProvider;

    private WebView webView;

    public IamJsBridge(IamDialog iamDialog, InAppMessageHandlerProvider messageHandlerProvider) {
        Assert.notNull(iamDialog, "IamDialog must not be null!");
        Assert.notNull(messageHandlerProvider, "MessageHandlerProvider must not be null!");
        this.iamDialog = iamDialog;
        this.messageHandlerProvider = messageHandlerProvider;
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
    }

    @JavascriptInterface
    public void close(String json) {
        iamDialog.dismiss();
    }

    @JavascriptInterface
    public void triggerAppEvent(String json) {
        InAppMessageHandler inAppMessageHandler = messageHandlerProvider.provideHandler();

        if (inAppMessageHandler != null) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                String eventName = jsonObject.getString("name");
                JSONObject payload = null;
                if (jsonObject.has("payload")) {
                    payload = jsonObject.getJSONObject("payload");
                }
                inAppMessageHandler.handleApplicationEvent(eventName, payload);
            } catch (JSONException je) {
                EMSLogger.log(MobileEngageTopic.IN_APP_MESSAGE, "Exception occurred, exception: %s json: %s", je, json);
            }
        }
    }

}
