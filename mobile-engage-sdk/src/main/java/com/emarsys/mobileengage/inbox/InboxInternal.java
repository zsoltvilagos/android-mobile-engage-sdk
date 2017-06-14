package com.emarsys.mobileengage.inbox;

import android.os.Handler;
import android.os.Looper;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.DeviceInfo;
import com.emarsys.core.request.RequestMethod;
import com.emarsys.core.request.RequestModel;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.HeaderUtils;
import com.emarsys.mobileengage.AppLoginParameters;
import com.emarsys.mobileengage.MobileEngageConfig;
import com.emarsys.mobileengage.MobileEngageException;
import com.emarsys.mobileengage.inbox.model.NotificationInboxStatus;

import java.util.HashMap;
import java.util.Map;

public class InboxInternal {

    private static String ENDPOINT_BASE = "https://me-inbox.eservice.emarsys.net/api/";
    private static String ENDPOINT_FETCH = ENDPOINT_BASE + "notifications";

    Handler handler;
    RestClient client;
    MobileEngageConfig config;
    AppLoginParameters appLoginParameters;

    public InboxInternal(MobileEngageConfig config) {
        Assert.notNull(config, "Config must not be null!");
        this.config = config;
        this.client = new RestClient();
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void fetchNotifications(final InboxResultListener<NotificationInboxStatus> resultListener) {
        Assert.notNull(resultListener, "ResultListener should not be null!");

        if (appLoginParameters != null && appLoginParameters.hasCredentials()) {
            handleFetchRequest(resultListener);
        } else {
            handleMissingApploginParameters(resultListener);
        }
    }

    private void handleFetchRequest(final InboxResultListener<NotificationInboxStatus> resultListener) {
        RequestModel model = new RequestModel.Builder()
                .url(ENDPOINT_FETCH)
                .headers(createBaseHeaders(config))
                .method(RequestMethod.GET)
                .build();

        client.execute(model, new CoreCompletionHandler() {
            @Override
            public void onSuccess(String id, ResponseModel responseModel) {
                resultListener.onSuccess(InboxParseUtils.parseNotificationInboxStatus(responseModel.getBody()));
            }

            @Override
            public void onError(String id, ResponseModel responseModel) {
                resultListener.onError(new MobileEngageException(
                        responseModel.getStatusCode(),
                        responseModel.getMessage(),
                        responseModel.getBody())
                );
            }

            @Override
            public void onError(String id, Exception cause) {
                resultListener.onError(cause);
            }
        });
    }

    private void handleMissingApploginParameters(final InboxResultListener<NotificationInboxStatus> resultListener) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                resultListener.onError(new NotificationInboxException("AppLogin must be called before calling fetchNotifications!"));
            }
        });
    }

    private Map<String, String> createBaseHeaders(MobileEngageConfig config) {
        Map<String, String> result = new HashMap<>();

        result.put("Authorization", HeaderUtils.createBasicAuth(config.getApplicationCode(), config.getApplicationPassword()));
        result.put("x-ems-me-hardware-id", new DeviceInfo(config.getApplication()).getHwid());
        result.put("x-ems-me-application-code", config.getApplicationCode());
        result.put("x-ems-me-contact-field-id", String.valueOf(appLoginParameters.getContactFieldId()));
        result.put("x-ems-me-contact-field-value", appLoginParameters.getContactFieldValue());

        return result;
    }

    public void setAppLoginParameters(AppLoginParameters appLoginParameters) {
        this.appLoginParameters = appLoginParameters;
    }

}
