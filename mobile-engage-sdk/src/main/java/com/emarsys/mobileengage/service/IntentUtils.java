package com.emarsys.mobileengage.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.Map;

public class IntentUtils {

    public static Intent createIntent(Map<String, String> remoteMessageData, Context context, String action) {
        Intent intent = new Intent(context, TrackMessageOpenService.class);

        if (action != null) {
            intent.setAction(action);
        }

        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> entry : remoteMessageData.entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }

        intent.putExtra("payload", bundle);
        return intent;
    }

    public static PendingIntent createPendingIntent(Context context, Map<String, String> remoteMessageData) {
        return createPendingIntent(context, remoteMessageData, null);
    }

    public static PendingIntent createPendingIntent(Context context, Map<String, String> remoteMessageData, String action) {
        Intent intent = createIntent(remoteMessageData, context, action);
        return PendingIntent.getService(
                context,
                (int) (System.currentTimeMillis() % Integer.MAX_VALUE),
                intent,
                0);
    }
}
