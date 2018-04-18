package com.emarsys.mobileengage.util;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.iam.model.IamConversionUtils;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestPayloadUtils {

    @SuppressWarnings("unchecked")
    public static Map<String, Object> createBasePayload(MobileEngageConfig config, AppLoginParameters parameters, DeviceInfo deviceInfo) {
        Assert.notNull(config, "Config must not be null!");
        Assert.notNull(deviceInfo, "DeviceInfo must not be null!");
        return createBasePayload(Collections.EMPTY_MAP, config, parameters, deviceInfo);
    }

    public static Map<String, Object> createBasePayload(Map<String, Object> additionalPayload, MobileEngageConfig config, AppLoginParameters parameters, DeviceInfo deviceInfo) {
        Assert.notNull(additionalPayload, "AdditionalPayload must not be null!");
        Assert.notNull(config, "Config must not be null!");
        Assert.notNull(deviceInfo, "DeviceInfo must not be null!");

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
            DeviceInfo deviceInfo,
            boolean doNotDisturb) {
        Assert.notNull(events, "Events must not be null!");
        Assert.notNull(displayedIams, "DisplayedIams must not be null!");
        Assert.notNull(buttonClicks, "ButtonClicks must not be null!");
        Assert.notNull(deviceInfo, "DeviceInfo must not be null!");

        Map<String, Object> compositePayload = new HashMap<>();
        compositePayload.put("viewed_messages", IamConversionUtils.displayedIamsToArray(displayedIams));
        compositePayload.put("clicks", IamConversionUtils.buttonClicksToArray(buttonClicks));
        if (doNotDisturb) {
            compositePayload.put("dnd", true);
        }
        compositePayload.put("events", events);
        compositePayload.put("hardware_id", deviceInfo.getHwid());
        compositePayload.put("language", deviceInfo.getLanguage());
        compositePayload.put("application_version", deviceInfo.getApplicationVersion());
        compositePayload.put("ems_sdk", MobileEngageInternal.MOBILEENGAGE_SDK_VERSION);
        return compositePayload;
    }
}
