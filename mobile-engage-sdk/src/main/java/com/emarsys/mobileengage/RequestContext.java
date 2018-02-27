package com.emarsys.mobileengage;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;

public class RequestContext {
    private final String applicationCode;
    private final DeviceInfo deviceInfo;
    private final AppLoginStorage appLoginStorage;
    private final MeIdStorage meIdStorage;
    private final MeIdSignatureStorage meIdSignatureStorage;
    private final TimestampProvider timestampProvider;

    public RequestContext(
            String applicationCode,
            DeviceInfo deviceInfo,
            AppLoginStorage appLoginStorage,
            MeIdStorage meIdStorage,
            MeIdSignatureStorage meIdSignatureStorage,
            TimestampProvider timestampProvider) {
        Assert.notNull(applicationCode, "ApplicationCode must not be null!");
        Assert.notNull(deviceInfo, "DeviceInfo must not be null!");
        Assert.notNull(appLoginStorage, "AppLoginStorage must not be null!");
        Assert.notNull(meIdStorage, "MeIdStorage must not be null!");
        Assert.notNull(meIdSignatureStorage, "MeIdSignatureStorage must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        this.applicationCode = applicationCode;
        this.deviceInfo = deviceInfo;
        this.appLoginStorage = appLoginStorage;
        this.meIdStorage = meIdStorage;
        this.meIdSignatureStorage = meIdSignatureStorage;
        this.timestampProvider = timestampProvider;
    }

    public String getApplicationCode() {
        return applicationCode;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public AppLoginStorage getAppLoginStorage() {
        return appLoginStorage;
    }

    public MeIdStorage getMeIdStorage() {
        return meIdStorage;
    }

    public MeIdSignatureStorage getMeIdSignatureStorage() {
        return meIdSignatureStorage;
    }

    public TimestampProvider getTimestampProvider() {
        return timestampProvider;
    }

}