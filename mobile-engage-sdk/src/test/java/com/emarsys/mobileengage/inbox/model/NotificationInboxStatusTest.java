package com.emarsys.mobileengage.inbox.model;

import junit.framework.Assert;

import org.junit.Test;

public class NotificationInboxStatusTest {

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testConstructor_notificationsShouldNotBeNull() {
        Assert.assertNotNull(new NotificationInboxStatus(null, 0).getNotifications());
    }

}