package com.emarsys.mobileengage.log.handler;

import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.HashMap;
import java.util.Map;

public class IamMetricsLogHandlerTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    private IamMetricsLogHandler handler;

    @Before
    public void init() {
        handler = new IamMetricsLogHandler();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandle_itemMustNotBeNull() {
        new IamMetricsLogHandler().handle(null);
    }

    @Test
    public void testHandle_returnsItem_ifContainsCorrespondingKey() {
        Map<String, Object> input = new HashMap<>();
        input.put("in-database", 123);

        Map<String, Object> handledItem = handler.handle(input);

        Map<String, Object> expectedOutput = new HashMap<>();
        expectedOutput.put("in-database", 123);

        Assert.assertEquals(expectedOutput, handledItem);
    }

    @Test
    public void testHandle_returnsNull_ifInputLacksCorrespondingKey() {
        Map<String, Object> input = new HashMap<>();
        input.put("a", 123);
        input.put("b", "value");
        input.put("c", false);
        input.put("d", 456);

        Assert.assertEquals(null, handler.handle(input));
    }
}