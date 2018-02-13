package com.emarsys.mobileengage.util;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.HeaderUtils;
import com.emarsys.core.util.TimestampUtils;
import com.emarsys.mobileengage.BuildConfig;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.iam.model.IamConversionUtils;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    public static Map<String, Object> createCompositeRequestModelPayload(
            List<?> events,
            List<DisplayedIam> displayedIams,
            List<ButtonClicked> buttonClicks,
            DeviceInfo deviceInfo) {
        Assert.notNull(events, "Events must not be null!");
        Assert.notNull(displayedIams, "DisplayedIams must not be null!");
        Assert.notNull(buttonClicks, "ButtonClicks must not be null!");
        Assert.notNull(deviceInfo, "DeviceInfo must not be null!");

        Map<String, Object> compositePayload = new HashMap<>();
        compositePayload.put("viewed_messages", IamConversionUtils.displayedIamsToArray(displayedIams));
        compositePayload.put("clicks", IamConversionUtils.buttonClicksToArray(buttonClicks));
        compositePayload.put("events", events);
        compositePayload.put("hardware_id", deviceInfo.getHwid());
        compositePayload.put("language", deviceInfo.getLanguage());
        compositePayload.put("application_version", deviceInfo.getApplicationVersion());
        compositePayload.put("ems_sdk", MobileEngageInternal.MOBILEENGAGE_SDK_VERSION);
        return compositePayload;
    }

    public static RequestModel createInternalCustomEvent(
            String eventName,
            Map<String, String> attributes,
            String applicationCode,
            MeIdStorage meIdStorage,
            MeIdSignatureStorage meIdSignatureStorage,
            TimestampProvider timestampProvider) {
        Assert.notNull(eventName, "EventName must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        Assert.notNull(meIdStorage, "MeIdStorage must not be null!");
        Assert.notNull(meIdSignatureStorage, "MeIdSignatureStorage must not be null!");
        Assert.notNull(applicationCode, "ApplicationCode must not be null!");

        Map<String, Object> event = new HashMap<>();
        event.put("type", "internal");
        event.put("name", eventName);
        event.put("timestamp", TimestampUtils.formatTimestampWithUTC(timestampProvider.provideTimestamp()));
        if (attributes != null && !attributes.isEmpty()) {
            event.put("attributes", attributes);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("clicks", Collections.emptyList());
        payload.put("viewed_messages", Collections.emptyList());
        payload.put("events", Collections.singletonList(event));

        return new RequestModel(
                RequestUtils.createEventUrl_V3(meIdStorage.get()),
                RequestMethod.POST,
                payload,
                RequestUtils.createBaseHeaders_V3(applicationCode, meIdStorage, meIdSignatureStorage),
                timestampProvider.provideTimestamp(),
                Long.MAX_VALUE,
                RequestModel.nextId());
    }

}
