package com.emarsys.mobileengage.responsehandler;

import com.emarsys.core.response.ResponseModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbstractResponseHandlerTest {

    private AbstractResponseHandler abstractResponseHandler;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Before
    public void init() {
        abstractResponseHandler = mock(AbstractResponseHandler.class, Mockito.CALLS_REAL_METHODS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessResponse_responseShouldNotBeNull() {
        abstractResponseHandler.processResponse(null);
    }

    @Test
    public void testProcessResponse_shouldCallHandleResponse_whenResponseShouldBeHandled() {
        when(abstractResponseHandler.shouldHandleResponse(any(ResponseModel.class))).thenReturn(true);
        ResponseModel responseModel = new ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .build();

        abstractResponseHandler.processResponse(responseModel);

        verify(abstractResponseHandler).handleResponse(responseModel);
    }

    @Test
    public void testProcessResponse_shouldNotCallHandleResponse_whenResponseShouldNotBeHandled() {
        when(abstractResponseHandler.shouldHandleResponse(any(ResponseModel.class))).thenReturn(false);
        ResponseModel responseModel = new ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .build();

        abstractResponseHandler.processResponse(responseModel);

        verify(abstractResponseHandler, times(0)).handleResponse(responseModel);
    }
}