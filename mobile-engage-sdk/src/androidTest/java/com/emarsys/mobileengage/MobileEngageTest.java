package com.emarsys.mobileengage;

import android.app.Application;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.emarsys.mobileengage.inbox.InboxInternal;
import com.emarsys.mobileengage.inbox.InboxResultListener;
import com.emarsys.mobileengage.inbox.ResetBadgeCountResultListener;
import com.emarsys.mobileengage.inbox.model.Notification;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class MobileEngageTest {
    private static final String appID = "56789876";
    private static final String appSecret = "secret";

    private MobileEngageInternal mobileEngageInternal;
    private InboxInternal inboxInternal;
    private Application application;
    private MobileEngageConfig baseConfig;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Before
    public void init() {
        application = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        mobileEngageInternal = mock(MobileEngageInternal.class);
        inboxInternal = mock(InboxInternal.class);
        baseConfig = new MobileEngageConfig.Builder()
                .application(application)
                .credentials(appID, appSecret)
                .build();
        MobileEngage.inboxInstance = inboxInternal;
        MobileEngage.instance = mobileEngageInternal;
    }

    @Test
    public void testSetup_initializesMobileEngageInstance() {
        MobileEngage.instance = null;
        MobileEngage.setup(baseConfig);

        assertNotNull(MobileEngage.instance);
    }

    @Test
    public void testSetup_initializesInboxInstance() {
        MobileEngage.inboxInstance = null;
        MobileEngage.setup(baseConfig);

        assertNotNull(MobileEngage.inboxInstance);
    }

    @Test
    public void testSetup_initializesWithConfig() {
        MobileEngage.config = null;
        MobileEngage.setup(baseConfig);

        assertEquals(baseConfig, MobileEngage.getConfig());
    }

    @Test
    public void testSetPushToken_callsInternal() {
        String pushtoken = "pushtoken";
        MobileEngage.setPushToken(pushtoken);
        verify(mobileEngageInternal).setPushToken(pushtoken);
    }

    @Test
    public void testSetStatusListener_callsInternal() {
        MobileEngageStatusListener listener = mock(MobileEngageStatusListener.class);
        MobileEngage.setStatusListener(listener);
        verify(mobileEngageInternal).setStatusListener(listener);
    }

    @Test
    public void testAppLogin_anonymous_callsInternalMobileEngage() {
        MobileEngage.appLogin();
        verify(mobileEngageInternal).appLogin();
    }

    @Test
    public void testAppLogin_anonymous_callsInternalInbox() {
        MobileEngage.appLogin();
        verify(inboxInternal).setAppLoginParameters(new AppLoginParameters());
    }

    @Test
    public void testAppLogin_withUser_callsInternalMobileEngage() {
        MobileEngage.appLogin(4, "CONTACT_FIELD_VALUE");
        verify(mobileEngageInternal).appLogin(4, "CONTACT_FIELD_VALUE");
    }

    @Test
    public void testAppLogin_withUser_callsInternalInbox() {
        MobileEngage.appLogin(4, "CONTACT_FIELD_VALUE");
        verify(inboxInternal).setAppLoginParameters(new AppLoginParameters(4, "CONTACT_FIELD_VALUE"));
    }

    @Test
    public void testAppLogout_callsInternalMobileEngage() {
        MobileEngage.appLogout();
        verify(mobileEngageInternal).appLogout();
    }

    @Test
    public void testAppLogout_callsInternalInbox() {
        MobileEngage.appLogout();
        verify(inboxInternal).setAppLoginParameters(null);
    }

    @Test
    public void testTrackCustomEvent_callsInternal() throws Exception {
        Map<String, String> attributes = mock(Map.class);
        MobileEngage.trackCustomEvent("event", attributes);
        verify(mobileEngageInternal).trackCustomEvent("event", attributes);
    }

    @Test
    public void testTrackMessageOpen_intent_callsInternal() {
        Intent intent = mock(Intent.class);
        MobileEngage.trackMessageOpen(intent);
        verify(mobileEngageInternal).trackMessageOpen(intent);
    }

    @Test
    public void testTrackMessageOpen_message_callsInternal() throws JSONException {
        Notification message = new Notification("id", "sid", "title", new HashMap<String, String>(), new JSONObject(), 7200, new Date().getTime());
        MobileEngage.trackMessageOpen(message);
        verify(mobileEngageInternal).trackMessageOpen(message);
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

    @Test(expected = IllegalArgumentException.class)
    public void testFetchNotifications_whenListenerIsNull() {
        MobileEngage.Inbox.fetchNotifications(null);
    }

    @Test
    public void testFetchNotifications_callsInternal() {
        InboxResultListener inboxListenerMock = mock(InboxResultListener.class);
        MobileEngage.Inbox.fetchNotifications(inboxListenerMock);
        verify(inboxInternal).fetchNotifications(inboxListenerMock);
    }

    @Test
    public void testResetBadgeCount_callsInternal() {
        ResetBadgeCountResultListener listener = mock(ResetBadgeCountResultListener.class);
        MobileEngage.Inbox.resetBadgeCount(listener);
        verify(inboxInternal).resetBadgeCount(listener);
    }

    @Test
    public void testResetBadgeCount_zeroArgs_callsInternal_withNullListener() {
        MobileEngage.Inbox.resetBadgeCount();
        verify(inboxInternal).resetBadgeCount(null);
    }
}