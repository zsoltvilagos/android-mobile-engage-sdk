package com.emarsys.mobileengage.iam.ui;

import android.webkit.JavascriptInterface;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.iam.InAppMessageHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class IamJsBridge {

    private IamDialog iamDialog;
    private InAppMessageHandler inAppMessageHandler;

    public IamJsBridge(IamDialog iamDialog, InAppMessageHandler inAppMessageHandler) {
        Assert.notNull(iamDialog, "IamDialog must not be null!");
        this.iamDialog = iamDialog;
        this.inAppMessageHandler = inAppMessageHandler;
    }

    @JavascriptInterface
    public void close(String json) {
        iamDialog.dismiss();
    }

    @JavascriptInterface
    public void triggerAppEvent(String json) {
        if (inAppMessageHandler != null) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                if (jsonObject.has("name")) {
                    String eventName = jsonObject.getString("name");
                    JSONObject payload = null;
                    if (jsonObject.has("payload")) {
                        payload = jsonObject.getJSONObject("payload");
                    }
                    inAppMessageHandler.handleApplicationEvent(eventName, payload);
                }
            } catch (JSONException ignored) {
            }
        }
    }
}
