package com.emarsys.mobileengage.utils;

import com.emarsys.mobileengage.inbox.Notification;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ParseUtils {
    private ParseUtils() {
    }

    public static List<Notification> parseNotificationList(String json) {
        return null;
    }

    public static Notification parseNotification(String jsonString) {
        Notification result = null;
        if (jsonString != null) {
            try {
                JSONObject json = new JSONObject(jsonString);
                String id = json.getString("id");
                String title = json.getString("title");
                Map<String, String> customData = convertFlatJsonObject(new JSONObject(json.getString("customData")));
                Map<String, String> rootParams = convertFlatJsonObject(new JSONObject(json.getString("rootParams")));
                int expirationTime = json.getInt("expirationTime");
                Date receivedAt = new Date(json.getLong("receivedAt"));
                result = new Notification(id, title, customData, rootParams, expirationTime, receivedAt);
            } catch (JSONException e) {
            }
        }
        return result;
    }

    public static Map<String, String> convertFlatJsonObject(JSONObject jsonObject) {
        Map<String, String> result = new HashMap<>();
        if (jsonObject != null) {
            try {
                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    result.put(key, jsonObject.getString(key));
                }
            } catch (JSONException e) {
            }
        }
        return result;
    }
}
