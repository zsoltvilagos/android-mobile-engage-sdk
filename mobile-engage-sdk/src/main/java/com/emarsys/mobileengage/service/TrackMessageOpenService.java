package com.emarsys.mobileengage.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.MobileEngage;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

import org.json.JSONException;
import org.json.JSONObject;

public class TrackMessageOpenService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        EMSLogger.log(MobileEngageTopic.PUSH, "Notification was clicked");

        try {
            handleActions(intent);
        } catch (JSONException ignored) {
        }

        startActivity(createIntent(intent));
        if (intent != null) {
            MobileEngage.trackMessageOpen(intent);
        }
        stopSelf(startId);
        return START_NOT_STICKY;
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

    private void handleActions(Intent intent) throws JSONException {
        String actionId = intent.getAction();
        String actionsString = intent.getStringExtra("actions");
        if (actionId == null || actionsString == null) {
            return;
        }
        JSONObject action = new JSONObject(actionsString).getJSONObject(actionId);
        String type = action.getString("type");
        if ("MEAppEvent".equals(type)) {
            String name = action.getString("name");
            JSONObject payload = action.optJSONObject("payload");
            MobileEngage.getConfig().getNotificationEventHandler().handleEvent(name, payload);
        }
    }
}
