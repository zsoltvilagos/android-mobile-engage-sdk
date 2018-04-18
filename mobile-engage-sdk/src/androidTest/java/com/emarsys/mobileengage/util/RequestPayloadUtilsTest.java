package com.emarsys.mobileengage.util;

import android.app.Application;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.DeviceInfo;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.iam.model.IamConversionUtils;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;
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

public class RequestPayloadUtilsTest {
    private static final String APPLICATION_CODE = "applicationCode";
    private static final String APPLICATION_PASSWORD = "applicationPassword";

    private DeviceInfo deviceInfo;
    private MobileEngageConfig realConfig;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setup() {
        SharedPrefsUtils.deleteMobileEngageSharedPrefs();
        realConfig = new MobileEngageConfig.Builder()
                .application((Application) InstrumentationRegistry.getTargetContext().getApplicationContext())
                .credentials(APPLICATION_CODE, APPLICATION_PASSWORD)
                .disableDefaultChannel()
                .build();
        deviceInfo = new DeviceInfo(InstrumentationRegistry.getContext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBasePayload_config_configShouldNotBeNull() {
        RequestPayloadUtils.createBasePayload(null, null, deviceInfo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBasePayload_config_deviceInfoShouldNotBeNUll() {
        RequestPayloadUtils.createBasePayload(realConfig, null, null);
    }

    @Test
    public void testCreateBasePayload_config_shouldReturnTheCorrectPayload() {
        Map<String, Object> payload = RequestPayloadUtils.createBasePayload(realConfig, null, deviceInfo);
        Map<String, Object> expected = RequestPayloadUtils.createBasePayload(new HashMap<String, Object>(), realConfig, null, deviceInfo);
        assertEquals(expected, payload);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBasePayload_map_config_additionalPayloadShouldNotBeNull() {
        RequestPayloadUtils.createBasePayload(null, realConfig, null, deviceInfo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBasePayload_map_config_configShouldNotBeNull() {
        RequestPayloadUtils.createBasePayload(new HashMap<String, Object>(), null, null, deviceInfo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBasePayload_map_config_deviceInfoShouldNotBeNUll() {
        RequestPayloadUtils.createBasePayload(new HashMap<String, Object>(), realConfig, null, null);
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

        Map<String, Object> result = RequestPayloadUtils.createBasePayload(input, realConfig, null, deviceInfo);

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

        Map<String, Object> result = RequestPayloadUtils.createBasePayload(realConfig, new AppLoginParameters(contactFieldId, contactFieldValue), deviceInfo);

        assertEquals(expected, result);
    }

    @Test
    public void testCreateBasePayload_config_appLoginParameters_withoutCredentials() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("application_id", realConfig.getApplicationCode());
        expected.put("hardware_id", deviceInfo.getHwid());

        Map<String, Object> result = RequestPayloadUtils.createBasePayload(realConfig, new AppLoginParameters(), deviceInfo);

        assertEquals(expected, result);
    }

    @Test
    public void testCreateBasePayload_config_whenAppLoginParameters_isNull() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("application_id", realConfig.getApplicationCode());
        expected.put("hardware_id", deviceInfo.getHwid());

        Map<String, Object> result = RequestPayloadUtils.createBasePayload(realConfig, null, deviceInfo);

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