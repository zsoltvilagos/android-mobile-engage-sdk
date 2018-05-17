package com.emarsys.mobileengage.iam;

import org.json.JSONObject;

public interface EventHandler {
    void handleEvent(String eventName, JSONObject payload);
}
