package com.emarsys.mobileengage;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.rule.ActivityTestRule;

import com.emarsys.mobileengage.fake.FakeActivity;
import com.emarsys.mobileengage.fake.FakeStatusListener;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MobileEngageIntegrationTest {
    private CountDownLatch latch;
    private FakeStatusListener listener;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Rule
    public ActivityTestRule<FakeActivity> mActivityRule = new ActivityTestRule(FakeActivity.class);

    @Before
    public void setup() {
        latch = new CountDownLatch(1);
        listener = new FakeStatusListener(latch, FakeStatusListener.Mode.MAIN_THREAD);
        MobileEngageConfig config = new MobileEngageConfig.Builder()
                .credentials("14C19-A121F", "PaNkfOD90AVpYimMBuZopCpm8OWCrREu")
                .statusListener(listener)
                .build();
        MobileEngage.setup(mActivityRule.getActivity().getApplication(), config);
    }

    @Test
    public void testAppLogin_anonymous() throws Exception {
        eventuallyAssertSuccess(MobileEngage.appLogin());
    }

    @Test
    public void testAppLogin() throws Exception {
        eventuallyAssertSuccess(MobileEngage.appLogin(345, "contactFieldValue"));
    }

    @Test
    public void testAppLogout() throws Exception {
        eventuallyAssertSuccess(MobileEngage.appLogout());
    }

    @Test
    public void testTrackCustomEvent_noAttributes() throws Exception {
        eventuallyAssertSuccess(MobileEngage.trackCustomEvent("customEvent", null));
    }

    @Test
    public void testTrackCustomEvent_emptyAttributes() throws Exception {
        eventuallyAssertSuccess(MobileEngage.trackCustomEvent("customEvent", new HashMap<String, String>()));
    }

    @Test
    public void testTrackCustomEvent_withAttributes() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", "value2");

        eventuallyAssertSuccess(MobileEngage.trackCustomEvent("customEvent", attributes));
    }

    @Test
    public void testTrackMessageOpen_intent() throws Exception {
        Intent intent = new Intent();
        Bundle payload = new Bundle();
        payload.putString("key1", "value1");
        payload.putString("u", "{\"sid\": \"dd8_zXfDdndBNEQi\"}");
        intent.putExtra("payload", payload);

        eventuallyAssertSuccess(MobileEngage.trackMessageOpen(intent));
    }

    private void eventuallyAssertSuccess(String id) throws Exception {
        latch.await();
        assertEquals(1, listener.onStatusLogCount);
        assertEquals(0, listener.onErrorCount);
        assertEquals(id, listener.successId);
        assertNotNull(listener.successLog);
        assertNull(listener.errorId);
        assertNull(listener.errorCause);
    }
}