package com.emarsys.mobileengage.database;

import com.emarsys.mobileengage.iam.SqlSpecification;

public class QueryAll implements SqlSpecification {

    private final String tableName;

    public QueryAll(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String getSql() {
        return String.format("SELECT * FROM %s;", tableName);
    }

    @Override
    public String[] getArgs() {
        return null;
    }
}
