package com.emarsys.mobileengage.responsehandler;

import com.emarsys.core.response.ResponseModel;
import com.emarsys.mobileengage.MobileEngageInternal;

import org.json.JSONException;
import org.json.JSONObject;


public class MeIdResponseHandler extends AbstractResponseHandler {

    MobileEngageInternal mobileEngageInternal;

    public MeIdResponseHandler(MobileEngageInternal mobileEngageInternal) {
        this.mobileEngageInternal = mobileEngageInternal;
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
            mobileEngageInternal.setMeId(body.getString("api_me_id"));
        } catch (JSONException ignore) {
        }
    }
}
