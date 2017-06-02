package com.emarsys.mobileengage;

import android.app.Application;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class MobileEngageTest {
    private MobileEngageInternal mobileEngageInternal;
    private Application application;
    private MobileEngageConfig baseConfig;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Before
    public void init() {
        String appID = "56789876";
        String appSecret = "secret";

        application = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        mobileEngageInternal = mock(MobileEngageInternal.class);
        baseConfig = new MobileEngageConfig.Builder()
                .application(application)
                .credentials(appID, appSecret)
                .build();
    }

    @Test
    public void testSetup_initializesInstance() {
        MobileEngage.instance = null;
        MobileEngage.setup(baseConfig);

        assertNotNull(MobileEngage.instance);
    }

    @Test
    public void testSetPushToken_callsInternal() {
        String pushtoken = "pushtoken";
        MobileEngage.instance = mobileEngageInternal;
        MobileEngage.setPushToken(pushtoken);
        verify(mobileEngageInternal).setPushToken(pushtoken);
    }

    @Test
    public void testSetStatusListener_callsInternal() {
        MobileEngageStatusListener listener = mock(MobileEngageStatusListener.class);
        MobileEngage.instance = mobileEngageInternal;
        MobileEngage.setStatusListener(listener);
        verify(mobileEngageInternal).setStatusListener(listener);
    }

    @Test
    public void testAppLogin_anonymous_callsInternal() {
        MobileEngage.instance = mobileEngageInternal;
        MobileEngage.appLogin();
        verify(mobileEngageInternal).appLogin();
    }

    @Test
    public void testAppLogin_withUser_callsInternal() {
        MobileEngage.instance = mobileEngageInternal;
        MobileEngage.appLogin(4, "CONTACT_FIELD_VALUE");
        verify(mobileEngageInternal).appLogin(4, "CONTACT_FIELD_VALUE");
    }

    @Test
    public void testAppLogout_callsInternal() {
        MobileEngage.instance = mobileEngageInternal;
        MobileEngage.appLogout();
        verify(mobileEngageInternal).appLogout();
    }

    @Test
    public void testTrackCustomEvent_callsInternal() throws Exception {
        MobileEngage.instance = mobileEngageInternal;
        Map<String, String> attributes = mock(Map.class);
        MobileEngage.trackCustomEvent("event", attributes);
        verify(mobileEngageInternal).trackCustomEvent("event", attributes);
    }

    @Test
    public void testTrackMessageOpen_intent_callsInternal() {
        MobileEngage.instance = mobileEngageInternal;
        Intent intent = mock(Intent.class);
        MobileEngage.trackMessageOpen(intent);
        verify(mobileEngageInternal).trackMessageOpen(intent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetup_whenConfigIsNull() {
        MobileEngage.setup(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppLogin_whenContactFieldValueIsNull() {
        MobileEngage.appLogin(0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackCustomEvent_whenEventNameIsNull() {
        MobileEngage.trackCustomEvent(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTrackMessageOpen_intent_whenIntentIsNull() {
        MobileEngage.trackMessageOpen((Intent) null);
    }
}