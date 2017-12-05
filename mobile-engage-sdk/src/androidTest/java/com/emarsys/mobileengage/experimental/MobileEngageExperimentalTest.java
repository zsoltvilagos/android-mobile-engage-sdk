package com.emarsys.mobileengage.experimental;

import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MobileEngageExperimentalTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setUp() {
        MobileEngageExperimental.reset();
    }

    @Test
    public void testIsFeatureEnabled_shouldDefaultToBeingTurnedOff() {
        for (FlipperFeature feature : MobileEngageFeature.values()) {
            assertFalse(MobileEngageExperimental.isFeatureEnabled(feature));
        }
    }

    @Test
    public void testIsFeatureEnabled_shouldReturnTrue_whenFeatureIsTurnedOn() {
        MobileEngageExperimental.enableFeature(MobileEngageFeature.IN_APP_MESSAGING);
        assertTrue(MobileEngageExperimental.isFeatureEnabled(MobileEngageFeature.IN_APP_MESSAGING));
    }

    @Test
    public void testEnableFeature_shouldAppendTheFeatureToTheEnabledFeatureset() {
        assertEquals(0, MobileEngageExperimental.enabledFeatures.size());
        MobileEngageExperimental.enableFeature(MobileEngageFeature.IN_APP_MESSAGING);
        assertEquals(1, MobileEngageExperimental.enabledFeatures.size());
    }

    @Test
    public void testEnableFeature_shouldRemoveAllFeaturesFromTheEnabledFeatureset() {
        MobileEngageExperimental.enableFeature(MobileEngageFeature.IN_APP_MESSAGING);
        assertEquals(1, MobileEngageExperimental.enabledFeatures.size());
        MobileEngageExperimental.reset();
        assertEquals(0, MobileEngageExperimental.enabledFeatures.size());
    }
}