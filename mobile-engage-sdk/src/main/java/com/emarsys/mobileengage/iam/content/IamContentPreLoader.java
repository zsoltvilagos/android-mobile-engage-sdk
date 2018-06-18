package com.emarsys.mobileengage.iam.content;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;

public class IamContentPreLoader {

    private final RestClient restClient;

    public IamContentPreLoader(RestClient restClient) {
        Assert.notNull(restClient, "RestClient must not be null!");
        this.restClient = restClient;
    }

    public void preLoadContent(String url, final CompletionListener completionListener) {
        Assert.notNull(completionListener, "CompletionListener must not be null!");
        restClient.execute(new RequestModel.Builder().url(url).build(), new CoreCompletionHandler() {
            @Override
            public void onSuccess(String id, ResponseModel responseModel) {
                completionListener.onContentPreLoadCompleted(responseModel.getBody());
            }

            @Override
            public void onError(String id, ResponseModel responseModel) {
                completionListener.onContentPreLoadCompleted(null);
            }

            @Override
            public void onError(String id, Exception cause) {
                completionListener.onContentPreLoadCompleted(null);
            }
        });
    }

    public static interface CompletionListener {

        public void onContentPreLoadCompleted(String content);

    }

}
