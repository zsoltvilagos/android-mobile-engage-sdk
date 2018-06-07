package com.emarsys.mobileengage.notification.command;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

import com.emarsys.mobileengage.notification.NotificationCommandFactory;
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
    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getTargetContext().getApplicationContext();
        factory = new NotificationCommandFactory(context);
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

    @Test
    public void testCreateNotificationCommand_shouldCreateOpenExternalLinkCommand() throws JSONException {
        Intent intent = createOpenExternalLinkIntent("https://www.emarsys.com");
        Runnable command = factory.createNotificationCommand(intent);

        assertNotNull(command);
        assertEquals(OpenExternalUrlCommand.class, command.getClass());
    }

    @Test
    public void testCreateNotificationCommand_shouldCreateOpenExternalLinkCommand_withCorrectParameters() throws JSONException {
        OpenExternalUrlCommand command = (OpenExternalUrlCommand) factory.createNotificationCommand(createOpenExternalLinkIntent("https://www.emarsys.com"));

        assertEquals(context, command.getContext());
        assertEquals(Uri.parse("https://www.emarsys.com"), command.getIntent().getData());
        assertEquals(Intent.ACTION_VIEW, command.getIntent().getAction());
    }

    @Test
    public void testCreateNotificationCommand_shouldCreateAppLaunchCommand_insteadOf_OpenExternalLinkCommand_whenCantResolveUrl() throws JSONException {
        Runnable command = factory.createNotificationCommand(createOpenExternalLinkIntent("Not valid url!"));

        assertEquals(LaunchApplicationCommand.class, command.getClass());
    }

    @Test
    public void testCreateNotificationCommand_shouldCreateCustomEventCommand() throws JSONException {
        Intent intent = createCustomEventIntent("eventName");
        Runnable command = factory.createNotificationCommand(intent);

        assertNotNull(command);
        assertEquals(CustomEventCommand.class, command.getClass());
    }

    @Test
    public void testCreateNotificationCommand_shouldCreateCustomEventCommand_withCorrectEventName() throws JSONException {
        String eventName = "eventName";

        Intent intent = createCustomEventIntent(eventName);

        CustomEventCommand command = (CustomEventCommand) factory.createNotificationCommand(intent);

        assertEquals(eventName, command.getEventName());
    }

    @Test
    public void testCreateNotificationCommand_shouldCreateCustomEventCommand_withCorrectEventAttributes() throws JSONException {
        String eventName = "eventName";

        JSONObject payload = new JSONObject()
                .put("key1", true)
                .put("key2", 3.14)
                .put("key3", new JSONObject().put("key5", "value1"))
                .put("key4", "value2");

        Map<String, String> attributes = new HashMap<>();
        attributes.put("key1", "true");
        attributes.put("key2", "3.14");
        attributes.put("key3", "{\"key5\":\"value1\"}");
        attributes.put("key4", "value2");

        Intent intent = createCustomEventIntent(eventName, payload);

        CustomEventCommand command = (CustomEventCommand) factory.createNotificationCommand(intent);

        assertEquals(attributes, command.getEventAttributes());
    }

    @Test
    public void testCreateNotificationCommand_shouldCreateCustomEventCommand_withoutEventAttributes() throws JSONException {
        String eventName = "eventName";

        Intent intent = createCustomEventIntent(eventName);

        CustomEventCommand command = (CustomEventCommand) factory.createNotificationCommand(intent);

        assertEquals(null, command.getEventAttributes());
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

    private Intent createOpenExternalLinkIntent(String url) throws JSONException {
        String actionId = "uniqueActionId";
        JSONObject json = new JSONObject()
                .put("actions", new JSONArray()
                        .put(new JSONObject()
                                .put("id", actionId)
                                .put("url", url)
                                .put("type", "OpenExternalUrl")));
        return createIntent(actionId, json);
    }

    private Intent createCustomEventIntent(String eventName) throws JSONException {
        return createCustomEventIntent(eventName, null);
    }

    private Intent createCustomEventIntent(String eventName, JSONObject payload) throws JSONException {
        String actionId = "uniqueActionId";

        JSONObject action = new JSONObject()
                .put("id", actionId)
                .put("title", "Action button title")
                .put("type", "MECustomEvent")
                .put("name", eventName);
        if (payload != null) {
            action.put("payload", payload);
        }

        JSONObject json = new JSONObject().put("actions", new JSONArray().put(action));

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