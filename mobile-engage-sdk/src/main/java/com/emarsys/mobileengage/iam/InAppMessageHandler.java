package com.emarsys.mobileengage.iam;

import org.json.JSONObject;

public interface InAppMessageHandler {
    void handleApplicationEvent(String eventName, JSONObject payload);
}
