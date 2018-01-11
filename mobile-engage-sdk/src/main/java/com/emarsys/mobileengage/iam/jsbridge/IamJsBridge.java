package com.emarsys.mobileengage.iam.jsbridge;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.iam.InAppMessageHandler;
import com.emarsys.mobileengage.iam.Repository;
import com.emarsys.mobileengage.iam.SqlSpecification;
import com.emarsys.mobileengage.iam.dialog.IamDialog;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

import org.json.JSONException;
import org.json.JSONObject;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class IamJsBridge {

    private IamDialog iamDialog;
    private InAppMessageHandlerProvider messageHandlerProvider;
    private WebView webView;
    private Handler uiHandler;
    private Repository<ButtonClicked, SqlSpecification> repository;
    private String campaignId;
    private Handler coreSdkHandler;

    public IamJsBridge(IamDialog iamDialog, InAppMessageHandlerProvider messageHandlerProvider, Repository<ButtonClicked, SqlSpecification> repository, String campaignId, Handler coreSdkHandler) {
        Assert.notNull(iamDialog, "IamDialog must not be null!");
        Assert.notNull(messageHandlerProvider, "MessageHandlerProvider must not be null!");
        Assert.notNull(repository, "Repository must not be null!");
        Assert.notNull(campaignId, "CampaignId must not be null!");
        Assert.notNull(coreSdkHandler, "CoreSdkHandler must not be null!");
        this.iamDialog = iamDialog;
        this.messageHandlerProvider = messageHandlerProvider;
        this.uiHandler = new Handler(Looper.getMainLooper());
        this.repository = repository;
        this.campaignId = campaignId;
        this.coreSdkHandler = coreSdkHandler;
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
    }

    @JavascriptInterface
    public void close(String jsonString) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                iamDialog.dismiss();
            }
        });
    }

    @JavascriptInterface
    public void triggerAppEvent(String jsonString) {
        final InAppMessageHandler inAppMessageHandler = messageHandlerProvider.provideHandler();

        if (inAppMessageHandler != null) {
            try {
                final JSONObject json = new JSONObject(jsonString);
                final JSONObject result = new JSONObject().put("id", json.getString("id"));

                if (json.has("name")) {
                    final String eventName = json.getString("name");
                    final JSONObject payload = json.has("payload") ? json.getJSONObject("payload") : null;
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            inAppMessageHandler.handleApplicationEvent(eventName, payload);
                        }
                    });
                    result.put("success", true);
                } else {
                    result.put("success", false)
                            .put("error", "Missing name!");
                }
                sendResult(result);
            } catch (JSONException je) {
                EMSLogger.log(MobileEngageTopic.IN_APP_MESSAGE, "Exception occurred, exception: %s json: %s", je, jsonString);
            }
        }
    }

    @JavascriptInterface
    public void buttonClicked(String jsonString) {
        try {
            final JSONObject json = new JSONObject(jsonString);
            final JSONObject result = new JSONObject().put("id", json.getString("id"));

            if (json.has("buttonId")) {
                final String buttonId = json.getString("buttonId");
                coreSdkHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        repository.add(new ButtonClicked(campaignId, buttonId, System.currentTimeMillis()));
                    }
                });
                result.put("success", true);
            } else {
                result.put("success", false)
                        .put("error", "Missing buttonId!");
            }
            sendResult(result);
        } catch (JSONException je) {
            EMSLogger.log(MobileEngageTopic.IN_APP_MESSAGE, "Exception occurred, exception: %s json: %s", je, jsonString);
        }
    }

    void sendResult(final JSONObject payload) {
        Assert.notNull(payload, "Payload must not be null!");
        if (!payload.has("id")) {
            throw new IllegalArgumentException("Payload must have an id!");
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            webView.evaluateJavascript(String.format("MEIAM.handleResponse(%s);", payload), null);
        } else {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    webView.evaluateJavascript(String.format("MEIAM.handleResponse(%s);", payload), null);
                }
            });
        }
    }
}
