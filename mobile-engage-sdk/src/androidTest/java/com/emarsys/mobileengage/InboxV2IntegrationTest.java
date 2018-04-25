package com.emarsys.mobileengage;

import android.app.Application;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.database.repository.log.LogRepository;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.experimental.MobileEngageFeature;
import com.emarsys.mobileengage.fake.FakeInboxResultListener;
import com.emarsys.mobileengage.fake.FakeResetBadgeCountResultListener;
import com.emarsys.mobileengage.fake.FakeStatusListener;
import com.emarsys.mobileengage.iam.InAppMessageHandler;
import com.emarsys.mobileengage.inbox.InboxInternal;
import com.emarsys.mobileengage.inbox.InboxInternal_V2;
import com.emarsys.mobileengage.log.LogRepositoryProxy;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.testUtil.ConnectionTestUtils;
import com.emarsys.mobileengage.testUtil.DatabaseTestUtils;
import com.emarsys.mobileengage.testUtil.SharedPrefsUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class InboxV2IntegrationTest {

    private CountDownLatch latch;
    private CountDownLatch inboxLatch;
    private CountDownLatch resetLatch;
    private FakeStatusListener listener;
    private FakeInboxResultListener inboxListener;
    private FakeResetBadgeCountResultListener resetListener;
    private InboxInternal inboxInternal;
    private RequestContext requestContext;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        DatabaseTestUtils.deleteCoreDatabase();
        SharedPrefsUtils.deleteMobileEngageSharedPrefs();

        Application context = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        ConnectionTestUtils.checkConnection(context);

        latch = new CountDownLatch(1);
        listener = new FakeStatusListener(latch, FakeStatusListener.Mode.MAIN_THREAD);
        MobileEngageConfig config = new MobileEngageConfig.Builder()
                .application(context)
                .credentials("14C19-A121F", "PaNkfOD90AVpYimMBuZopCpm8OWCrREu")
                .statusListener(listener)
                .disableDefaultChannel()
                .enableExperimentalFeatures(MobileEngageFeature.IN_APP_MESSAGING)
                .setDefaultInAppMessageHandler(mock(InAppMessageHandler.class))
                .build();
        MobileEngage.setup(config);

        List<com.emarsys.core.handler.Handler<Map<String, Object>, Map<String, Object>>> handlers = new ArrayList<>();
        LogRepository logRepository = new LogRepository(context);
        Repository<Map<String, Object>, SqlSpecification> logRepositoryProxy = new LogRepositoryProxy(logRepository, handlers);
        TimestampProvider timestampProvider = new TimestampProvider();
        RestClient restClient = new RestClient(logRepositoryProxy, timestampProvider);

        requestContext = new RequestContext(
                config.getApplicationCode(),
                mock(DeviceInfo.class),
                new AppLoginStorage(context),
                new MeIdStorage(context),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class));

        inboxInternal = new InboxInternal_V2(config, MobileEngage.instance.manager, restClient, requestContext);

        inboxLatch = new CountDownLatch(1);
        resetLatch = new CountDownLatch(1);
        inboxListener = new FakeInboxResultListener(inboxLatch, FakeInboxResultListener.Mode.MAIN_THREAD);
        resetListener = new FakeResetBadgeCountResultListener(resetLatch);
    }

    @After
    public void tearDown() {
        DatabaseTestUtils.deleteCoreDatabase();
        SharedPrefsUtils.deleteMobileEngageSharedPrefs();
        MobileEngage.coreSdkHandler.getLooper().quit();
    }

    @Test
    public void fetchNotifications() throws InterruptedException {
        doAppLogin();

        inboxInternal.fetchNotifications(inboxListener);
        inboxLatch.await();

        assertNull(inboxListener.errorCause);
        assertEquals(1, inboxListener.successCount);
        assertEquals(0, inboxListener.errorCount);
        assertNotNull(inboxListener.resultStatus);
    }

    @Test
    public void resetBadgeCount() throws InterruptedException {
        doAppLogin();

        MobileEngage.Inbox.resetBadgeCount(resetListener);
        resetLatch.await();

        assertNull(resetListener.errorCause);
        assertEquals(1, resetListener.successCount);
        assertEquals(0, resetListener.errorCount);
    }

    private void doAppLogin() throws InterruptedException {
        MobileEngage.appLogin(3, "test@test.com");
        latch.await();
    }

}
