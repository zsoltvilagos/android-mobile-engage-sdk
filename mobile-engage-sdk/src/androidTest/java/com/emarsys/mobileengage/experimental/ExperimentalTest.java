package com.emarsys.mobileengage.experimental;

import org.junit.Before;
import org.junit.Test;

import static com.emarsys.mobileengage.experimental.Experimental.enableFeature;
import static com.emarsys.mobileengage.experimental.Experimental.isFeatureEnabled;
import static com.emarsys.mobileengage.experimental.Experimental.reset;
import static com.emarsys.mobileengage.experimental.FlipperFeatures.INAPP_MESSAGING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExperimentalTest {

    @Before
    public void setUp() {
        reset();
    }

    @Test
    public void testIsFeatureEnabled_shouldDefaultToBeingTurnedOff() {
        for (FlipperFeature feature : FlipperFeatures.values()) {
            assertFalse(isFeatureEnabled(feature));
        }
    }

    @Test
    public void testIsFeatureEnabled_shouldReturnTrue_whenFeatureIsTurnedOn() {
        enableFeature(INAPP_MESSAGING);
        assertTrue(isFeatureEnabled(INAPP_MESSAGING));
    }

    @Test
    public void testEnableFeature_shouldAppendTheFeatureToTheEnabledFeatureset() {
        assertEquals(0, Experimental.enabledFeatures.size());
        enableFeature(INAPP_MESSAGING);
        assertEquals(1, Experimental.enabledFeatures.size());
    }

    @Test
    public void testEnableFeature_shouldRemoveAllFeaturesFromTheEnabledFeatureset() {
        enableFeature(INAPP_MESSAGING);
        assertEquals(1, Experimental.enabledFeatures.size());
        reset();
        assertEquals(0, Experimental.enabledFeatures.size());
    }
}