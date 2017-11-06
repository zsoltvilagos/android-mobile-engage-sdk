package com.emarsys.mobileengage.util;

import android.app.Application;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.util.HeaderUtils;
import com.emarsys.mobileengage.BuildConfig;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.testUtil.ApplicationTestUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RequestUtilsTest {
    private static final String APPLICATION_CODE = "applicationCode";
    private static final String APPLICATION_PASSWORD = "applicationPassword";

    private MobileEngageConfig config;
    private MobileEngageConfig debugConfig;
    private MobileEngageConfig releaseConfig;
    private DeviceInfo deviceInfo;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Before
    public void setup() {
        config = new MobileEngageConfig.Builder()
                .application((Application) InstrumentationRegistry.getTargetContext().getApplicationContext())
                .credentials(APPLICATION_CODE, APPLICATION_PASSWORD)
                .disableDefaultChannel()
                .build();

        debugConfig = new MobileEngageConfig.Builder()
                .application(ApplicationTestUtils.applicationDebug())
                .credentials(APPLICATION_CODE, APPLICATION_PASSWORD)
                .disableDefaultChannel()
                .build();

        releaseConfig = new MobileEngageConfig.Builder()
                .application(ApplicationTestUtils.applicationRelease())
                .credentials(APPLICATION_CODE, APPLICATION_PASSWORD)
                .disableDefaultChannel()
                .build();

        deviceInfo = new DeviceInfo(InstrumentationRegistry.getContext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEventUrl_shouldNotAcceptNull() {
        RequestUtils.createEventUrl(null);
    }

    @Test
    public void testCreateEventUrl_shouldReturnTheCorrectEventUrl() {
        String url = RequestUtils.createEventUrl("my-custom-event");
        String expected = "https://push.eservice.emarsys.net/api/mobileengage/v2/events/my-custom-event";
        assertEquals(expected, url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDefaultHeaders_configShouldNotBeNull() {
        RequestUtils.createDefaultHeaders(null);
    }

    @Test
    public void testCreateDefaultHeaders_returnedValueShouldNotBeNull() {
        assertNotNull(RequestUtils.createDefaultHeaders(debugConfig));
    }

    @Test
    public void testCreateDefaultHeaders_debug_shouldReturnCorrectMap() {
        Map<String, String> expected = new HashMap<>();
        expected.put("Authorization", HeaderUtils.createBasicAuth(debugConfig.getApplicationCode(), debugConfig.getApplicationPassword()));
        expected.put("Content-Type", "application/json");
        expected.put("X-MOBILEENGAGE-SDK-VERSION", BuildConfig.VERSION_NAME);
        expected.put("X-MOBILEENGAGE-SDK-MODE", "debug");

        Map<String, String> result = RequestUtils.createDefaultHeaders(debugConfig);

        assertEquals(expected, result);
    }

    @Test
    public void testCreateDefaultHeaders_release_shouldReturnCorrectMap() {
        Map<String, String> expected = new HashMap<>();
        expected.put("Authorization", HeaderUtils.createBasicAuth(releaseConfig.getApplicationCode(), releaseConfig.getApplicationPassword()));
        expected.put("Content-Type", "application/json");
        expected.put("X-MOBILEENGAGE-SDK-VERSION", BuildConfig.VERSION_NAME);
        expected.put("X-MOBILEENGAGE-SDK-MODE", "production");

        Map<String, String> result = RequestUtils.createDefaultHeaders(releaseConfig);

        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBasePayload_config_configShouldNotBeNull() {
        RequestUtils.createBasePayload(null, null);
    }

    @Test
    public void testCreateBasePayload_config_shouldReturnTheCorrectPayload() {
        Map<String, Object> payload = RequestUtils.createBasePayload(config, null);
        Map<String, Object> expected = RequestUtils.createBasePayload(new HashMap<String, Object>(), config, null);
        assertEquals(expected, payload);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBasePayload_map_config_additionalPayloadShouldNotBeNull() {
        RequestUtils.createBasePayload(null, config, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBasePayload_map_config_configShouldNotBeNull() {
        RequestUtils.createBasePayload(new HashMap<String, Object>(), null, null);
    }

    @Test
    public void testCreateBasePayload_map_config_shouldReturnTheCorrectMap() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("application_id", config.getApplicationCode());
        expected.put("hardware_id", deviceInfo.getHwid());
        expected.put("key1", "value1");
        expected.put("key2", "value2");

        Map<String, Object> input = new HashMap<>();
        input.put("key1", "value1");
        input.put("key2", "value2");

        Map<String, Object> result = RequestUtils.createBasePayload(input, config, null);

        assertEquals(expected, result);
    }

    @Test
    public void testCreateBasePayload_config_appLoginParameters_hasCredentials() {
        int contactFieldId = 123;
        String contactFieldValue = "contactFieldValue";

        Map<String, Object> expected = new HashMap<>();
        expected.put("application_id", config.getApplicationCode());
        expected.put("hardware_id", deviceInfo.getHwid());
        expected.put("contact_field_id", contactFieldId);
        expected.put("contact_field_value", contactFieldValue);

        Map<String, Object> result = RequestUtils.createBasePayload(config, new AppLoginParameters(contactFieldId, contactFieldValue));

        assertEquals(expected, result);
    }

    @Test
    public void testCreateBasePayload_config_appLoginParameters_withoutCredentials() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("application_id", config.getApplicationCode());
        expected.put("hardware_id", deviceInfo.getHwid());

        Map<String, Object> result = RequestUtils.createBasePayload(config, new AppLoginParameters());

        assertEquals(expected, result);
    }

    @Test
    public void testCreateBasePayload_config_whenAppLoginParameters_isNull() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("application_id", config.getApplicationCode());
        expected.put("hardware_id", deviceInfo.getHwid());

        Map<String, Object> result = RequestUtils.createBasePayload(config, null);

        assertEquals(expected, result);
    }
}