package com.emarsys.mobileengage.iam.specification;

import com.emarsys.mobileengage.iam.SqlSpecification;

public class RemoveDisplayedIamByCampaignId implements SqlSpecification {

    private final String campaignId;

    public RemoveDisplayedIamByCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    @Override
    public String getSql() {
        return "campaign_id=?";
    }

    @Override
    public String[] getArgs() {
        return new String[]{campaignId};
    }
}
