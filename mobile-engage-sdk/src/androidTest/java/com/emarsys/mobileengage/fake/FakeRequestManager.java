package com.emarsys.mobileengage.fake;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.connection.ConnectionWatchDog;
import com.emarsys.core.queue.Queue;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.RequestModel;
import com.emarsys.core.response.ResponseModel;

import java.util.concurrent.CountDownLatch;

public class FakeRequestManager extends RequestManager {

    public static enum ResponseType {
        SUCCESS,
        FAILURE
    }

    private final CoreCompletionHandler coreCompletionHandler;

    private ResponseType responseType;
    public CountDownLatch latch;

    public FakeRequestManager(ResponseType responseType, CountDownLatch latch, ConnectionWatchDog connectionWatchDog, Queue<RequestModel> queue, CoreCompletionHandler coreCompletionHandler) {
        super(connectionWatchDog, queue, coreCompletionHandler);
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
                    coreCompletionHandler.onSuccess(model.getId(), new ResponseModel.Builder().statusCode(200).message("OK").build());
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
