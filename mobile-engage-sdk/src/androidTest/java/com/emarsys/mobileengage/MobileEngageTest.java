package com.emarsys.mobileengage;

import android.app.Application;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.emarsys.core.activity.ApplicationStartAction;
import com.emarsys.core.activity.ApplicationStartWatchdog;
import com.emarsys.core.activity.CurrentActivityWatchdog;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModelRepository;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.experimental.MobileEngageExperimental;
import com.emarsys.mobileengage.experimental.MobileEngageFeature;
import com.emarsys.mobileengage.fake.FakeRequestManager;
import com.emarsys.mobileengage.fake.FakeStatusListener;
import com.emarsys.mobileengage.iam.InAppStartAction;
import com.emarsys.mobileengage.iam.model.requestRepositoryProxy.RequestRepositoryProxy;
import com.emarsys.mobileengage.inbox.InboxInternal;
import com.emarsys.mobileengage.inbox.InboxResultListener;
import com.emarsys.mobileengage.inbox.ResetBadgeCountResultListener;
import com.emarsys.mobileengage.inbox.model.Notification;
import com.emarsys.mobileengage.responsehandler.AbstractResponseHandler;
import com.emarsys.mobileengage.responsehandler.InAppCleanUpResponseHandler;
import com.emarsys.mobileengage.responsehandler.InAppMessageResponseHandler;
import com.emarsys.mobileengage.responsehandler.MeIdResponseHandler;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.testUtil.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.emarsys.mobileengage.fake.FakeRequestManager.ResponseType.SUCCESS;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(AndroidJUnit4.class)
public class MobileEngageTest {

    static {
        mock(Application.class);
        mock(Intent.class);
    }

    private static final String appID = "56789876";
    private static final String appSecret = "secret";

    private MobileEngageCoreCompletionHandler coreCompletionHandler;
    private MobileEngageInternal mobileEngageInternal;
    private InboxInternal inboxInternal;
    private Application application;
    private MobileEngageConfig baseConfig;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() throws Exception {
        MobileEngageExperimental.enableFeature(MobileEngageFeature.IN_APP_MESSAGING);

        DatabaseTestUtils.deleteMobileEngageDatabase();
        DatabaseTestUtils.deleteCoreDatabase();

        application = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        coreCompletionHandler = mock(MobileEngageCoreCompletionHandler.class);
        mobileEngageInternal = mock(MobileEngageInternal.class);
        inboxInternal = mock(InboxInternal.class);
        baseConfig = new MobileEngageConfig.Builder()
                .application(application)
                .credentials(appID, appSecret)
                .disableDefaultChannel()
                .build();
        MobileEngage.inboxInstance = inboxInternal;
        MobileEngage.instance = mobileEngageInternal;
        MobileEngage.completionHandler = coreCompletionHandler;

        CurrentActivityWatchdogTestUtils.resetCurrentActivityWatchdog();
    }

