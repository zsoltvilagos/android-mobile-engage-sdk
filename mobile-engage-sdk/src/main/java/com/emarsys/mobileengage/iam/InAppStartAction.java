package com.emarsys.mobileengage.iam;

import com.emarsys.core.activity.ApplicationStartAction;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.TimestampUtils;
import com.emarsys.mobileengage.MobileEngageUtils;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.util.RequestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InAppStartAction implements ApplicationStartAction {

    private final RequestManager manager;
    private final TimestampProvider timestampProvider;
    private final MeIdStorage meIdStorage;
    private final MeIdSignatureStorage meIdSignatureStorage;
    private final MobileEngageConfig config;

    public InAppStartAction(
            RequestManager requestManager,
            MobileEngageConfig config,
            MeIdStorage meIdStorage,
            MeIdSignatureStorage meIdSignatureStorage,
            TimestampProvider timestampProvider) {
        Assert.notNull(requestManager, "RequestManager must not be null!");
        Assert.notNull(config, "Config must not be null!");
        Assert.notNull(meIdStorage, "MeIdStorage must not be null!");
        Assert.notNull(meIdSignatureStorage, "MeIdSignatureStorage must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        this.manager = requestManager;
        this.meIdStorage = meIdStorage;
        this.meIdSignatureStorage = meIdSignatureStorage;
        this.timestampProvider = timestampProvider;
        this.config = config;
    }

    @Override
    public void execute() {
        if (meIdStorage.get() != null && meIdSignatureStorage.get() != null) {
            Map<String, Object> event = new HashMap<>();
            event.put("type", "internal");
            event.put("name", "inapp:start");
            event.put("timestamp", TimestampUtils.formatTimestampWithUTC(timestampProvider.provideTimestamp()));

            Map<String, Object> payload = new HashMap<>();
            payload.put("clicks", new ArrayList<>());
            payload.put("viewed_messages", new ArrayList<>());
            payload.put("events", Collections.singletonList(event));

            RequestModel model = new RequestModel.Builder()
                    .url(RequestUtils.createEventUrl_V3(meIdStorage.get()))
                    .payload(payload)
                    .headers(RequestUtils.createBaseHeaders_V3(
                            config.getApplicationCode(),
                            meIdStorage,
                            meIdSignatureStorage))
                    .build();

            MobileEngageUtils.incrementIdlingResource();
            manager.submit(model);
        }
    }
}
