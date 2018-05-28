package com.emarsys.mobileengage.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.emarsys.core.validate.JsonObjectValidator;
import com.emarsys.mobileengage.notification.command.NotificationCommandFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NotificationActionUtils {

    public static void handleAction(Intent intent, NotificationCommandFactory commandFactory) {
        Runnable command = commandFactory.createNotificationCommand(intent);
        command.run();
    }

    public static List<NotificationCompat.Action> createActions(Context context, Map<String, String> remoteMessageData) {
        List<NotificationCompat.Action> result = new ArrayList<>();
        String emsPayload = remoteMessageData.get("ems");
        if (emsPayload != null) {
            try {
                JSONObject actions = new JSONObject(emsPayload).getJSONObject("actions");
                Iterator<String> iterator = actions.keys();
                while (iterator.hasNext()) {
                    NotificationCompat.Action action = createAction(
                            iterator.next(),
                            actions,
                            context,
                            remoteMessageData);
                    if (action != null) {
                        result.add(action);
                    }
                }
            } catch (JSONException ignored) {
            }
        }
        return result;
    }

    private static NotificationCompat.Action createAction(
            String uniqueId,
            JSONObject actions,
            Context context,
            Map<String, String> remoteMessageData) {
        NotificationCompat.Action result = null;

        try {
            JSONObject action = actions.getJSONObject(uniqueId);

            List<String> validationErrors = validate(action);

            if (validationErrors.isEmpty()) {
                result = new NotificationCompat.Action.Builder(
                        0,
                        action.getString("title"),
                        IntentUtils.createTrackMessageOpenServicePendingIntent(context, remoteMessageData, uniqueId)).build();
            }

        } catch (JSONException ignored) {
        }

        return result;
    }

    private static List<String> validate(JSONObject action) throws JSONException {
        String actionType = action.getString("type");
        List<String> errors = new ArrayList<>();
        if ("MEAppEvent".equals(actionType)) {
            errors = JsonObjectValidator.from(action)
                    .hasField("name")
                    .validate();
        }
        return errors;
    }

}
