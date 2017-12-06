package com.emarsys.mobileengage.responsehandler;

import com.emarsys.core.response.ResponseModel;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class InAppMessageResponseHandlerTest {
    private InAppMessageResponseHandler handler;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        handler = new InAppMessageResponseHandler();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testShouldHandleResponse_shouldNotAcceptNull(){
        handler.shouldHandleResponse(null);
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
}