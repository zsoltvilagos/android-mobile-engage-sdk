package com.emarsys.mobileengage.iam.ui;

import android.webkit.JavascriptInterface;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.iam.DialogOwner;
import com.emarsys.mobileengage.iam.InAppMessageHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class IamJsBridge {

    private IamDialog dialog;
    private InAppMessageHandler inAppMessageHandler;

    public IamJsBridge(DialogOwner dialogOwner, InAppMessageHandler inAppMessageHandler) {
        Assert.notNull(dialogOwner, "DialogOwner must not be null!");
        this.dialog = dialogOwner.getIamDialog();
        this.inAppMessageHandler = inAppMessageHandler;
    }

    @JavascriptInterface
    public void close(String json) {
        dialog.dismiss();
    }

    @JavascriptInterface
    public void triggerAppEvent(String json) {
        if (inAppMessageHandler == null) {
            return;
        }

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
