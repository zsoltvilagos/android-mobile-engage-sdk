package com.emarsys.mobileengage.iam.jsbridge;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.emarsys.core.activity.CurrentActivityWatchdog;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.iam.InAppMessageHandler;
import com.emarsys.mobileengage.iam.dialog.IamDialog;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

import org.json.JSONException;
import org.json.JSONObject;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class IamJsBridge {

    private InAppMessageHandlerProvider messageHandlerProvider;
    private WebView webView;
    private Handler uiHandler;
    private Repository<ButtonClicked, SqlSpecification> repository;
    private String campaignId;
    private Handler coreSdkHandler;

    public IamJsBridge(InAppMessageHandlerProvider messageHandlerProvider, Repository<ButtonClicked, SqlSpecification> repository, String campaignId, Handler coreSdkHandler) {
        Assert.notNull(messageHandlerProvider, "MessageHandlerProvider must not be null!");
        Assert.notNull(repository, "Repository must not be null!");
        Assert.notNull(campaignId, "CampaignId must not be null!");
        Assert.notNull(coreSdkHandler, "CoreSdkHandler must not be null!");
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
                Activity currentActivity = CurrentActivityWatchdog.getCurrentActivity();
                if (currentActivity != null) {
                    Fragment fragment = currentActivity.getFragmentManager().findFragmentByTag(IamDialog.TAG);
                    if (fragment instanceof DialogFragment) {
                        ((DialogFragment) fragment).dismiss();
                    }
                }
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

    @JavascriptInterface
    public void openExternalLink(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            final String id = json.getString("id");

            if (json.has("url")) {
                final Uri link = Uri.parse(json.getString("url"));
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Activity activity = CurrentActivityWatchdog.getCurrentActivity();
                        if (activity != null) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, link);
                            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                                activity.startActivity(intent);
                                sendSuccess(id);
                            } else {
                                sendError(id, "Url cannot be handled by any application!");
                            }
                        } else {
                            sendError(id, "UI unavailable!");
                        }
                    }
                });
            } else {
                sendError(id, "Missing url!");
            }
        } catch (JSONException je) {
            EMSLogger.log(MobileEngageTopic.IN_APP_MESSAGE, "Exception occurred, exception: %s json: %s", je, jsonString);
        }
    }

    void sendSuccess(String id) {
        try {
            sendResult(new JSONObject()
                    .put("id", id)
                    .put("success", true));
        } catch (JSONException ignore) {
        }
    }

    void sendError(String id, String error) {
        try {
            sendResult(new JSONObject()
                    .put("id", id)
                    .put("success", false)
                    .put("error", error));
        } catch (JSONException ignore) {
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
