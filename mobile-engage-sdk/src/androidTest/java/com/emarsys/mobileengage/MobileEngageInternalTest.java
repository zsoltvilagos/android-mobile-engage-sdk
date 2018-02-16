package com.emarsys.mobileengage;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.emarsys.core.DeviceInfo;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.core.util.TimestampUtils;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.experimental.MobileEngageExperimental;
import com.emarsys.mobileengage.experimental.MobileEngageFeature;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.testUtil.ExperimentalTestUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;
import com.emarsys.mobileengage.util.RequestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.emarsys.mobileengage.MobileEngageInternal.MOBILEENGAGE_SDK_VERSION;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(AndroidJUnit4.class)
public class MobileEngageInternalTest {
    private static String APPLICATION_ID = "user";
    private static String APPLICATION_SECRET = "pass";
    private static String ENDPOINT_BASE_V2 = "https://push.eservice.emarsys.net/api/mobileengage/v2/";
    private static String ENDPOINT_BASE_V3 = "https://mobile-events.eservice.emarsys.net/v3/devices/";
    private static String ENDPOINT_LOGIN = ENDPOINT_BASE_V2 + "users/login";
    private static String ENDPOINT_LOGOUT = ENDPOINT_BASE_V2 + "users/logout";
    private static String ENDPOINT_LAST_MOBILE_ACTIVITY = ENDPOINT_BASE_V2 + "events/ems_lastMobileActivity";
    private static String ME_ID = "ASD123";

    private MobileEngageStatusListener statusListener;
    private MobileEngageCoreCompletionHandler coreCompletionHandler;
    private Map<String, String> defaultHeaders;
    private MobileEngageConfig baseConfig;
    private RequestManager manager;
    private Application application;
    private DeviceInfo deviceInfo;
    private AppLoginStorage appLoginStorage;
    private Context context;
    private MobileEngageInternal mobileEngage;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        MobileEngageExperimental.enableFeature(MobileEngageFeature.IN_APP_MESSAGING);

        manager = mock(RequestManager.class);
        coreCompletionHandler = mock(MobileEngageCoreCompletionHandler.class);
        application = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        deviceInfo = new DeviceInfo(application);
        appLoginStorage = new AppLoginStorage(application);
        appLoginStorage.remove();

        statusListener = mock(MobileEngageStatusListener.class);
        baseConfig = new MobileEngageConfig.Builder()
                .application(application)
                .credentials(APPLICATION_ID, APPLICATION_SECRET)
                .statusListener(statusListener)
                .disableDefaultChannel()
                .build();

        defaultHeaders = RequestUtils.createDefaultHeaders(baseConfig);

        mobileEngage = new MobileEngageInternal(baseConfig, manager, appLoginStorage, coreCompletionHandler);

