package com.emarsys.mobileengage.notification.command;

import android.app.Application;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.emarsys.mobileengage.EventHandler;
import com.emarsys.mobileengage.MobileEngage;
import com.emarsys.mobileengage.config.MobileEngageConfig;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AppEventCommandTest {

    private MobileEngageConfig config;
    private Context applicationContext = InstrumentationRegistry.getTargetContext().getApplicationContext();
    private EventHandler notificationHandler;

    @Before
    public void setUp() {
        notificationHandler = mock(EventHandler.class);
        config = new MobileEngageConfig.Builder()
                .application((Application) applicationContext)
                .credentials("EMSEC-B103E", "RM1ZSuX8mgRBhQIgOsf6m8bn/bMQLAIb")
                .setNotificationEventHandler(notificationHandler)
                .disableDefaultChannel()
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_shouldThrowException_whenThereIsNoEventName() {
        new AppEventCommand(null, mock(JSONObject.class));
    }

    @Test
    public void testRun_invokeHandleEventMethod_onNotificationEventHandler() throws JSONException {
        MobileEngage.setup(config);

        String name = "nameOfTheEvent";
        JSONObject payload = new JSONObject()
                .put("payloadKey", "payloadValue");
        new AppEventCommand(name, payload).run();

        verify(notificationHandler).handleEvent(name, payload);
    }

    @Test
    public void testRun_invokeHandleEventMethod_onNotificationEventHandler_whenThereIsNoPayload() throws JSONException {
        MobileEngage.setup(config);

        String name = "nameOfTheEvent";
        new AppEventCommand(name, null).run();

        verify(notificationHandler).handleEvent(name, null);
    }
}