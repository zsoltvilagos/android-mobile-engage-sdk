package com.emarsys.mobileengage.deeplink;

import android.app.Activity;

import com.emarsys.core.activity.ActivityLifecycleAction;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.MobileEngageInternal;

public class DeepLinkAction implements ActivityLifecycleAction {

    private MobileEngageInternal mobileEngageInternal;

    public DeepLinkAction(MobileEngageInternal mobileEngageInternal) {
        Assert.notNull(mobileEngageInternal, "MobileEngageInternal must not be null!");
        this.mobileEngageInternal = mobileEngageInternal;
    }

    @Override
    public void execute(Activity activity) {
        if (activity != null && activity.getIntent() != null) {
            mobileEngageInternal.trackDeepLinkOpen(activity, activity.getIntent());
        }
    }

}