        context = InstrumentationRegistry.getContext();
        new MeIdStorage(context).set(ME_ID);
    }

    @After
    public void tearDown() {
        new MeIdStorage(context).remove();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_configShouldNotBeNull() {
        new MobileEngageInternal(null, manager, appLoginStorage, coreCompletionHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_requestManagerShouldNotBeNull() {
        new MobileEngageInternal(baseConfig, null, appLoginStorage, coreCompletionHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_apploginStorageShouldNotBeNull() {
        new MobileEngageInternal(baseConfig, manager, null, coreCompletionHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_coreCompletionHandlerShouldNotBeNull() {
        new MobileEngageInternal(baseConfig, manager, appLoginStorage, null);
    }

    @Test
    public void testSetup_constructorInitializesFields() {
        MobileEngageInternal engage = new MobileEngageInternal(baseConfig, manager, appLoginStorage, coreCompletionHandler);
        assertEquals(manager, engage.manager);
        assertEquals(appLoginStorage, engage.appLoginStorage);
        assertEquals(coreCompletionHandler, engage.coreCompletionHandler);
        assertNotNull(engage.getManager());
        assertNotNull(engage.meIdStorage);
    }

    @Test
    public void testSetup_withAuthHeaderSet() {
        verify(manager).setDefaultHeaders(defaultHeaders);
    }

    @Test
    public void testAppLogin_anonymous_requestManagerCalledWithCorrectRequestModel() throws Exception {
        Map<String, Object> payload = injectLoginPayload(createBasePayload());
        RequestModel expected = new RequestModel.Builder()
                .url(ENDPOINT_LOGIN)
                .payload(payload)
                .headers(defaultHeaders)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.appLogin();

        verify(manager).setDefaultHeaders(defaultHeaders);
        verify(manager).submit(captor.capture());

        RequestModel result = captor.getValue();
        assertRequestModels(expected, result);
    }

    @Test
    public void testAppLogin_anonymous_returnsRequestModelId() {
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        String result = mobileEngage.appLogin();

        verify(manager).submit(captor.capture());

        assertEquals(captor.getValue().getId(), result);
    }

    @Test
    public void testAppLogin_withApploginParameters_requestManagerCalledWithCorrectRequestModel() throws Exception {
        int contactFieldId = 3;
        String contactFieldValue = "value";
        RequestModel expected = createLoginRequestModel(new AppLoginParameters(contactFieldId, contactFieldValue));

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.setAppLoginParameters(new AppLoginParameters(contactFieldId, contactFieldValue));
        mobileEngage.appLogin();

        verify(manager).setDefaultHeaders(defaultHeaders);
        verify(manager).submit(captor.capture());

        RequestModel result = captor.getValue();
        assertRequestModels(expected, result);
    }

    @Test
    public void testAppLogin_returnsRequestModelId() {
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.setAppLoginParameters(new AppLoginParameters(5, "value"));
        String result = mobileEngage.appLogin();

        verify(manager).submit(captor.capture());

        assertEquals(captor.getValue().getId(), result);
    }

    @Test
    public void testAppLogin_shouldNotResult_inMultipleAppLoginRequests_evenIfPayloadIsTheSame() {
        AppLoginParameters sameAppLoginParameter = new AppLoginParameters(3, "test@test.com");

        testSequentialApplogins(
                sameAppLoginParameter,
                createLoginRequestModel(sameAppLoginParameter),
                sameAppLoginParameter,
                createLastMobileActivityRequestModel(sameAppLoginParameter)
        );
    }

    @Test
    public void testAppLogin_shouldResult_inMultipleAppLoginRequests_ifPayloadIsNotTheSame() {
        AppLoginParameters appLoginParameters = new AppLoginParameters(3, "test@test.com");
        AppLoginParameters otherAppLoginParameter = new AppLoginParameters(3, "test2@test.com");

        testSequentialApplogins(
                appLoginParameters,
                createLoginRequestModel(appLoginParameters),
                otherAppLoginParameter,
                createLoginRequestModel(otherAppLoginParameter)
        );
    }

    @Test
    public void testAppLogin_shouldResult_inMultipleAppLoginRequests_when_thereWasALogout() {
        AppLoginParameters appLoginParameters = new AppLoginParameters(3, "test@test.com");

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.setAppLoginParameters(appLoginParameters);
        mobileEngage.appLogin();

        verify(manager).submit(captor.capture());
        RequestModel requestModel = captor.getValue();
        assertRequestModels(createLoginRequestModel(appLoginParameters), requestModel);

        clearInvocations(manager);

        mobileEngage.appLogout();

        clearInvocations(manager);

        captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.setAppLoginParameters(appLoginParameters);
        mobileEngage.appLogin();

        verify(manager).submit(captor.capture());
        requestModel = captor.getValue();
        assertRequestModels(createLoginRequestModel(appLoginParameters), requestModel);
    }

    @Test
    public void testAppLogin_shouldNotResult_inMultipleAppLoginRequests_ifPayloadIsTheSame_evenIfMobileEngageIsReInitialized() {
        AppLoginParameters sameLoginParameters = new AppLoginParameters(3, "test@test.com");

        testSequentialApplogins_withReinstantiationOfMobileEngage(
                sameLoginParameters,
                createLoginRequestModel(sameLoginParameters),
                sameLoginParameters,
                createLastMobileActivityRequestModel(sameLoginParameters)
        );
    }

    @Test
    public void testAppLogin_shouldResult_inMultipleAppLoginRequests_ifPayloadIsNotTheSame_evenIfMobileEngageIsReInitialized() {
        AppLoginParameters appLoginParameters = new AppLoginParameters(3, "test@test.com");
        AppLoginParameters otherAppLoginParameter = new AppLoginParameters(3, "test2@test.com");

        testSequentialApplogins_withReinstantiationOfMobileEngage(
                appLoginParameters,
                createLoginRequestModel(appLoginParameters),
                otherAppLoginParameter,
                createLoginRequestModel(otherAppLoginParameter)
        );
    }

    @Test
    public void testAppLogout_requestManagerCalledWithCorrectRequestModel() {
        Map<String, Object> payload = createBasePayload();
        RequestModel expected = new RequestModel.Builder()
                .url(ENDPOINT_LOGOUT)
                .payload(payload)
                .headers(defaultHeaders)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.appLogout();

        verify(manager).setDefaultHeaders(defaultHeaders);
        verify(manager).submit(captor.capture());

        RequestModel result = captor.getValue();
        assertRequestModels(expected, result);
    }

    @Test
    public void testAppLogout_returnsRequestModelId() {
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        String result = mobileEngage.appLogout();

        verify(manager).submit(captor.capture());

        assertEquals(captor.getValue().getId(), result);
    }

    @Test
    public void testAppLogout_removesStoredMeId() {
        MeIdStorage storage = new MeIdStorage(application);
        storage.set("testMeID");

        mobileEngage.appLogout();
        assertEquals(storage.get(), null);
    }

    @Test
    public void testAppLogout_removesStoredApploginParameters() {
        AppLoginStorage storage = new AppLoginStorage(application);
        storage.set(42);

        mobileEngage.appLogout();
        assertEquals(storage.get(), null);
    }

    @Test
    public void testTrackCustomEvent_V3_requestManagerCalledWithCorrectRequestModel() {
        long timestamp = 123;
        String dateStringWithTimeZone = TimestampUtils.formatTimestampWithUTC(timestamp);

        TimestampProvider fakeProvider = mock(TimestampProvider.class);
        when(fakeProvider.provideTimestamp()).thenReturn(timestamp);
        mobileEngage.timestampProvider = fakeProvider;

        String eventName = "cartoon";
        Map<String, String> eventAttributes = new HashMap<>();
        eventAttributes.put("tom", "jerry");

        Map<String, Object> event = new HashMap<>();
        event.put("type", "custom");
        event.put("name", eventName);
        event.put("timestamp", dateStringWithTimeZone);
        event.put("attributes", eventAttributes);

        Map<String, Object> payload = new HashMap<>();
        payload.put("clicks", new ArrayList<>());
        payload.put("viewed_messages", new ArrayList<>());
        payload.put("events", Collections.singletonList(event));
        payload.put("hardware_id", deviceInfo.getHwid());

        RequestModel expected = new RequestModel.Builder()
                .url(ENDPOINT_BASE_V3 + ME_ID + "/events")
                .payload(payload)
                .headers(defaultHeaders)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.trackCustomEvent(eventName, eventAttributes);

        verify(manager).setDefaultHeaders(defaultHeaders);
        verify(manager).submit(captor.capture());

        RequestModel result = captor.getValue();

        assertRequestModels_withPayloadAsString(expected, result);
    }

    @Test
    public void testTrackCustomEvent_V2_requestManagerCalledWithCorrectRequestModel() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();

        String eventName = "cartoon";
        Map<String, String> eventAttributes = new HashMap<>();
        eventAttributes.put("tom", "jerry");

        Map<String, Object> payload = createBasePayload();
        payload.put("attributes", eventAttributes);

        RequestModel expected = new RequestModel.Builder()
                .url(ENDPOINT_BASE_V2 + "events/" + eventName)
                .payload(payload)
                .headers(defaultHeaders)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.trackCustomEvent(eventName, eventAttributes);

        verify(manager).setDefaultHeaders(defaultHeaders);
        verify(manager).submit(captor.capture());

        RequestModel result = captor.getValue();

        assertRequestModels_withPayloadAsString(expected, result);
    }

    @Test
    public void testCustomEvent_V2_containsCredentials_fromApploginParameters() throws Exception {
        ExperimentalTestUtils.resetExperimentalFeatures();

        int contactFieldId = 3;
        String contactFieldValue = "test@test.com";
        mobileEngage.setAppLoginParameters(new AppLoginParameters(contactFieldId, contactFieldValue));
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.trackCustomEvent("customEvent", null);
        verify(manager).submit(captor.capture());

        Map<String, Object> payload = captor.getValue().getPayload();
        assertEquals(payload.get("contact_field_id"), contactFieldId);
        assertEquals(payload.get("contact_field_value"), contactFieldValue);
    }

    @Test
    public void testTrackCustomEvent_returnsRequestModelId() {
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        String result = mobileEngage.trackCustomEvent("event", new HashMap<String, String>());

        verify(manager).submit(captor.capture());

        assertEquals(captor.getValue().getId(), result);
    }

    @Test
    public void testTrackMessageOpen_requestManagerCalledWithCorrectRequestModel() throws Exception {
        Intent intent = getTestIntent();

        Map<String, Object> payload = createBasePayload();
        payload.put("sid", "+43c_lODSmXqCvdOz");

        RequestModel expected = new RequestModel.Builder()
                .url(ENDPOINT_BASE_V2 + "events/message_open")
                .payload(payload)
                .headers(defaultHeaders)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.trackMessageOpen(intent);

        verify(manager).setDefaultHeaders(defaultHeaders);
        verify(manager).submit(captor.capture());

        RequestModel result = captor.getValue();
        assertRequestModels(expected, result);
    }

    @Test
    public void testTrackMessageOpen_returnsRequestModelId() throws Exception {
        Intent intent = getTestIntent();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        String result = mobileEngage.trackMessageOpen(intent);

        verify(manager).submit(captor.capture());

        assertEquals(captor.getValue().getId(), result);
    }

    @Test
    public void testTrackMessageOpen_containsCredentials_fromApploginParameters() {
        Intent intent = getTestIntent();
        int contactFieldId = 3;
        String contactFieldValue = "test@test.com";
        mobileEngage.setAppLoginParameters(new AppLoginParameters(contactFieldId, contactFieldValue));
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.trackMessageOpen(intent);
        verify(manager).submit(captor.capture());

        Map<String, Object> payload = captor.getValue().getPayload();
        assertEquals(payload.get("contact_field_id"), contactFieldId);
        assertEquals(payload.get("contact_field_value"), contactFieldValue);
    }

    @Test
    public void testTrackDeepLink_requestManagerCalledWithCorrectRequestModel() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://demo-mobileengage.emarsys.net/something?fancy_url=1&ems_dl=1_2_3_4_5"));

        HashMap<String, Object> payload = new HashMap<>();
        payload.put("ems_dl", "1_2_3_4_5");

        RequestModel expected = new RequestModel.Builder()
                .url("https://deep-link.eservice.emarsys.net/api/clicks")
                .payload(payload)
                .headers(defaultHeaders)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.trackDeepLinkOpen(intent);

        verify(manager).setDefaultHeaders(defaultHeaders);
        verify(manager).submit(captor.capture());

        RequestModel result = captor.getValue();
        assertRequestModels(expected, result);
    }

    @Test
    public void testTrackDeepLink_doesNotCallRequestManager_whenDataIsNull() {
        Intent intent = new Intent();

        mobileEngage.trackDeepLinkOpen(intent);

        verify(manager, times(0)).submit(any(RequestModel.class));
    }

    @Test
    public void testTrackDeepLink_doesNotCallRequestManager_whenUriDoesNotContainEmsDlQueryParameter() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://demo-mobileengage.emarsys.net/something?fancy_url=1&other=1_2_3_4_5"));

        mobileEngage.trackDeepLinkOpen(intent);

        verify(manager, times(0)).submit(any(RequestModel.class));
    }

    @Test
    public void testGetMessageId_shouldReturnNullWithEmptyIntent() {
        String result = mobileEngage.getMessageId(new Intent());
        assertNull(result);
    }

    @Test
    public void testGetMessageId_shoudReturnTheCorrectSIDValue() throws Exception {
        Intent intent = getTestIntent();
        String result = mobileEngage.getMessageId(intent);
        assertEquals("+43c_lODSmXqCvdOz", result);
    }

    @Test
    public void testSetPushToken_whenApploginParameters_isEmpty() {
        MobileEngageInternal spy = spy(mobileEngage);

        spy.setAppLoginParameters(new AppLoginParameters());
        spy.setPushToken("123456789");

        verify(spy, times(1)).appLogin();
    }

    @Test
    public void testSetPushToken_whenApploginParameters_hasCredentials() {
        int contactFieldId = 12;
        String contactFieldValue = "asdf";
        MobileEngageInternal spy = spy(mobileEngage);

        spy.setAppLoginParameters(new AppLoginParameters(contactFieldId, contactFieldValue));
        spy.setPushToken("123456789");

        verify(spy, times(1)).appLogin();
    }

    @Test
    public void testSetPushToken_doesNotCallAppLogins_whenApploginParameters_isNull() {
        MobileEngageInternal spy = spy(mobileEngage);

        spy.setAppLoginParameters(null);
        spy.setPushToken("123456789");

        verify(spy, times(0)).appLogin();
    }

    private void testSequentialApplogins(
            AppLoginParameters firstAppLoginParameter,
            RequestModel firstExpectedRequestModel,
            AppLoginParameters secondAppLoginParameter,
            RequestModel secondExpectedRequestModel) {
        testSequentialApplogins(firstAppLoginParameter, firstExpectedRequestModel, secondAppLoginParameter, secondExpectedRequestModel, false);
    }

    private void testSequentialApplogins_withReinstantiationOfMobileEngage(
            AppLoginParameters firstAppLoginParameter,
            RequestModel firstExpectedRequestModel,
            AppLoginParameters secondAppLoginParameter,
            RequestModel secondExpectedRequestModel) {
        testSequentialApplogins(firstAppLoginParameter, firstExpectedRequestModel, secondAppLoginParameter, secondExpectedRequestModel, true);
    }

    private void testSequentialApplogins(
            AppLoginParameters firstAppLoginParameter,
            RequestModel firstExpectedRequestModel,
            AppLoginParameters secondAppLoginParameter,
            RequestModel secondExpectedRequestModel,
            boolean shouldReinstantiateMobileEngage) {
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.setAppLoginParameters(firstAppLoginParameter);
        mobileEngage.appLogin();

        verify(manager).submit(captor.capture());
        RequestModel requestModel = captor.getValue();
        assertRequestModels(firstExpectedRequestModel, requestModel);

        clearInvocations(manager);

        if (shouldReinstantiateMobileEngage) {
            appLoginStorage = new AppLoginStorage(application);
            mobileEngage = new MobileEngageInternal(baseConfig, manager, appLoginStorage, coreCompletionHandler);
        }

        captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.setAppLoginParameters(secondAppLoginParameter);
        mobileEngage.appLogin();

        verify(manager).submit(captor.capture());
        requestModel = captor.getValue();
        assertRequestModels(secondExpectedRequestModel, requestModel);
    }

    private Intent getTestIntent() {
        Intent intent = new Intent();
        Bundle bundlePayload = new Bundle();
        bundlePayload.putString("key1", "value1");
        bundlePayload.putString("u", "{\"sid\": \"+43c_lODSmXqCvdOz\"}");
        intent.putExtra("payload", bundlePayload);
        return intent;
    }

    private Map<String, Object> createBasePayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("application_id", APPLICATION_ID);
        payload.put("hardware_id", deviceInfo.getHwid());

        return payload;
    }

    private Map<String, Object> injectLoginPayload(Map<String, Object> payload) {
        payload.put("platform", deviceInfo.getPlatform());
        payload.put("language", deviceInfo.getLanguage());
        payload.put("timezone", deviceInfo.getTimezone());
        payload.put("device_model", deviceInfo.getModel());
        payload.put("application_version", deviceInfo.getApplicationVersion());
        payload.put("os_version", deviceInfo.getOsVersion());
        payload.put("ems_sdk", MOBILEENGAGE_SDK_VERSION);

        String pushToken = mobileEngage.getPushToken();
        if (pushToken == null) {
            payload.put("push_token", false);
        } else {
            payload.put("push_token", pushToken);
        }

        return payload;
    }

    private RequestModel createLoginRequestModel(AppLoginParameters appLoginParameters) {
        Map<String, Object> payload = injectLoginPayload(createBasePayload());
        payload.put("contact_field_id", appLoginParameters.getContactFieldId());
        payload.put("contact_field_value", appLoginParameters.getContactFieldValue());
        return new RequestModel.Builder()
                .url(ENDPOINT_LOGIN)
                .payload(payload)
                .headers(defaultHeaders)
                .build();
    }

    private RequestModel createLastMobileActivityRequestModel(AppLoginParameters appLoginParameters) {
        Map<String, Object> payload = createBasePayload();
        payload.put("contact_field_id", appLoginParameters.getContactFieldId());
        payload.put("contact_field_value", appLoginParameters.getContactFieldValue());
        return new RequestModel.Builder()
                .url(ENDPOINT_LAST_MOBILE_ACTIVITY)
                .payload(payload)
                .headers(defaultHeaders)
                .build();
    }

    private void assertRequestModels(RequestModel expected, RequestModel result) {
        assertEquals(expected.getUrl(), result.getUrl());
        assertEquals(expected.getMethod(), result.getMethod());
        assertEquals(expected.getPayload(), result.getPayload());
    }

    private void assertRequestModels_withPayloadAsString(RequestModel expected, RequestModel result) {
        assertEquals(expected.getUrl(), result.getUrl());
        assertEquals(expected.getMethod(), result.getMethod());
        assertEquals(expected.getPayload().toString(), result.getPayload().toString());
    }
}