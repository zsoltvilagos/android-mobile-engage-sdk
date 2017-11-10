package com.emarsys.mobileengage.iam.specification;

import com.emarsys.mobileengage.iam.DisplayedIamContract;
import com.emarsys.mobileengage.iam.SqlSpecification;

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
