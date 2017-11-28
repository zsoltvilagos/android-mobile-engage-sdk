package com.emarsys.mobileengage;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.responsehandler.AbstractResponseHandler;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

import java.lang.ref.WeakReference;
import java.util.List;

public class MobileEngageCoreCompletionHandler implements CoreCompletionHandler {

    WeakReference<MobileEngageStatusListener> weakStatusListener;
    List<AbstractResponseHandler> responseHandlers;

    public MobileEngageCoreCompletionHandler(MobileEngageStatusListener listener) {
        this.weakStatusListener = new WeakReference<>(listener);
    }

    public void setResponseHandlers(List<AbstractResponseHandler> responseHandlers) {
        this.responseHandlers = responseHandlers;
    }

    MobileEngageStatusListener getStatusListener() {
        return weakStatusListener.get();
    }

    void setStatusListener(MobileEngageStatusListener listener) {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Argument: %s", listener);
        this.weakStatusListener = new WeakReference<>(listener);
    }

    @Override
    public void onSuccess(final String id, final ResponseModel responseModel) {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Argument: %s", responseModel);
        MobileEngageUtils.decrementIdlingResource();

        for (AbstractResponseHandler responseHandler : responseHandlers) {
            responseHandler.processResponse(responseModel);
        }

        MobileEngageStatusListener listener = getStatusListener();
        if (listener != null) {
            EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Notifying statusListener");
            listener.onStatusLog(id, responseModel.getMessage());
        }
    }

    @Override
    public void onError(final String id, final Exception cause) {
        MobileEngageUtils.decrementIdlingResource();
        handleOnError(id, cause);
    }

    @Override
    public void onError(final String id, final ResponseModel responseModel) {
        MobileEngageUtils.decrementIdlingResource();
        Exception exception = new MobileEngageException(
                responseModel.getStatusCode(),
                responseModel.getMessage(),
                responseModel.getBody());
        handleOnError(id, exception);
    }

    private void handleOnError(String id, Exception cause) {
        EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Argument: %s", cause);
        MobileEngageStatusListener listener = getStatusListener();
        if (listener != null) {
            EMSLogger.log(MobileEngageTopic.MOBILE_ENGAGE, "Notifying statusListener");
            listener.onError(id, cause);
        }
    }
}
