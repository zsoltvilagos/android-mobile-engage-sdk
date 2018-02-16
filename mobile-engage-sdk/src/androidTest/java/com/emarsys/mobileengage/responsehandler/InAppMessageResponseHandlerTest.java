package com.emarsys.mobileengage.responsehandler;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;

import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.mobileengage.iam.dialog.IamDialog;
import com.emarsys.mobileengage.iam.dialog.IamDialogProvider;
import com.emarsys.mobileengage.iam.dialog.action.OnDialogShownAction;
import com.emarsys.mobileengage.iam.dialog.action.SaveDisplayedIamAction;
import com.emarsys.mobileengage.iam.dialog.action.SendDisplayedIamAction;
import com.emarsys.mobileengage.iam.jsbridge.IamJsBridge;
import com.emarsys.mobileengage.iam.jsbridge.InAppMessageHandlerProvider;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedRepository;
import com.emarsys.mobileengage.iam.webview.DefaultMessageLoadedListener;
import com.emarsys.mobileengage.iam.webview.IamWebViewProvider;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.testUtil.CollectionTestUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static android.os.Build.VERSION_CODES.KITKAT;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InAppMessageResponseHandlerTest {

    static {
        mock(Handler.class);
    }

    private static final String APPLICATION_CODE = "applicationcode";

    private InAppMessageResponseHandler handler;
    private IamWebViewProvider webViewProvider;
    private IamDialog dialog;
    private RequestManager requestManager;
    private Context context;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        context = InstrumentationRegistry.getTargetContext().getApplicationContext();

        webViewProvider = mock(IamWebViewProvider.class);

        dialog = mock(IamDialog.class);
        IamDialogProvider dialogProvider = mock(IamDialogProvider.class);
        when(dialogProvider.provideDialog(any(String.class))).thenReturn(dialog);

        requestManager = mock(RequestManager.class);

        handler = new InAppMessageResponseHandler(
                context,
                mock(Handler.class),
                webViewProvider,
                mock(InAppMessageHandlerProvider.class),
                dialogProvider,
                mock(ButtonClickedRepository.class),
                requestManager,
                APPLICATION_CODE,
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_context_shouldNotBeNull() {
        new InAppMessageResponseHandler(
                null,
                mock(Handler.class),
                mock(IamWebViewProvider.class),
                mock(InAppMessageHandlerProvider.class),
                mock(IamDialogProvider.class),
                mock(ButtonClickedRepository.class),
                mock(RequestManager.class),
                APPLICATION_CODE,
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_coreSdkHandler_shouldNotBeNull() {
        new InAppMessageResponseHandler(
                context,
                null,
                mock(IamWebViewProvider.class),
                mock(InAppMessageHandlerProvider.class),
                mock(IamDialogProvider.class),
                mock(ButtonClickedRepository.class),
                mock(RequestManager.class),
                APPLICATION_CODE,
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_webViewProvider_shouldNotBeNull() {
        new InAppMessageResponseHandler(
                context,
                mock(Handler.class),
                null,
                mock(InAppMessageHandlerProvider.class),
                mock(IamDialogProvider.class),
                mock(ButtonClickedRepository.class),
                mock(RequestManager.class),
                APPLICATION_CODE,
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_messageHandlerProvider_shouldNotBeNull() {
        new InAppMessageResponseHandler(
                context,
                mock(Handler.class),
                mock(IamWebViewProvider.class),
                null,
                mock(IamDialogProvider.class),
                mock(ButtonClickedRepository.class),
                mock(RequestManager.class),
                APPLICATION_CODE,
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_dialogProvider_shouldNotBeNull() {
        new InAppMessageResponseHandler(
                context,
                mock(Handler.class),
                mock(IamWebViewProvider.class),
                mock(InAppMessageHandlerProvider.class),
                null,
                mock(ButtonClickedRepository.class),
                mock(RequestManager.class),
                APPLICATION_CODE,
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_buttonClickedRepository_shouldNotBeNull() {
        new InAppMessageResponseHandler(
                context,
                mock(Handler.class),
                mock(IamWebViewProvider.class),
                mock(InAppMessageHandlerProvider.class),
                mock(IamDialogProvider.class),
                null,
                mock(RequestManager.class),
                APPLICATION_CODE,
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_requestManager_shouldNotBeNull() {
        new InAppMessageResponseHandler(
                context,
                mock(Handler.class),
                mock(IamWebViewProvider.class),
                mock(InAppMessageHandlerProvider.class),
                mock(IamDialogProvider.class),
                mock(ButtonClickedRepository.class),
                null,
                APPLICATION_CODE,
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_applicationCode_shouldNotBeNull() {
        new InAppMessageResponseHandler(
                context,
                mock(Handler.class),
                mock(IamWebViewProvider.class),
                mock(InAppMessageHandlerProvider.class),
                mock(IamDialogProvider.class),
                mock(ButtonClickedRepository.class),
                mock(RequestManager.class),
                null,
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_meIdStorage_shouldNotBeNull() {
        new InAppMessageResponseHandler(
                context,
                mock(Handler.class),
                mock(IamWebViewProvider.class),
                mock(InAppMessageHandlerProvider.class),
                mock(IamDialogProvider.class),
                mock(ButtonClickedRepository.class),
                mock(RequestManager.class),
                APPLICATION_CODE,
                null,
                mock(MeIdSignatureStorage.class),
                mock(TimestampProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_meIdSignatureStorage_shouldNotBeNull() {
        new InAppMessageResponseHandler(
                context,
                mock(Handler.class),
                mock(IamWebViewProvider.class),
                mock(InAppMessageHandlerProvider.class),
                mock(IamDialogProvider.class),
                mock(ButtonClickedRepository.class),
                mock(RequestManager.class),
                APPLICATION_CODE,
                mock(MeIdStorage.class),
                null,
                mock(TimestampProvider.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_timestampProvider_shouldNotBeNull() {
        new InAppMessageResponseHandler(
                context,
                mock(Handler.class),
                mock(IamWebViewProvider.class),
                mock(InAppMessageHandlerProvider.class),
                mock(IamDialogProvider.class),
                mock(ButtonClickedRepository.class),
                mock(RequestManager.class),
                APPLICATION_CODE,
                mock(MeIdStorage.class),
                mock(MeIdSignatureStorage.class),
                null);
    }

    @Test
    public void testShouldHandleResponse_shouldHandleOnly_kitkatAndAbove() {
        ResponseModel validResponse = buildResponseModel("{'message': {'html':'some html'}}");
        boolean shouldHandle = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        assertEquals(shouldHandle, handler.shouldHandleResponse(validResponse));
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testShouldHandleResponse_shouldReturnTrueWhenTheResponseHasHtmlAttribute() {
        ResponseModel response = buildResponseModel("{'message': {'html':'some html'}}");
        assertTrue(handler.shouldHandleResponse(response));
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testShouldHandleResponse_shouldReturnFalseWhenTheResponseHasANonJsonBody() {
        ResponseModel response = buildResponseModel("Created");
        assertFalse(handler.shouldHandleResponse(response));
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testShouldHandleResponse_shouldReturnFalseWhenTheResponseHasNoMessageAttribute() {
        ResponseModel response = buildResponseModel("{'not_a_message': {'html':'some html'}}");
        assertFalse(handler.shouldHandleResponse(response));
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testShouldHandleResponse_shouldReturnFalseWhenTheResponseHasNoHtmlAttribute() {
        ResponseModel response = buildResponseModel("{'message': {'not_html':'some html'}}");
        assertFalse(handler.shouldHandleResponse(response));
    }

    @Test
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testHandleResponse_shouldCallLoadMessageAsync_withCorrectArguments() {
        String html = "<p>hello</p>";
        String responseBody = String.format("{'message': {'html':'%s', 'id': '123'} }", html);
        ResponseModel response = buildResponseModel(responseBody);

        handler.handleResponse(response);

        verify(webViewProvider).loadMessageAsync(eq(html), any(IamJsBridge.class), any(DefaultMessageLoadedListener.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testHandleResponse_setsSaveDisplayIamAction_onDialog() {
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        String html = "<p>hello</p>";
        String responseBody = String.format("{'message': {'html':'%s', 'id': '123'} }", html);
        ResponseModel response = buildResponseModel(responseBody);

        handler.handleResponse(response);

        verify(dialog).setActions(captor.capture());
        List<OnDialogShownAction> actions = captor.getValue();

        assertEquals(1, CollectionTestUtils.numberOfElementsIn(actions, SaveDisplayedIamAction.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    @SdkSuppress(minSdkVersion = KITKAT)
    public void testHandleResponse_setsSendDisplayIamAction_onDialog() {
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        String html = "<p>hello</p>";
        String responseBody = String.format("{'message': {'html':'%s', 'id': '123'} }", html);
        ResponseModel response = buildResponseModel(responseBody);

        handler.handleResponse(response);

        verify(dialog).setActions(captor.capture());
        List<OnDialogShownAction> actions = captor.getValue();

        assertEquals(1, CollectionTestUtils.numberOfElementsIn(actions, SendDisplayedIamAction.class));
    }

    private ResponseModel buildResponseModel(String responseBody) {
        return new ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body(responseBody)
                .requestModel(mock(RequestModel.class))
                .build();
    }
}