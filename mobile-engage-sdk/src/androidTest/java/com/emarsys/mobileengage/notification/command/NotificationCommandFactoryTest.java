package com.emarsys.mobileengage.notification.command;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

import com.emarsys.mobileengage.service.IntentUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NotificationCommandFactoryTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    private NotificationCommandFactory factory;

    @Before
    public void setUp() {
        factory = new NotificationCommandFactory(InstrumentationRegistry.getTargetContext().getApplicationContext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_contextShouldNotBeNull() {
        new NotificationCommandFactory(null);
    }

    @Test
    public void testCreateNotificationCommand_shouldCreateAppLaunchCommand_whenIntentIsEmpty() {
        Runnable command = factory.createNotificationCommand(new Intent());

        assertEquals(LaunchApplicationCommand.class, command.getClass());
    }

    @Test
    public void testCreateNotificationCommand_shouldCreateAppLaunchCommand_whenTypeIsNotSupported() throws JSONException {
        Runnable command = factory.createNotificationCommand(createUnknownCommandIntent());

        assertEquals(LaunchApplicationCommand.class, command.getClass());
    }

    @Test
    public void testCreateNotificationCommand_shouldCreateAppLaunchCommand_whenActionsKeyIsMissing() throws JSONException {
        Runnable command = factory.createNotificationCommand(createIntent("actionId", new JSONObject()));

        assertEquals(LaunchApplicationCommand.class, command.getClass());
    }

    @Test
    public void testCreateNotificationCommand_shouldCreateAppEvent() throws JSONException {
        Intent intent = createAppEventIntent();
        Runnable command = factory.createNotificationCommand(intent);

        assertNotNull(command);
        assertEquals(AppEventCommand.class, command.getClass());
    }

    @Test
    public void testCreateNotificationCommand_shouldCreateAppEvent_withCorrectName() throws JSONException {
        Intent intent = createAppEventIntent();
        AppEventCommand command = (AppEventCommand) factory.createNotificationCommand(intent);

        assertEquals("nameOfTheEvent", command.getName());
    }

    @Test
    public void testCreateNotificationCommand_shouldCreateAppEvent_withCorrectPayload() throws JSONException {
        Intent intent = createAppEventIntent();
        AppEventCommand command = (AppEventCommand) factory.createNotificationCommand(intent);

        JSONObject payload = command.getPayload();
        assertEquals("payloadValue", payload.getString("payloadKey"));
    }

    @Test
    public void testCreateNotificationCommand_appEvent_worksWithIntentUtils() {
        String emsPayload = "{'actions':[" +
                "{" +
                "'id':'actionId', 'type': 'MEAppEvent', 'title':'action title', 'name':'eventName'" +
                "}" +
                "]}";
        Map<String, String> remoteMessageData = new HashMap<>();
        remoteMessageData.put("ems", emsPayload);

        Intent intent = IntentUtils.createTrackMessageOpenServiceIntent(
                InstrumentationRegistry.getTargetContext().getApplicationContext(),
                remoteMessageData,
                "actionId"
        );

        AppEventCommand command = (AppEventCommand) factory.createNotificationCommand(intent);
        assertEquals("eventName", command.getName());
    }

    private Intent createUnknownCommandIntent() throws JSONException {
        String unknownType = "NOT_SUPPORTED";
        JSONObject json = new JSONObject()
                .put("actions", new JSONArray()
                        .put(new JSONObject()
                                .put("id", "uniqueActionId")
                                .put("name", "nameOfTheEvent")
                                .put("type", unknownType)));
        return createIntent("uniqueActionId", json);
    }

    private Intent createAppEventIntent() throws JSONException {
        String actionId = "uniqueActionId";
        String name = "nameOfTheEvent";
        JSONObject payload = new JSONObject()
                .put("payloadKey", "payloadValue");
        JSONObject json = new JSONObject()
                .put("actions", new JSONArray()
                        .put(new JSONObject()
                                .put("id", actionId)
                                .put("name", name)
                                .put("payload", payload)
                                .put("type", "MEAppEvent")));
        return createIntent(actionId, json);
    }

    private Intent createIntent(String actionId, JSONObject payload) {
        Intent intent = new Intent();
        if (actionId != null) {
            intent.setAction(actionId);
        }
        if (payload != null) {
            Bundle bundle = new Bundle();
            bundle.putString("ems", payload.toString());
            intent.putExtra("payload", bundle);
        }
        return intent;
    }
}