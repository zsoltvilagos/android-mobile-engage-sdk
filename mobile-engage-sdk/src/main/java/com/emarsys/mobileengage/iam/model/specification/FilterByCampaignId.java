package com.emarsys.mobileengage.iam.model.specification;

import com.emarsys.mobileengage.iam.SqlSpecification;

public class FilterByCampaignId implements SqlSpecification {

    private final String campaignId;

    public FilterByCampaignId(String campaignId) {
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
