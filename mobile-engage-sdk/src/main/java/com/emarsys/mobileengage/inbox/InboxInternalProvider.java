package com.emarsys.mobileengage.inbox;

import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.RestClient;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.storage.MeIdStorage;

public class InboxInternalProvider {

    public InboxInternal provideInboxInternal(boolean experimental,
                                              MobileEngageConfig config,
                                              RequestManager requestManager,
                                              RestClient restClient,
                                              MeIdStorage meIdStorage) {
        InboxInternal result;
        if (experimental) {
            result = new InboxInternal_V2(config, requestManager, restClient, meIdStorage);
        } else {
            result = new InboxInternal_V1(config, requestManager, restClient);
        }
        return result;
    }

}
