package com.emarsys.mobileengage.responsehandler;

import com.emarsys.core.response.ResponseModel;
import com.emarsys.mobileengage.storage.MeIdStorage;

import org.json.JSONException;
import org.json.JSONObject;


public class MeIdResponseHandler extends AbstractResponseHandler {

    MeIdStorage meIdStorage;

    public MeIdResponseHandler(MeIdStorage meIdStorage) {
        this.meIdStorage = meIdStorage;
    }

    @Override
    protected boolean shouldHandleResponse(ResponseModel responseModel) {
        JSONObject body = responseModel.getParsedBody();
        return body != null && body.has("api_me_id");
    }

    @Override
    protected void handleResponse(ResponseModel responseModel) {
        JSONObject body = responseModel.getParsedBody();
        try {
            meIdStorage.set(body.getString("api_me_id"));
        } catch (JSONException ignore) {
        }
    }
}
