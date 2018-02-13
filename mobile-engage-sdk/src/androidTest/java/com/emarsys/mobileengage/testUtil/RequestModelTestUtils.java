package com.emarsys.mobileengage.testUtil;

import com.emarsys.core.request.model.RequestModel;

import static org.junit.Assert.assertEquals;

public class RequestModelTestUtils {

    public static void assertEqualsExceptId(RequestModel expected, RequestModel actual) {
        assertEquals(expected.getUrl(), actual.getUrl());
        assertEquals(expected.getMethod(), actual.getMethod());
        assertEquals(expected.getPayload(), actual.getPayload());
        assertEquals(expected.getHeaders(), actual.getHeaders());
        assertEquals(expected.getTimestamp(), actual.getTimestamp());
        assertEquals(expected.getTtl(), actual.getTtl());
    }

}
