package com.emarsys.mobileengage.notification.command;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.junit.Assert.*;

public class NotificationCommandFactoryTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    private NotificationCommandFactory factory;

    @Before
    public void setUp() {
        factory = new NotificationCommandFactory();
    }

    @Test
    public void testCreateNotificationCommand_shouldCreateAppEvent() throws JSONException {
        Intent intent = createAppEventIntent();
        Runnable command = factory.createNotificationCommand(intent);

        assertNotNull(command);
        assertEquals(command.getClass(), AppEventCommand.class);
    }

    @Test
    public void testCreateNotificationCommand_shouldCreateAppEvent_withCorrectName() throws JSONException {
        Intent intent = createAppEventIntent();
        AppEventCommand command = (AppEventCommand) factory.createNotificationCommand(intent);

        assertEquals(command.getName(), "nameOfTheEvent");
    }

    @Test
    public void testCreateNotificationCommand_shouldCreateAppEvent_withCorrectPayload() throws JSONException {
        Intent intent = createAppEventIntent();
        AppEventCommand command = (AppEventCommand) factory.createNotificationCommand(intent);

        JSONObject payload = command.getPayload();
        assertEquals(payload.getString("payloadKey"),"payloadValue");
    }

    @NonNull
    private Intent createAppEventIntent() throws JSONException {
        String actionId = "uniqueActionId";
        String name = "nameOfTheEvent";
        JSONObject payload = new JSONObject()
                .put("payloadKey", "payloadValue");
        JSONObject json = new JSONObject()
                .put("actions", new JSONObject()
                        .put(actionId, new JSONObject()
                                .put("name", name)
                                .put("payload", payload)
                                .put("type", "MEAppEvent")));
        Intent intent = new Intent();
        intent.setAction(actionId);
        intent.putExtra("payload", json.toString());
        return intent;
    }
}