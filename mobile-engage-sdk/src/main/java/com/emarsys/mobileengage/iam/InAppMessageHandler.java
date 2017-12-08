package com.emarsys.mobileengage.iam;


import java.util.Map;

public interface InAppMessageHandler {
    void handleApplicationEvent(String eventName, Map<String, Object> payload);
}
