package com.emarsys.mobileengage.util;

import android.app.Application;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.core.util.HeaderUtils;
import com.emarsys.core.util.TimestampUtils;
import com.emarsys.mobileengage.BuildConfig;
import com.emarsys.mobileengage.MobileEngageInternal;
import com.emarsys.mobileengage.config.MobileEngageConfig;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.iam.model.IamConversionUtils;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.testUtil.ApplicationTestUtils;
import com.emarsys.mobileengage.testUtil.RandomTestUtils;
import com.emarsys.mobileengage.testUtil.SharedPrefsUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        String expected = "https://mobile-events.eservice.emarsys.net/v3/devices/meId/events";
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
        RequestUtils.createBaseHeaders_V3(null, mock(MeIdStorage.class), mock(MeIdSignatureStorage.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBaseHeaders_V3_meIdStorageShouldNotBeNull() {
        RequestUtils.createBaseHeaders_V3("1234-ABCD", null, mock(MeIdSignatureStorage.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBaseHeaders_V3_meIdSignatureStorageShouldNotBeNull() {
        RequestUtils.createBaseHeaders_V3("1234-ABCD", mock(MeIdStorage.class), null);
    }

    @Test
    public void testCreateBaseHeaders_V3_shouldReturnCorrectMap() {
        String meId = "meid";
        String meIdSignature = "meidsignature";
        MeIdStorage meIdStorage = new MeIdStorage(context);
        meIdStorage.set(meId);
        MeIdSignatureStorage meIdSignatureStorage = new MeIdSignatureStorage(context);
        meIdSignatureStorage.set(meIdSignature);

        Map<String, String> expected = new HashMap<>();
        expected.put("X-ME-ID", meId);
        expected.put("X-ME-ID-SIGNATURE", meIdSignature);
        expected.put("X-ME-APPLICATIONCODE", APPLICATION_CODE);

        Map<String, String> result = RequestUtils.createBaseHeaders_V3(
                APPLICATION_CODE,
                meIdStorage,
                meIdSignatureStorage);

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

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompositeRequestModelPayload_eventsMustNotBeNull() {
        RequestUtils.createCompositeRequestModelPayload(
                null,
                Collections.<DisplayedIam>emptyList(),
                Collections.<ButtonClicked>emptyList(),
                deviceInfo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompositeRequestModelPayload_displayedIamsMustNotBeNull() {
        RequestUtils.createCompositeRequestModelPayload(
                Collections.emptyList(),
                null,
                Collections.<ButtonClicked>emptyList(),
                deviceInfo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompositeRequestModelPayload_buttonClicksMustNotBeNull() {
        RequestUtils.createCompositeRequestModelPayload(
                Collections.emptyList(),
                Collections.<DisplayedIam>emptyList(),
                null,
                deviceInfo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompositeRequestModelPayload_deviceInfoMustNotBeNull() {
        RequestUtils.createCompositeRequestModelPayload(
                Collections.emptyList(),
                Collections.<DisplayedIam>emptyList(),
                Collections.<ButtonClicked>emptyList(),
                null);
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

        Map<String, Object> resultPayload = RequestUtils.createCompositeRequestModelPayload(
                events,
                displayedIams,
                buttonClicks,
                deviceInfo
        );

        assertEquals(expectedPayload, resultPayload);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateInternalCustomEvent_eventNameShouldNotBeNull() {
        RequestUtils.createInternalCustomEvent(
                null,
                new HashMap<String, String>(),
                APPLICATION_CODE,
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateInternalCustomEvent_applicationCodeShouldNotBeNull() {
        RequestUtils.createInternalCustomEvent(
                "eventname",
                new HashMap<String, String>(),
                null,
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateInternalCustomEvent_meIdStorageShouldNotBeNull() {
        RequestUtils.createInternalCustomEvent(
                "eventname",
                new HashMap<String, String>(),
                APPLICATION_CODE,
                null,
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateInternalCustomEvent_meIdSignatureStorageShouldNotBeNull() {
        RequestUtils.createInternalCustomEvent(
                "eventname",
                new HashMap<String, String>(),
                APPLICATION_CODE,
                mock(MeIdStorage.class),
                null,
                mock(TimestampProvider.class)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateInternalCustomEvent_timestampProviderShouldNotBeNull() {
        RequestUtils.createInternalCustomEvent(
                "eventname",
                new HashMap<String, String>(),
                APPLICATION_CODE,
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                null
        );
    }

    @Test
    public void testCreateInternalCustomEvent_withoutAttributes() {
        long timestamp = 90_000;
        TimestampProvider timestampProvider = mock(TimestampProvider.class);
        when(timestampProvider.provideTimestamp()).thenReturn(timestamp);
        String eventName = "name";
        String meId = "12345";
        String meIdSignature = "12345";
        MeIdStorage meIdStorage = mock(MeIdStorage.class);
        when(meIdStorage.get()).thenReturn(meId);
        MeIdSignatureStorage meIdSignatureStorage = mock(MeIdSignatureStorage.class);
        when(meIdSignatureStorage.get()).thenReturn(meIdSignature);

        Map<String, Object> event = new HashMap<>();
        event.put("type", "internal");
        event.put("name", eventName);
        event.put("timestamp", TimestampUtils.formatTimestampWithUTC(timestamp));

        Map<String, Object> payload = new HashMap<>();
        payload.put("clicks", new ArrayList<>());
        payload.put("viewed_messages", new ArrayList<>());
        payload.put("events", Collections.singletonList(event));

        RequestModel actual = RequestUtils.createInternalCustomEvent(
                eventName,
                null,
                APPLICATION_CODE,
                meIdStorage,
                meIdSignatureStorage,
                timestampProvider);

        RequestModel expected = new RequestModel(
                RequestUtils.createEventUrl_V3(meId),
                RequestMethod.POST,
                payload,
                RequestUtils.createBaseHeaders_V3(APPLICATION_CODE, meIdStorage, meIdSignatureStorage),
                timestamp,
                Long.MAX_VALUE,
                actual.getId());

        assertEquals(expected, actual);
    }

    @Test
    public void testCreateInternalCustomEvent_withAttributes() {
        long timestamp = 90_000;
        TimestampProvider timestampProvider = mock(TimestampProvider.class);
        when(timestampProvider.provideTimestamp()).thenReturn(timestamp);
        String eventName = "name";
        String meId = "12345";
        String meIdSignature = "12345";
        MeIdStorage meIdStorage = mock(MeIdStorage.class);
        when(meIdStorage.get()).thenReturn(meId);
        MeIdSignatureStorage meIdSignatureStorage = mock(MeIdSignatureStorage.class);
        when(meIdSignatureStorage.get()).thenReturn(meIdSignature);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", "value2");
        attributes.put("key3", "value3");

        Map<String, Object> event = new HashMap<>();
        event.put("type", "internal");
        event.put("name", eventName);
        event.put("timestamp", TimestampUtils.formatTimestampWithUTC(timestamp));
        event.put("attributes", attributes);

        Map<String, Object> payload = new HashMap<>();
        payload.put("clicks", new ArrayList<>());
        payload.put("viewed_messages", new ArrayList<>());
        payload.put("events", Collections.singletonList(event));

        RequestModel actual = RequestUtils.createInternalCustomEvent(
                eventName,
                attributes,
                APPLICATION_CODE,
                meIdStorage,
                meIdSignatureStorage,
                timestampProvider);

        RequestModel expected = new RequestModel(
                RequestUtils.createEventUrl_V3(meId),
                RequestMethod.POST,
                payload,
                RequestUtils.createBaseHeaders_V3(APPLICATION_CODE, meIdStorage, meIdSignatureStorage),
                timestamp,
                Long.MAX_VALUE,
                actual.getId());

        assertEquals(expected, actual);
    }
}