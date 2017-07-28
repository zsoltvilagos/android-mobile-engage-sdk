package com.emarsys.mobileengage.inbox.model;

import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public class NotificationCacheTest {

    private NotificationCache notificationCache;

    @Before
    public void init() {
        notificationCache = new NotificationCache();
        NotificationCache.internalCache.clear();
    }

    @Test
    public void testCache(){
        Notification notification = mock(Notification.class);
        notificationCache.cache(notification);

        Assert.assertFalse(NotificationCache.internalCache.isEmpty());
        Assert.assertEquals(notification, NotificationCache.internalCache.get(0));
    }

    @Test
    public void testCache_ignoresNull() {
        Notification notification1 = mock(Notification.class);
        Notification notification2 = mock(Notification.class);

        notificationCache.cache(notification1);
        notificationCache.cache(null);
        notificationCache.cache(notification2);

        Assert.assertEquals(2, NotificationCache.internalCache.size());
        Assert.assertEquals(notification1, NotificationCache.internalCache.get(0));
        Assert.assertEquals(notification2, NotificationCache.internalCache.get(1));
    }
}