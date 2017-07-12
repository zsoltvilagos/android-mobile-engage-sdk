package com.emarsys.mobileengage.inbox.model;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

public class NotificationInboxStatusTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testConstructor_notificationsShouldNotBeNull() {
        Assert.assertNotNull(new NotificationInboxStatus(null, 0).getNotifications());
    }

}