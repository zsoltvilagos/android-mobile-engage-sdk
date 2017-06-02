package com.emarsys.mobileengage;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.inbox.InboxResultListener;
import com.emarsys.mobileengage.inbox.Notification;

import java.util.List;
import java.util.Map;

public class MobileEngage {
    private static final String TAG = "MobileEngage";
    static MobileEngageInternal instance;

    public static class Inbox {

        public static void fetchNotifications(InboxResultListener<List<Notification>> resultListener){

        }
    }

    public static void setup(@NonNull MobileEngageConfig config) {
        Assert.notNull(config, "Config must not be null!");
        instance = new MobileEngageInternal(config);
    }

    public static void setPushToken(String pushToken){
        instance.setPushToken(pushToken);
    }

    public static void setStatusListener(MobileEngageStatusListener listener){
        instance.setStatusListener(listener);
    }

    public static String appLogin() {
        return instance.appLogin();
    }

    public static String appLogin(int contactField,
                                  @NonNull String contactFieldValue) {
        Assert.notNull(contactFieldValue, "ContactFieldValue must not be null!");
        return instance.appLogin(contactField, contactFieldValue);
    }

    public static String appLogout() {
        return instance.appLogout();
    }

    public static String trackCustomEvent(@NonNull String eventName,
                                          @Nullable Map<String, String>  eventAttributes) {
        Assert.notNull(eventName, "EventName must not be null!");
        return instance.trackCustomEvent(eventName, eventAttributes);
    }

    public static String trackMessageOpen(@NonNull Intent intent) {
        Assert.notNull(intent, "Intent must not be null!");
        return instance.trackMessageOpen(intent);
    }

}
