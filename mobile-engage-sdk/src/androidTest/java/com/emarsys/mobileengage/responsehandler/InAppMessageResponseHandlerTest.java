package com.emarsys.mobileengage.responsehandler;

import com.emarsys.core.response.ResponseModel;
import com.emarsys.mobileengage.iam.ui.IamJsBridge;
import com.emarsys.mobileengage.iam.ui.IamWebViewProvider;
import com.emarsys.mobileengage.iam.ui.MessageLoadedListener;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class InAppMessageResponseHandlerTest {
    private InAppMessageResponseHandler handler;
    private IamWebViewProvider webViewProvider;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        webViewProvider = mock(IamWebViewProvider.class);
        handler = new InAppMessageResponseHandler(webViewProvider);
    }

    @Test
    public void testShouldHandleResponse_shouldReturnTrueWhenTheResponseHasHtmlAttribute() {
        String responseBody = "{'message': {'html':'some html'}}";
        ResponseModel response = new ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body(responseBody)
                .build();
        Assert.assertTrue(handler.shouldHandleResponse(response));
    }

    @Test
    public void testShouldHandleResponse_shouldReturnFalseWhenTheResponseHasANonJsonBody() {
        String responseBody = "Created";
        ResponseModel response = new ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body(responseBody)
                .build();
        Assert.assertFalse(handler.shouldHandleResponse(response));
    }

    @Test
    public void testShouldHandleResponse_shouldReturnFalseWhenTheResponseHasNoMessageAttribute() {
        String responseBody = "{'not_a_message': {'html':'some html'}}";
        ResponseModel response = new ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body(responseBody)
                .build();
        Assert.assertFalse(handler.shouldHandleResponse(response));
    }


    @Test
    public void testShouldHandleResponse_shouldReturnFalseWhenTheResponseHasNoHtmlAttribute() {
        String responseBody = "{'message': {'not_html':'some html'}}";
        ResponseModel response = new ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body(responseBody)
                .build();
        Assert.assertFalse(handler.shouldHandleResponse(response));
    }

    @Test
    public void testHandleResponse_shouldCallloadMessageAsyncWithCorrectArguments(){
        String html = "<p>hello</p>";
        String responseBody = String.format("{'message': {'html':'%s'}}", html);
        ResponseModel response = new ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body(responseBody)
                .build();

        handler.handleResponse(response);

        verify(webViewProvider).loadMessageAsync(eq(html), any(IamJsBridge.class), any(MessageLoadedListener.class));
    }
}