    @After
    public void tearDown() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();
    }

    @Test
    public void testSetup_initializesMobileEngageInstance() {
        MobileEngage.instance = null;
        MobileEngage.setup(baseConfig);

        assertNotNull(MobileEngage.instance);
    }

    @Test
    public void testSetup_initializesCoreCompletionHandler_withMeIdResponseHandler() {
        MobileEngage.completionHandler = null;
        MobileEngage.setup(baseConfig);

        MobileEngageCoreCompletionHandler coreCompletionHandler = MobileEngage.completionHandler;
        assertNotNull(coreCompletionHandler);
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(coreCompletionHandler.responseHandlers, MeIdResponseHandler.class));
    }

    @Test
    public void testSetup_whenInAppMessagingFlipperIsOff_initializesCoreCompletionHandler_withoutMeIdResponseHandler() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();

        MobileEngage.completionHandler = null;
        MobileEngage.setup(baseConfig);

        MobileEngageCoreCompletionHandler coreCompletionHandler = MobileEngage.completionHandler;
        assertNotNull(coreCompletionHandler);
        assertEquals(0, CollectionTestUtils.numberOfElementsIn(coreCompletionHandler.responseHandlers, MeIdResponseHandler.class));
    }

    @Test
    public void testSetup_initializesCoreCompletionHandler_withInAppMessageResponseHandler() {
        MobileEngage.completionHandler = null;
        MobileEngage.setup(baseConfig);

        MobileEngageCoreCompletionHandler coreCompletionHandler = MobileEngage.completionHandler;
        assertNotNull(coreCompletionHandler);
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(coreCompletionHandler.responseHandlers, InAppMessageResponseHandler.class));
    }

    @Test
    public void testSetup_whenInAppMessagingFlipperIsOff_initializesCoreCompletionHandler_withoutInAppMessageResponseHandler() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();

        MobileEngage.completionHandler = null;
        MobileEngage.setup(baseConfig);

        MobileEngageCoreCompletionHandler coreCompletionHandler = MobileEngage.completionHandler;
        assertNotNull(coreCompletionHandler);
        assertEquals(0, CollectionTestUtils.numberOfElementsIn(coreCompletionHandler.responseHandlers, InAppMessageResponseHandler.class));
    }

    @Test
    public void testSetup_initializesCoreCompletionHandler_withInAppCleanUpResponseHandler() {
        MobileEngage.completionHandler = null;
        MobileEngage.setup(baseConfig);

        MobileEngageCoreCompletionHandler coreCompletionHandler = MobileEngage.completionHandler;
        assertNotNull(coreCompletionHandler);
        assertEquals(1, CollectionTestUtils.numberOfElementsIn(coreCompletionHandler.responseHandlers, InAppCleanUpResponseHandler.class));
    }

    @Test
    public void testSetup_whenInAppMessagingFlipperIsOff_initializesCoreCompletionHandler_withoutInAppCleanUpResponseHandler() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();

        MobileEngage.completionHandler = null;
        MobileEngage.setup(baseConfig);

        MobileEngageCoreCompletionHandler coreCompletionHandler = MobileEngage.completionHandler;
        assertNotNull(coreCompletionHandler);
        assertEquals(0, CollectionTestUtils.numberOfElementsIn(coreCompletionHandler.responseHandlers, InAppCleanUpResponseHandler.class));
    }

    @Test
    public void testSetup_initializesRequestManager_withRequestModelProxy() throws Exception {
        MobileEngage.setup(baseConfig);

        Field repositoryField = RequestManager.class.getDeclaredField("requestRepository");
        repositoryField.setAccessible(true);
        Object repository = repositoryField.get(MobileEngage.instance.manager);
        assertEquals(repository.getClass(), RequestRepositoryProxy.class);
    }

    @Test
    public void testSetup_whenFlipperIsOff_initializesRequestManager_withPlainRequestModelRepository() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();
        MobileEngage.setup(baseConfig);

        Field repositoryField = RequestManager.class.getDeclaredField("requestRepository");
        repositoryField.setAccessible(true);
        Object repository = repositoryField.get(MobileEngage.instance.manager);
        assertEquals(repository.getClass(), RequestModelRepository.class);
    }

    @Test
    public void testSetup_initializesInboxInstance() {
        MobileEngage.inboxInstance = null;
        MobileEngage.setup(baseConfig);

        assertNotNull(MobileEngage.inboxInstance);
    }

    @Test
    public void testSetup_registers_currentActivityWatchDog() throws Exception {
        CurrentActivityWatchdogTestUtils.resetCurrentActivityWatchdog();

        MobileEngage.setup(baseConfig);

        try {
            CurrentActivityWatchdog.getCurrentActivity();
        } catch (Exception e) {
            fail("getCurrentActivity should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testSetup_doesNotRegister_applicationStartWatchDog_ifIamFlipperIsTurnedOff() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();

        MobileEngageConfig config = createConfigWithSpyApplication();
        Application spyApplication = config.getApplication();

        MobileEngage.setup(config);

        verify(spyApplication, times(0))
                .registerActivityLifecycleCallbacks(
                        any(ApplicationStartWatchdog.class));
    }

    @Test
    public void testSetup_registers_applicationStartWatchDog() throws Exception {
        MobileEngageConfig config = createConfigWithSpyApplication();
        Application spyApplication = config.getApplication();

        MobileEngage.setup(config);

        verify(spyApplication).registerActivityLifecycleCallbacks(any(ApplicationStartWatchdog.class));
    }

    @Test
    public void testSetup_registers_applicationStartWatchDog_withInAppStartAction() throws Exception {
        ArgumentCaptor<ApplicationStartWatchdog> captor = ArgumentCaptor.forClass(ApplicationStartWatchdog.class);

        MobileEngageConfig config = createConfigWithSpyApplication();
        Application spyApplication = config.getApplication();

        MobileEngage.setup(config);

        verify(spyApplication, Mockito.atLeastOnce()).registerActivityLifecycleCallbacks(captor.capture());
        ApplicationStartAction[] actions = captor.getValue().getApplicationStartActions();

        assertEquals(1, CollectionTestUtils.numberOfElementsIn(actions, InAppStartAction.class));
    }

    @Test
    public void testSetup_initializesWithConfig() {
        MobileEngage.config = null;
        MobileEngage.setup(baseConfig);

        assertEquals(baseConfig, MobileEngage.getConfig());
    }

    @Test
    public void testSetup_initializesMobileEngageUtils() {
        MobileEngageConfig disabled = new MobileEngageConfig.Builder()
                .from(baseConfig)
                .enableIdlingResource(false)
                .build();

        MobileEngageConfig enabled = new MobileEngageConfig.Builder()
                .from(baseConfig)
                .enableIdlingResource(true)
                .build();

        MobileEngageUtils.setup(disabled);
        assertNull(MobileEngageUtils.getIdlingResource());

        MobileEngage.setup(enabled);
        assertNotNull(MobileEngageUtils.getIdlingResource());
    }

    @Test
    public void testSetPushToken_callsInternal() {
        String pushtoken = "pushtoken";
        MobileEngage.setPushToken(pushtoken);
        verify(mobileEngageInternal).setPushToken(pushtoken);
    }

    @Test
    public void testSetStatusListener_callsInternal() {
        MobileEngageStatusListener listener = mock(MobileEngageStatusListener.class);
        MobileEngage.setStatusListener(listener);
        verify(coreCompletionHandler).setStatusListener(listener);
    }

    @Test
    public void testSetStatusListener_shouldSwapListener() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        FakeStatusListener originalListener = new FakeStatusListener();
        FakeStatusListener newListener = new FakeStatusListener(latch);

        MobileEngageCoreCompletionHandler completionHandler = new MobileEngageCoreCompletionHandler(new ArrayList<AbstractResponseHandler>(), originalListener);
        RequestManager succeedingManager = new FakeRequestManager(
                SUCCESS,
                null,
                completionHandler);
        AppLoginStorage storage = new AppLoginStorage(application);
        MobileEngageInternal internal = new MobileEngageInternal(baseConfig, succeedingManager, storage, completionHandler);

        MobileEngage.completionHandler = completionHandler;
        MobileEngage.instance = internal;

        MobileEngage.setStatusListener(newListener);
        MobileEngage.appLogin();

        latch.await();

        assertEquals(0, originalListener.onStatusLogCount);
        assertEquals(0, originalListener.onErrorCount);
        assertEquals(1, newListener.onStatusLogCount);
        assertEquals(0, newListener.onErrorCount);
    }

    @Test
    public void testAppLogin_anonymous_callsInternalMobileEngage() {
        MobileEngage.appLogin();
        verify(mobileEngageInternal).appLogin();
    }

    @Test
    public void testAppLogin_anonymous_callsSetAppLoginParameters_onInternalInbox() {
        MobileEngage.appLogin();
        verify(inboxInternal).setAppLoginParameters(new AppLoginParameters());
    }

    @Test
    public void testAppLogin_anonymous_callsSetAppLoginParameters_onInternalMobileEngage() {
        MobileEngage.appLogin();
        verify(mobileEngageInternal).setAppLoginParameters(new AppLoginParameters());
    }

    @Test
    public void testAppLogin_withUser_callsInternalMobileEngage() {
        MobileEngage.appLogin(4, "CONTACT_FIELD_VALUE");

        verify(mobileEngageInternal).setAppLoginParameters(new AppLoginParameters(4, "CONTACT_FIELD_VALUE"));
        verify(mobileEngageInternal).appLogin();
    }

    @Test
    public void testAppLogin_withUser_callsSetAppLoginParameters_onInternalInbox() {
        MobileEngage.appLogin(4, "CONTACT_FIELD_VALUE");
        verify(inboxInternal).setAppLoginParameters(new AppLoginParameters(4, "CONTACT_FIELD_VALUE"));
    }

    @Test
    public void testAppLogin_withUser_callsSetAppLoginParameters_onInternalMobileEngage() {
        MobileEngage.appLogin(4, "CONTACT_FIELD_VALUE");
        verify(mobileEngageInternal).setAppLoginParameters(new AppLoginParameters(4, "CONTACT_FIELD_VALUE"));
    }

    @Test
    public void testAppLogout_callsInternalMobileEngage() {
        MobileEngage.appLogout();
        verify(mobileEngageInternal).appLogout();
    }

    @Test
    public void testAppLogout_callsSetAppLoginParameters_onInternalInbox() {
        MobileEngage.appLogout();
        verify(inboxInternal).setAppLoginParameters(null);
    }

    @Test
    public void testAppLogout_callsSetAppLoginParameters_onInternalMobileEngage() {
        MobileEngage.appLogout();
        verify(mobileEngageInternal).setAppLoginParameters(null);
    }

    @Test
    public void testTrackCustomEvent_callsInternal() throws Exception {
        Map<String, String> attributes = mock(Map.class);
        MobileEngage.trackCustomEvent("event", attributes);
        verify(mobileEngageInternal).trackCustomEvent("event", attributes);
    }

    @Test
    public void testTrackMessageOpen_intent_callsInternal() {
        Intent intent = mock(Intent.class);
        MobileEngage.trackMessageOpen(intent);
        verify(mobileEngageInternal).trackMessageOpen(intent);
    }

    @Test
    public void testTrackMessageOpen_message_callsInternal() throws JSONException {
        Notification message = new Notification("id", "sid", "title", null, new HashMap<String, String>(), new JSONObject(), 7200, new Date().getTime());
        MobileEngage.Inbox.trackMessageOpen(message);
        verify(inboxInternal).trackMessageOpen(message);
    }

    @Test
    public void testTrackDeepLinkOpen_callsInternal() throws Exception {
        Intent intent = mock(Intent.class);
        MobileEngage.trackDeepLink(intent);
        verify(mobileEngageInternal).trackDeepLinkOpen(intent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackDeepLinkOpen_throwExceptionWhenIntentIsNull() throws Exception {
        MobileEngage.trackDeepLink(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetup_whenConfigIsNull() {
        MobileEngage.setup(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppLogin_whenContactFieldValueIsNull() {
        MobileEngage.appLogin(0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackCustomEvent_whenEventNameIsNull() {
        MobileEngage.trackCustomEvent(null, new HashMap<String, String>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackMessageOpen_intent_whenIntentIsNull() {
        MobileEngage.trackMessageOpen(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFetchNotifications_whenListenerIsNull() {
        MobileEngage.Inbox.fetchNotifications(null);
    }

    @Test
    public void testFetchNotifications_callsInternal() {
        InboxResultListener inboxListenerMock = mock(InboxResultListener.class);
        MobileEngage.Inbox.fetchNotifications(inboxListenerMock);
        verify(inboxInternal).fetchNotifications(inboxListenerMock);
    }

    @Test
    public void testResetBadgeCount_callsInternal() {
        ResetBadgeCountResultListener listener = mock(ResetBadgeCountResultListener.class);
        MobileEngage.Inbox.resetBadgeCount(listener);
        verify(inboxInternal).resetBadgeCount(listener);
    }

    @Test
    public void testResetBadgeCount_zeroArgs_callsInternal_withNullListener() {
        MobileEngage.Inbox.resetBadgeCount();
        verify(inboxInternal).resetBadgeCount(null);
    }

    private MobileEngageConfig createConfigWithSpyApplication() {
        return new MobileEngageConfig.Builder()
                .application(spy(application))
                .credentials(appID, appSecret)
                .disableDefaultChannel()
                .build();
    }
}