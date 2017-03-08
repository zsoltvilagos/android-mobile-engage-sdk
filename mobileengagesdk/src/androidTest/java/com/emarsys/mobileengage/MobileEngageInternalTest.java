package com.emarsys.mobileengage;

import android.app.Application;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.DeviceInfo;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.RequestModel;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class MobileEngageInternalTest {
    private static String APPLICATION_ID = "user";
    private static String APPLICATION_SECRET = "pass";
    private static String ENDPOINT_BASE = "https://push.eservice.emarsys.net/api/mobileengage/v2/";
    private static String ENDPOINT_LOGIN = ENDPOINT_BASE + "users/login";

    private MobileEngageStatusListener statusListener;
    private Map<String, String> authHeader;
    private MobileEngageConfig baseConfig;
    private RequestManager manager;
    private Application application;
    private Context context;
    private DeviceInfo deviceInfo;

    private MobileEngageInternal mobileEngage;

    @Before
    public void init() {
        authHeader = new HashMap<>();
        authHeader.put("Authorization", "Basic dXNlcjpwYXNz");
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
        RequestManager requestManager = mock(RequestManager.class);
        new MobileEngageInternal(application, baseConfig, requestManager);

        verify(manager).setDefaultHeaders(authHeader);
    }

    @Test
    public void testAppLogin_anonymous_requestManagerCalledWithCorrectRequestModel() throws Exception {
        Map<String, Object> payload = createBasePayload(deviceInfo, mobileEngage);
        RequestModel expected = new RequestModel.Builder()
                .url(ENDPOINT_LOGIN)
                .payload(payload)
                .headers(authHeader)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.appLogin();

        verify(manager).setDefaultHeaders(authHeader);
        verify(manager).submit(captor.capture(), any(CoreCompletionHandler.class));

        RequestModel result = captor.getValue();
        assertRequestModels(expected, result);
    }

    @Test
    public void testAppLogin_requestManagerCalledWithCorrectRequestModel() throws Exception {
        int contactField = 3;
        String contactFieldValue = "value";
        Map<String, Object> payload = createBasePayload(deviceInfo, mobileEngage);
        payload.put("contact_field_id", contactField);
        payload.put("contact_field_value", contactFieldValue);
        RequestModel expected = new RequestModel.Builder()
                .url(ENDPOINT_LOGIN)
                .payload(payload)
                .headers(authHeader)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        mobileEngage.appLogin(contactField, contactFieldValue);

        verify(manager).setDefaultHeaders(authHeader);
        verify(manager).submit(captor.capture(), any(CoreCompletionHandler.class));

        RequestModel result = captor.getValue();
        assertRequestModels(expected, result);
    }

    private Map<String, Object> createBasePayload(DeviceInfo info, MobileEngageInternal mobileEngage) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("application_id", APPLICATION_ID);
        payload.put("hardware_id", deviceInfo.getHwid());
        payload.put("platform", deviceInfo.getPlatform());
        payload.put("language", deviceInfo.getLanguage());
        payload.put("timezone", deviceInfo.getTimezone());
        payload.put("device_model", deviceInfo.getModel());
        payload.put("application_version", deviceInfo.getApplicationVersion());
        payload.put("os_version", deviceInfo.getOsVersion());

        String pushToken = mobileEngage.getPushToken();
        if (pushToken == null) {
            payload.put("push_token", false);
        } else {
            payload.put("push_token", pushToken);
        }

        return payload;
    }

    private void assertRequestModels(RequestModel expected, RequestModel result) throws Exception {
        assertEquals(expected.getUrl(), result.getUrl());
        assertEquals(expected.getMethod(), result.getMethod());
        assertEquals(expected.getPayload(), result.getPayload());
    }

    private Map<String, Object> jsonToMap(JSONObject json) throws Exception {
        Map<String, Object> result = new HashMap<>();
        Iterator<String> iterator = json.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            result.put(key, json.get(key));
        }
        return result;
    }
}