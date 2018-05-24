package com.emarsys.mobileengage.service;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;

import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

public class IntentUtilsTest {

    private Context context;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        context = InstrumentationRegistry.getContext();

    }

    @Test
    public void createIntent() {
        Map<String, String> remoteMessageData = new HashMap<>();
        remoteMessageData.put("key1", "value1");
        remoteMessageData.put("key2", "value2");

        Intent resultIntent = IntentUtils.createIntent(remoteMessageData, context, "action");
        assertEquals("action", resultIntent.getAction());
        assertEquals("value1", resultIntent.getBundleExtra("payload").getString("key1"));
        assertEquals("value2", resultIntent.getBundleExtra("payload").getString("key2"));
    }

    @Test
    public void createIntent_withoutAction() {
        Intent resultIntent = IntentUtils.createIntent(new HashMap<String, String>(), context, null);
        assertEquals(null, resultIntent.getAction());
    }


}