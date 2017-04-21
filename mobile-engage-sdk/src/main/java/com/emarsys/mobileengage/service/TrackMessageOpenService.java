package com.emarsys.mobileengage.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.emarsys.mobileengage.MobileEngage;

public class TrackMessageOpenService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startActivity(createIntent(intent));
        if (intent != null) {
            MobileEngage.trackMessageOpen(intent);
        }
        stopSelf();
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Intent createIntent(Intent remoteIntent) {
        String packageName = getApplicationContext().getPackageName();
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        intent.putExtras(remoteIntent.getExtras());
        return intent;
    }
}
