package com.emarsys.mobileengage.inbox;

import com.emarsys.mobileengage.fake.FakeInboxResultListener;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.emarsys.mobileengage.fake.FakeInboxResultListener.Mode;

public class InboxInternalTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    private static List<Notification> notificationList;

    InboxInternal inbox;
    CountDownLatch latch;

    @Before
    public void init() {
        latch = new CountDownLatch(1);
        notificationList = Arrays.asList(
                new Notification("id1", "title1", Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap(), 100, new Date(1000)),
                new Notification("id2", "title2", Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap(), 200, new Date(2000)),
                new Notification("id3", "title3", Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap(), 300, new Date(3000))

        );
        inbox = new InboxInternal();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFetchNotifications_listenerShouldNotBeNull() {
        inbox.fetchNotifications(null);
    }

    @Test
    public void testFetchNotifications_listener_shouldReturnPredefinedResult() throws InterruptedException {
        FakeInboxResultListener listener = new FakeInboxResultListener(latch);
        inbox.fetchNotifications(listener);

        latch.await();

        Assert.assertEquals(notificationList, listener.resultList);
        Assert.assertEquals(1, listener.successCount);
    }

    @Test
    public void testFetchNotifications_listener_success_shouldBeCalledOnMainThread() throws InterruptedException {
        FakeInboxResultListener listener = new FakeInboxResultListener(latch, Mode.MAIN_THREAD);
        inbox.fetchNotifications(listener);

        latch.await();

        Assert.assertEquals(1, listener.successCount);
    }
}