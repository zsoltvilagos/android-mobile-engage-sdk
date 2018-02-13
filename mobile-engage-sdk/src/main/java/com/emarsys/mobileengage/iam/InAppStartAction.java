package com.emarsys.mobileengage.iam;

import com.emarsys.core.activity.ApplicationStartAction;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.MobileEngageUtils;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.util.RequestUtils;

public class InAppStartAction implements ApplicationStartAction {

    private final RequestManager manager;
    private final TimestampProvider timestampProvider;
    private final MeIdStorage meIdStorage;
    private final MeIdSignatureStorage meIdSignatureStorage;
    private final String applicationCode;

    public InAppStartAction(
            RequestManager requestManager,
            String applicationCode,
            MeIdStorage meIdStorage,
            MeIdSignatureStorage meIdSignatureStorage,
            TimestampProvider timestampProvider) {
        Assert.notNull(requestManager, "RequestManager must not be null!");
        Assert.notNull(applicationCode, "ApplicationCode must not be null!");
        Assert.notNull(meIdStorage, "MeIdStorage must not be null!");
        Assert.notNull(meIdSignatureStorage, "MeIdSignatureStorage must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        this.manager = requestManager;
        this.meIdStorage = meIdStorage;
        this.meIdSignatureStorage = meIdSignatureStorage;
        this.timestampProvider = timestampProvider;
        this.applicationCode = applicationCode;
    }

    @Override
    public void execute() {
        if (meIdStorage.get() != null && meIdSignatureStorage.get() != null) {
            RequestModel model = RequestUtils.createInternalCustomEvent("inapp:start", applicationCode, meIdStorage, meIdSignatureStorage, timestampProvider);

            MobileEngageUtils.incrementIdlingResource();
            manager.submit(model);
        }
    }
}
