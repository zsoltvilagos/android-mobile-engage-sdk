package com.emarsys.mobileengage;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.connection.ConnectionWatchDog;
import com.emarsys.core.queue.InMemoryQueue;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.mobileengage.fake.FakeRequestManager;
import com.emarsys.mobileengage.fake.FakeStatusListener;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.emarsys.mobileengage.fake.FakeRequestManager.ResponseType.FAILURE;
import static com.emarsys.mobileengage.fake.FakeRequestManager.ResponseType.SUCCESS;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MobileEngageInternalStatusListenerTest {

    public static final String EVENT_NAME = "event";
    private static String APPLICATION_ID = "user";
    private static String APPLICATION_SECRET = "pass";
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
    private CountDownLatch latch;
    private Intent intent;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Before
    public void init() throws Exception {
        authHeader = new HashMap<>();
        authHeader.put("Authorization", "Basic dXNlcjpwYXNz");
        context = InstrumentationRegistry.getTargetContext();
        application = mock(Application.class);
        when(application.getApplicationContext()).thenReturn(context);

        intent = new Intent();
        Bundle payload = new Bundle();
        payload.putString("key1", "value1");
        payload.putString("u", "{\"sid\": \"+43c_lODSmXqCvdOz\"}");
        intent.putExtra("payload", payload);

        manager = mock(RequestManager.class);
        latch = new CountDownLatch(1);
        CoreCompletionHandler completionHandler = new CoreCompletionHandler() {
            @Override
            public void onSuccess(String id, ResponseModel responseModel) {
                mainThreadStatusListener.onStatusLog(id, responseModel.getMessage());
            }

            @Override
            public void onError(String id, ResponseModel responseModel) {
                Exception exception = new MobileEngageException(
                        responseModel.getStatusCode(),
                        responseModel.getMessage(),
                        responseModel.getBody());
                mainThreadStatusListener.onError(id, exception);
            }

            @Override
            public void onError(String id, Exception cause) {
                mainThreadStatusListener.onError(id, cause);
            }
        };
        succeedingManager = new FakeRequestManager(SUCCESS, latch, new ConnectionWatchDog(context), new InMemoryQueue(), completionHandler);
        failingManager = new FakeRequestManager(FAILURE, latch, new ConnectionWatchDog(context), new InMemoryQueue(), completionHandler);
        statusListener = mock(MobileEngageStatusListener.class);
        mainThreadStatusListener = new FakeStatusListener(latch);
        mobileEngageWith(mainThreadStatusListener, succeedingManager);
    }

    private void mobileEngageWith(MobileEngageStatusListener statusListener, RequestManager requestManager) {
        baseConfig = new MobileEngageConfig.Builder()
                .credentials(APPLICATION_ID, APPLICATION_SECRET)
                .statusListener(statusListener)
                .build();
        mobileEngage = new MobileEngageInternal(application, baseConfig, requestManager);
    }

    @Test
    public void testAppLogin_anonymus_statusListenerCalledWithSuccess() throws Exception {
        eventuallyAssertSuccess(mobileEngage.appLogin());
    }

    @Test
    public void testAppLogin_statusListenerCalledWithSuccess() throws Exception {
        eventuallyAssertSuccess(mobileEngage.appLogin(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE));
    }

    @Test
    public void testAppLogout_statusListenerCalledWithSuccess() throws Exception {
        eventuallyAssertSuccess(mobileEngage.appLogout());
    }

    @Test
    public void testTrackCustomEvent_statusListenerCalledWithSuccess() throws Exception {
        eventuallyAssertSuccess(mobileEngage.trackCustomEvent(EVENT_NAME, null));
    }

    @Test
    public void testTrackMessageOpen_intent_statusListenerCalledWithSuccess() throws Exception {
        eventuallyAssertSuccess(mobileEngage.trackMessageOpen(intent));
    }

    @Test
    public void testAppLogin_anonymous_statusListenerCalledWithFailure() throws Exception {
        mobileEngageWith(mainThreadStatusListener, failingManager);
        eventuallyAssertFailure(mobileEngage.appLogin());
    }

    @Test
    public void testAppLogin_statusListenerCalledWithFailure() throws Exception {
        mobileEngageWith(mainThreadStatusListener, failingManager);
        eventuallyAssertFailure(mobileEngage.appLogin(CONTACT_FIELD_ID, CONTACT_FIELD_VALUE));
    }

    @Test
    public void testAppLogout_statusListenerCalledWithFailure() throws Exception {
        mobileEngageWith(mainThreadStatusListener, failingManager);
        eventuallyAssertFailure(mobileEngage.appLogout());
    }

    @Test
    public void testTrackCustomEvent_statusListenerCalledWithFailure() throws Exception {
        mobileEngageWith(mainThreadStatusListener, failingManager);
        eventuallyAssertFailure(mobileEngage.trackCustomEvent(EVENT_NAME, null));
    }

    @Test
    public void testTrackMessageOpen_intent_statusListenerCalledWithFailure() throws Exception {
        mobileEngageWith(mainThreadStatusListener, failingManager);
        eventuallyAssertFailure(mobileEngage.trackMessageOpen(intent));
    }

    @Test
    public void testTrackMessageOpen_intent_whenIntentIsEmpty() throws Exception {
        mobileEngageWith(mainThreadStatusListener, manager);
        eventuallyAssertFailure(mobileEngage.trackMessageOpen(new Intent()), IllegalArgumentException.class, "No messageId found!");
    }

    @Test
    public void testSetStatusListener_shouldOverridePreviousListener() throws Exception {
        FakeStatusListener originalListener = new FakeStatusListener();
        FakeStatusListener newListener = new FakeStatusListener(latch);
        mobileEngageWith(originalListener, succeedingManager);

        mobileEngage.setStatusListener(newListener);
        mobileEngage.appLogin();

        latch.await();
        assertEquals(0, originalListener.onStatusLogCount);
        assertEquals(0, originalListener.onErrorCount);
    }

    private void eventuallyAssertSuccess(String expectedId) throws Exception {
        latch.await();
        assertEquals(1, mainThreadStatusListener.onStatusLogCount);
        assertEquals(0, mainThreadStatusListener.onErrorCount);
        assertEquals(expectedId, mainThreadStatusListener.successId);
        assertEquals("OK", mainThreadStatusListener.successLog);
        assertNull(mainThreadStatusListener.errorId);
        assertNull(mainThreadStatusListener.errorCause);
    }

    private void eventuallyAssertFailure(String expectedId) throws Exception {
        eventuallyAssertFailure(expectedId, Exception.class, null);
    }

    private void eventuallyAssertFailure(String expectedId, Class type, String errorMessage) throws Exception {
        latch.await();
        assertEquals(0, mainThreadStatusListener.onStatusLogCount);
        assertEquals(1, mainThreadStatusListener.onErrorCount);
        assertEquals(expectedId, mainThreadStatusListener.errorId);
        assertEquals(type, mainThreadStatusListener.errorCause.getClass());
        assertEquals(errorMessage, mainThreadStatusListener.errorCause.getMessage());
        assertNull(mainThreadStatusListener.successId);
        assertNull(mainThreadStatusListener.successLog);
    }
}