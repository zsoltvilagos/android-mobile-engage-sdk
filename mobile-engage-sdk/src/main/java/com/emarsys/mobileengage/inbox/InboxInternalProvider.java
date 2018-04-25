package com.emarsys.mobileengage.inbox;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.RestClient;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.config.MobileEngageConfig;

public class InboxInternalProvider {

    public InboxInternal provideInboxInternal(boolean experimental,
                                              MobileEngageConfig config,
                                              RequestManager requestManager,
                                              RestClient restClient,
                                              DeviceInfo deviceInfo,
                                              RequestContext requestContext) {
        InboxInternal result;
        if (experimental) {
            result = new InboxInternal_V2(config, requestManager, restClient, requestContext);
        } else {
            result = new InboxInternal_V1(config, requestManager, restClient, deviceInfo, requestContext);
        }
        return result;
    }

}
