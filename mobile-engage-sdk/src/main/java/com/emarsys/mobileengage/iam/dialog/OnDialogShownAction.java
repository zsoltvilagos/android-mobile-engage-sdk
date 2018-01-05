package com.emarsys.mobileengage.iam.dialog;

import android.os.Handler;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.iam.model.DisplayedIam;
import com.emarsys.mobileengage.iam.model.DisplayedIamRepository;

public class OnDialogShownAction {

    Handler handler;
    DisplayedIamRepository repository;

    public OnDialogShownAction(Handler handler, DisplayedIamRepository repository) {
        Assert.notNull(handler, "Handler must not be null!");
        Assert.notNull(repository, "Repository must not be null!");
        this.handler = handler;
        this.repository = repository;
    }

    public void execute(final String campaignId, final long timestamp) {
        Assert.notNull(campaignId, "CampaignId must not be null!");
        handler.post(new Runnable() {
            @Override
            public void run() {
                DisplayedIam iam = new DisplayedIam(campaignId, timestamp, "");
                repository.add(iam);
            }
        });
    }
}
