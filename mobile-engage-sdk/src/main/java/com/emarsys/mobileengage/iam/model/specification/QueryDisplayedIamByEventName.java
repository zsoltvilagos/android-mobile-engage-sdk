package com.emarsys.mobileengage.iam.model.specification;

import com.emarsys.mobileengage.iam.SqlSpecification;
import com.emarsys.mobileengage.iam.model.DisplayedIamContract;

public class QueryDisplayedIamByEventName implements SqlSpecification {

    private final String eventName;

    public QueryDisplayedIamByEventName(String eventName) {
        this.eventName = eventName;
    }

    @Override
    public String getSql() {
        return DisplayedIamContract.SQL_SELECT_BY_EVENT_NAME;
    }

    @Override
    public String[] getArgs() {
        return new String[]{eventName};
    }
}
