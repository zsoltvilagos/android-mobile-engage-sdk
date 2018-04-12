package com.emarsys.mobileengage.inbox;

import android.os.Handler;
import android.os.Looper;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.MobileEngageException;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.endpoint.Endpoint;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.inbox.model.Notification;
import com.emarsys.mobileengage.inbox.model.NotificationCache;
import com.emarsys.mobileengage.inbox.model.NotificationInboxStatus;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.util.RequestUtils;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

import java.util.HashMap;
import java.util.Map;

public class InboxInternal_V2 implements InboxInternal {

    private Handler mainHandler;
    private RestClient client;
    private MobileEngageConfig config;
    private NotificationCache cache;
    private MeIdStorage meIdStorage;
    private RequestManager manager;

    public InboxInternal_V2(MobileEngageConfig config,
                            RequestManager requestManager,
                            RestClient restClient,
                            MeIdStorage meIdStorage) {
        Assert.notNull(config, "Config must not be null!");
        Assert.notNull(requestManager, "RequestManager must not be null!");
        Assert.notNull(restClient, "RestClient must not be null!");
        Assert.notNull(meIdStorage, "MeIdStorage must not be null!");
        EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: config %s, requestManager %s", config, requestManager);

        this.config = config;
        this.client = restClient;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.cache = new NotificationCache();
        this.manager = requestManager;
        this.meIdStorage = meIdStorage;
    }

    @Override
    public void fetchNotifications(final InboxResultListener<NotificationInboxStatus> resultListener) {
        Assert.notNull(resultListener, "ResultListener should not be null!");
        EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: resultListener %s", resultListener);

        String meId = meIdStorage.get();

        if (meId == null) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    resultListener.onError(new NotificationInboxException("Missing MeId, appLogin must be called before calling fetchNotifications!"));
                }
            });
        } else {
            RequestModel model = new RequestModel.Builder()
                    .url(String.format(Endpoint.INBOX_FETCH_V2, meId))
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

    }

    @Override
    public void resetBadgeCount(ResetBadgeCountResultListener listener) {

    }

    @Override
    public String trackMessageOpen(Notification message) {
        return null;
    }

    @Override
    public void setAppLoginParameters(AppLoginParameters appLoginParameters) {

    }

    private Map<String, String> createBaseHeaders(MobileEngageConfig config) {
        Map<String, String> result = new HashMap<>();

        result.put("x-ems-me-application-code", config.getApplicationCode());
        result.putAll(RequestUtils.createDefaultHeaders(config));
        result.putAll(RequestUtils.createBaseHeaders_V2(config));

        return result;
    }
}
