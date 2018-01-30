package com.emarsys.mobileengage.iam.model.requestRepositoryProxy;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.emarsys.core.DeviceInfo;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.database.repository.specification.QueryAll;
import com.emarsys.core.request.model.CompositeRequestModel;
import com.emarsys.core.request.model.RequestContract;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.request.model.RequestModelRepository;
import com.emarsys.core.request.model.specification.QueryNewestRequestModel;
import com.emarsys.core.util.TimestampUtils;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClickedRepository;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIamRepository;
import com.emarsys.mobileengage.testUtil.DatabaseTestUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;
import com.emarsys.mobileengage.util.RequestUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class RequestRepositoryProxyTest {

    public static final String MEID = "123meid456";

    private DeviceInfo deviceInfo;
    private DeviceInfo mockDeviceInfo;

    private Repository<RequestModel, SqlSpecification> mockRequestModelRepository;
    private Repository<DisplayedIam, SqlSpecification> mockDisplayedIamRepository;
    private Repository<ButtonClicked, SqlSpecification> mockButtonClickedRepository;

    private Repository<RequestModel, SqlSpecification> requestModelRepository;
    private Repository<DisplayedIam, SqlSpecification> displayedIamRepository;
    private Repository<ButtonClicked, SqlSpecification> buttonClickedRepository;
    private RequestRepositoryProxy compositeRepository;
    private Context context;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        DatabaseTestUtils.deleteCoreDatabase();
        DatabaseTestUtils.deleteMobileEngageDatabase();

        context = InstrumentationRegistry.getTargetContext();

        mockDeviceInfo = mock(DeviceInfo.class);
        deviceInfo = new DeviceInfo(context);

        mockRequestModelRepository = mock(Repository.class);
        mockDisplayedIamRepository = mock(Repository.class);
        mockButtonClickedRepository = mock(Repository.class);

        requestModelRepository = new RequestModelRepository(context);
        displayedIamRepository = new DisplayedIamRepository(context);
        buttonClickedRepository = new ButtonClickedRepository(context);

        compositeRepository = new RequestRepositoryProxy(
                mockDeviceInfo,
                mockRequestModelRepository,
                mockDisplayedIamRepository,
                mockButtonClickedRepository);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_deviceInfo_mustNotBeNull() {
        new RequestRepositoryProxy(null, mockRequestModelRepository, mockDisplayedIamRepository, mockButtonClickedRepository);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_requestRepository_mustNotBeNull() {
        new RequestRepositoryProxy(mockDeviceInfo, null, mockDisplayedIamRepository, mockButtonClickedRepository);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_displayedIamRepository_mustNotBeNull() {
        new RequestRepositoryProxy(mockDeviceInfo, mockRequestModelRepository, null, mockButtonClickedRepository);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_buttonClickedRepository_mustNotBeNull() {
        new RequestRepositoryProxy(mockDeviceInfo, mockRequestModelRepository, mockDisplayedIamRepository, null);
    }

    @Test
    public void testAdd_shouldDelegate_toRequestModelRepository() {
        RequestModel requestModel = mock(RequestModel.class);

        compositeRepository.add(requestModel);

        verify(mockRequestModelRepository).add(requestModel);
    }

    @Test
    public void testAdd_shouldNotStoreCompositeRequestModels() {
        CompositeRequestModel requestModel = mock(CompositeRequestModel.class);

        compositeRepository.add(requestModel);

        verifyZeroInteractions(mockRequestModelRepository);
    }

    @Test
    public void testRemove_shouldDelegate_toRequestModelRepository() {
        SqlSpecification spec = mock(SqlSpecification.class);

        compositeRepository.remove(spec);

        verify(mockRequestModelRepository).remove(spec);
    }

    @Test
    public void testIsEmpty_whenEmpty_shouldDelegate_toRequestModelRepository() {
        when(mockRequestModelRepository.isEmpty()).thenReturn(true);

        assertTrue(compositeRepository.isEmpty());
        verify(mockRequestModelRepository).isEmpty();
    }

    @Test
    public void testIsEmpty_whenNotEmpty_shouldDelegate_toRequestModelRepository() {
        when(mockRequestModelRepository.isEmpty()).thenReturn(false);

        assertFalse(compositeRepository.isEmpty());
        verify(mockRequestModelRepository).isEmpty();
    }

    @Test
    public void testQuery_shouldReturnOriginalQuery_whenThereAreNoCustomEvents() {
        compositeRepository = compositeRepositoryWithRealDependencies();

        RequestModel firstRequestModel = requestModel();
        requestModelRepository.add(firstRequestModel);
        requestModelRepository.add(requestModel());
        requestModelRepository.add(requestModel());

        List<RequestModel> expected = Collections.singletonList(firstRequestModel);

        assertEquals(expected, compositeRepository.query(new QueryNewestRequestModel()));
    }

    @Test
    public void testQuery_resultShouldContainCompositeRequestModel_whenResultContainsCustomEvent() {
        compositeRepository = compositeRepositoryWithRealDependencies();

        RequestModel request1 = requestModel();
        RequestModel request2 = requestModel();
        RequestModel request3 = requestModel();

        final RequestModel customEvent1 = customEvent_V3(900, "event1");

        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", "value2");
        final RequestModel customEvent2 = customEvent_V3(1000, "event2", attributes);
        final RequestModel customEvent3 = customEvent_V3(1200, "event3");

        requestModelRepository.add(request1);
        requestModelRepository.add(request2);
        requestModelRepository.add(customEvent1);
        requestModelRepository.add(customEvent2);
        requestModelRepository.add(request3);
        requestModelRepository.add(customEvent3);

        RequestModel expectedComposite = new CompositeRequestModel(
                RequestUtils.createEventUrl_V3(MEID),
                RequestMethod.POST,
                new HashMap<String, Object>() {{

                    Map<String, Object> event1 = new HashMap<>();
                    event1.put("type", "custom");
                    event1.put("name", "event1");
                    event1.put("timestamp", TimestampUtils.formatTimestampWithUTC(900));

                    Map<String, Object> event2 = new HashMap<>();
                    event2.put("type", "custom");
                    event2.put("name", "event2");
                    event2.put("timestamp", TimestampUtils.formatTimestampWithUTC(1000));
                    event2.put("attributes", new HashMap<String, String>() {{
                        put("key1", "value1");
                        put("key2", "value2");
                    }});

                    Map<String, Object> event3 = new HashMap<>();
                    event3.put("type", "custom");
                    event3.put("name", "event3");
                    event3.put("timestamp", TimestampUtils.formatTimestampWithUTC(1200));

                    put("viewed_messages", new ArrayList<>());
                    put("clicks", new ArrayList<>());
                    put("events", Arrays.asList(event1, event2, event3));
                    put("hardware_id", new DeviceInfo(context).getHwid());
                }},
                customEvent1.getHeaders(),
                new String[]{customEvent1.getId(), customEvent2.getId(), customEvent3.getId()}
        );

        List<RequestModel> expected = Arrays.asList(
                request1,
                request2,
                expectedComposite,
                request3);

        assertEquals(expected, compositeRepository.query(new QueryAll(RequestContract.TABLE_NAME)));
    }

    private RequestRepositoryProxy compositeRepositoryWithRealDependencies() {
        return new RequestRepositoryProxy(
                deviceInfo,
                requestModelRepository,
                displayedIamRepository,
                buttonClickedRepository
        );
    }

    private RequestModel customEvent_V3(long timestamp, String eventName) {
        return customEvent_V3(timestamp, eventName, null);
    }

    private RequestModel customEvent_V3(long timestamp, final String eventName, final Map<String, Object> attributes) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "custom");
        event.put("name", eventName);
        event.put("timestamp", TimestampUtils.formatTimestampWithUTC(timestamp));
        if (attributes != null && !attributes.isEmpty()) {
            event.put("attributes", attributes);
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("clicks", new ArrayList<>());
        payload.put("viewed_messages", new ArrayList<>());
        payload.put("events", Collections.singletonList(event));
        payload.put("hardware_id", "dummy_hardware_id");

        Map<String, String> headers = new HashMap<>();
        headers.put("custom_event_header1", "custom_event_value1");
        headers.put("custom_event_header2", "custom_event_value2");

        return new RequestModel(RequestUtils.createEventUrl_V3(MEID), RequestMethod.POST, payload, headers, System.currentTimeMillis(), 999, RequestModel.nextId());
    }

    private RequestModel requestModel() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("key", randomString());

        Map<String, String> headers = new HashMap<>();
        headers.put("header1", "value1");
        headers.put("header2", "value2");

        return new RequestModel.Builder()
                .url("https://emarsys.com")
                .payload(payload)
                .headers(headers)
                .build();
    }

    private String randomString() {
        return UUID.randomUUID().toString();
    }

}