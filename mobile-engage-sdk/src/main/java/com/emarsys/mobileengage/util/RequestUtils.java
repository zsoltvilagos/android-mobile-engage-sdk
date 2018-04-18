package com.emarsys.mobileengage.util;

import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.TimestampUtils;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RequestUtils {

    public static boolean isCustomEvent_V3(RequestModel requestModel) {
        Assert.notNull(requestModel, "RequestModel must not be null");
        String url = requestModel.getUrl().toString();
        return RequestUrlUtils.isCustomEvent_V3(url);
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
                RequestUrlUtils.createEventUrl_V3(meIdStorage.get()),
                RequestMethod.POST,
                payload,
                RequestHeaderUtils.createBaseHeaders_V3(applicationCode, meIdStorage, meIdSignatureStorage),
                timestampProvider.provideTimestamp(),
                Long.MAX_VALUE,
                RequestModel.nextId());
    }

}
