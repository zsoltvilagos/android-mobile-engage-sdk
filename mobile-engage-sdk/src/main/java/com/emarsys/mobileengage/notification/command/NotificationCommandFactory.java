package com.emarsys.mobileengage.notification.command;

import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

public class NotificationCommandFactory {

    public Runnable createNotificationCommand(Intent intent) {
        Runnable result = null;
        String actionId = intent.getAction();
        String payloadString = intent.getStringExtra("payload");
        if (actionId == null || payloadString == null) {
            return null;
        }
        try {
            JSONObject actions = new JSONObject(payloadString).getJSONObject("actions");
            JSONObject action = actions.getJSONObject(actionId);
            String type = action.getString("type");
            if ("MEAppEvent".equals(type)) {
                String name = action.getString("name");
                JSONObject payload = action.optJSONObject("payload");
                result = new AppEventCommand(name, payload);
            }
        } catch (JSONException ignored) {}
        return result;
    }

}
