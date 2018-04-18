package com.emarsys.mobileengage.util;

import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.core.util.TimestampUtils;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.testUtil.SharedPrefsUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestUtilsTest {
    private static final String APPLICATION_CODE = "applicationCode";
    public static final String VALID_CUSTOM_EVENT_V3 = "https://mobile-events.eservice.emarsys.net/v3/devices/12345/events";

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setup() {
        SharedPrefsUtils.deleteMobileEngageSharedPrefs();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsCustomEvent_V3_mustNotBeNull() {
        RequestUtils.isCustomEvent_V3(null);
    }

    @Test
    public void testIsCustomEvent_V3_returnsTrue_ifIndeedV3Event() {
        RequestModel requestModel = new RequestModel.Builder()
                .url(VALID_CUSTOM_EVENT_V3)
                .build();

        assertTrue(RequestUtils.isCustomEvent_V3(requestModel));
    }

    @Test
    public void testIsCustomEvent_V3_returnsFalse_ifThereIsNoMatch() {
        RequestModel requestModel = new RequestModel.Builder()
                .url("https://www.google.com")
                .build();

        assertFalse(RequestUtils.isCustomEvent_V3(requestModel));
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
                RequestUrlUtils.createEventUrl_V3(meId),
                RequestMethod.POST,
                payload,
                RequestHeaderUtils.createBaseHeaders_V3(APPLICATION_CODE, meIdStorage, meIdSignatureStorage),
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
                RequestUrlUtils.createEventUrl_V3(meId),
                RequestMethod.POST,
                payload,
                RequestHeaderUtils.createBaseHeaders_V3(APPLICATION_CODE, meIdStorage, meIdSignatureStorage),
                timestamp,
                Long.MAX_VALUE,
                actual.getId());

        assertEquals(expected, actual);
    }
}