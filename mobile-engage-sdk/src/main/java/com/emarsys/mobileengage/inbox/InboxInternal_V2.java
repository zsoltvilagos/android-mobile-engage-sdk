package com.emarsys.mobileengage.inbox;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.MobileEngageException;
import com.emarsys.mobileengage.MobileEngageStatusListener;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.endpoint.Endpoint;
import com.emarsys.mobileengage.inbox.model.Notification;
import com.emarsys.mobileengage.inbox.model.NotificationCache;
import com.emarsys.mobileengage.inbox.model.NotificationInboxStatus;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.util.RequestHeaderUtils;
import com.emarsys.mobileengage.util.RequestModelUtils;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.emarsys.mobileengage.endpoint.Endpoint.INBOX_RESET_BADGE_COUNT_V2;

public class InboxInternal_V2 implements InboxInternal {

    private Handler mainHandler;
    private RestClient client;
    private MobileEngageConfig config;
    private NotificationCache cache;
    private RequestContext requestContext;
    private MeIdStorage meIdStorage;
    private RequestManager manager;

    public InboxInternal_V2(MobileEngageConfig config,
                            RequestManager requestManager,
                            RestClient restClient,
                            RequestContext requestContext) {
        Assert.notNull(config, "Config must not be null!");
        Assert.notNull(requestManager, "RequestManager must not be null!");
        Assert.notNull(restClient, "RestClient must not be null!");
        Assert.notNull(requestContext, "RequestContext must not be null!");
        EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: config %s, requestManager %s", config, requestManager);

        this.config = config;
        this.client = restClient;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.cache = new NotificationCache();
        this.manager = requestManager;
        this.requestContext = requestContext;
        this.meIdStorage = requestContext.getMeIdStorage();
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
    public void resetBadgeCount(final ResetBadgeCountResultListener listener) {
        EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: resultListener %s", listener);
        String meId = meIdStorage.get();
        if (meId != null) {
            RequestModel model = new RequestModel.Builder()
                    .url(String.format(INBOX_RESET_BADGE_COUNT_V2, meId))
                    .headers(createBaseHeaders(config))
                    .method(RequestMethod.DELETE)
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
        } else {
            if (listener != null) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onError(new NotificationInboxException("AppLogin must be called before calling fetchNotifications!"));
                    }
                });
            }
        }
    }

    @Override
    public String trackMessageOpen(Notification message) {
        final Exception exception = validateNotification(message);
        String requestId;

        if (exception == null) {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("message_id", message.getId());
            attributes.put("sid", message.getSid());
            RequestModel requestModel = RequestModelUtils.createInternalCustomEvent(
                    "inbox:open",
                    attributes,
                    requestContext.getApplicationCode(),
                    requestContext.getMeIdStorage(),
                    requestContext.getMeIdSignatureStorage(),
                    requestContext.getTimestampProvider());
            manager.submit(requestModel);
            requestId = requestModel.getId();
        } else {
            final String uuid = UUID.randomUUID().toString();
            final MobileEngageStatusListener statusListener = config.getStatusListener();
            if (statusListener != null) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        statusListener.onError(uuid, exception);
                    }
                });
            }
            requestId = uuid;
        }
        return requestId;
    }

    private Exception validateNotification(Notification notification) {
        Exception exception = null;
        List<String> missingFields = new ArrayList<>();

        if (notification.getId() == null) {
            missingFields.add("Id");
        }
        if (notification.getSid() == null) {
            missingFields.add("Sid");
        }
        if (!missingFields.isEmpty()) {
            exception = new IllegalArgumentException(TextUtils.join(", ", missingFields) + " is missing!");
        }
        return exception;
    }

    private Map<String, String> createBaseHeaders(MobileEngageConfig config) {
        Map<String, String> result = new HashMap<>();

        result.put("x-ems-me-application-code", config.getApplicationCode());
        result.putAll(RequestHeaderUtils.createDefaultHeaders(config));
        result.putAll(RequestHeaderUtils.createBaseHeaders_V2(config));

        return result;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }
}
