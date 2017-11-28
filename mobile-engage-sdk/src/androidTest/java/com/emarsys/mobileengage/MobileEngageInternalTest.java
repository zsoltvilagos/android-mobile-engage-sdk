package com.emarsys.mobileengage;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.RequestModel;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.event.applogin.AppLoginStorage;
import com.emarsys.mobileengage.responsehandler.AbstractResponseHandler;
import com.emarsys.mobileengage.responsehandler.MeIdResponseHandler;
import com.emarsys.mobileengage.util.RequestUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.emarsys.mobileengage.MobileEngageInternal.MOBILEENGAGE_SDK_VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class MobileEngageInternalTest {
    private static String APPLICATION_ID = "user";
    private static String APPLICATION_SECRET = "pass";
    private static String ENDPOINT_BASE = "https://push.eservice.emarsys.net/api/mobileengage/v2/";
    private static String ENDPOINT_LOGIN = ENDPOINT_BASE + "users/login";
    private static String ENDPOINT_LOGOUT = ENDPOINT_BASE + "users/logout";
    private static String ENDPOINT_LAST_MOBILE_ACTIVITY = ENDPOINT_BASE + "events/ems_lastMobileActivity";

    private MobileEngageStatusListener statusListener;
    private MobileEngageCoreCompletionHandler coreCompletionHandler;
    private Map<String, String> defaultHeaders;
    private MobileEngageConfig baseConfig;
    private RequestManager manager;
    private Application application;
    private DeviceInfo deviceInfo;
    private AppLoginStorage appLoginStorage;

    private MobileEngageInternal mobileEngage;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Before
    public void init() {
        manager = mock(RequestManager.class);
        coreCompletionHandler = mock(MobileEngageCoreCompletionHandler.class);
        application = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        deviceInfo = new DeviceInfo(application);
        appLoginStorage = new AppLoginStorage(application);
        appLoginStorage.clear();

        statusListener = mock(MobileEngageStatusListener.class);
        baseConfig = new MobileEngageConfig.Builder()
                .application(application)
                .credentials(APPLICATION_ID, APPLICATION_SECRET)
                .statusListener(statusListener)
                .disableDefaultChannel()
                .build();

        defaultHeaders = RequestUtils.createDefaultHeaders(baseConfig);

        mobileEngage = new MobileEngageInternal(baseConfig, manager, appLoginStorage, coreCompletionHandler);
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
    }

    @Test
    public void testSetup_constructorSetsResponseHandlers_onCoreCompletionHandler() {
        ArgumentCaptor<List<AbstractResponseHandler>> captor = ArgumentCaptor.forClass(List.class);
        MobileEngageCoreCompletionHandler coreCompletionHandler = mock(MobileEngageCoreCompletionHandler.class);

        new MobileEngageInternal(baseConfig, manager, appLoginStorage, coreCompletionHandler);

        verify(coreCompletionHandler).setResponseHandlers(captor.capture());

        List<AbstractResponseHandler> handlers = captor.getValue();
        assertEquals(1, handlers.size());
        assertEquals(1, numberOfElementsIn(handlers, MeIdResponseHandler.class));
    }

    private int numberOfElementsIn(List list, Class klass) {
        int count = 0;

        for (Object object : list) {
            if (object.getClass().equals(klass)) {
                count++;
            }
        }

        return count;
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
    public void testTrackCustomEvent_requestManagerCalledWithCorrectRequestModel() {
        String eventName = "cartoon";
        Map<String, String> eventAttributes = new HashMap<>();
        eventAttributes.put("tom", "jerry");

        Map<String, Object> payload = createBasePayload();
        payload.put("attributes", eventAttributes);

        RequestModel expected = new RequestModel.Builder()
                .url(ENDPOINT_BASE + "events/" + eventName)
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
    public void testTrackCustomEvent_returnsRequestModelId() {
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        String result = mobileEngage.trackCustomEvent("event", new HashMap<String, String>());

        verify(manager).submit(captor.capture());

        assertEquals(captor.getValue().getId(), result);
    }

    @Test
    public void testCustomEvent_containsCredentials_fromApploginParameters() {
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
    public void testTrackMessageOpen_requestManagerCalledWithCorrectRequestModel() throws Exception {
        Intent intent = getTestIntent();

        Map<String, Object> payload = createBasePayload();
        payload.put("sid", "+43c_lODSmXqCvdOz");

        RequestModel expected = new RequestModel.Builder()
                .url(ENDPOINT_BASE + "events/message_open")
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