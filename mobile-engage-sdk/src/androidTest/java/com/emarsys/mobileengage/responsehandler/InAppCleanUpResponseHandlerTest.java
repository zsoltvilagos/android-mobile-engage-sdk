package com.emarsys.mobileengage.responsehandler;

import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;
import com.emarsys.mobileengage.iam.model.specification.FilterByCampaignId;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class InAppCleanUpResponseHandlerTest {

    InAppCleanUpResponseHandler handler;
    Repository<DisplayedIam, SqlSpecification> displayedIamRepository;
    Repository<ButtonClicked, SqlSpecification> buttonClickRepository;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        displayedIamRepository = mock(Repository.class);
        buttonClickRepository = mock(Repository.class);
        handler = new InAppCleanUpResponseHandler(displayedIamRepository, buttonClickRepository);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_inappRepository_shouldNotBeNull() {
        new InAppCleanUpResponseHandler(null, buttonClickRepository);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_buttonClickedRepository_shouldNotBeNull() {
        new InAppCleanUpResponseHandler(displayedIamRepository, null);
    }

    @Test
    public void testShouldHandleResponse_shouldReturnFalse_parsedJsonIsNull() {
        ResponseModel response = buildResponseModel("html");
        assertFalse(handler.shouldHandleResponse(response));
    }

    @Test
    public void testShouldHandleResponse_shouldReturnFalse_responseHasNotOldMessages() {
        ResponseModel response = buildResponseModel("{}");
        assertFalse(handler.shouldHandleResponse(response));
    }

    @Test
    public void testShouldHandleResponse_shouldReturnTrueWhen_responseHasOldMessages() {
        ResponseModel response = buildResponseModel("{'old_messages': ['123', '456', '78910']}");
        assertTrue(handler.shouldHandleResponse(response));
    }

    @Test
    public void testShouldHandleResponse_shouldReturnFalseWhen_oldMessagesIsEmpty() {
        ResponseModel response = buildResponseModel("{'old_messages': []}");
        assertFalse(handler.shouldHandleResponse(response));
    }

    @Test
    public void testHandleResponse_shouldDelete_oldInApp() {
        ResponseModel response = buildResponseModel("{'old_messages': ['123']}");
        handler.handleResponse(response);
        verify(displayedIamRepository).remove(new FilterByCampaignId("123"));
    }

    @Test
    public void testHandleResponse_shouldDelete_multiple_oldInApps() {
        ResponseModel response = buildResponseModel("{'old_messages': ['123', '456', '78910']}");
        handler.handleResponse(response);
        verify(displayedIamRepository).remove(new FilterByCampaignId("123", "456", "78910"));
    }

    @Test
    public void testHandleResponse_shouldDelete_oldButtonClick() {
        ResponseModel response = buildResponseModel("{'old_messages': ['123']}");
        handler.handleResponse(response);
        verify(buttonClickRepository).remove(new FilterByCampaignId("123"));
    }

    @Test
    public void testHandleResponse_shouldDelete_multiple_oldButtonClicks() {
        ResponseModel response = buildResponseModel("{'old_messages': ['123', '456', '78910']}");
        handler.handleResponse(response);
        verify(buttonClickRepository).remove(new FilterByCampaignId("123", "456", "78910"));
    }

    private ResponseModel buildResponseModel(String responseBody) {
        return new ResponseModel.Builder()
                .statusCode(200)
                .message("OK")
                .body(responseBody)
                .requestModel(mock(RequestModel.class))
                .build();
    }

}