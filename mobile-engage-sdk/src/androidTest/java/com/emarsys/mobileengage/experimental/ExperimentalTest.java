package com.emarsys.mobileengage.experimental;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExperimentalTest {

    @Before
    public void setUp() {
        Experimental.reset();
    }

    @Test
    public void testIsFeatureEnabled_shouldDefaultToBeingTurnedOff() {
        for (FlipperFeature feature : MobileEngageFeature.values()) {
            assertFalse(Experimental.isFeatureEnabled(feature));
        }
    }

    @Test
    public void testIsFeatureEnabled_shouldReturnTrue_whenFeatureIsTurnedOn() {
        Experimental.enableFeature(MobileEngageFeature.IN_APP_MESSAGING);
        assertTrue(Experimental.isFeatureEnabled(MobileEngageFeature.IN_APP_MESSAGING));
    }

    @Test
    public void testEnableFeature_shouldAppendTheFeatureToTheEnabledFeatureset() {
        assertEquals(0, Experimental.enabledFeatures.size());
        Experimental.enableFeature(MobileEngageFeature.IN_APP_MESSAGING);
        assertEquals(1, Experimental.enabledFeatures.size());
    }

    @Test
    public void testEnableFeature_shouldRemoveAllFeaturesFromTheEnabledFeatureset() {
        Experimental.enableFeature(MobileEngageFeature.IN_APP_MESSAGING);
        assertEquals(1, Experimental.enabledFeatures.size());
        Experimental.reset();
        assertEquals(0, Experimental.enabledFeatures.size());
    }
}