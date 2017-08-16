package com.emarsys.mobileengage.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.inbox.InboxParseUtils;
import com.emarsys.mobileengage.inbox.model.NotificationCache;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

class MessagingServiceUtils {

    public static final String MESSAGE_FILTER = "ems_msg";
    public static final String METADATA_SMALL_NOTIFICATION_ICON_KEY = "com.emarsys.mobileengage.small_notification_icon";

    static NotificationCache notificationCache = new NotificationCache();

    static boolean isMobileEngageMessage(Map<String, String> remoteMessageData) {
        return remoteMessageData != null && remoteMessageData.size() > 0 && remoteMessageData.containsKey(MESSAGE_FILTER);
    }

    static Notification createNotification(Map<String, String> remoteMessageData, Context context) {
        int resourceId = getSmallIconResourceId(context);

        String title = getTitle(remoteMessageData, context);
        String body = remoteMessageData.get("body");

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(body)
                                        .setBigContentTitle(title))
                        .setSmallIcon(resourceId)
                        .setAutoCancel(true);

        Intent intent = createIntent(remoteMessageData, context);
        PendingIntent resultPendingIntent = PendingIntent.getService(context, 0, intent, 0);
        mBuilder.setContentIntent(resultPendingIntent);
        return mBuilder.build();
    }

    static String getTitle(Map<String, String> remoteMessageData, Context context) {
        String title = remoteMessageData.get("title");
        if (title == null || title.isEmpty()) {
            title = getDefaultTitle(remoteMessageData, context);
        }
        return title;
    }

    private static String getDefaultTitle(Map<String, String> remoteMessageData, Context context) {
        String title = "";
        if (Build.VERSION.SDK_INT < 23) {
            ApplicationInfo applicationInfo = context.getApplicationInfo();
            int stringId = applicationInfo.labelRes;
            title = stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);

            try {
                String u = remoteMessageData.get("u");
                if (u != null) {
                    JSONObject customData = new JSONObject(u);
                    title = customData.getString("ems_default_title");
                }
            } catch (JSONException ignored) {
            }
        }
        return title;
    }

    static int getSmallIconResourceId(Context context) {
        final int DEFAULT_SMALL_NOTIFICATION_ICON = com.emarsys.mobileengage.R.drawable.default_small_notification_icon;
        int resourceId;
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (ai.metaData.containsKey(METADATA_SMALL_NOTIFICATION_ICON_KEY)) {
                resourceId = ai.metaData.getInt(METADATA_SMALL_NOTIFICATION_ICON_KEY);
            } else {
                resourceId = DEFAULT_SMALL_NOTIFICATION_ICON;
            }
        } catch (PackageManager.NameNotFoundException e) {
            resourceId = DEFAULT_SMALL_NOTIFICATION_ICON;
        }
        return resourceId;
    }

    static Intent createIntent(Map<String, String> remoteMessageData, Context context) {
        Intent intent = new Intent(context, TrackMessageOpenService.class);
        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> entry : remoteMessageData.entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }
        intent.putExtra("payload", bundle);
        return intent;
    }

    static void cacheNotification(Map<String, String> remoteMessageData) {
        Assert.notNull(remoteMessageData, "RemoteMessageData must not be null!");
        notificationCache.cache(InboxParseUtils.parseNotificationFromPushMessage(remoteMessageData));
    }

}