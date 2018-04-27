package com.emarsys.mobileengage;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.activity.ActivityLifecycleAction;
import com.emarsys.core.activity.ActivityLifecycleWatchdog;
import com.emarsys.core.activity.CurrentActivityWatchdog;
import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.core.connection.ConnectionWatchDog;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.database.repository.log.LogRepository;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.request.model.RequestModelRepository;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.core.worker.DefaultWorker;
import com.emarsys.core.worker.Worker;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.deeplink.DeepLinkAction;
import com.emarsys.mobileengage.deeplink.DeepLinkInternal;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.experimental.FlipperFeature;
import com.emarsys.mobileengage.experimental.MobileEngageExperimental;
import com.emarsys.mobileengage.experimental.MobileEngageFeature;
import com.emarsys.mobileengage.iam.DoNotDisturbProvider;
import com.emarsys.mobileengage.iam.InAppStartAction;
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider;
import com.emarsys.mobileengage.iam.jsbridge.InAppMessageHandlerProvider;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedRepository;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIamRepository;
import com.emarsys.mobileengage.iam.model.requestRepositoryProxy.RequestRepositoryProxy;
import com.emarsys.mobileengage.iam.webview.IamWebViewProvider;
import com.emarsys.mobileengage.inbox.InboxInternal;
import com.emarsys.mobileengage.inbox.InboxInternalProvider;
import com.emarsys.mobileengage.inbox.InboxResultListener;
import com.emarsys.mobileengage.inbox.ResetBadgeCountResultListener;
import com.emarsys.mobileengage.inbox.model.Notification;
import com.emarsys.mobileengage.inbox.model.NotificationInboxStatus;
import com.emarsys.mobileengage.log.LogRepositoryProxy;
import com.emarsys.mobileengage.log.handler.IamMetricsLogHandler;
import com.emarsys.mobileengage.responsehandler.AbstractResponseHandler;
import com.emarsys.mobileengage.responsehandler.InAppCleanUpResponseHandler;
import com.emarsys.mobileengage.responsehandler.InAppMessageResponseHandler;
import com.emarsys.mobileengage.responsehandler.MeIdResponseHandler;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.util.RequestHeaderUtils;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobileEngage {

    static MobileEngageInternal instance;
    static InboxInternal inboxInstance;
    static DeepLinkInternal deepLinkInstance;
    static MobileEngageCoreCompletionHandler completionHandler;
    static Handler coreSdkHandler;
    static MobileEngageConfig config;
    static RequestContext requestContext;
    private static Handler uiHandler;
    private static TimestampProvider timestampProvider;
    private static DoNotDisturbProvider doNotDisturbProvider;
    private static AppLoginStorage appLoginStorage;
    private static MeIdStorage meIdStorage;
    private static MeIdSignatureStorage meIdSignatureStorage;
    private static DeviceInfo deviceInfo;
    private static RequestManager requestManager;
    private static ButtonClickedRepository buttonClickedRepository;
    private static DisplayedIamRepository displayedIamRepository;
    private static Repository<RequestModel, SqlSpecification> requestModelRepository;
    private static RestClient restClient;
    private static Repository<Map<String, Object>, SqlSpecification> logRepositoryProxy;

    public static class Inbox {

        public static void fetchNotifications(@NonNull InboxResultListener<NotificationInboxStatus> resultListener) {
            Assert.notNull(resultListener, "ResultListener must not be null!");
            inboxInstance.fetchNotifications(resultListener);
        }

        public static void resetBadgeCount() {
            resetBadgeCount(null);
        }

        public static void resetBadgeCount(@Nullable ResetBadgeCountResultListener resultListener) {
            inboxInstance.resetBadgeCount(resultListener);
        }

        public static String trackMessageOpen(Notification message) {
            return inboxInstance.trackMessageOpen(message);
        }

    }

    public static class InApp {

        private static boolean enabled;

        public static void setPaused(boolean enabled) {
            InApp.enabled = enabled;
        }

        public static boolean isPaused() {
            return InApp.enabled;
        }

    }

    public static void setup(@NonNull MobileEngageConfig config) {
        Assert.notNull(config, "Config must not be null!");
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Argument: %s", config);

        for (FlipperFeature feature : config.getExperimentalFeatures()) {
            MobileEngageExperimental.enableFeature(feature);
        }

        MobileEngage.config = config;
        Application application = config.getApplication();

        initializeDependencies(config, application);

        initializeInstances(config);

        initializeInApp();

        registerResponseHandlers();

        registerApplicationLifecycleWatchdog(application);

        registerCurrentActivityWatchdog(application);

        MobileEngageUtils.setup(config);
    }

    public static MobileEngageConfig getConfig() {
        return config;
    }

    public static void setPushToken(String pushToken) {
        instance.setPushToken(pushToken);
    }

    public static void setStatusListener(MobileEngageStatusListener listener) {
        completionHandler.setStatusListener(listener);
    }

    public static String appLogin() {
        requestContext.setAppLoginParameters(new AppLoginParameters());
        return instance.appLogin();
    }

    public static String appLogin(int contactFieldId,
                                  @NonNull String contactFieldValue) {
        Assert.notNull(contactFieldValue, "ContactFieldValue must not be null!");
        requestContext.setAppLoginParameters(new AppLoginParameters(contactFieldId, contactFieldValue));
        return instance.appLogin();
    }

    public static String appLogout() {
        requestContext.setAppLoginParameters(null);
        return instance.appLogout();
    }

    public static String trackCustomEvent(@NonNull String eventName,
                                          @Nullable Map<String, String> eventAttributes) {
        Assert.notNull(eventName, "EventName must not be null!");
        return instance.trackCustomEvent(eventName, eventAttributes);
    }

    public static String trackMessageOpen(@NonNull Intent intent) {
        Assert.notNull(intent, "Intent must not be null!");
        return instance.trackMessageOpen(intent);
    }

    public static void trackDeepLink(@NonNull Activity activity, @NonNull Intent intent) {
        Assert.notNull(activity, "Activity must not be null!");
        Assert.notNull(activity.getIntent(), "Intent from Activity must not be null!");
        Assert.notNull(intent, "Intent must not be null!");
        deepLinkInstance.trackDeepLinkOpen(activity, intent);
    }

    private static void initializeDependencies(MobileEngageConfig config, Application application) {
        uiHandler = new Handler(Looper.getMainLooper());
        coreSdkHandler = new CoreSdkHandlerProvider().provideHandler();
        timestampProvider = new TimestampProvider();
        doNotDisturbProvider = new DoNotDisturbProvider();
        appLoginStorage = new AppLoginStorage(application);
        meIdStorage = new MeIdStorage(application);
        meIdSignatureStorage = new MeIdSignatureStorage(application);
        deviceInfo = new DeviceInfo(application);
        buttonClickedRepository = new ButtonClickedRepository(application);
        displayedIamRepository = new DisplayedIamRepository(application);
        completionHandler = new MobileEngageCoreCompletionHandler(config.getStatusListener());

        requestModelRepository = createRequestModelRepository(application);

        Repository<Map<String, Object>, SqlSpecification> logRepository = new LogRepository(application);
        List<com.emarsys.core.handler.Handler<Map<String, Object>, Map<String, Object>>> logHandlers = Arrays.<com.emarsys.core.handler.Handler<Map<String, Object>, Map<String, Object>>>asList(
                new IamMetricsLogHandler(new HashMap<String, Map<String, Object>>())
        );
        logRepositoryProxy = new LogRepositoryProxy(logRepository, logHandlers);
        restClient = new RestClient(logRepositoryProxy, timestampProvider);

        ConnectionWatchDog connectionWatchDog = new ConnectionWatchDog(application, coreSdkHandler);
        Worker worker = new DefaultWorker(
                requestModelRepository,
                connectionWatchDog,
                coreSdkHandler,
                completionHandler,
                restClient);

        requestManager = new RequestManager(
                coreSdkHandler,
                requestModelRepository,
                worker);
        requestManager.setDefaultHeaders(RequestHeaderUtils.createDefaultHeaders(config));

        requestContext = new RequestContext(
                config.getApplicationCode(),
                deviceInfo,
                appLoginStorage,
                meIdStorage,
                meIdSignatureStorage,
                timestampProvider);
    }

    private static void initializeInstances(@NonNull MobileEngageConfig config) {
        instance = new MobileEngageInternal(
                config,
                requestManager,
                uiHandler,
                completionHandler,
                requestContext
        );
        inboxInstance = new InboxInternalProvider().provideInboxInternal(
                MobileEngageExperimental.isFeatureEnabled(MobileEngageFeature.USER_CENTRIC_INBOX),
                config,
                requestManager,
                restClient,
                deviceInfo,
                requestContext
        );
        deepLinkInstance = new DeepLinkInternal(requestManager);
    }

    private static void initializeInApp() {
        InApp.setPaused(false);
    }

    private static void registerResponseHandlers() {
        List<AbstractResponseHandler> responseHandlers = new ArrayList<>();
        if (MobileEngageExperimental.isFeatureEnabled(MobileEngageFeature.IN_APP_MESSAGING) ||
                MobileEngageExperimental.isFeatureEnabled(MobileEngageFeature.USER_CENTRIC_INBOX)) {
            responseHandlers.add(new MeIdResponseHandler(
                    meIdStorage,
                    meIdSignatureStorage));
        }

        if (MobileEngageExperimental.isFeatureEnabled(MobileEngageFeature.IN_APP_MESSAGING)) {
            responseHandlers.add(new InAppMessageResponseHandler(
                    coreSdkHandler,
                    new IamWebViewProvider(),
                    new InAppMessageHandlerProvider(),
                    new IamDialogProvider(),
                    buttonClickedRepository,
                    displayedIamRepository,
                    logRepositoryProxy,
                    timestampProvider,
                    instance));
            responseHandlers.add(new InAppCleanUpResponseHandler(
                    displayedIamRepository,
                    buttonClickedRepository
            ));
        }

        completionHandler.addResponseHandlers(responseHandlers);
    }

    private static void registerCurrentActivityWatchdog(Application application) {
        CurrentActivityWatchdog.registerApplication(application);
    }

    private static void registerApplicationLifecycleWatchdog(Application application) {
        ActivityLifecycleAction[] applicationStartActions = null;
        if (MobileEngageExperimental.isFeatureEnabled(MobileEngageFeature.IN_APP_MESSAGING)) {
            applicationStartActions = new ActivityLifecycleAction[]{
                    new InAppStartAction(instance)
            };
        }

        ActivityLifecycleAction[] activityCreatedActions = new ActivityLifecycleAction[]{
                new DeepLinkAction(deepLinkInstance)
        };

        application.registerActivityLifecycleCallbacks(new ActivityLifecycleWatchdog(
                applicationStartActions,
                activityCreatedActions));
    }

    private static Repository<RequestModel, SqlSpecification> createRequestModelRepository(Context application) {
        RequestModelRepository requestModelRepository = new RequestModelRepository(application);
        if (MobileEngageExperimental.isFeatureEnabled(MobileEngageFeature.IN_APP_MESSAGING) ||
                MobileEngageExperimental.isFeatureEnabled(MobileEngageFeature.USER_CENTRIC_INBOX)) {
            return new RequestRepositoryProxy(
                    deviceInfo,
                    requestModelRepository,
                    displayedIamRepository,
                    buttonClickedRepository,
                    timestampProvider,
                    doNotDisturbProvider);
        } else {
            return requestModelRepository;
        }
    }

}
