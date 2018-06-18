package com.emarsys.mobileengage.iam.content;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.mobileengage.testUtil.RequestModelTestUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class IamContentPreLoaderTest {

    public static final String HTML_STRING = "<html><body style=\"background:red;\">test</body></html>";
    IamContentPreLoader preLoader;
    RestClient restClient;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void setUp() throws Exception {
        restClient = mock(RestClient.class);
        preLoader = new IamContentPreLoader(restClient);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_mobileEngageInternalMustNotBeNull() {
        new IamContentPreLoader(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPreLoadContent_urlMustNotBeNull() {
        preLoader.preLoadContent(null, mock(IamContentPreLoader.CompletionListener.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPreLoadContent_completionListenerMustNotBeNull() {
        preLoader.preLoadContent("https://www.emarsys.com", null);
    }

    @Test
    public void testPreLoadContent_shouldInvokeRestClient() {
        String url = "https://emarsys.com/path";
        preLoader.preLoadContent(url, mock(IamContentPreLoader.CompletionListener.class));

        verify(restClient).execute(any(RequestModel.class), any(CoreCompletionHandler.class));
    }

    @Test
    public void testPreLoadContent_shouldInvokeRestClient_withCorrectRequestModel() {
        String url = "https://emarsys.com/path";
        RequestModel expected = new RequestModel.Builder()
                .url(url)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        preLoader.preLoadContent(url, mock(IamContentPreLoader.CompletionListener.class));

        verify(restClient).execute(captor.capture(), any(CoreCompletionHandler.class));

        RequestModel capturedRequestModel = captor.getValue();

        RequestModelTestUtils.assertEqualsRequestModels(expected, capturedRequestModel);
    }

    @Test
    public void testPreLoadContent_shouldInvokeCompletionListener_withContentIfSuccess() {
        String url = "https://emarsys.com/path";
        IamContentPreLoader.CompletionListener completionListener = mock(IamContentPreLoader.CompletionListener.class);
        preLoader.preLoadContent(url, completionListener);

        ArgumentCaptor<CoreCompletionHandler> captor = ArgumentCaptor.forClass(CoreCompletionHandler.class);
        verify(restClient).execute(any(RequestModel.class), captor.capture());

        ResponseModel responseModel = new ResponseModel.Builder().body(HTML_STRING).statusCode(200).message("OK").requestModel(mock(RequestModel.class)).build();
        captor.getValue().onSuccess("sadadad", responseModel);

        verify(completionListener).onContentPreLoadCompleted(HTML_STRING);
    }

    @Test
    public void testPreLoadContent_shouldInvokeCompletionListener_withNullIfErrorWithResponseModel() {
        String url = "https://emarsys.com/path";
        IamContentPreLoader.CompletionListener completionListener = mock(IamContentPreLoader.CompletionListener.class);
        preLoader.preLoadContent(url, completionListener);

        ArgumentCaptor<CoreCompletionHandler> captor = ArgumentCaptor.forClass(CoreCompletionHandler.class);
        verify(restClient).execute(any(RequestModel.class), captor.capture());

        ResponseModel responseModel = new ResponseModel.Builder().body("some server error").statusCode(200).message("OK").requestModel(mock(RequestModel.class)).build();
        captor.getValue().onError("sadadad", responseModel);

        verify(completionListener).onContentPreLoadCompleted(null);
    }

    @Test
    public void testPreLoadContent_shouldInvokeCompletionListener_withNullIfErrorWithException() {
        String url = "https://emarsys.com/path";
        IamContentPreLoader.CompletionListener completionListener = mock(IamContentPreLoader.CompletionListener.class);
        preLoader.preLoadContent(url, completionListener);

        ArgumentCaptor<CoreCompletionHandler> captor = ArgumentCaptor.forClass(CoreCompletionHandler.class);
        verify(restClient).execute(any(RequestModel.class), captor.capture());

        captor.getValue().onError("sadadad", new IOException("some network error"));

        verify(completionListener).onContentPreLoadCompleted(null);
    }
}