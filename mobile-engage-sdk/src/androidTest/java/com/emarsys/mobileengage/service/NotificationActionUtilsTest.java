package com.emarsys.mobileengage.service;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.v4.app.NotificationCompat;

import com.emarsys.mobileengage.notification.command.NotificationCommandFactory;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.Build.VERSION_CODES.KITKAT;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NotificationActionUtilsTest {

    private Context context;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        this.context = InstrumentationRegistry.getTargetContext().getApplicationContext();
    }

    @Test
    public void testHandleAction_runsNotificationCommand() {
        NotificationCommandFactory factory = mock(NotificationCommandFactory.class);
        Intent intent = mock(Intent.class);
        Runnable command = mock(Runnable.class);

        when(factory.createNotificationCommand(intent)).thenReturn(command);

        NotificationActionUtils.handleAction(intent, factory);
        verify(command).run();
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testCreateActions_idValueNotJson() throws JSONException {
        JSONObject actions = new JSONObject()
                .put("uniqueActionId", 987);

        Map<String, String> input = new HashMap<>();
        input.put("actions", actions.toString());

        List<NotificationCompat.Action> result = NotificationActionUtils.createActions(context, input);
        assertTrue(result.isEmpty());
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testCreateActions_missingTitle() throws JSONException {
        JSONObject actions = new JSONObject()
                .put("uniqueActionId", new JSONObject()
                        .put("type", "MEAppEvent")
                );

        Map<String, String> input = new HashMap<>();
        input.put("actions", actions.toString());

        List<NotificationCompat.Action> result = NotificationActionUtils.createActions(context, input);
        assertTrue(result.isEmpty());
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testCreateActions_missingType() throws JSONException {
        JSONObject actions = new JSONObject().put("uniqueActionId", new JSONObject()
                .put("title", "Action button title")
        );

        Map<String, String> input = new HashMap<>();
        input.put("actions", actions.toString());

        List<NotificationCompat.Action> result = NotificationActionUtils.createActions(context, input);
        assertTrue(result.isEmpty());
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testCreateActions_appEvent_missingEventName() throws JSONException {
        JSONObject actions = new JSONObject().put("uniqueActionId", new JSONObject()
                .put("title", "Action button title")
                .put("type", "MEAppEvent")
        );

        Map<String, String> input = new HashMap<>();
        input.put("actions", actions.toString());

        List<NotificationCompat.Action> result = NotificationActionUtils.createActions(context, input);
        assertTrue(result.isEmpty());
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testCreateActions_appEvent_withSingleAction() throws JSONException {
        JSONObject payload = new JSONObject()
                .put("actions", new JSONObject()
                        .put("uniqueActionId", new JSONObject()
                                .put("title", "Action button title")
                                .put("type", "MEAppEvent")
                                .put("name", "Name of the event")
                                .put("payload", new JSONObject()
                                        .put("payloadKey", "payloadValue")))
                );

        Map<String, String> input = new HashMap<>();
        input.put("ems", payload.toString());

        List<NotificationCompat.Action> result = NotificationActionUtils.createActions(context, input);
        assertEquals(1, result.size());
        assertEquals("Action button title", result.get(0).title);
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testCreateActions_appEvent_withMultipleActions() throws JSONException {
        JSONObject payload = new JSONObject()
                .put("actions", new JSONObject()
                        .put("uniqueActionId1", new JSONObject()
                                .put("title", "title1")
                                .put("type", "MEAppEvent")
                                .put("name", "event1")
                        )
                        .put("uniqueActionId2", new JSONObject()
                                .put("title", "title2")
                                .put("type", "MEAppEvent")
                                .put("name", "event2")
                                .put("payload", new JSONObject()
                                        .put("payloadKey", "payloadValue"))
                        ));

        Map<String, String> input = new HashMap<>();
        input.put("ems", payload.toString());

        List<NotificationCompat.Action> result = NotificationActionUtils.createActions(context, input);
        assertEquals(2, result.size());

        assertEquals("title1", result.get(0).title);

        assertEquals("title2", result.get(1).title);
    }
}