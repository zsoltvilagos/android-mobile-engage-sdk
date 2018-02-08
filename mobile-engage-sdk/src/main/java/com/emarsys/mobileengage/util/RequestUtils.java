package com.emarsys.mobileengage.util;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.HeaderUtils;
import com.emarsys.mobileengage.BuildConfig;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.emarsys.mobileengage.endpoint.Endpoint.ME_BASE_V2;
import static com.emarsys.mobileengage.endpoint.Endpoint.ME_BASE_V3;

public class RequestUtils {

    private static DeviceInfo deviceInfo;

    public static String createEventUrl_V2(String eventName) {
        Assert.notNull(eventName, "EventName must not be null!");
        return ME_BASE_V2 + "events/" + eventName;
    }

    public static String createEventUrl_V3(String meId) {
        Assert.notNull(meId, "MEID must not be null!");

        return ME_BASE_V3 + meId + "/events";
    }

    public static Map<String, String> createBaseHeaders_V2(MobileEngageConfig config) {
        Assert.notNull(config, "Config must not be null!");
        Map<String, String> baseHeaders = new HashMap<>();
        baseHeaders.put("Authorization", HeaderUtils.createBasicAuth(config.getApplicationCode(), config.getApplicationPassword()));
        return baseHeaders;
    }

    public static Map<String, String> createBaseHeaders_V3(MobileEngageConfig config) {
        Assert.notNull(config, "Config must not be null!");
        MeIdStorage meIdStorage = new MeIdStorage(config.getApplication());
        MeIdSignatureStorage meIdSignatureStorage = new MeIdSignatureStorage(config.getApplication());

        Map<String, String> baseHeaders = new HashMap<>();
        baseHeaders.put("X-ME-ID", meIdStorage.get());
        baseHeaders.put("X-ME-ID-SIGNATURE", meIdSignatureStorage.get());
        baseHeaders.put("X-ME-APPLICATIONCODE", config.getApplicationCode());

        return baseHeaders;
    }

    public static Map<String, String> createDefaultHeaders(MobileEngageConfig config) {
        Assert.notNull(config, "Config must not be null!");

        HashMap<String, String> defaultHeaders = new HashMap<>();
        defaultHeaders.put("Content-Type", "application/json");
        defaultHeaders.put("X-MOBILEENGAGE-SDK-VERSION", BuildConfig.VERSION_NAME);
        defaultHeaders.put("X-MOBILEENGAGE-SDK-MODE", config.isDebugMode() ? "debug" : "production");

        return defaultHeaders;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> createBasePayload(MobileEngageConfig config, AppLoginParameters parameters) {
        Assert.notNull(config, "Config must not be null!");
        return createBasePayload(Collections.EMPTY_MAP, config, parameters);
    }

    public static Map<String, Object> createBasePayload(Map<String, Object> additionalPayload, MobileEngageConfig config, AppLoginParameters parameters) {
        Assert.notNull(config, "Config must not be null!");
        Assert.notNull(additionalPayload, "AdditionalPayload must not be null!");
        if (deviceInfo == null) {
            deviceInfo = new DeviceInfo(config.getApplication());
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("application_id", config.getApplicationCode());
        payload.put("hardware_id", deviceInfo.getHwid());

        if (parameters != null && parameters.hasCredentials()) {
            payload.put("contact_field_id", parameters.getContactFieldId());
            payload.put("contact_field_value", parameters.getContactFieldValue());
        }

        for (Map.Entry<String, Object> entry : additionalPayload.entrySet()) {
            payload.put(entry.getKey(), entry.getValue());
        }
        return payload;
    }

}
