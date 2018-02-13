package com.emarsys.mobileengage.iam.dialog.action;

import android.os.Handler;

import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.testUtil.RequestModelTestUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;
import com.emarsys.mobileengage.testUtil.mockito.ThreadSpy;
import com.emarsys.mobileengage.util.RequestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SendDisplayedIamActionTest {

    private final static String APPLICATION_CODE = "application_code";
    private static final String CAMPAIGN_ID = "123445";
    private static final long TIMESTAMP = 123445;
    private static final String ME_ID = "123";
    private static final String ME_ID_SIGNATURE = "signature";

    private SendDisplayedIamAction action;

    private Handler handler;
    private RequestManager requestManager;
    private MeIdStorage meIdStorage;
    private MeIdSignatureStorage meIdSignatureStorage;
    private TimestampProvider timestampProvider;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        handler = new CoreSdkHandlerProvider().provideHandler();
        requestManager = mock(RequestManager.class);

        meIdStorage = mock(MeIdStorage.class);
        when(meIdStorage.get()).thenReturn(ME_ID);

        meIdSignatureStorage = mock(MeIdSignatureStorage.class);
        when(meIdSignatureStorage.get()).thenReturn(ME_ID_SIGNATURE);

        timestampProvider = mock(TimestampProvider.class);
        when(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP);

        action = new SendDisplayedIamAction(handler, requestManager, APPLICATION_CODE, meIdStorage, meIdSignatureStorage, timestampProvider);
    }

    @After
    public void tearDown() {
        handler.getLooper().quit();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_handler_mustNotBeNull() {
        new SendDisplayedIamAction(
                null,
                mock(RequestManager.class),
                APPLICATION_CODE,
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_requestManager_mustNotBeNull() {
        new SendDisplayedIamAction(
                mock(Handler.class),
                null,
                APPLICATION_CODE,
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_applicationCode_mustNotBeNull() {
        new SendDisplayedIamAction(
                mock(Handler.class),
                mock(RequestManager.class),
                null,
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_meIdStorage_mustNotBeNull() {
        new SendDisplayedIamAction(
                mock(Handler.class),
                mock(RequestManager.class),
                APPLICATION_CODE,
                null,
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_meIdSignatureStorage_mustNotBeNull() {
        new SendDisplayedIamAction(
                mock(Handler.class),
                mock(RequestManager.class),
                APPLICATION_CODE,
                mock(MeIdStorage.class),
                null,
                mock(TimestampProvider.class)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_timestampProvider_mustNotBeNull() {
        new SendDisplayedIamAction(
                mock(Handler.class),
                mock(RequestManager.class),
                APPLICATION_CODE,
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                null
        );
    }


    @Test(expected = IllegalArgumentException.class)
    public void testExecute_campaignIdMustNotBeNull() {
        action.execute(null, 0);
    }

    @Test
    public void testExecute_callsRequestManager() {
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        action.execute(CAMPAIGN_ID, TIMESTAMP);
        verify(requestManager, timeout(1000)).submit(captor.capture());

        RequestModel actual = captor.getValue();

        Map<String, String> attributes = new HashMap<>();
        attributes.put("message_id", CAMPAIGN_ID);

        RequestModel expected = RequestUtils.createInternalCustomEvent(
                "inapp:viewed",
                attributes,
                APPLICATION_CODE,
                meIdStorage,
                meIdSignatureStorage,
                timestampProvider);

        RequestModelTestUtils.assertEqualsExceptId(expected, actual);
    }

    @Test
    public void testExecute_callsRequestManager_onCoreSdkThread() throws InterruptedException {
        ThreadSpy threadSpy = new ThreadSpy();
        doAnswer(threadSpy).when(requestManager).submit(any(RequestModel.class));

        action.execute(CAMPAIGN_ID, TIMESTAMP);

        threadSpy.verifyCalledOnCoreSdkThread();
    }


}