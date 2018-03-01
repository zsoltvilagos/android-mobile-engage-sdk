package com.emarsys.mobileengage.iam;

import com.emarsys.mobileengage.MobileEngage;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DoNotDisturbProviderTest {

    @Test
    public void testIsPaused_returnsTrue_ifInAppIsSetToBePaused() {
        MobileEngage.InApp.setPaused(true);
        DoNotDisturbProvider provider = new DoNotDisturbProvider();

        assertTrue(provider.isPaused());
    }

    @Test
    public void testIsPaused_returnsFalse_ifInAppIsSetToBeResumed() {
        MobileEngage.InApp.setPaused(false);
        DoNotDisturbProvider provider = new DoNotDisturbProvider();

        assertFalse(provider.isPaused());
    }
}