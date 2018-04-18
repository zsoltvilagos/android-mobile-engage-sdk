package com.emarsys.mobileengage.util;

import com.emarsys.core.util.Assert;
import com.emarsys.core.util.HeaderUtils;
import com.emarsys.mobileengage.BuildConfig;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;

import java.util.HashMap;
import java.util.Map;

public class RequestHeaderUtils {

    public static Map<String, String> createBaseHeaders_V2(MobileEngageConfig config) {
        Assert.notNull(config, "Config must not be null!");
        Map<String, String> baseHeaders = new HashMap<>();
        baseHeaders.put("Authorization", HeaderUtils.createBasicAuth(config.getApplicationCode(), config.getApplicationPassword()));
        return baseHeaders;
    }

    public static Map<String, String> createBaseHeaders_V3(
            String applicationCode,
            MeIdStorage meIdStorage,
            MeIdSignatureStorage meIdSignatureStorage) {
        Assert.notNull(applicationCode, "ApplicationCode must not be null!");
        Assert.notNull(meIdStorage, "MeIdStorage must not be null!");
        Assert.notNull(meIdSignatureStorage, "MeIdSignatureStorage must not be null!");

        Map<String, String> baseHeaders = new HashMap<>();
        baseHeaders.put("X-ME-ID", meIdStorage.get());
        baseHeaders.put("X-ME-ID-SIGNATURE", meIdSignatureStorage.get());
        baseHeaders.put("X-ME-APPLICATIONCODE", applicationCode);

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
}
