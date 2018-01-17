package com.emarsys.mobileengage.fake;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.RequestModel;
import com.emarsys.core.response.ResponseModel;

import java.util.concurrent.CountDownLatch;

import static org.mockito.Mockito.mock;

public class FakeRequestManager extends RequestManager {

    public static enum ResponseType {
        SUCCESS,
        FAILURE
    }

    private final CoreCompletionHandler coreCompletionHandler;

    private ResponseType responseType;
    public CountDownLatch latch;

    public FakeRequestManager(ResponseType responseType, CountDownLatch latch, CoreCompletionHandler coreCompletionHandler) {
        this.coreCompletionHandler = coreCompletionHandler;
        this.responseType = responseType;
        this.latch = latch;
    }

    @Override
    public void submit(final RequestModel model) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (responseType == ResponseType.SUCCESS) {
                    coreCompletionHandler.onSuccess(
                            model.getId(),
                            new ResponseModel.Builder()
                                    .statusCode(200)
                                    .message("OK")
                                    .requestModel(mock(RequestModel.class))
                                    .build());
                } else {
                    coreCompletionHandler.onError(model.getId(), new Exception());
                }
                if (latch != null) {
                    latch.countDown();
                }
            }
        }).start();
    }


}
