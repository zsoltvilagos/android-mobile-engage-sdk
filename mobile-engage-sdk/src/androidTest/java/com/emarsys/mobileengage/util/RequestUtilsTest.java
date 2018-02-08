package com.emarsys.mobileengage.util;

import android.app.Application;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.util.HeaderUtils;
import com.emarsys.mobileengage.BuildConfig;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.testUtil.ApplicationTestUtils;
import com.emarsys.mobileengage.testUtil.SharedPrefsUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RequestUtilsTest {
    private static final String APPLICATION_CODE = "applicationCode";
    private static final String APPLICATION_PASSWORD = "applicationPassword";

    private MobileEngageConfig realConfig;
    private MobileEngageConfig mockDebugConfig;
    private MobileEngageConfig mockReleaseConfig;
    private DeviceInfo deviceInfo;
    private Context context;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setup() {
        SharedPrefsUtils.deleteMobileEngageSharedPrefs();

        context = InstrumentationRegistry.getTargetContext();

        realConfig = new MobileEngageConfig.Builder()
                .application((Application) InstrumentationRegistry.getTargetContext().getApplicationContext())
                .credentials(APPLICATION_CODE, APPLICATION_PASSWORD)
                .disableDefaultChannel()
                .build();

        mockDebugConfig = new MobileEngageConfig.Builder()
                .application(ApplicationTestUtils.applicationDebug())
                .credentials(APPLICATION_CODE, APPLICATION_PASSWORD)
                .disableDefaultChannel()
                .build();

        mockReleaseConfig = new MobileEngageConfig.Builder()
                .application(ApplicationTestUtils.applicationRelease())
                .credentials(APPLICATION_CODE, APPLICATION_PASSWORD)
                .disableDefaultChannel()
                .build();

        deviceInfo = new DeviceInfo(InstrumentationRegistry.getContext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEventUrl_shouldNotAcceptNull() {
        RequestUtils.createEventUrl_V2(null);
    }

    @Test
    public void testCreateEventUrl_shouldReturnTheCorrectEventUrl() {
        String url = RequestUtils.createEventUrl_V2("my-custom-event");
        String expected = "https://push.eservice.emarsys.net/api/mobileengage/v2/events/my-custom-event";
        assertEquals(expected, url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEventUrl_V3_meIdShouldNotBeNull() {
        RequestUtils.createEventUrl_V3(null);
    }

    @Test
    public void testCreateEventUrl_V3_shouldReturnTheCorrectEventUrl() {
        String url = RequestUtils.createEventUrl_V3("meId");
        String expected = "https://ems-me-deviceevent.herokuapp.com/v3/devices/meId/events";
        assertEquals(expected, url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBaseHeaders_V2_configShouldNotBeNull() {
        RequestUtils.createBaseHeaders_V2(null);
    }

    @Test
    public void testCreateBaseHeaders_V2_shouldReturnCorrectMap() {
        Map<String, String> expected = new HashMap<>();
        expected.put("Authorization", HeaderUtils.createBasicAuth(realConfig.getApplicationCode(), realConfig.getApplicationPassword()));

        Map<String, String> result = RequestUtils.createBaseHeaders_V2(realConfig);

        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBaseHeaders_V3_configShouldNotBeNull() {
        RequestUtils.createBaseHeaders_V3(null);
    }

    @Test
    public void testCreateBaseHeaders_V3_shouldReturnCorrectMap() {
        String meId = "meid";
        String meIdSignature = "meidsignature";
        new MeIdStorage(context).set(meId);
        new MeIdSignatureStorage(context).set(meIdSignature);

        Map<String, String> expected = new HashMap<>();
        expected.put("X-ME-ID", meId);
        expected.put("X-ME-ID-SIGNATURE", meIdSignature);
        expected.put("X-ME-APPLICATIONCODE", APPLICATION_CODE);

        Map<String, String> result = RequestUtils.createBaseHeaders_V3(realConfig);

        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDefaultHeaders_configShouldNotBeNull() {
        RequestUtils.createDefaultHeaders(null);
    }

    @Test
    public void testCreateDefaultHeaders_returnedValueShouldNotBeNull() {
        assertNotNull(RequestUtils.createDefaultHeaders(mockDebugConfig));
    }

    @Test
    public void testCreateDefaultHeaders_debug_shouldReturnCorrectMap() {
        Map<String, String> expected = new HashMap<>();
        expected.put("Content-Type", "application/json");
        expected.put("X-MOBILEENGAGE-SDK-VERSION", BuildConfig.VERSION_NAME);
        expected.put("X-MOBILEENGAGE-SDK-MODE", "debug");

        Map<String, String> result = RequestUtils.createDefaultHeaders(mockDebugConfig);

        assertEquals(expected, result);
    }

    @Test
    public void testCreateDefaultHeaders_release_shouldReturnCorrectMap() {
        Map<String, String> expected = new HashMap<>();
        expected.put("Content-Type", "application/json");
        expected.put("X-MOBILEENGAGE-SDK-VERSION", BuildConfig.VERSION_NAME);
        expected.put("X-MOBILEENGAGE-SDK-MODE", "production");

        Map<String, String> result = RequestUtils.createDefaultHeaders(mockReleaseConfig);

        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBasePayload_config_configShouldNotBeNull() {
        RequestUtils.createBasePayload(null, null);
    }

    @Test
    public void testCreateBasePayload_config_shouldReturnTheCorrectPayload() {
        Map<String, Object> payload = RequestUtils.createBasePayload(realConfig, null);
        Map<String, Object> expected = RequestUtils.createBasePayload(new HashMap<String, Object>(), realConfig, null);
        assertEquals(expected, payload);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBasePayload_map_config_additionalPayloadShouldNotBeNull() {
        RequestUtils.createBasePayload(null, realConfig, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBasePayload_map_config_configShouldNotBeNull() {
        RequestUtils.createBasePayload(new HashMap<String, Object>(), null, null);
    }

    @Test
    public void testCreateBasePayload_map_config_shouldReturnTheCorrectMap() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("application_id", realConfig.getApplicationCode());
        expected.put("hardware_id", deviceInfo.getHwid());
        expected.put("key1", "value1");
        expected.put("key2", "value2");

        Map<String, Object> input = new HashMap<>();
        input.put("key1", "value1");
        input.put("key2", "value2");

        Map<String, Object> result = RequestUtils.createBasePayload(input, realConfig, null);

        assertEquals(expected, result);
    }

    @Test
    public void testCreateBasePayload_config_appLoginParameters_hasCredentials() {
        int contactFieldId = 123;
        String contactFieldValue = "contactFieldValue";

        Map<String, Object> expected = new HashMap<>();
        expected.put("application_id", realConfig.getApplicationCode());
        expected.put("hardware_id", deviceInfo.getHwid());
        expected.put("contact_field_id", contactFieldId);
        expected.put("contact_field_value", contactFieldValue);

        Map<String, Object> result = RequestUtils.createBasePayload(realConfig, new AppLoginParameters(contactFieldId, contactFieldValue));

        assertEquals(expected, result);
    }

    @Test
    public void testCreateBasePayload_config_appLoginParameters_withoutCredentials() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("application_id", realConfig.getApplicationCode());
        expected.put("hardware_id", deviceInfo.getHwid());

        Map<String, Object> result = RequestUtils.createBasePayload(realConfig, new AppLoginParameters());

        assertEquals(expected, result);
    }

    @Test
    public void testCreateBasePayload_config_whenAppLoginParameters_isNull() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("application_id", realConfig.getApplicationCode());
        expected.put("hardware_id", deviceInfo.getHwid());

        Map<String, Object> result = RequestUtils.createBasePayload(realConfig, null);

        assertEquals(expected, result);
    }
}