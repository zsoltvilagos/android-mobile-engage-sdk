package com.emarsys.mobileengage;

import com.emarsys.core.response.ResponseModel;
import com.emarsys.mobileengage.responsehandler.AbstractResponseHandler;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MobileEngageCoreCompletionHandlerTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_handlersShouldNotBeNull() {
        new MobileEngageCoreCompletionHandler(null, null);
    }

    @Test
    public void testOnSuccess_shouldCallProcessResponseOnTheHandlers() throws Exception {
        AbstractResponseHandler abstractResponseHandler1 = mock(AbstractResponseHandler.class);
        AbstractResponseHandler abstractResponseHandler2 = mock(AbstractResponseHandler.class);
        List<AbstractResponseHandler> handlers = Arrays.asList(abstractResponseHandler1, abstractResponseHandler2);

        MobileEngageCoreCompletionHandler coreCompletionHandler = new MobileEngageCoreCompletionHandler(handlers, null);
        ResponseModel responseModel = mock(ResponseModel.class);

        coreCompletionHandler.onSuccess("", responseModel);

        verify(abstractResponseHandler1).processResponse(responseModel);
        verify(abstractResponseHandler2).processResponse(responseModel);
    }

}