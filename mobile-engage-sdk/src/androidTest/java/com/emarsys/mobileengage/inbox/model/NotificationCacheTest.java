package com.emarsys.mobileengage.inbox.model;

import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public class NotificationCacheTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    private NotificationCache notificationCache;

    private Notification notification1;
    private Notification notification2;
    private Notification notification3;
    private Notification notification4;
    private Notification notification5;

    @Before
    public void init() {
        notificationCache = new NotificationCache();
        NotificationCache.internalCache.clear();

        notification1 = mock(Notification.class);
        notification2 = mock(Notification.class);
        notification3 = mock(Notification.class);
        notification4 = mock(Notification.class);
        notification5 = mock(Notification.class);
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
        notificationCache.cache(notification1);
        notificationCache.cache(null);
        notificationCache.cache(notification2);

        Assert.assertEquals(2, NotificationCache.internalCache.size());
        Assert.assertEquals(notification2, NotificationCache.internalCache.get(0));
        Assert.assertEquals(notification1, NotificationCache.internalCache.get(1));
    }

    @Test
    public void testMerge_withEmptyLists(){
        List<Notification> result = notificationCache.merge(new ArrayList<Notification>());
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testMerge_withEmptyCache(){
        List<Notification> fetched = new ArrayList<>(Arrays.asList(notification1, notification2, notification3));
        List<Notification> result = notificationCache.merge(fetched);

        List<Notification> expected = new ArrayList<>(Arrays.asList(notification1, notification2, notification3));

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testMerge_withEmptyFetched(){
        notificationCache.cache(notification3);
        notificationCache.cache(notification2);
        notificationCache.cache(notification1);

        List<Notification> fetched = new ArrayList<>();
        List<Notification> result = notificationCache.merge(fetched);

        List<Notification> expected = new ArrayList<>(Arrays.asList(notification1, notification2, notification3));

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testMerge_withNonEmptyLists(){
        notificationCache.cache(notification3);
        notificationCache.cache(notification2);
        notificationCache.cache(notification1);

        List<Notification> fetched = new ArrayList<>();
        fetched.add(notification4);
        fetched.add(notification5);

        List<Notification> result = notificationCache.merge(fetched);

        List<Notification> expected = new ArrayList<>(
                Arrays.asList(
                        notification1,
                        notification2,
                        notification3,
                        notification4,
                        notification5));

        Assert.assertEquals(expected, result);
    }
}