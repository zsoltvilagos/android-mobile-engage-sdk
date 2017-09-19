package com.emarsys.mobileengage.util;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.HeaderUtils;
import com.emarsys.mobileengage.BuildConfig;
import com.emarsys.mobileengage.MobileEngageConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RequestUtils {
    public static final String ENDPOINT_BASE = "https://push.eservice.emarsys.net/api/mobileengage/v2/";
    public static final String ENDPOINT_LOGIN = ENDPOINT_BASE + "users/login";
    public static final String ENDPOINT_LOGOUT = ENDPOINT_BASE + "users/logout";

    private static DeviceInfo deviceInfo;

    public static String createEventUrl(String eventName) {
        Assert.notNull(eventName, "EventName must not be null!");
        return ENDPOINT_BASE + "events/" + eventName;
    }

    public static Map<String, String> createDefaultHeaders(MobileEngageConfig config) {
        Assert.notNull(config, "Config must not be null!");

        HashMap<String, String> defaultHeaders = new HashMap<>();
        defaultHeaders.put("Authorization", HeaderUtils.createBasicAuth(config.getApplicationCode(), config.getApplicationPassword()));
        defaultHeaders.put("Content-Type", "application/json");
        defaultHeaders.put("X-MOBILEENGAGE-SDK-VERSION", BuildConfig.VERSION_NAME);
        defaultHeaders.put("X-MOBILEENGAGE-SDK-MODE", config.isDebugMode() ? "debug" : "production");

        return defaultHeaders;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> createBasePayload(MobileEngageConfig config) {
        Assert.notNull(config, "Config must not be null!");
        return createBasePayload(Collections.EMPTY_MAP, config);
    }

    public static Map<String, Object> createBasePayload(Map<String, Object> additionalPayload, MobileEngageConfig config) {
        Assert.notNull(config, "Config must not be null!");
        Assert.notNull(additionalPayload, "AdditionalPayload must not be null!");
        if (deviceInfo == null) {
            deviceInfo = new DeviceInfo(config.getApplication());
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("application_id", config.getApplicationCode());
        payload.put("hardware_id", deviceInfo.getHwid());

        for (Map.Entry<String, Object> entry : additionalPayload.entrySet()) {
            payload.put(entry.getKey(), entry.getValue());
        }
        return payload;
    }

}
