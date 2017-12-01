package com.emarsys.mobileengage;

import android.support.test.rule.DisableOnAndroidDebug;

import com.emarsys.core.response.ResponseModel;
import com.emarsys.mobileengage.responsehandler.AbstractResponseHandler;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MobileEngageCoreCompletionHandlerTest {

    @Rule
    public TestRule timeout = new DisableOnAndroidDebug(Timeout.seconds(TimeoutUtils.getTimeout()));

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