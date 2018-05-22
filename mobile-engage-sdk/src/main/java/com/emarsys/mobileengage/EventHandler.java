package com.emarsys.mobileengage;

import org.json.JSONObject;

public interface EventHandler {
    void handleEvent(String eventName, JSONObject payload);
}
