package com.emarsys.mobileengage.util;

import android.app.Application;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.util.HeaderUtils;
import com.emarsys.mobileengage.BuildConfig;
import com.emarsys.mobileengage.config.MobileEngageConfig;
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class RequestHeaderUtilsTest {
    private static final String APPLICATION_CODE = "applicationCode";
    private static final String APPLICATION_PASSWORD = "applicationPassword";

    private MobileEngageConfig realConfig;
    private MobileEngageConfig mockDebugConfig;
    private MobileEngageConfig mockReleaseConfig;
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
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBaseHeaders_V2_configShouldNotBeNull() {
        RequestHeaderUtils.createBaseHeaders_V2(null);
    }

    @Test
    public void testCreateBaseHeaders_V2_shouldReturnCorrectMap() {
        Map<String, String> expected = new HashMap<>();
        expected.put("Authorization", HeaderUtils.createBasicAuth(realConfig.getApplicationCode(), realConfig.getApplicationPassword()));

        Map<String, String> result = RequestHeaderUtils.createBaseHeaders_V2(realConfig);

        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBaseHeaders_V3_configShouldNotBeNull() {
        RequestHeaderUtils.createBaseHeaders_V3(null, mock(MeIdStorage.class), mock(MeIdSignatureStorage.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBaseHeaders_V3_meIdStorageShouldNotBeNull() {
        RequestHeaderUtils.createBaseHeaders_V3("1234-ABCD", null, mock(MeIdSignatureStorage.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBaseHeaders_V3_meIdSignatureStorageShouldNotBeNull() {
        RequestHeaderUtils.createBaseHeaders_V3("1234-ABCD", mock(MeIdStorage.class), null);
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

        Map<String, String> result = RequestHeaderUtils.createBaseHeaders_V3(
                APPLICATION_CODE,
                meIdStorage,
                meIdSignatureStorage);

        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDefaultHeaders_configShouldNotBeNull() {
        RequestHeaderUtils.createDefaultHeaders(null);
    }

    @Test
    public void testCreateDefaultHeaders_returnedValueShouldNotBeNull() {
        assertNotNull(RequestHeaderUtils.createDefaultHeaders(mockDebugConfig));
    }

    @Test
    public void testCreateDefaultHeaders_debug_shouldReturnCorrectMap() {
        Map<String, String> expected = new HashMap<>();
        expected.put("Content-Type", "application/json");
        expected.put("X-MOBILEENGAGE-SDK-VERSION", BuildConfig.VERSION_NAME);
        expected.put("X-MOBILEENGAGE-SDK-MODE", "debug");

        Map<String, String> result = RequestHeaderUtils.createDefaultHeaders(mockDebugConfig);

        assertEquals(expected, result);
    }

    @Test
    public void testCreateDefaultHeaders_release_shouldReturnCorrectMap() {
        Map<String, String> expected = new HashMap<>();
        expected.put("Content-Type", "application/json");
        expected.put("X-MOBILEENGAGE-SDK-VERSION", BuildConfig.VERSION_NAME);
        expected.put("X-MOBILEENGAGE-SDK-MODE", "production");

        Map<String, String> result = RequestHeaderUtils.createDefaultHeaders(mockReleaseConfig);

        assertEquals(expected, result);
    }

}