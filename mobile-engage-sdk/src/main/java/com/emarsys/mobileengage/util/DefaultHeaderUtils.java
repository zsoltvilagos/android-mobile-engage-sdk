package com.emarsys.mobileengage.util;

import com.emarsys.core.util.Assert;
import com.emarsys.core.util.HeaderUtils;
import com.emarsys.mobileengage.BuildConfig;
import com.emarsys.mobileengage.MobileEngageConfig;

import java.util.HashMap;
import java.util.Map;

public class DefaultHeaderUtils {

    public static Map<String, String> createDefaultHeaders(MobileEngageConfig config) {
        Assert.notNull(config, "Config must not be null!");

        HashMap<String, String> defaultHeaders = new HashMap<>();
        defaultHeaders.put("Authorization", HeaderUtils.createBasicAuth(config.getApplicationCode(), config.getApplicationPassword()));
        defaultHeaders.put("Content-Type", "application/json");
        defaultHeaders.put("X-MOBILEENGAGE-SDK-VERSION", BuildConfig.VERSION_NAME);
        defaultHeaders.put("X-MOBILEENGAGE-SDK-MODE", config.isDebugMode() ? "debug" : "production");

        return defaultHeaders;
    }
}
