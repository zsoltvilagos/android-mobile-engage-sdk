package com.emarsys.mobileengage.iam.jsbridge;

import android.support.test.filters.SdkSuppress;
import android.webkit.WebView;

import com.emarsys.mobileengage.iam.InAppMessageHandler;
import com.emarsys.mobileengage.iam.dialog.IamDialog;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;
import com.emarsys.mobileengage.testUtil.mockito.ThreadSpy;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static android.os.Build.VERSION_CODES.KITKAT;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SdkSuppress(minSdkVersion = KITKAT)
public class IamJsBridgeTest {

    static {
        mock(IamDialog.class);
        mock(WebView.class);
    }

    private IamJsBridge jsBridge;
    private WebView webView;

    @Before
    public void setUp() throws Exception {
        IamDialog dialog = mock(IamDialog.class);
        InAppMessageHandler handler = mock(InAppMessageHandler.class);
        InAppMessageHandlerProvider provider = mock(InAppMessageHandlerProvider.class);
        when(provider.provideHandler()).thenReturn(handler);

        jsBridge = new IamJsBridge(dialog, provider);
        webView = mock(WebView.class);
        jsBridge.setWebView(webView);
    }

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
        verify(iamDialog, Mockito.timeout(1000)).dismiss();
    }

    @Test
    public void testClose_calledOnMainThread() throws InterruptedException {
        IamDialog iamDialog = mock(IamDialog.class);
        ThreadSpy threadSpy = new ThreadSpy();
        doAnswer(threadSpy).when(iamDialog).dismiss();

        IamJsBridge jsBridge = new IamJsBridge(iamDialog, mock(InAppMessageHandlerProvider.class));
        jsBridge.close("");

        threadSpy.verifyCalledOnMainThread();
    }

    @Test
    public void testTriggerAppEvent_shouldCallHandleApplicationEventMethodOnInAppMessageHandler() throws JSONException {
        JSONObject payload =
                new JSONObject()
                        .put("payloadKey1",
                                new JSONObject()
                                        .put("payloadKey2", "payloadValue1"));
        JSONObject json =
                new JSONObject()
                        .put("name", "eventName")
                        .put("id", "123456789")
                        .put("payload", payload);

        InAppMessageHandler inAppMessageHandler = mock(InAppMessageHandler.class);
        InAppMessageHandlerProvider messageHandlerProvider = mock(InAppMessageHandlerProvider.class);
        when(messageHandlerProvider.provideHandler()).thenReturn(inAppMessageHandler);

        IamJsBridge jsBridge = new IamJsBridge(mock(IamDialog.class), messageHandlerProvider);
        jsBridge.setWebView(webView);
        jsBridge.triggerAppEvent(json.toString());

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<JSONObject> payloadCaptor = ArgumentCaptor.forClass(JSONObject.class);
        verify(inAppMessageHandler, Mockito.timeout(1000)).handleApplicationEvent(nameCaptor.capture(), payloadCaptor.capture());

        assertEquals(payload.toString(), payloadCaptor.getValue().toString());
        assertEquals("eventName", nameCaptor.getValue());
    }

    @Test
    public void testTriggerAppEvent_shouldNotThrowException_whenInAppMessageHandle_isNotSet() throws JSONException {
        JSONObject json = new JSONObject().put("name", "eventName").put("id", "123456789");

        InAppMessageHandlerProvider messageHandlerProvider = mock(InAppMessageHandlerProvider.class);
        when(messageHandlerProvider.provideHandler()).thenReturn(null);

        IamJsBridge jsBridge = new IamJsBridge(mock(IamDialog.class), messageHandlerProvider);
        jsBridge.triggerAppEvent(json.toString());
    }

    @Test
    public void testTriggerAppEvent_inAppMessageHandler_calledOnMainThread() throws JSONException, InterruptedException {
        JSONObject json = new JSONObject().put("name", "eventName").put("id", "123456789");
        ThreadSpy threadSpy = new ThreadSpy();

        InAppMessageHandler messageHandler = mock(InAppMessageHandler.class);
        doAnswer(threadSpy).when(messageHandler).handleApplicationEvent("eventName", null);

        InAppMessageHandlerProvider messageHandlerProvider = mock(InAppMessageHandlerProvider.class);
        when(messageHandlerProvider.provideHandler()).thenReturn(messageHandler);

        IamJsBridge jsBridge = new IamJsBridge(mock(IamDialog.class), messageHandlerProvider);
        jsBridge.setWebView(webView);
        jsBridge.triggerAppEvent(json.toString());

        threadSpy.verifyCalledOnMainThread();
    }

    @Test
    public void testTriggerAppEvent_shouldInvokeCallback_onSuccess() throws Exception {
        String id = "123456789";
        JSONObject json = new JSONObject().put("id", id).put("name", "value");
        jsBridge.triggerAppEvent(json.toString());

        JSONObject result = new JSONObject().put("id", id).put("success", true);

        verify(webView, Mockito.timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null);
    }

    @Test
    public void testTriggerAppEvent_shouldInvokeCallback_whenNameIsMissing() throws Exception {
        String id = "123456789";
        JSONObject json = new JSONObject().put("id", id);
        jsBridge.triggerAppEvent(json.toString());

        JSONObject result = new JSONObject().put("id", id).put("success", false).put("error", "Missing name!");

        verify(webView, Mockito.timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", result), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendResult_whenPayloadIsNull() throws Exception {
        jsBridge.sendResult(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendResult_whenPayloadDoesntContainId() throws Exception {
        jsBridge.sendResult(new JSONObject());
    }

    @Test
    public void testSendResult_shouldInvokeEvaluateJavascript_onWebView() throws Exception {
        JSONObject json = new JSONObject().put("id", "123456789").put("key", "value");
        jsBridge.sendResult(json);

        verify(webView, Mockito.timeout(1000)).evaluateJavascript(String.format("MEIAM.handleResponse(%s);", json), null);
    }
}