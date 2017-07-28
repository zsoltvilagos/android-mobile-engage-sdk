package com.emarsys.mobileengage.service;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MessagingServiceUtilsTest {

    private Context context;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Before
    public void init() {
        context = InstrumentationRegistry.getContext();
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

}