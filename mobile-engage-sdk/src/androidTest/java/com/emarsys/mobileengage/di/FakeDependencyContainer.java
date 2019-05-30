package com.emarsys.mobileengage.di;

import android.os.Handler;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.activity.ActivityLifecycleWatchdog;
import com.emarsys.mobileengage.MobileEngageCoreCompletionHandler;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.deeplink.DeepLinkInternal;
import com.emarsys.mobileengage.iam.InAppPresenter;
import com.emarsys.mobileengage.inbox.InboxInternal;

public class FakeDependencyContainer implements DependencyContainer {
    private final Handler coreSdkHandler;
    private final ActivityLifecycleWatchdog activityLifecycleWatchdog;
    private final DeviceInfo deviceInfo;
    private final InboxInternal inboxInternal;
    private final DeepLinkInternal deepLinkInternal;
    private final RequestContext requestContext;
    private final InAppPresenter inAppPresenter;
    private final MobileEngageCoreCompletionHandler coreCompletionHandler;

    public FakeDependencyContainer(
            Handler coreSdkHandler,
            ActivityLifecycleWatchdog activityLifecycleWatchdog,
            DeviceInfo deviceInfo,
            InboxInternal inboxInternal,
            DeepLinkInternal deepLinkInternal,
            RequestContext requestContext,
            InAppPresenter inAppPresenter,
            MobileEngageCoreCompletionHandler coreCompletionHandler) {
        this.coreSdkHandler = coreSdkHandler;
        this.activityLifecycleWatchdog = activityLifecycleWatchdog;
        this.deviceInfo = deviceInfo;
        this.inboxInternal = inboxInternal;
        this.deepLinkInternal = deepLinkInternal;
        this.requestContext = requestContext;
        this.inAppPresenter = inAppPresenter;
        this.coreCompletionHandler = coreCompletionHandler;
    }

    @Override
    public MobileEngageInternal getMobileEngageInternal() {
        return null;
    }

    @Override
    public InboxInternal getInboxInternal() {
        return inboxInternal;
    }

    @Override
    public DeepLinkInternal getDeepLinkInternal() {
        return deepLinkInternal;
    }

    @Override
    public MobileEngageCoreCompletionHandler getCoreCompletionHandler() {
        return coreCompletionHandler;
    }

    @Override
    public Handler getCoreSdkHandler() {
        return coreSdkHandler;
    }

    @Override
    public RequestContext getRequestContext() {
        return requestContext;
    }

    @Override
    public ActivityLifecycleWatchdog getActivityLifecycleWatchdog() {
        return activityLifecycleWatchdog;
    }

    @Override
    public InAppPresenter getInAppPresenter() {
        return inAppPresenter;
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

}
