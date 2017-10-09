package com.emarsys.mobileengage.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.config.OreoConfig;
import com.emarsys.mobileengage.inbox.InboxParseUtils;
import com.emarsys.mobileengage.inbox.model.NotificationCache;
import com.emarsys.mobileengage.util.ImageUtils;

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

    static Notification createNotification(Context context, Map<String, String> remoteMessageData, OreoConfig oreoConfig) {
        int resourceId = getSmallIconResourceId(context);

        String title = getTitle(remoteMessageData, context);
        String body = remoteMessageData.get("body");
        Bitmap image = ImageUtils.loadBitmapFromUrl(remoteMessageData.get("imageUrl"));
        String channelId = getChannelId(remoteMessageData, oreoConfig);

        if (OreoConfig.DEFAULT_CHANNEL_ID.equals(channelId)) {
            createDefaultChannel(context, oreoConfig);
        }

        PendingIntent resultPendingIntent = createPendingIntent(context, remoteMessageData);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(resourceId)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent);

        styleNotification(builder, title, body, image);

        return builder.build();
    }

    private static PendingIntent createPendingIntent(Context context, Map<String, String> remoteMessageData) {
        Intent intent = createIntent(remoteMessageData, context);
        return PendingIntent.getService(context, 0, intent, 0);
    }

    private static void styleNotification(NotificationCompat.Builder builder, String title, String body, Bitmap bitmap) {
        if (bitmap != null) {
            builder.setLargeIcon(bitmap)
                    .setStyle(new NotificationCompat
                            .BigPictureStyle()
                            .bigPicture(bitmap)
                            .bigLargeIcon(null)
                            .setBigContentTitle(title)
                            .setSummaryText(body));
        } else {
            builder.setStyle(new NotificationCompat
                    .BigTextStyle()
                    .bigText(body)
                    .setBigContentTitle(title));
        }
    }

    static String getTitle(Map<String, String> remoteMessageData, Context context) {
        String title = remoteMessageData.get("title");
        if (title == null || title.isEmpty()) {
            title = getDefaultTitle(remoteMessageData, context);
        }
        return title;
    }

    static String getChannelId(Map<String, String> remoteMessageData, OreoConfig oreoConfig) {
        String result = remoteMessageData.get("channel_id");
        if (result == null && oreoConfig.isDefaultChannelEnabled()) {
            result = OreoConfig.DEFAULT_CHANNEL_ID;
        }
        return result;
    }

    static void createDefaultChannel(Context context, OreoConfig oreoConfig) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(OreoConfig.DEFAULT_CHANNEL_ID, oreoConfig.getDefaultChannelName(), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(oreoConfig.getDefaultChannelDescription());
            notificationManager.createNotificationChannel(channel);
        }
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
