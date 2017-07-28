package com.emarsys.mobileengage.service;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.emarsys.mobileengage.inbox.model.Notification;
import com.emarsys.mobileengage.inbox.model.NotificationCache;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MessagingServiceUtilsTest {

    private Context context;
    private List<Notification> notificationCache;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Before
    @SuppressWarnings("unchecked")
    public void init() throws Exception {
        context = InstrumentationRegistry.getContext();

        Field cacheField = NotificationCache.class.getDeclaredField("internalCache");
        cacheField.setAccessible(true);
        notificationCache = (List) cacheField.get(null);
        notificationCache.clear();
    }

    @Test
    public void getSmallIconResourceId_shouldBePositive() {
        assertTrue(MessagingServiceUtils.getSmallIconResourceId(context) > 0);
    }

    @Test
    public void isMobileEngageMessage_shouldBeFalse_withEmptyData() {
        Map<String, String> remoteMessageData = new HashMap<>();
        assertFalse(MessagingServiceUtils.isMobileEngageMessage(remoteMessageData));
    }

    @Test
    public void isMobileEngageMessage_shouldBeTrue_withDataWhichContainsTheCorrectKey() {
        Map<String, String> remoteMessageData = new HashMap<>();
        remoteMessageData.put("ems_msg", "value");
        assertTrue(MessagingServiceUtils.isMobileEngageMessage(remoteMessageData));
    }

    @Test
    public void isMobileEngageMessage_shouldBeFalse_withDataWithout_ems_msg() {
        Map<String, String> remoteMessageData = new HashMap<>();
        remoteMessageData.put("key1", "value1");
        remoteMessageData.put("key2", "value2");
        assertFalse(MessagingServiceUtils.isMobileEngageMessage(remoteMessageData));
    }

    @Test
    public void createIntent() {
        Map<String, String> remoteMessageData = new HashMap<>();
        remoteMessageData.put("key1", "value1");
        remoteMessageData.put("key2", "value2");

        Intent resultIntent = MessagingServiceUtils.createIntent(remoteMessageData, context);
        assertEquals("value1", resultIntent.getBundleExtra("payload").getString("key1"));
        assertEquals("value2", resultIntent.getBundleExtra("payload").getString("key2"));
    }

    @Test
    public void createNotification_shouldNotBeNull() {
        Map<String, String> input = new HashMap<>();
        input.put("title", "titletex");

        assertNotNull(MessagingServiceUtils.createNotification(input, context));
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

        Assert.assertEquals(1, notificationCache.size());

        Notification result = notificationCache.get(0);

        Assert.assertEquals("21022.150123121212.43223434c3b9", result.getId());
        Assert.assertEquals("sid_here", result.getSid());
        Assert.assertEquals("hello there", result.getTitle());
        Assert.assertEquals(customData, result.getCustomData());
        Assert.assertTrue(before <= result.getReceivedAt());
        Assert.assertTrue(result.getReceivedAt() <= after);
    }

}