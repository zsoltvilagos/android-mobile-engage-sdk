package com.emarsys.mobileengage;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.espresso.idling.CountingIdlingResource;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.inbox.InboxInternal;
import com.emarsys.mobileengage.inbox.InboxResultListener;
import com.emarsys.mobileengage.inbox.ResetBadgeCountResultListener;
import com.emarsys.mobileengage.inbox.model.Notification;
import com.emarsys.mobileengage.inbox.model.NotificationInboxStatus;

import java.util.Map;

public class MobileEngage {
    public static CountingIdlingResource idlingResource = new CountingIdlingResource("mobile-engage-idling-resource");
    private static final String TAG = "MobileEngage";
    static MobileEngageInternal instance;
    static InboxInternal inboxInstance;

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
    }

    public static void setup(@NonNull MobileEngageConfig config) {
        Assert.notNull(config, "Config must not be null!");
        instance = new MobileEngageInternal(config);
        inboxInstance = new InboxInternal(config);
    }

    public static void setPushToken(String pushToken) {
        instance.setPushToken(pushToken);
    }

    public static void setStatusListener(MobileEngageStatusListener listener) {
        instance.setStatusListener(listener);
    }

    public static String appLogin() {
        inboxInstance.setAppLoginParameters(new AppLoginParameters());
        return instance.appLogin();
    }

    public static String appLogin(int contactField,
                                  @NonNull String contactFieldValue) {
        Assert.notNull(contactFieldValue, "ContactFieldValue must not be null!");
        inboxInstance.setAppLoginParameters(new AppLoginParameters(contactField, contactFieldValue));
        return instance.appLogin(contactField, contactFieldValue);
    }

    public static String appLogout() {
        inboxInstance.setAppLoginParameters(null);
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

    public static String trackMessageOpen(Notification message) {
        return instance.trackMessageOpen(message);
    }

}
