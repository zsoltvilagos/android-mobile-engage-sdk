package com.emarsys.mobileengage.experimental;

import junit.framework.Assert;

import org.junit.Test;

public class MobileEngageFeatureTest {

    @Test
    public void testGetName() {
        Assert.assertEquals("in_app_messaging", MobileEngageFeature.IN_APP_MESSAGING.getName());
    }

}