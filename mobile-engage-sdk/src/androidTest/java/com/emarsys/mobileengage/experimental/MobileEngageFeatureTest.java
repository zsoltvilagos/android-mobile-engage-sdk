package com.emarsys.mobileengage.experimental;

import android.support.test.rule.DisableOnAndroidDebug;

import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

public class MobileEngageFeatureTest {

    @Rule
    public TestRule timeout = new DisableOnAndroidDebug(Timeout.seconds(TimeoutUtils.getTimeout()));

    @Test
    public void testGetName() {
        Assert.assertEquals("in_app_messaging", MobileEngageFeature.IN_APP_MESSAGING.getName());
    }

}