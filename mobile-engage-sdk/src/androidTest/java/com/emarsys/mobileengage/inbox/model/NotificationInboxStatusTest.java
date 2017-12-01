package com.emarsys.mobileengage.inbox.model;

import android.support.test.rule.DisableOnAndroidDebug;

import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

public class NotificationInboxStatusTest {

    @Rule
    public TestRule timeout = new DisableOnAndroidDebug(Timeout.seconds(TimeoutUtils.getTimeout()));

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testConstructor_notificationsShouldNotBeNull() {
        Assert.assertNotNull(new NotificationInboxStatus(null, 0).getNotifications());
    }

}
