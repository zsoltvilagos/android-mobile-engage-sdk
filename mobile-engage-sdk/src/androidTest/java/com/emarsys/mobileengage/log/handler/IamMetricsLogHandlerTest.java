package com.emarsys.mobileengage.log.handler;

import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class IamMetricsLogHandlerTest {
    private static final String CUSTOM_EVENT_V3_URL = "https://mobile-events.eservice.emarsys.net/v3/devices/123456789/events";
    private static final String NOT_CUSTOM_EVENT_V3_URL = "https://push.eservice.emarsys.net/api/mobileengage/v2/events/mycustomevent";
    private static final String REQUEST_ID = "request_id";
    private static final String URL = "url";
    private static final String IN_DATABASE = "in_database";
    private static final String NETWORKING_TIME = "networking_time";
    private static final String LOADING_TIME = "loading_time";
    private static final String ON_SCREEN_TIME = "on_screen_time";

    private IamMetricsLogHandler handler;
    private Map<String, Map<String, Object>> metricsBuffer;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        metricsBuffer = mock(HashMap.class, Mockito.CALLS_REAL_METHODS);
        handler = new IamMetricsLogHandler(metricsBuffer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_metricsBuffer_mustNotBeNull() {
        new IamMetricsLogHandler(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandle_itemMustNotBeNull() {
        handler.handle(null);
    }

    @Test
    public void testHandle_doesNotStore_anyMetric_ifRequestIdIsMissing() {
        Map<String, Object> metric = new HashMap<>();
        metric.put("url", CUSTOM_EVENT_V3_URL);
        metric.put(IN_DATABASE, 100);
        metric.put(NETWORKING_TIME, 200);
        metric.put(LOADING_TIME, 200);
        metric.put(ON_SCREEN_TIME, 200);

        handler.handle(metric);

        verifyZeroInteractions(metricsBuffer);
    }

    @Test
    public void testHandle_doesNotStore_anyMetric_ifRequestId_isNotString() {
        Map<String, Object> metric = new HashMap<>();
        metric.put("url", CUSTOM_EVENT_V3_URL);
        metric.put(IN_DATABASE, 100);
        metric.put(NETWORKING_TIME, 200);
        metric.put(REQUEST_ID, 200_00_00);
        metric.put(LOADING_TIME, 200);
        metric.put(ON_SCREEN_TIME, 200);

        handler.handle(metric);

        verifyZeroInteractions(metricsBuffer);
    }

    @Test
    public void testHandle_storesMetric_onlyIfContains_metricKey() {
        Map<String, Object> metric = new HashMap<>();
        metric.put(REQUEST_ID, "hash");
        metric.put(URL, CUSTOM_EVENT_V3_URL);

        handler.handle(metric);

        verifyZeroInteractions(metricsBuffer);
    }

    @Test
    public void testHandle_doesNotStore_inDatabaseMetric_ifUrlIsMissing() {
        Map<String, Object> metric = new HashMap<>();
        metric.put(IN_DATABASE, 123);
        metric.put(REQUEST_ID, "hash");

        handler.handle(metric);

        verifyZeroInteractions(metricsBuffer);
    }

    @Test
    public void testHandle_doesNotStore_inDatabaseMetric_ifUrl_isNotString() {
        Map<String, Object> metric = new HashMap<>();
        metric.put(IN_DATABASE, 123);
        metric.put(REQUEST_ID, "hash");
        metric.put(URL, Math.PI);

        handler.handle(metric);

        verifyZeroInteractions(metricsBuffer);
    }

    @Test
    public void testHandle_doesNotStore_inDatabaseMetric_ifUrlIsNot_V3CustomEvent() {
        Map<String, Object> metric = new HashMap<>();
        metric.put(IN_DATABASE, 123);
        metric.put(REQUEST_ID, "hash");
        metric.put(URL, NOT_CUSTOM_EVENT_V3_URL);

        handler.handle(metric);

        verifyZeroInteractions(metricsBuffer);
    }

    @Test
    public void testHandle_storesInDatabaseMetric() {
        Map<String, Object> input = new HashMap<>();
        input.put(IN_DATABASE, 200);
        input.put(REQUEST_ID, "id");
        input.put(URL, CUSTOM_EVENT_V3_URL);

        Map<String, Object> expectedStoredMetric = new HashMap<>(input);

        handler.handle(input);
        verify(metricsBuffer).put("id", expectedStoredMetric);
    }

    @Test
    public void testHandle_inDatabaseMetric_doesNotReturnIncompleteMetric() {
        Map<String, Object> input = new HashMap<>();
        input.put(IN_DATABASE, 200);
        input.put(REQUEST_ID, "id");
        input.put(URL, CUSTOM_EVENT_V3_URL);

        Assert.assertNull(handler.handle(input));
    }

    @Test
    public void testHandle_doesNotStore_networkingTimeMetric_ifUrlIsMissing() {
        Map<String, Object> metric = new HashMap<>();
        metric.put(NETWORKING_TIME, 1200);
        metric.put(REQUEST_ID, "hash");

        handler.handle(metric);

        verifyZeroInteractions(metricsBuffer);
    }

    @Test
    public void testHandle_doesNotStore_networkingTimeMetric_ifUrl_isNotString() {
        Map<String, Object> metric = new HashMap<>();
        metric.put(NETWORKING_TIME, 1200);
        metric.put(REQUEST_ID, "hash");
        metric.put(URL, Math.PI);

        handler.handle(metric);

        verifyZeroInteractions(metricsBuffer);
    }

    @Test
    public void testHandle_doesNotStore_networkingTimeMetric_ifUrlIsNot_V3CustomEvent() {
        Map<String, Object> metric = new HashMap<>();
        metric.put(NETWORKING_TIME, 1200);
        metric.put(REQUEST_ID, "hash");
        metric.put(URL, NOT_CUSTOM_EVENT_V3_URL);

        handler.handle(metric);

        verifyZeroInteractions(metricsBuffer);
    }

    @Test
    public void testHandle_storesNetworkingTimeMetric() {
        Map<String, Object> input = new HashMap<>();
        input.put(NETWORKING_TIME, 1200);
        input.put(REQUEST_ID, "id");
        input.put(URL, CUSTOM_EVENT_V3_URL);

        handler.handle(input);
        Map<String, Object> expectedStoredMetric = new HashMap<>(input);

        verify(metricsBuffer).put("id", expectedStoredMetric);
    }

    @Test
    public void testHandle_networkingTime_doesNotReturnIncompleteMetric() {
        Map<String, Object> input = new HashMap<>();
        input.put(NETWORKING_TIME, 1200);
        input.put(REQUEST_ID, "id");
        input.put(URL, CUSTOM_EVENT_V3_URL);

        Assert.assertNull(handler.handle(input));
    }

    @Test
    public void testHandle_mergesMetrics() {
        Map<String, Object> inDatabase = new HashMap<>();
        inDatabase.put(IN_DATABASE, 200);
        inDatabase.put(REQUEST_ID, "id");
        inDatabase.put(URL, CUSTOM_EVENT_V3_URL);

        Map<String, Object> networkingTime = new HashMap<>();
        networkingTime.put(NETWORKING_TIME, 1200);
        networkingTime.put(REQUEST_ID, "id");
        networkingTime.put(URL, CUSTOM_EVENT_V3_URL);

        Map<String, Object> merged = new HashMap<>();
        merged.put(IN_DATABASE, 200);
        merged.put(NETWORKING_TIME, 1200);
        merged.put(REQUEST_ID, "id");
        merged.put(URL, CUSTOM_EVENT_V3_URL);

        handler.handle(inDatabase);
        handler.handle(networkingTime);

        Assert.assertEquals(merged, metricsBuffer.get("id"));
    }
}