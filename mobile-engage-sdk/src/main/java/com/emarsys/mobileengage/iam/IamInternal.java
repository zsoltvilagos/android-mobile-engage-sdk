package com.emarsys.mobileengage.iam;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.iam.lifecycle.IamActivityLifecycleListener;

public class IamInternal {
    IamActivityLifecycleListener activityLifecycleListener;

    public IamInternal(MobileEngageConfig config) {
        Assert.notNull(config, "Config must not be null!");

        this.activityLifecycleListener = new IamActivityLifecycleListener();
        config.getApplication().registerActivityLifecycleCallbacks(activityLifecycleListener);
    }
}
