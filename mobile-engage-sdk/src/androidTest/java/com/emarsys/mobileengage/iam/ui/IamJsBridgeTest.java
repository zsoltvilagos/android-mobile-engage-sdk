package com.emarsys.mobileengage.iam.ui;

import com.emarsys.mobileengage.iam.DialogOwner;
import com.emarsys.mobileengage.iam.InAppMessageHandler;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.ArgumentCaptor;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IamJsBridgeTest {
    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_shouldNotAcceptNull() {
        new IamJsBridge(null, mock(InAppMessageHandler.class));
    }

    @Test
    public void testClose_shouldInvokeCloseOnTheDialogOfTheMessageHandler() {
        DialogOwner dialogOwner = mock(DialogOwner.class);
        IamDialog iamDialog = mock(IamDialog.class);
        when(dialogOwner.getIamDialog()).thenReturn(iamDialog);

        IamJsBridge jsBridge = new IamJsBridge(dialogOwner, mock(InAppMessageHandler.class));
        jsBridge.close("");

        verify(iamDialog).dismiss();
    }

    @Test
    public void testTriggerAppEvent_shouldCallHandleApplicationEventMethodOnInAppMessageHandler() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", "eventName");
        JSONObject payload = new JSONObject();
        JSONObject payloadValue = new JSONObject();
        payloadValue.put("payloadKey2", "payloadValue1");
        payload.put("payloadKey1", payloadValue);
        json.put("payload", payload);

        InAppMessageHandler inAppMessageHandler = mock(InAppMessageHandler.class);

        IamJsBridge jsBridge = new IamJsBridge(mock(DialogOwner.class), inAppMessageHandler);
        jsBridge.triggerAppEvent(json.toString());

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<JSONObject> payloadCaptor = ArgumentCaptor.forClass(JSONObject.class);
        verify(inAppMessageHandler).handleApplicationEvent(nameCaptor.capture(), payloadCaptor.capture());

        assertEquals(payload.toString(), payloadCaptor.getValue().toString());
        assertEquals("eventName", nameCaptor.getValue());
    }

    @Test
    public void testTriggerAppEvent_shouldNotThrowExceptionWhenInappMessageHandleIsNotSet() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", "eventName");
        JSONObject payload = new JSONObject();
        JSONObject payloadValue = new JSONObject();
        payloadValue.put("payloadKey2", "payloadValue1");
        payload.put("payloadKey1", payloadValue);
        json.put("payload", payload);


        IamJsBridge jsBridge = new IamJsBridge(mock(DialogOwner.class), null);
        jsBridge.triggerAppEvent(json.toString());
    }
}