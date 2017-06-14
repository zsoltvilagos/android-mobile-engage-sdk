package com.emarsys.mobileengage.util;

import android.app.Application;

import com.emarsys.core.util.HeaderUtils;
import com.emarsys.mobileengage.BuildConfig;
import com.emarsys.mobileengage.MobileEngageConfig;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class DefaultHeaderUtilsTest {

    private MobileEngageConfig config;

    @Before
    public void setup() {
        config = new MobileEngageConfig.Builder()
                .application(mock(Application.class))
                .credentials("applicationCode", "applicationPassword")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDefaultHeaders_configShouldNotBeNull() {
        DefaultHeaderUtils.createDefaultHeaders(null);
    }

    @Test
    public void testCreateDefaultHeaders_returnedValueShouldNotBeNull() {
        assertNotNull(DefaultHeaderUtils.createDefaultHeaders(config));
    }

    @Test
    public void testCreateDefaultHeaders_shouldReturnCorrectMap() {
        Map<String, String> expected = new HashMap<>();
        expected.put("Authorization", HeaderUtils.createBasicAuth(config.getApplicationCode(), config.getApplicationPassword()));
        expected.put("Content-Type", "application/json");
        expected.put("X-MOBILEENGAGE-SDK-VERSION", BuildConfig.VERSION_NAME);

        Map<String, String> result = DefaultHeaderUtils.createDefaultHeaders(config);

        assertEquals(expected, result);
    }

}