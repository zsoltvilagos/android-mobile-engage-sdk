package com.emarsys.mobileengage.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MobileEngageMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (MobileEngageMessagingServiceUtils.isMobileEngageMessage(remoteMessage.getData())) {

            Notification notification = MobileEngageMessagingServiceUtils.createNotification(remoteMessage.getData(), getApplicationContext());

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify((int) System.currentTimeMillis(), notification);
        }
    }
}
