package com.emarsys.mobileengage.fake;

import android.os.Handler;
import android.os.Looper;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.request.RequestModel;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.response.ResponseModel;


public class FakeRestClient extends RestClient {

    private ResponseModel returnValue;
    private Mode mode;
    private Exception exception;

    public enum Mode {SUCCESS, ERROR_RESPONSE_MODEL, ERROR_EXCEPTION}

    public FakeRestClient(Exception exception) {
        this.exception = exception;
        this.mode = Mode.ERROR_EXCEPTION;
    }

    public FakeRestClient(ResponseModel returnValue, Mode mode) {
        this.returnValue = returnValue;
        this.mode = mode;
    }

    @Override
    public void execute(final RequestModel model, final CoreCompletionHandler completionHandler) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mode == Mode.SUCCESS) {
                    completionHandler.onSuccess(model.getId(), returnValue);
                } else if (mode == Mode.ERROR_RESPONSE_MODEL) {
                    completionHandler.onError(model.getId(), returnValue);
                } else if (mode == Mode.ERROR_EXCEPTION) {
                    completionHandler.onError(model.getId(), exception);
                }
            }
        }, 100);

    }
}
