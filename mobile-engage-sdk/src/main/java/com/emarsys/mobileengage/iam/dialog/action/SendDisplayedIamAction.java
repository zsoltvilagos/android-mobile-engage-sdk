package com.emarsys.mobileengage.iam.dialog.action;

import android.os.Handler;

import com.emarsys.core.request.RequestManager;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.storage.MeIdSignatureStorage;
import com.emarsys.mobileengage.storage.MeIdStorage;
import com.emarsys.mobileengage.util.RequestUtils;

import java.util.HashMap;
import java.util.Map;

public class SendDisplayedIamAction implements OnDialogShownAction {

    private Handler handler;
    private RequestManager requestManager;
    private String applicationCode;
    private MeIdStorage meIdStorage;
    private MeIdSignatureStorage meIdSignatureStorage;
    private TimestampProvider timestampProvider;

    public SendDisplayedIamAction(
            Handler handler,
            RequestManager requestManager,
            String applicationCode,
            MeIdStorage meIdStorage,
            MeIdSignatureStorage meIdSignatureStorage,
            TimestampProvider timestampProvider) {
        Assert.notNull(handler, "Handler must not be null!");
        Assert.notNull(requestManager, "RequestManager must not be null!");
        Assert.notNull(applicationCode, "ApplicationCode must not be null!");
        Assert.notNull(meIdStorage, "MeIdStorage must not be null!");
        Assert.notNull(meIdSignatureStorage, "MeIdSignatureStorage must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        this.handler = handler;
        this.requestManager = requestManager;
        this.applicationCode = applicationCode;
        this.meIdStorage = meIdStorage;
        this.meIdSignatureStorage = meIdSignatureStorage;
        this.timestampProvider = timestampProvider;
    }

    @Override
    public void execute(final String campaignId, final long timestamp) {
        Assert.notNull(campaignId, "CampaignId must not be null!");
        handler.post(new Runnable() {
            @Override
            public void run() {
                Map<String, String> attributes = new HashMap<>();
                attributes.put("message_id", campaignId);

                requestManager.submit(RequestUtils.createInternalCustomEvent(
                        "inapp:viewed",
                        attributes,
                        applicationCode,
                        meIdStorage,
                        meIdSignatureStorage,
                        timestampProvider));
            }
        });
    }
}
