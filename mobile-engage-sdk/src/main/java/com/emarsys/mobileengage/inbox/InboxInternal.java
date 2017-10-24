package com.emarsys.mobileengage.inbox;

import android.os.Handler;
import android.os.Looper;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.DeviceInfo;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.RequestMethod;
import com.emarsys.core.request.RequestModel;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.AppLoginParameters;
import com.emarsys.mobileengage.MobileEngageException;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.inbox.model.Notification;
import com.emarsys.mobileengage.inbox.model.NotificationCache;
import com.emarsys.mobileengage.inbox.model.NotificationInboxStatus;
import com.emarsys.mobileengage.util.RequestUtils;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

import java.util.HashMap;
import java.util.Map;

public class InboxInternal {

    private static String ENDPOINT_BASE = "https://me-inbox.eservice.emarsys.net/api/";
    private static String ENDPOINT_FETCH = ENDPOINT_BASE + "notifications";

    Handler handler;
    RestClient client;
    MobileEngageConfig config;
    AppLoginParameters appLoginParameters;
    NotificationCache cache;
    RequestManager manager;

    public InboxInternal(MobileEngageConfig config, RequestManager requestManager) {
        Assert.notNull(config, "Config must not be null!");
        Assert.notNull(requestManager, "RequestManager must not be null!");
        EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: config %s, requestManager %s", config, requestManager);

        this.config = config;
        this.client = new RestClient();
        this.handler = new Handler(Looper.getMainLooper());
        this.cache = new NotificationCache();
        this.manager = requestManager;
    }

    public void fetchNotifications(final InboxResultListener<NotificationInboxStatus> resultListener) {
        Assert.notNull(resultListener, "ResultListener should not be null!");
        EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: resultListener %s", resultListener);

        if (appLoginParameters != null && appLoginParameters.hasCredentials()) {
            handleFetchRequest(resultListener);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    resultListener.onError(new NotificationInboxException("AppLogin must be called before calling fetchNotifications!"));
                }
            });
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
                EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: id %s, responseModel %s", id, responseModel);
                NotificationInboxStatus status = InboxParseUtils.parseNotificationInboxStatus(responseModel.getBody());
                NotificationInboxStatus resultStatus = new NotificationInboxStatus(cache.merge(status.getNotifications()), status.getBadgeCount());
                resultListener.onSuccess(resultStatus);
            }

            @Override
            public void onError(String id, ResponseModel responseModel) {
                EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: id %s, responseModel %s", id, responseModel);
                resultListener.onError(new MobileEngageException(responseModel));
            }

            @Override
            public void onError(String id, Exception cause) {
                EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: id %s, cause %s", id, cause);
                resultListener.onError(cause);
            }
        });
    }

    public void resetBadgeCount(final ResetBadgeCountResultListener listener) {
        EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: resultListener %s", listener);
        if (appLoginParameters != null && appLoginParameters.hasCredentials()) {
            handleResetRequest(listener);
        } else {
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onError(new NotificationInboxException("AppLogin must be called before calling fetchNotifications!"));
                    }
                });
            }
        }
    }

    public String trackMessageOpen(Notification message) {
        EMSLogger.log(MobileEngageTopic.INBOX, "Argument: %s", message);
        
        Map<String, Object> payload = RequestUtils.createBasePayload(config, appLoginParameters);
        payload.put("source", "inbox");
        payload.put("sid", message.getSid());
        RequestModel model = new RequestModel.Builder()
                .url(RequestUtils.createEventUrl("message_open"))
                .payload(payload)
                .build();

        manager.submit(model);
        return model.getId();
    }


    private void handleResetRequest(final ResetBadgeCountResultListener listener) {
        RequestModel model = new RequestModel.Builder()
                .url("https://me-inbox.eservice.emarsys.net/api/reset-badge-count")
                .headers(createBaseHeaders(config))
                .method(RequestMethod.POST)
                .build();

        client.execute(model, new CoreCompletionHandler() {
            @Override
            public void onSuccess(String id, ResponseModel responseModel) {
                EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: id %s, responseModel %s", id, responseModel);
                if (listener != null) {
                    listener.onSuccess();
                }
            }

            @Override
            public void onError(String id, ResponseModel responseModel) {
                EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: id %s, responseModel %s", id, responseModel);
                if (listener != null) {
                    listener.onError(new MobileEngageException(responseModel));
                }
            }

            @Override
            public void onError(String id, Exception cause) {
                EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: id %s, cause %s", id, cause);
                if (listener != null) {
                    listener.onError(cause);
                }
            }
        });
    }

    private Map<String, String> createBaseHeaders(MobileEngageConfig config) {
        Map<String, String> result = new HashMap<>();

        result.put("x-ems-me-hardware-id", new DeviceInfo(config.getApplication()).getHwid());
        result.put("x-ems-me-application-code", config.getApplicationCode());
        result.put("x-ems-me-contact-field-id", String.valueOf(appLoginParameters.getContactFieldId()));
        result.put("x-ems-me-contact-field-value", appLoginParameters.getContactFieldValue());

        result.putAll(RequestUtils.createDefaultHeaders(config));

        return result;
    }

    public void setAppLoginParameters(AppLoginParameters appLoginParameters) {
        this.appLoginParameters = appLoginParameters;
    }

}
