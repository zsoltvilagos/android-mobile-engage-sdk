package com.emarsys.mobileengage;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.core.connection.ConnectionWatchDog;
import com.emarsys.core.queue.sqlite.SqliteQueue;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.fake.FakeStatusListener;
import com.emarsys.mobileengage.inbox.model.Notification;
import com.emarsys.mobileengage.testUtil.ConnectionTestUtils;
import com.emarsys.mobileengage.testUtil.DatabaseTestUtils;
import com.emarsys.mobileengage.testUtil.TestDbHelper;
import com.emarsys.mobileengage.util.RequestUtils;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MobileEngageIntegrationTest {
    private CountDownLatch latch;
    private FakeStatusListener listener;

    private Application context;
    private Handler coreSdkHandler;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Before
    public void setup() {
        DatabaseTestUtils.deleteCoreDatabase();

        context = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();

        ConnectionTestUtils.checkConnection(context);

        latch = new CountDownLatch(1);
        listener = new FakeStatusListener(latch, FakeStatusListener.Mode.MAIN_THREAD);
        MobileEngageConfig config = new MobileEngageConfig.Builder()
                .application(context)
                .credentials("14C19-A121F", "PaNkfOD90AVpYimMBuZopCpm8OWCrREu")
                .statusListener(listener)
                .disableDefaultChannel()
                .build();
        MobileEngage.setup(config);
        SqliteQueue queue = new SqliteQueue(context);
        queue.setHelper(new TestDbHelper(context));

        coreSdkHandler = new CoreSdkHandlerProvider().provideHandler();
        MobileEngage.instance.manager = new RequestManager(coreSdkHandler, new ConnectionWatchDog(context, coreSdkHandler), queue, new CoreCompletionHandler() {
            @Override
            public void onSuccess(String id, ResponseModel responseModel) {
                listener.onStatusLog(id, "");
            }

            @Override
            public void onError(String id, ResponseModel responseModel) {
                listener.onError(id, new MobileEngageException(responseModel));
            }

            @Override
            public void onError(String id, Exception cause) {
                listener.onError(id, cause);
            }
        });
        MobileEngage.instance.manager.setDefaultHeaders(RequestUtils.createDefaultHeaders(config));
    }

    @After
    public void tearDown() {
        coreSdkHandler.getLooper().quit();
    }

    @Test
    public void testAppLogin_anonymous() throws Exception {
        eventuallyAssertSuccess(MobileEngage.appLogin());
    }

    @Test
    public void testAppLogin() throws Exception {
        eventuallyAssertSuccess(MobileEngage.appLogin(345, "contactFieldValue"));
    }

    @Test
    public void testAppLogout() throws Exception {
        eventuallyAssertSuccess(MobileEngage.appLogout());
    }

    @Test
    public void testTrackCustomEvent_noAttributes() throws Exception {
        eventuallyAssertSuccess(MobileEngage.trackCustomEvent("customEvent", null));
    }

    @Test
    public void testTrackCustomEvent_emptyAttributes() throws Exception {
        eventuallyAssertSuccess(MobileEngage.trackCustomEvent("customEvent", new HashMap<String, String>()));
    }

    @Test
    public void testTrackCustomEvent_withAttributes() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", "value2");

        eventuallyAssertSuccess(MobileEngage.trackCustomEvent("customEvent", attributes));
    }

    @Test
    public void testTrackMessageOpen_intent() throws Exception {
        Intent intent = new Intent();
        Bundle payload = new Bundle();
        payload.putString("key1", "value1");
        payload.putString("u", "{\"sid\": \"dd8_zXfDdndBNEQi\"}");
        intent.putExtra("payload", payload);

        eventuallyAssertSuccess(MobileEngage.trackMessageOpen(intent));
    }

    @Test
    public void testTrackMessageOpen_notification() throws Exception {
        Notification notification = new Notification(
                "id",
                "161e_D/1UiO/jCmE4",
                "title",
                null,
                new HashMap<String, String>(),
                new JSONObject(),
                2000,
                new Date().getTime());

        eventuallyAssertSuccess(MobileEngage.Inbox.trackMessageOpen(notification));
    }

    private void eventuallyAssertSuccess(String id) throws Exception {
        latch.await();
        assertNull(listener.errorCause);
        assertEquals(id, listener.successId);
        assertEquals(1, listener.onStatusLogCount);
        assertEquals(0, listener.onErrorCount);
        assertNotNull(listener.successLog);
        assertNull(listener.errorId);
    }
}