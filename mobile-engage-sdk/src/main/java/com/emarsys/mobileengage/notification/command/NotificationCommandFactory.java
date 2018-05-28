package com.emarsys.mobileengage.notification.command;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.emarsys.core.util.Assert;

import org.json.JSONArray;
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
            String emsPayload = bundle.getString("ems");

            if (actionId != null && emsPayload != null) {
                try {
                    JSONArray actions = new JSONObject(emsPayload).getJSONArray("actions");
                    JSONObject action = findActionWithId(actions, actionId);
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

    private JSONObject findActionWithId(JSONArray actions, String actionId) throws JSONException {
        for (int i = 0; i < actions.length(); ++i) {
            JSONObject action = actions.optJSONObject(i);
            if (action != null && actionId.equals(action.optString("id"))) {
                return action;
            }
        }
        throw new JSONException("Cannot find action with id: " + actionId);
    }

}
