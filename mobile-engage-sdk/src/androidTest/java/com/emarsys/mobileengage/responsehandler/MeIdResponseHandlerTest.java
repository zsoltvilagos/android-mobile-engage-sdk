package com.emarsys.mobileengage.responsehandler;

import android.support.test.rule.DisableOnAndroidDebug;

import com.emarsys.core.response.ResponseModel;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MeIdResponseHandlerTest {

    private String meId;
    private MeIdResponseHandler handler;
    private MeIdStorage meIdStorage;

    @Rule
    public TestRule timeout = new DisableOnAndroidDebug(Timeout.seconds(TimeoutUtils.getTimeout()));
    private ResponseModel responseModelWithMeId;

    @Before
    public void init() {
        meId = "123";
        meIdStorage = mock(MeIdStorage.class);
        handler = new MeIdResponseHandler(meIdStorage);

        responseModelWithMeId = new ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body(String.format("{ 'api_me_id': '%s' }", meId))
                .build();
    }

    @Test
    public void testConstructor_initializesField() {
        meIdStorage = mock(MeIdStorage.class);
        handler = new MeIdResponseHandler(meIdStorage);

        assertEquals(meIdStorage, handler.meIdStorage);
    }

    @Test
    public void testShouldHandleResponse_shouldReturnTrue_whenResponseBodyIncludesMeId() {
        boolean result = handler.shouldHandleResponse(responseModelWithMeId);

        Assert.assertTrue(result);
    }

    @Test
    public void testShouldHandleResponse_shouldReturnTrue_whenResponseBodyLacksMeId() {
        ResponseModel responseModel = new ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body("{ 'yolo': '123' }")
                .build();

        boolean result = handler.shouldHandleResponse(responseModel);

        Assert.assertFalse(result);
    }

    @Test
    public void testHandleResponse_shouldSetMeIdInMobileEngageInternal() {
        handler.handleResponse(responseModelWithMeId);

        verify(meIdStorage).set(meId);
    }

}