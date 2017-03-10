package com.emarsys.mobileengage;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.request.RequestManager;
import com.emarsys.mobileengage.fake.FakeRequestManager;
import com.emarsys.mobileengage.fake.FakeStatusListener;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.emarsys.mobileengage.fake.FakeRequestManager.ResponseType.FAILURE;
import static com.emarsys.mobileengage.fake.FakeRequestManager.ResponseType.SUCCESS;
import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MobileEngageInternalStatusListenerTest {

    public static final String EVENT_NAME = "event";
    public static final String NOT_CALLED_ON_UI_THREAD = "Not called on UI thread";
    public static final int TIME = 500;
    private static String APPLICATION_ID = "user";
    private static String APPLICATION_SECRET = "pass";
    private static final long TIMEOUT = 1000;
    private static final int CONTACT_FIELD_ID = 3456;
    public static final String CONTACT_FIELD_VALUE = "value";

    private MobileEngageInternal mobileEngage;
    private MobileEngageStatusListener statusListener;
    private FakeStatusListener mainThreadStatusListener;
    private Map<String, String> authHeader;
    private MobileEngageConfig baseConfig;
    private RequestManager manager;
    private RequestManager failingManager;
    private RequestManager succeedingManager;
    private Application application;
    private Context context;
    private CountDownLatch managerLatch;
    private CountDownLatch statusListenerLatch;
    private Intent intent;

    @Before
    public void init() throws Exception {
        authHeader = new HashMap<>();
        authHeader.put("Authorization", "Basic dXNlcjpwYXNz");
        context = InstrumentationRegistry.getTargetContext();
        application = mock(Application.class);
        when(application.getApplicationContext()).thenReturn(context);

        intent = new Intent();
        JSONObject json = new JSONObject()
                .put("key1", "value1")
                .put("u", "{\"sid\": \"+43c_lODSmXqCvdOz\"}");
        intent.putExtra("pw_data_json_string", json.toString());

        manager = mock(RequestManager.class);
        managerLatch = new CountDownLatch(1);
        statusListenerLatch = new CountDownLatch(1);
        succeedingManager = new FakeRequestManager(SUCCESS, managerLatch);
        failingManager = new FakeRequestManager(FAILURE, managerLatch);

        statusListener = mock(MobileEngageStatusListener.class);
        mainThreadStatusListener = new FakeStatusListener();
        mobileEngageWith(statusListener, succeedingManager);
    }

    @Test(timeout = TIMEOUT)
    public void testAppLogin_anonymus_statusListenerCalledWithSuccess() throws Exception {
        eventuallyAssertSuccess(mobileEngage.appLogin());
    }

    @Test(timeout = TIMEOUT)
    public void testAppLogin_statusListenerCalledWithSuccess() throws Exception {
        eventuallyAssertSuccess(mobileEngage.appLogin(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE));
    }

    @Test(timeout = TIMEOUT)
    public void testAppLogout_statusListenerCalledWithSuccess() throws Exception {
        eventuallyAssertSuccess(mobileEngage.appLogout());
    }

    @Test(timeout = TIMEOUT)
    public void testTrackCustomEvent_statusListenerCalledWithSuccess() throws Exception {
        eventuallyAssertSuccess(mobileEngage.trackCustomEvent(EVENT_NAME, null));
    }

    @Test(timeout = TIMEOUT)
    public void testTrackMessageOpen_statusListenerCalledWithSuccess() throws Exception {
        eventuallyAssertSuccess(mobileEngage.trackMessageOpen(intent));
    }

    @Test(timeout = TIMEOUT)
    public void testAppLogin_anonymus_statusListenerCalledWithFailure() throws Exception {
        mobileEngageWith(statusListener, failingManager);
        eventuallyAssertFailure(mobileEngage.appLogin());
    }

    @Test(timeout = TIMEOUT)
    public void testAppLogin_statusListenerCalledWithFailure() throws Exception {
        mobileEngageWith(statusListener, failingManager);
        eventuallyAssertFailure(mobileEngage.appLogin(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE));
    }

    @Test(timeout = TIMEOUT)
    public void testAppLogout_statusListenerCalledWithFailure() throws Exception {
        mobileEngageWith(statusListener, failingManager);
        eventuallyAssertFailure(mobileEngage.appLogout());
    }

    @Test(timeout = TIMEOUT)
    public void testTrackCustomEvent_statusListenerCalledWithFailure() throws Exception {
        mobileEngageWith(statusListener, failingManager);
        eventuallyAssertFailure(mobileEngage.trackCustomEvent(EVENT_NAME, null));
    }

    @Test(timeout = TIMEOUT)
    public void testTrackMessageOpen_statusListenerCalledWithFailure() throws Exception {
        mobileEngageWith(statusListener, failingManager);
        eventuallyAssertFailure(mobileEngage.trackMessageOpen(intent));
    }

    @Test(timeout = TIMEOUT)
    public void testTrackMessageOpen_statusListenerCalledWithFailure_whenIntentIsNull() throws Exception {
        mainThreadStatusListener.latch = statusListenerLatch;
        mobileEngageWith(mainThreadStatusListener, manager);

        mobileEngage.trackMessageOpen(null);

        statusListenerLatch.await();
        assertEquals(0, mainThreadStatusListener.onStatusLogCount);
        assertEquals(1, mainThreadStatusListener.onErrorCount);
    }

    @Test(timeout = TIMEOUT)
    public void testAppLogin_anonymus_statusListenerCalledOnMainThread_Success() throws Exception {
        mobileEngageWith(mainThreadStatusListener, succeedingManager);
        mobileEngage.appLogin();
        eventuallyAssertSuccess();
    }

    @Test(timeout = TIMEOUT)
    public void testAppLogin_statusListenerCalledOnMainThread_Success() throws Exception {
        mobileEngageWith(mainThreadStatusListener, succeedingManager);
        mobileEngage.appLogin(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE);
        eventuallyAssertSuccess();
    }

    @Test(timeout = TIMEOUT)
    public void testAppLogout_statusListenerCalledOnMainThread_Success() throws Exception {
        mobileEngageWith(mainThreadStatusListener, succeedingManager);
        mobileEngage.appLogout();
        eventuallyAssertSuccess();
    }

    @Test(timeout = TIMEOUT)
    public void testTrackCustomEvent_statusListenerCalledOnMainThread_Success() throws Exception {
        mobileEngageWith(mainThreadStatusListener, succeedingManager);
        mobileEngage.trackCustomEvent(EVENT_NAME, null);
        eventuallyAssertSuccess();
    }

    @Test(timeout = TIMEOUT)
    public void testTrackMessagerOpen_statusListenerCalledOnMainThread_Success() throws Exception {
        mobileEngageWith(mainThreadStatusListener, succeedingManager);
        mobileEngage.trackMessageOpen(intent);
        eventuallyAssertSuccess();
    }

    @Test(timeout = TIMEOUT)
    public void testAppLogin_anonymus_statusListenerCalledOnMainThread_Failure() throws Exception {
        mobileEngageWith(mainThreadStatusListener, failingManager);
        mobileEngage.appLogin();
        eventuallyAssertFailure();
    }

    @Test(timeout = TIMEOUT)
    public void testAppLogin_statusListenerCalledOnMainThread_Failure() throws Exception {
        mobileEngageWith(mainThreadStatusListener, failingManager);
        mobileEngage.appLogin(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE);
        eventuallyAssertFailure();
    }

    @Test(timeout = TIMEOUT)
    public void testAppLogout_statusListenerCalledOnMainThread_Failure() throws Exception {
        mobileEngageWith(mainThreadStatusListener, failingManager);
        mobileEngage.appLogout();
        eventuallyAssertFailure();
    }

    @Test(timeout = TIMEOUT)
    public void testTrackCustomEvent_statusListenerCalledOnMainThread_Failure() throws Exception {
        mobileEngageWith(mainThreadStatusListener, failingManager);
        mobileEngage.trackCustomEvent(EVENT_NAME, null);
        eventuallyAssertFailure();
    }

    @Test(timeout = TIMEOUT)
    public void testTrackMessageOpen_statusListenerCalledOnMainThread_Failure() throws Exception {
        mobileEngageWith(mainThreadStatusListener, failingManager);
        mobileEngage.trackMessageOpen(intent);
        eventuallyAssertFailure();
    }

    @Test(timeout = TIMEOUT)
    public void testTrackMessageOpen_statusListenerCalledOnMainThread_FailureWithIntentNull() throws Exception {
        mainThreadStatusListener.latch = statusListenerLatch;
        mobileEngageWith(mainThreadStatusListener, manager);

        mobileEngage.trackMessageOpen(null);

        statusListenerLatch.await();
        assertEquals(0, mainThreadStatusListener.onStatusLogCount);
        assertEquals(1, mainThreadStatusListener.onErrorCount);
    }

    @Test(timeout = TIMEOUT)
    public void testTrackMessageOpen_returnsNullWithEmptyIntent() throws Exception {
        mainThreadStatusListener.latch = statusListenerLatch;
        mobileEngageWith(mainThreadStatusListener, manager);

        String expected = mobileEngage.trackMessageOpen(new Intent());

        statusListenerLatch.await();
        assertEquals(expected, mainThreadStatusListener.errorId);
    }

    @Test(timeout = TIMEOUT)
    public void testTrackMessageOpen_returnsNullWithNullIntent() throws Exception {
        mainThreadStatusListener.latch = statusListenerLatch;
        mobileEngageWith(mainThreadStatusListener, manager);

        String expected = mobileEngage.trackMessageOpen(null);

        statusListenerLatch.await();
        assertEquals(expected, mainThreadStatusListener.errorId);
    }

    private void mobileEngageWith(MobileEngageStatusListener statusListener, RequestManager requestManager) {
        baseConfig = new MobileEngageConfig.Builder()
                .credentials(APPLICATION_ID, APPLICATION_SECRET)
                .statusListener(statusListener)
                .build();
        mobileEngage = new MobileEngageInternal(application, baseConfig, requestManager);
    }

    private void eventuallyAssertSuccess() throws Exception {
        managerLatch.await();
        assertEquals(1, mainThreadStatusListener.onStatusLogCount);
        assertEquals(0, mainThreadStatusListener.onErrorCount);
    }

    private void eventuallyAssertFailure() throws Exception {
        managerLatch.await();
        assertEquals(0, mainThreadStatusListener.onStatusLogCount);
        assertEquals(1, mainThreadStatusListener.onErrorCount);
    }

    private void eventuallyAssertSuccess(String expected) throws InterruptedException {
        managerLatch.await();
        verify(statusListener).onStatusLog(eq(expected), any(String.class));
    }

    private void eventuallyAssertFailure(String expected) throws InterruptedException {
        managerLatch.await();
        verify(statusListener).onError(eq(expected), any(Exception.class));
    }
}