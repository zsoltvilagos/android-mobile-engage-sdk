package com.emarsys.mobileengage.service;

import android.support.test.runner.AndroidJUnit4;

import com.emarsys.mobileengage.MobileEngage;
import com.emarsys.mobileengage.inbox.InboxInternal;
import com.emarsys.mobileengage.inbox.model.Notification;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class MessagingServiceUtilsTest_withMobileEngage {
    private InboxInternal inboxInternal;
    private List<Notification> cache;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Before
    public void init() throws Exception {
        cache = new ArrayList<>();

        inboxInternal = mock(InboxInternal.class);
        when(inboxInternal.getNotificationCache()).thenReturn(cache);

        Field inboxInstanceField = MobileEngage.class.getDeclaredField("inboxInstance");
        inboxInstanceField.setAccessible(true);
        inboxInstanceField.set(null, inboxInternal);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCacheNotification_shouldNotAcceptNull(){
        MessagingServiceUtils.cacheNotification(null);
    }

    @Test
    public void testCacheNotification_shouldCacheNotification(){
        Map<String, String> remoteData = new HashMap<>();
        remoteData.put("ems_msg", "true");
        remoteData.put("u", "{\"test_field\":\"\",\"image\":\"https:\\/\\/media.giphy.com\\/media\\/ktvFa67wmjDEI\\/giphy.gif\",\"deep_link\":\"lifestylelabels.com\\/mobile\\/product\\/3245678\",\"sid\":\"sid_here\"}");
        remoteData.put("id", "21022.150123121212.43223434c3b9");
        remoteData.put("inbox", "true");
        remoteData.put("title", "hello there");
        remoteData.put("rootParam1", "param_param");

        Map<String, String> customData = new HashMap<>();
        customData.put("test_field", "");
        customData.put("image", "https://media.giphy.com/media/ktvFa67wmjDEI/giphy.gif");
        customData.put("deep_link", "lifestylelabels.com/mobile/product/3245678");
        customData.put("sid", "sid_here");

        long before = System.currentTimeMillis();
        MessagingServiceUtils.cacheNotification(remoteData);
        long after = System.currentTimeMillis();

        Assert.assertEquals(1, cache.size());

        Notification result = cache.get(0);

        Assert.assertEquals("21022.150123121212.43223434c3b9", result.getId());
        Assert.assertEquals("sid_here", result.getSid());
        Assert.assertEquals("hello there", result.getTitle());
        Assert.assertEquals(customData, result.getCustomData());
        Assert.assertTrue(before <= result.getReceivedAt());
        Assert.assertTrue(result.getReceivedAt() <= after);
    }
}