package com.emarsys.mobileengage;

import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.emarsys.core.activity.CurrentActivityWatchdog;
import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.core.connection.ConnectionWatchDog;
import com.emarsys.core.queue.sqlite.SqliteQueue;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.inbox.InboxInternal;
import com.emarsys.mobileengage.inbox.InboxResultListener;
import com.emarsys.mobileengage.inbox.ResetBadgeCountResultListener;
import com.emarsys.mobileengage.inbox.model.Notification;
import com.emarsys.mobileengage.inbox.model.NotificationInboxStatus;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

import java.util.Map;

public class MobileEngage {
    private static final String TAG = "MobileEngage";
    static MobileEngageInternal instance;
    static InboxInternal inboxInstance;
    static MobileEngageConfig config;
    static MobileEngageCoreCompletionHandler completionHandler;

    public static class Inbox {

        public static void fetchNotifications(@NonNull InboxResultListener<NotificationInboxStatus> resultListener) {
            Assert.notNull(resultListener, "ResultListener must not be null!");
            inboxInstance.fetchNotifications(resultListener);
        }

        public static void resetBadgeCount() {
            resetBadgeCount(null);
        }

        public static void resetBadgeCount(@Nullable ResetBadgeCountResultListener resultListener) {
            inboxInstance.resetBadgeCount(resultListener);
        }

        public static String trackMessageOpen(Notification message) {
            return inboxInstance.trackMessageOpen(message);
        }

    }

    public static void setup(@NonNull MobileEngageConfig config) {
        Assert.notNull(config, "Config must not be null!");
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Argument: %s", config);

        MobileEngage.config = config;
        MobileEngageUtils.setup(config);

        Application application = config.getApplication();
        CurrentActivityWatchdog.registerApplication(application);

        completionHandler = new MobileEngageCoreCompletionHandler(config.getStatusListener());

        Handler handler = new CoreSdkHandlerProvider().provideHandler();
        RequestManager requestManager = new RequestManager(handler, new ConnectionWatchDog(application, handler), new SqliteQueue(application), completionHandler);

        instance = new MobileEngageInternal(config, requestManager, new AppLoginStorage(application), completionHandler);
        inboxInstance = new InboxInternal(config, requestManager);
    }

    public static MobileEngageConfig getConfig() {
        return config;
    }

    public static void setPushToken(String pushToken) {
        instance.setPushToken(pushToken);
    }

    public static void setStatusListener(MobileEngageStatusListener listener) {
        completionHandler.setStatusListener(listener);
    }

    public static String appLogin() {
        setAppLoginParameters(new AppLoginParameters());
        return instance.appLogin();
    }

    public static String appLogin(int contactFieldId,
                                  @NonNull String contactFieldValue) {
        Assert.notNull(contactFieldValue, "ContactFieldValue must not be null!");
        setAppLoginParameters(new AppLoginParameters(contactFieldId, contactFieldValue));
        return instance.appLogin();
    }

    public static String appLogout() {
        setAppLoginParameters(null);
        return instance.appLogout();
    }

    public static String trackCustomEvent(@NonNull String eventName,
                                          @Nullable Map<String, String> eventAttributes) {
        Assert.notNull(eventName, "EventName must not be null!");
        return instance.trackCustomEvent(eventName, eventAttributes);
    }

    public static String trackMessageOpen(@NonNull Intent intent) {
        Assert.notNull(intent, "Intent must not be null!");
        return instance.trackMessageOpen(intent);
    }

    private static void setAppLoginParameters(AppLoginParameters parameters) {
        instance.setAppLoginParameters(parameters);
        inboxInstance.setAppLoginParameters(parameters);
    }

}
