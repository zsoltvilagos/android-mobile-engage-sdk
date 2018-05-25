package com.emarsys.mobileengage.notification.command;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.emarsys.core.util.Assert;

import org.json.JSONException;
import org.json.JSONObject;

public class NotificationCommandFactory {

    Context context;

    public NotificationCommandFactory(Context context) {
        Assert.notNull(context, "Context must not be null!");
        this.context = context;
    }

    public Runnable createNotificationCommand(Intent intent) {
        Runnable result = null;
        String actionId = intent.getAction();
        Bundle bundle = intent.getBundleExtra("payload");

        if (bundle != null) {
            String actionsString = bundle.getString("actions");

            if (actionId != null && actionsString != null) {
                try {
                    JSONObject actions = new JSONObject(actionsString);
                    JSONObject action = actions.getJSONObject(actionId);
                    String type = action.getString("type");
                    if ("MEAppEvent".equals(type)) {
                        String name = action.getString("name");
                        JSONObject payload = action.optJSONObject("payload");
                        result = new AppEventCommand(name, payload);
                    }
                } catch (JSONException ignored) {
                }
            }
        }

        if (result == null) {
            result = new LaunchApplicationCommand(intent, context);
        }

        return result;
    }

}
