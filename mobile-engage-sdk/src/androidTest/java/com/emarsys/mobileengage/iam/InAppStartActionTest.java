package com.emarsys.mobileengage.iam;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.core.util.TimestampUtils;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.testUtil.DatabaseTestUtils;
import com.emarsys.mobileengage.testUtil.SharedPrefsUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;
import com.emarsys.mobileengage.util.RequestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class InAppStartActionTest {

    private static final long TIMESTAMP = 400;
    private static final String ME_ID = "123";
    private static final String ME_ID_SIGNATURE = "signature";
    private static final String APPLICATION_CODE = "1234-abcd";

    private TimestampProvider timestampProvider;
    private RequestManager requestManager;
    private MeIdStorage meIdStorage;
    private MeIdSignatureStorage meIdSignatureStorage;
    private MobileEngageConfig config;

    private InAppStartAction startAction;
    private Context context;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        DatabaseTestUtils.deleteCoreDatabase();
        DatabaseTestUtils.deleteMobileEngageDatabase();
        SharedPrefsUtils.deleteMobileEngageSharedPrefs();
        context = InstrumentationRegistry.getTargetContext();

        timestampProvider = mock(TimestampProvider.class);
        when(timestampProvider.provideTimestamp()).thenReturn(TIMESTAMP);

        requestManager = mock(RequestManager.class);

        meIdStorage = new MeIdStorage(context);
        meIdStorage.set(ME_ID);

        meIdSignatureStorage = new MeIdSignatureStorage(context);
        meIdSignatureStorage.set(ME_ID_SIGNATURE);

        config = mock(MobileEngageConfig.class);
        when(config.getApplicationCode()).thenReturn(APPLICATION_CODE);

        startAction = new InAppStartAction(
                requestManager,
                APPLICATION_CODE,
                meIdStorage,
                meIdSignatureStorage,
                timestampProvider);
    }

    @After
    public void tearDown() {
        DatabaseTestUtils.deleteCoreDatabase();
        DatabaseTestUtils.deleteCoreDatabase();
        SharedPrefsUtils.deleteMobileEngageSharedPrefs();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_requestManagerMustNotBeNull() {
        new InAppStartAction(
                null,
                APPLICATION_CODE,
                meIdStorage,
                meIdSignatureStorage,
                timestampProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_configMustNotBeNull() {
        new InAppStartAction(
                requestManager,
                null,
                meIdStorage,
                meIdSignatureStorage,
                timestampProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_meIdStorageMustNotBeNull() {
        new InAppStartAction(
                requestManager,
                APPLICATION_CODE,
                null,
                meIdSignatureStorage,
                timestampProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_meIdStorageSignatureMustNotBeNull() {
        new InAppStartAction(
                requestManager,
                APPLICATION_CODE,
                meIdStorage,
                null,
                timestampProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_timestampProviderMustNotBeNull() {
        new InAppStartAction(
                requestManager,
                APPLICATION_CODE,
                meIdStorage,
                meIdSignatureStorage,
                null);
    }

    @Test
    public void testExecute_sendsInternalEvent_toRequestManager() {
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        startAction.execute();

        verify(requestManager).submit(captor.capture());

        RequestModel actual = captor.getValue();
        RequestModel expected = createInternalEvent(actual.getTimestamp(), actual.getId());

        assertEquals(expected, actual);
    }

    @Test
    public void testExecute_shouldNotRunIf_meId_isMissing() {
        meIdStorage.remove();

        startAction.execute();

        verifyZeroInteractions(requestManager);
    }

    @Test
    public void testExecute_shouldNotRunIf_meIdSignature_isMissing() {
        meIdSignatureStorage.remove();

        startAction.execute();

        verifyZeroInteractions(requestManager);
    }

    private RequestModel createInternalEvent(long timestamp, String id) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "internal");
        event.put("name", "inapp:start");
        event.put("timestamp", TimestampUtils.formatTimestampWithUTC(TIMESTAMP));

        Map<String, Object> payload = new HashMap<>();
        payload.put("viewed_messages", new ArrayList<>());
        payload.put("clicks", new ArrayList<>());
        payload.put("events", Collections.singletonList(event));

        return new RequestModel(
                RequestUtils.createEventUrl_V3(meIdStorage.get()),
                RequestMethod.POST,
                payload,
                RequestUtils.createBaseHeaders_V3(
                        APPLICATION_CODE,
                        meIdStorage,
                        meIdSignatureStorage),
                timestamp,
                Long.MAX_VALUE,
                id);
    }

}