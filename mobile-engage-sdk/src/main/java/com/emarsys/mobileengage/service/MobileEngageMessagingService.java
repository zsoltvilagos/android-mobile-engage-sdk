package com.emarsys.mobileengage.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import com.emarsys.core.resource.MetaDataReader;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.MobileEngage;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MobileEngageMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> remoteData = remoteMessage.getData();

        EMSLogger.log(MobileEngageTopic.PUSH, "Remote message data %s", remoteData);

        if (MessagingServiceUtils.isMobileEngageMessage(remoteData)) {

            EMSLogger.log(MobileEngageTopic.PUSH, "RemoteMessage is ME message");

            MessagingServiceUtils.cacheNotification(remoteData);

            int notificationId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

            Notification notification = MessagingServiceUtils.createNotification(
                    notificationId,
                    getApplicationContext(),
                    remoteData,
                    MobileEngage.getConfig().getOreoConfig(),
                    new MetaDataReader());

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(notificationId, notification);
        }
    }
}
