package com.emarsys.mobileengage;

import android.app.Application;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class MobileEngageTest {
    private MobileEngageInternal mobileEngageInternal;
    private Application application;

    @Before
    public void init() {
        application = mock(Application.class);
        when(application.getApplicationContext()).thenReturn(InstrumentationRegistry.getTargetContext());
        mobileEngageInternal = mock(MobileEngageInternal.class);
    }

    @Test
    public void testSetup_initializesInstance() {
        String appID = "56789876";
        String appSecret = "secret";
        MobileEngageConfig baseConfig = new MobileEngageConfig.Builder()
                .credentials(appID, appSecret)
                .build();

        MobileEngage.instance = null;
        MobileEngage.setup(application, baseConfig);

        assertNotNull(MobileEngage.instance);
    }

    @Test
    public void testSetPushToken() {
        String pushtoken = "pushtoken";
        MobileEngage.instance = mobileEngageInternal;
        MobileEngage.setPushToken(pushtoken);
        verify(mobileEngageInternal).setPushToken(pushtoken);
    }

    @Test
    public void testAppLogin_anonymous_callsInternal() {
        MobileEngage.instance = mobileEngageInternal;
        MobileEngage.appLogin();
        verify(mobileEngageInternal).appLogin();
    }

    @Test
    public void testAppLogin_withUser() {
        MobileEngage.instance = mobileEngageInternal;
        MobileEngage.appLogin(4, "contactFieldValue");
        verify(mobileEngageInternal).appLogin(4, "contactFieldValue");
    }

    @Test
    public void testAppLogout() {
        MobileEngage.instance = mobileEngageInternal;
        MobileEngage.appLogout();
        verify(mobileEngageInternal).appLogout();
    }

    @Test
    public void testTrackCustomEvent() throws Exception {
        MobileEngage.instance = mobileEngageInternal;
        Map<String, String> attributes = mock(Map.class);
        MobileEngage.trackCustomEvent("event", attributes);
        verify(mobileEngageInternal).trackCustomEvent("event", attributes);
    }

    @Test
    public void testTrackMessageOpen() {
        MobileEngage.instance = mobileEngageInternal;
        Intent intent = mock(Intent.class);
        MobileEngage.trackMessageOpen(intent);
        verify(mobileEngageInternal).trackMessageOpen(intent);
    }
}