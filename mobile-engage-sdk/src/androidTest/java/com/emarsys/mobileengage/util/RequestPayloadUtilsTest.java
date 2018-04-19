package com.emarsys.mobileengage.util;

import android.app.Application;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.mobileengage.BuildConfig;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.iam.model.IamConversionUtils;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;
import com.emarsys.mobileengage.storage.AppLoginStorage;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.testUtil.RandomTestUtils;
import com.emarsys.mobileengage.testUtil.SharedPrefsUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class RequestPayloadUtilsTest {
    private static final String APPLICATION_CODE = "applicationCode";
    private static final String APPLICATION_PASSWORD = "applicationPassword";
    public static final String MOBILEENGAGE_SDK_VERSION = BuildConfig.VERSION_NAME;
    public static final String PUSH_TOKEN = "pushToken";

    private DeviceInfo deviceInfo;
    private MobileEngageConfig config;
    private AppLoginParameters appLoginParameters;
    private RequestContext requestContext;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setup() {
        SharedPrefsUtils.deleteMobileEngageSharedPrefs();
        config = new MobileEngageConfig.Builder()
                .application((Application) InstrumentationRegistry.getTargetContext().getApplicationContext())
                .credentials(APPLICATION_CODE, APPLICATION_PASSWORD)
                .disableDefaultChannel()
                .build();
        deviceInfo = new DeviceInfo(InstrumentationRegistry.getContext());
        appLoginParameters = new AppLoginParameters(3, "test@test.com");
        requestContext = new RequestContext(
                APPLICATION_CODE,
                deviceInfo,
                mock(AppLoginStorage.class),
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBasePayload_config_configShouldNotBeNull() {
        RequestPayloadUtils.createBasePayload(null, null, deviceInfo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBasePayload_config_deviceInfoShouldNotBeNUll() {
        RequestPayloadUtils.createBasePayload(config, null, null);
    }

    @Test
    public void testCreateBasePayload_config_shouldReturnTheCorrectPayload() {
        Map<String, Object> payload = RequestPayloadUtils.createBasePayload(config, null, deviceInfo);
        Map<String, Object> expected = RequestPayloadUtils.createBasePayload(new HashMap<String, Object>(), config, null, deviceInfo);
        assertEquals(expected, payload);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBasePayload_map_config_additionalPayloadShouldNotBeNull() {
        RequestPayloadUtils.createBasePayload(null, config, null, deviceInfo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBasePayload_map_config_configShouldNotBeNull() {
        RequestPayloadUtils.createBasePayload(new HashMap<String, Object>(), null, null, deviceInfo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBasePayload_map_config_deviceInfoShouldNotBeNUll() {
        RequestPayloadUtils.createBasePayload(new HashMap<String, Object>(), config, null, null);
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

        Map<String, Object> result = RequestPayloadUtils.createBasePayload(input, config, null, deviceInfo);

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

        Map<String, Object> result = RequestPayloadUtils.createBasePayload(config, new AppLoginParameters(contactFieldId, contactFieldValue), deviceInfo);

        assertEquals(expected, result);
    }

    @Test
    public void testCreateBasePayload_config_appLoginParameters_withoutCredentials() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("application_id", config.getApplicationCode());
        expected.put("hardware_id", deviceInfo.getHwid());

        Map<String, Object> result = RequestPayloadUtils.createBasePayload(config, new AppLoginParameters(), deviceInfo);

        assertEquals(expected, result);
    }

    @Test
    public void testCreateBasePayload_config_whenAppLoginParameters_isNull() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("application_id", config.getApplicationCode());
        expected.put("hardware_id", deviceInfo.getHwid());

        Map<String, Object> result = RequestPayloadUtils.createBasePayload(config, null, deviceInfo);

        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAppLoginPayload_config_mustNotBeNull() {
        RequestPayloadUtils.createAppLoginPayload(null, appLoginParameters, requestContext, PUSH_TOKEN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAppLoginPayload_requestContext_mustNotBeNull() {
        RequestPayloadUtils.createAppLoginPayload(config, appLoginParameters, null, PUSH_TOKEN);
    }

    @Test
    public void testCreateAppLoginPayload_withMissingPushToken() {
        Map<String, Object> expected = RequestPayloadUtils.createBasePayload(config, appLoginParameters, deviceInfo);
        expected.put("platform", requestContext.getDeviceInfo().getPlatform());
        expected.put("language", requestContext.getDeviceInfo().getLanguage());
        expected.put("timezone", requestContext.getDeviceInfo().getTimezone());
        expected.put("device_model", requestContext.getDeviceInfo().getModel());
        expected.put("application_version", requestContext.getDeviceInfo().getApplicationVersion());
        expected.put("os_version", requestContext.getDeviceInfo().getOsVersion());
        expected.put("ems_sdk", MOBILEENGAGE_SDK_VERSION);

        expected.put("push_token", false);

        Map<String, Object> result = RequestPayloadUtils.createAppLoginPayload(config, appLoginParameters, requestContext, null);

        assertEquals(expected, result);
    }

    @Test
    public void testCreateAppLoginPayload_withPushToken() {
        Map<String, Object> expected = RequestPayloadUtils.createBasePayload(config, appLoginParameters, deviceInfo);
        expected.put("platform", requestContext.getDeviceInfo().getPlatform());
        expected.put("language", requestContext.getDeviceInfo().getLanguage());
        expected.put("timezone", requestContext.getDeviceInfo().getTimezone());
        expected.put("device_model", requestContext.getDeviceInfo().getModel());
        expected.put("application_version", requestContext.getDeviceInfo().getApplicationVersion());
        expected.put("os_version", requestContext.getDeviceInfo().getOsVersion());
        expected.put("ems_sdk", MOBILEENGAGE_SDK_VERSION);

        expected.put("push_token", PUSH_TOKEN);

        Map<String, Object> result = RequestPayloadUtils.createAppLoginPayload(config, appLoginParameters, requestContext, PUSH_TOKEN);

        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompositeRequestModelPayload_eventsMustNotBeNull() {
        RequestPayloadUtils.createCompositeRequestModelPayload(
                null,
                Collections.<DisplayedIam>emptyList(),
                Collections.<ButtonClicked>emptyList(),
                deviceInfo,
                false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompositeRequestModelPayload_displayedIamsMustNotBeNull() {
        RequestPayloadUtils.createCompositeRequestModelPayload(
                Collections.emptyList(),
                null,
                Collections.<ButtonClicked>emptyList(),
                deviceInfo,
                false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompositeRequestModelPayload_buttonClicksMustNotBeNull() {
        RequestPayloadUtils.createCompositeRequestModelPayload(
                Collections.emptyList(),
                Collections.<DisplayedIam>emptyList(),
                null,
                deviceInfo,
                false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompositeRequestModelPayload_deviceInfoMustNotBeNull() {
        RequestPayloadUtils.createCompositeRequestModelPayload(
                Collections.emptyList(),
                Collections.<DisplayedIam>emptyList(),
                Collections.<ButtonClicked>emptyList(),
                null,
                false);
    }

    @Test
    public void testCreateCompositeRequestModelPayload_payloadContainsDoNotDisturb_whenDoNotDisturbIsTrue() {
        Map<String, Object> payload = RequestPayloadUtils.createCompositeRequestModelPayload(
                Collections.emptyList(),
                Collections.<DisplayedIam>emptyList(),
                Collections.<ButtonClicked>emptyList(),
                deviceInfo,
                true);

        assertTrue((Boolean) payload.get("dnd"));
    }

    @Test
    public void testCreateCompositeRequestModelPayload() {
        List<Object> events = Arrays.asList(
                RandomTestUtils.randomMap(),
                RandomTestUtils.randomMap(),
                RandomTestUtils.randomMap()
        );
        List<DisplayedIam> displayedIams = Arrays.asList(
                RandomTestUtils.randomDisplayedIam(),
                RandomTestUtils.randomDisplayedIam()
        );
        List<ButtonClicked> buttonClicks = Arrays.asList(
                RandomTestUtils.randomButtonClick(),
                RandomTestUtils.randomButtonClick(),
                RandomTestUtils.randomButtonClick()
        );
        Map<String, Object> expectedPayload = new HashMap<>();
        expectedPayload.put("events", events);
        expectedPayload.put("viewed_messages", IamConversionUtils.displayedIamsToArray(displayedIams));
        expectedPayload.put("clicks", IamConversionUtils.buttonClicksToArray(buttonClicks));
        expectedPayload.put("hardware_id", deviceInfo.getHwid());
        expectedPayload.put("language", deviceInfo.getLanguage());
        expectedPayload.put("application_version", deviceInfo.getApplicationVersion());
        expectedPayload.put("ems_sdk", MobileEngageInternal.MOBILEENGAGE_SDK_VERSION);

        Map<String, Object> resultPayload = RequestPayloadUtils.createCompositeRequestModelPayload(
                events,
                displayedIams,
                buttonClicks,
                deviceInfo,
                false);

        assertEquals(expectedPayload, resultPayload);
    }

}