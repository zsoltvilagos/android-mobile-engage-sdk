package com.emarsys.mobileengage;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.DeviceInfo;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.RequestModel;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;

import static com.emarsys.mobileengage.MobileEngageInternal.MOBILEENGAGE_SDK_VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class MobileEngageInternalTest {
    private static String APPLICATION_ID = "user";
    private static String APPLICATION_SECRET = "pass";
    private static String ENDPOINT_BASE = "https://push.eservice.emarsys.net/api/mobileengage/v2/";
    private static String ENDPOINT_LOGIN = ENDPOINT_BASE + "users/login";
    private static String ENDPOINT_LOGOUT = ENDPOINT_BASE + "users/logout";

    private MobileEngageStatusListener statusListener;
    private Map<String, String> defaultHeaders;
    private MobileEngageConfig baseConfig;
    private RequestManager manager;
    private Application application;
    private Context context;
    private DeviceInfo deviceInfo;

    private MobileEngageInternal mobileEngage;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Before
    public void init() {
        defaultHeaders = new HashMap<>();
        defaultHeaders.put("Authorization", "Basic dXNlcjpwYXNz");
        defaultHeaders.put("Content-Type", "application/json");
        defaultHeaders.put("X-MOBILEENGAGE-SDK-VERSION", MOBILEENGAGE_SDK_VERSION);
        manager = mock(RequestManager.class);
        context = InstrumentationRegistry.getTargetContext();
        deviceInfo = new DeviceInfo(context);
        application = mock(Application.class);
        when(application.getApplicationContext()).thenReturn(context);

        statusListener = mock(MobileEngageStatusListener.class);
        baseConfig = new MobileEngageConfig.Builder()
                .credentials(APPLICATION_ID, APPLICATION_SECRET)
                .statusListener(statusListener)
                .build();

        mobileEngage = new MobileEngageInternal(application, baseConfig, manager);
    }

    @Test
    public void testSetup_constructorInitializesFields() {
        MobileEngageInternal engage = new MobileEngageInternal(application, baseConfig, manager);
        new DeviceInfo(context);
        assertEquals(baseConfig.getStatusListener(), engage.getStatusListener());
        assertNotNull(engage.getManager());
        assertNotNull(engage.getCompletionHandler());
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
        verify(manager).submit(captor.capture(), any(CoreCompletionHandler.class));

        RequestModel result = captor.getValue();
        assertRequestModels(expected, result);
    }

    @Test
    public void testAppLogin_anonymous_returnsRequestModelId() {
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        String result = mobileEngage.appLogin();

        verify(manager).submit(captor.capture(), any(CoreCompletionHandler.class));

        assertEquals(captor.getValue().getId(), result);
    }

    @Test
    public void testAppLogin_requestManagerCalledWithCorrectRequestModel() throws Exception {
        int contactField = 3;
        String contactFieldValue = "value";
        Map<String, Object> payload = injectLoginPayload(createBasePayload());
        payload.put("contact_field_id", contactField);
        payload.put("contact_field_value", contactFieldValue);
        RequestModel expected = new RequestModel.Builder()
                .url(ENDPOINT_LOGIN)
                .payload(payload)
                .headers(defaultHeaders)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.appLogin(contactField, contactFieldValue);

        verify(manager).setDefaultHeaders(defaultHeaders);
        verify(manager).submit(captor.capture(), any(CoreCompletionHandler.class));

        RequestModel result = captor.getValue();
        assertRequestModels(expected, result);
    }

    @Test
    public void testAppLogin_returnsRequestModelId() {
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        String result = mobileEngage.appLogin(5, "value");

        verify(manager).submit(captor.capture(), any(CoreCompletionHandler.class));

        assertEquals(captor.getValue().getId(), result);
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
        verify(manager).submit(captor.capture(), any(CoreCompletionHandler.class));

        RequestModel result = captor.getValue();
        assertRequestModels(expected, result);
    }

    @Test
    public void testAppLogout_returnsRequestModelId() {
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        String result = mobileEngage.appLogout();

        verify(manager).submit(captor.capture(), any(CoreCompletionHandler.class));

        assertEquals(captor.getValue().getId(), result);
    }

    @Test
    public void testTrackCustomEvent_requestManagerCalledWithCorrectRequestModel() {
        String eventName = "cartoon";
        Map<String, String> eventAttributes = new HashMap<>();
        eventAttributes.put("tom", "jerry");

        Map<String, Object> payload = createBasePayload();
        payload.put("attributes", new JSONObject(eventAttributes));

        RequestModel expected = new RequestModel.Builder()
                .url(ENDPOINT_BASE + "events/" + eventName)
                .payload(payload)
                .headers(defaultHeaders)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.trackCustomEvent(eventName, eventAttributes);

        verify(manager).setDefaultHeaders(defaultHeaders);
        verify(manager).submit(captor.capture(), any(CoreCompletionHandler.class));

        RequestModel result = captor.getValue();
        assertRequestModels_withPayloadAsString(expected, result);
    }

    @Test
    public void testTrackCustomEvent_returnsRequestModelId() {
        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        String result = mobileEngage.trackCustomEvent("event", new HashMap<String, String>());

        verify(manager).submit(captor.capture(), any(CoreCompletionHandler.class));

        assertEquals(captor.getValue().getId(), result);
    }

    @Test
    public void testTrackMessageOpen_requestManagerCalledWithCorrectRequestModel() throws Exception {
        Intent intent = new Intent();
        Bundle bundlePayload = new Bundle();
        bundlePayload.putString("key1", "value1");
        bundlePayload.putString("u", "{\"sid\": \"+43c_lODSmXqCvdOz\"}");
        intent.putExtra("payload", bundlePayload);

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
        verify(manager).submit(captor.capture(), any(CoreCompletionHandler.class));

        RequestModel result = captor.getValue();
        assertRequestModels(expected, result);
    }

    @Test
    public void testTrackMessageOpen_returnsRequestModelId() throws Exception {
        Intent intent = new Intent();
        Bundle payload = new Bundle();
        payload.putString("key1", "value1");
        payload.putString("u", "{\"sid\": \"+43c_lODSmXqCvdOz\"}");
        intent.putExtra("payload", payload);

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        String result = mobileEngage.trackMessageOpen(intent);

        verify(manager).submit(captor.capture(), any(CoreCompletionHandler.class));

        assertEquals(captor.getValue().getId(), result);
    }

    @Test
    public void testGetMessageId_shouldReturnNullWithEmptyIntent() {
        String result = mobileEngage.getMessageId(new Intent());
        assertNull(result);
    }

    @Test
    public void testGetMessageId_shoudReturnTheCorrectSIDValue() throws Exception {
        Intent intent = new Intent();
        Bundle payload = new Bundle();
        payload.putString("key1", "value1");
        payload.putString("u", "{\"sid\": \"+43c_lODSmXqCvdOz\"}");
        intent.putExtra("payload", payload);
        String result = mobileEngage.getMessageId(intent);
        assertEquals("+43c_lODSmXqCvdOz", result);
    }

    @Test
    public void testSetPushToken_shouldNotCallAppLogins() {
        MobileEngageInternal spy = spy(mobileEngage);

        spy.setPushToken("123456789");

        verify(spy, times(0)).appLogin();
        verify(spy, times(0)).appLogin(any(Integer.class), any(String.class));
    }

    @Test
    public void testSetPushToken_shouldCallAnonymousAppLogin() {
        MobileEngageInternal spy = spy(mobileEngage);

        spy.appLogin();
        spy.setPushToken("123456789");

        verify(spy, times(2)).appLogin();
    }

    @Test
    public void testSetPushToken_shouldCallAppLogin() {
        int contactFieldId = 12;
        String contactFieldValue = "asdf";
        MobileEngageInternal spy = spy(mobileEngage);

        spy.appLogin(contactFieldId, contactFieldValue);
        spy.setPushToken("123456789");

        verify(spy, times(2)).appLogin(any(Integer.class), any(String.class));
    }

    @Test
    public void testSetPushToken_shouldNotCallAnonymousLogin_afterLogout() {
        MobileEngageInternal spy = spy(mobileEngage);

        spy.appLogin();
        spy.appLogout();
        spy.setPushToken("123456789");

        verify(spy, times(1)).appLogin();
    }

    @Test
    public void testSetPushToken_shouldNotCallLogin_afterLogout() {
        int contactFieldId = 12;
        String contactFieldValue = "asdf";
        MobileEngageInternal spy = spy(mobileEngage);

        spy.appLogin(contactFieldId, contactFieldValue);
        spy.appLogout();
        spy.setPushToken("123456789");

        verify(spy, times(1)).appLogin(any(Integer.class), any(String.class));
    }

    @Test
    public void testSetPushToken_appLoginShouldOverride_anonymousAppLogin() {
        int contactFieldId = 12;
        String contactFieldValue = "asdf";
        MobileEngageInternal spy = spy(mobileEngage);

        spy.appLogin();
        spy.appLogin(contactFieldId, contactFieldValue);
        spy.setPushToken("123456789");

        verify(spy, times(1)).appLogin();
        verify(spy, times(2)).appLogin(any(Integer.class), any(String.class));
    }

    @Test
    public void testSetPushToken_anonymousAppLoginShouldOverride_appLogin() {
        int contactFieldId = 12;
        String contactFieldValue = "asdf";
        MobileEngageInternal spy = spy(mobileEngage);

        spy.appLogin(contactFieldId, contactFieldValue);
        spy.appLogin();
        spy.setPushToken("123456789");

        verify(spy, times(1)).appLogin(any(Integer.class), any(String.class));
        verify(spy, times(2)).appLogin();
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