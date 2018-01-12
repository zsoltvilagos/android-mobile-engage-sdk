package com.emarsys.mobileengage.iam.model.specification;

import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIamContract;

public class FilterByEventName implements SqlSpecification {

    private final String eventName;

    public FilterByEventName(String eventName) {
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
