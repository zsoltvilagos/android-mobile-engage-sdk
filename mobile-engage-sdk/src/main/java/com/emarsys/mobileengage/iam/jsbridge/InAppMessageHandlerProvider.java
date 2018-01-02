package com.emarsys.mobileengage.iam.jsbridge;

import com.emarsys.mobileengage.MobileEngage;
import com.emarsys.mobileengage.iam.InAppMessageHandler;

public class InAppMessageHandlerProvider {

    public InAppMessageHandler provideHandler() {
        return MobileEngage.getConfig().getDefaultInAppMessageHandler();
    }

}
