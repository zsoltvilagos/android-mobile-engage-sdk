package com.emarsys.mobileengage.experimental;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

public class MobileEngageFeatureTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Test
    public void testGetName() {
        Assert.assertEquals("in_app_messaging", MobileEngageFeature.IN_APP_MESSAGING.getName());
    }

}