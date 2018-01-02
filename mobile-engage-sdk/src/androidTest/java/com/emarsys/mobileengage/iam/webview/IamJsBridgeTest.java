package com.emarsys.mobileengage.iam.webview;

import com.emarsys.mobileengage.iam.IamDialog;
import com.emarsys.mobileengage.iam.InAppMessageHandler;
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge;
import com.emarsys.mobileengage.iam.jsbridge.InAppMessageHandlerProvider;
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
    public void testConstructor_dialog_shouldNotAcceptNull() {
        new IamJsBridge(null, mock(InAppMessageHandlerProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_messageHandlerProvider_shouldNotAcceptNull() {
        new IamJsBridge(mock(IamDialog.class), null);
    }

    @Test
    public void testClose_shouldInvokeCloseOnTheDialogOfTheMessageHandler() {
        IamDialog iamDialog = mock(IamDialog.class);

        IamJsBridge jsBridge = new IamJsBridge(iamDialog, mock(InAppMessageHandlerProvider.class));
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
        InAppMessageHandlerProvider messageHandlerProvider = mock(InAppMessageHandlerProvider.class);
        when(messageHandlerProvider.provideHandler()).thenReturn(inAppMessageHandler);

        IamJsBridge jsBridge = new IamJsBridge(mock(IamDialog.class), messageHandlerProvider);
        jsBridge.triggerAppEvent(json.toString());

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<JSONObject> payloadCaptor = ArgumentCaptor.forClass(JSONObject.class);
        verify(inAppMessageHandler).handleApplicationEvent(nameCaptor.capture(), payloadCaptor.capture());

        assertEquals(payload.toString(), payloadCaptor.getValue().toString());
        assertEquals("eventName", nameCaptor.getValue());
    }

    @Test
    public void testTriggerAppEvent_shouldNotThrowExceptionWhenInAppMessageHandleIsNotSet() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", "eventName");
        JSONObject payload = new JSONObject();
        JSONObject payloadValue = new JSONObject();
        payloadValue.put("payloadKey2", "payloadValue1");
        payload.put("payloadKey1", payloadValue);
        json.put("payload", payload);


        InAppMessageHandlerProvider messageHandlerProvider = mock(InAppMessageHandlerProvider.class);
        when(messageHandlerProvider.provideHandler()).thenReturn(null);

        IamJsBridge jsBridge = new IamJsBridge(mock(IamDialog.class), messageHandlerProvider);
        jsBridge.triggerAppEvent(json.toString());
    }


}