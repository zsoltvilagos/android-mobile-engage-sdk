package com.emarsys.mobileengage.util;

import com.emarsys.core.util.HeaderUtils;
import com.emarsys.mobileengage.BuildConfig;
import com.emarsys.mobileengage.MobileEngageConfig;
import com.emarsys.mobileengage.testUtil.ApplicationTestUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DefaultHeaderUtilsTest {

    private MobileEngageConfig debugConfig;
    private MobileEngageConfig releaseConfig;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Before
    public void setup() {
        debugConfig = new MobileEngageConfig.Builder()
                .application(ApplicationTestUtils.applicationDebug())
                .credentials("applicationCode", "applicationPassword")
                .build();

        releaseConfig = new MobileEngageConfig.Builder()
                .application(ApplicationTestUtils.applicationRelease())
                .credentials("applicationCode", "applicationPassword")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDefaultHeaders_configShouldNotBeNull() {
        DefaultHeaderUtils.createDefaultHeaders(null);
    }

    @Test
    public void testCreateDefaultHeaders_returnedValueShouldNotBeNull() {
        assertNotNull(DefaultHeaderUtils.createDefaultHeaders(debugConfig));
    }

    @Test
    public void testCreateDefaultHeaders_debug_shouldReturnCorrectMap() {
        Map<String, String> expected = new HashMap<>();
        expected.put("Authorization", HeaderUtils.createBasicAuth(debugConfig.getApplicationCode(), debugConfig.getApplicationPassword()));
        expected.put("Content-Type", "application/json");
        expected.put("X-MOBILEENGAGE-SDK-VERSION", BuildConfig.VERSION_NAME);
        expected.put("X-MOBILEENGAGE-SDK-MODE", "true");

        Map<String, String> result = DefaultHeaderUtils.createDefaultHeaders(debugConfig);

        assertEquals(expected, result);
    }

    @Test
    public void testCreateDefaultHeaders_release_shouldReturnCorrectMap() {
        Map<String, String> expected = new HashMap<>();
        expected.put("Authorization", HeaderUtils.createBasicAuth(releaseConfig.getApplicationCode(), releaseConfig.getApplicationPassword()));
        expected.put("Content-Type", "application/json");
        expected.put("X-MOBILEENGAGE-SDK-VERSION", BuildConfig.VERSION_NAME);
        expected.put("X-MOBILEENGAGE-SDK-MODE", "false");

        Map<String, String> result = DefaultHeaderUtils.createDefaultHeaders(releaseConfig);

        assertEquals(expected, result);
    }

}