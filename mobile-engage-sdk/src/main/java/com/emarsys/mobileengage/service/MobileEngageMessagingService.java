package com.emarsys.mobileengage.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;

public class MobileEngageMessagingService extends FirebaseMessagingService {

    public static final String MESSAGE_FILTER = "ems_msg";
    public static final String SMALL_NOTIFICATION_ICON = "com.emarsys.mobileengage.small_notification_icon";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().size() > 0 && remoteMessage.getData().containsKey(MESSAGE_FILTER)) {

            Intent intent = createIntent(remoteMessage);
            Notification notification = createNotification(remoteMessage, intent);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(42, notification);
        }
    }

    @NonNull
    private Notification createNotification(RemoteMessage remoteMessage, Intent intent) {
        final int DEFAULT_SMALL_NOTIFICATION_ICON = com.emarsys.mobileengage.R.drawable.default_small_notification_icon;
        int resourceId = getSmallIconResourceId(DEFAULT_SMALL_NOTIFICATION_ICON);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setContentTitle(remoteMessage.getData().get("title"))
                        .setSmallIcon(resourceId);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        mBuilder.setContentIntent(resultPendingIntent);
        return mBuilder.build();
    }

    private int getSmallIconResourceId(int DEFAULT_SMALL_NOTIFICATION_ICON) {
        int resourceId;
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            if (ai.metaData.containsKey(SMALL_NOTIFICATION_ICON)) {
                resourceId = ai.metaData.getInt(SMALL_NOTIFICATION_ICON);
            } else {
                resourceId = DEFAULT_SMALL_NOTIFICATION_ICON;
            }
        } catch (PackageManager.NameNotFoundException e) {
            resourceId = DEFAULT_SMALL_NOTIFICATION_ICON;
        }
        return resourceId;
    }

    @NonNull
    private Intent createIntent(RemoteMessage remoteMessage) {
        String packageName = getPackageName();
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }
        intent.putExtra("payload", bundle);
        return intent;
    }
}
