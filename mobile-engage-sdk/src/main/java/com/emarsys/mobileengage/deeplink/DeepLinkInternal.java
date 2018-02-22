package com.emarsys.mobileengage.deeplink;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.mobileengage.MobileEngageUtils;

import java.util.HashMap;

import static com.emarsys.mobileengage.endpoint.Endpoint.DEEP_LINK_CLICK;

public class DeepLinkInternal {

    private static final String EMS_DEEP_LINK_TRACKED_KEY = "ems_deep_link_tracked";

    private RequestManager manager;

    public DeepLinkInternal(RequestManager manager) {
        this.manager = manager;
    }

    public void trackDeepLinkOpen(Activity activity, Intent intent) {
        Uri uri = intent.getData();
        Intent intentFromActivity = activity.getIntent();
        boolean isLinkTracked = intentFromActivity.getBooleanExtra(EMS_DEEP_LINK_TRACKED_KEY, false);

        if (!isLinkTracked && uri != null) {
            String ems_dl = "ems_dl";
            String deepLinkQueryParam = uri.getQueryParameter(ems_dl);

            if (deepLinkQueryParam != null) {
                HashMap<String, Object> payload = new HashMap<>();
                payload.put(ems_dl, deepLinkQueryParam);

                RequestModel model = new RequestModel.Builder()
                        .url(DEEP_LINK_CLICK)
                        .payload(payload)
                        .build();

                MobileEngageUtils.incrementIdlingResource();
                intentFromActivity.putExtra(EMS_DEEP_LINK_TRACKED_KEY, true);
                manager.submit(model);
            }
        }
    }

}
