package com.emarsys.mobileengage.util;

import android.app.Application;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.util.HeaderUtils;
import com.emarsys.mobileengage.BuildConfig;
import com.emarsys.mobileengage.MobileEngageConfig;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DefaultHeaderUtilsTest {

    private MobileEngageConfig config;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Before
    public void setup() {
        Application application = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();

        config = new MobileEngageConfig.Builder()
                .application(application)
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