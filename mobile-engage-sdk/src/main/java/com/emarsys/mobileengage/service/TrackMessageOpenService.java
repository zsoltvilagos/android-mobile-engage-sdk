package com.emarsys.mobileengage.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.MobileEngage;
import com.emarsys.mobileengage.notification.command.NotificationCommandFactory;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

public class TrackMessageOpenService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        EMSLogger.log(MobileEngageTopic.PUSH, "Notification was clicked");

        NotificationActionHandler.handleAction(intent, new NotificationCommandFactory());

        startActivity(createIntent(intent));
        if (intent != null) {
            MobileEngage.trackMessageOpen(intent);
        }
        stopSelf(startId);
        return START_NOT_STICKY;
    }

    private Intent createIntent(Intent remoteIntent) {
        String packageName = getPackageName();
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        intent.putExtras(remoteIntent.getExtras());
        return intent;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
