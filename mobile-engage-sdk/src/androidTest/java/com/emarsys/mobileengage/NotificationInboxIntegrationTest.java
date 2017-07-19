package com.emarsys.mobileengage;

import android.app.Application;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.connection.ConnectionWatchDog;
import com.emarsys.core.queue.sqlite.SqliteQueue;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.mobileengage.fake.FakeInboxResultListener;
import com.emarsys.mobileengage.fake.FakeResetBadgeCountResultListener;
import com.emarsys.mobileengage.fake.FakeStatusListener;
import com.emarsys.mobileengage.testUtil.ConnectionTestUtils;
import com.emarsys.mobileengage.testUtil.TestDbHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class NotificationInboxIntegrationTest {

    private CountDownLatch latch;
    private CountDownLatch inboxLatch;
    private CountDownLatch resetLatch;
    private FakeStatusListener listener;
    private FakeInboxResultListener inboxListener;
    private FakeResetBadgeCountResultListener resetListener;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    private Application context;

    @Before
    public void setup() {
        context = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();

        ConnectionTestUtils.checkConnection(context);

        latch = new CountDownLatch(1);
        listener = new FakeStatusListener(latch, FakeStatusListener.Mode.MAIN_THREAD);
        MobileEngageConfig config = new MobileEngageConfig.Builder()
                .application(context)
                .credentials("14C19-A121F", "PaNkfOD90AVpYimMBuZopCpm8OWCrREu")
                .statusListener(listener)
                .build();
        MobileEngage.setup(config);
        SqliteQueue queue = new SqliteQueue(context);
        queue.setHelper(new TestDbHelper(context));
        MobileEngage.instance.manager = new RequestManager(new ConnectionWatchDog(context), queue, new CoreCompletionHandler() {
            @Override
            public void onSuccess(String id, ResponseModel responseModel) {
                listener.onStatusLog(id, "");
            }

            @Override
            public void onError(String id, ResponseModel responseModel) {
                listener.onError(id, mock(Exception.class));
            }

            @Override
            public void onError(String id, Exception cause) {
                listener.onError(id, mock(Exception.class));
            }
        });
        MobileEngage.instance.initializeRequestManager(config.getApplicationCode(), config.getApplicationPassword());

        inboxLatch = new CountDownLatch(1);
        resetLatch = new CountDownLatch(1);
        inboxListener = new FakeInboxResultListener(inboxLatch, FakeInboxResultListener.Mode.MAIN_THREAD);
        resetListener = new FakeResetBadgeCountResultListener(resetLatch);
    }

    @Test
    public void fetchNotifications() throws InterruptedException {
        MobileEngage.appLogin(3, "test@test.com");
        latch.await();

        MobileEngage.Inbox.fetchNotifications(inboxListener);
        inboxLatch.await();

        assertEquals(1, inboxListener.successCount);
        assertEquals(0, inboxListener.errorCount);
        assertNull(inboxListener.errorCause);
        assertNotNull(inboxListener.resultStatus);
    }

    @Test
    public void resetBadgeCount() throws InterruptedException {
        MobileEngage.appLogin(3, "test@test.com");
        latch.await();

        MobileEngage.Inbox.resetBadgeCount(resetListener);
        resetLatch.await();

        assertEquals(1, resetListener.successCount);
        assertEquals(0, resetListener.errorCount);
        assertNull(resetListener.errorCause);
    }
}
