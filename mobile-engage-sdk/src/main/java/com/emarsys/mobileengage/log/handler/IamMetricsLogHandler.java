package com.emarsys.mobileengage.log.handler;

import com.emarsys.core.handler.Handler;
import com.emarsys.core.util.Assert;

import java.util.Map;

public class IamMetricsLogHandler implements Handler<Map<String, Object>, Map<String, Object>> {
    @Override
    public Map<String, Object> handle(Map<String, Object> item) {
        Assert.notNull(item, "Item must not be null!");
        Map<String, Object> result = null;
        if (item.containsKey("in-database")) {
            result = item;
        }
        return result;
    }
}
