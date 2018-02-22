package com.emarsys.mobileengage.deeplink;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeepLinkInternalTest {

    private Activity mockActivity;
    private DeepLinkInternal deepLinkInternal;
    private RequestManager manager;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        mockActivity = mock(Activity.class, Mockito.RETURNS_DEEP_STUBS);

        manager = mock(RequestManager.class);
        deepLinkInternal = new DeepLinkInternal(manager);
    }

    @Test
    public void testConstructor_requestManagerMustNotBeNull() {
        new DeepLinkInternal(null);
    }

    @Test
    public void testTrackDeepLink_requestManagerCalledWithCorrectRequestModel() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://demo-mobileengage.emarsys.net/something?fancy_url=1&ems_dl=1_2_3_4_5"));

        HashMap<String, Object> payload = new HashMap<>();
        payload.put("ems_dl", "1_2_3_4_5");

        RequestModel expected = new RequestModel.Builder()
                .url("https://deep-link.eservice.emarsys.net/api/clicks")
                .payload(payload)
                .build();

        ArgumentCaptor<RequestModel> captor = ArgumentCaptor.forClass(RequestModel.class);

        deepLinkInternal.trackDeepLinkOpen(mockActivity, intent);

        verify(manager).submit(captor.capture());

        RequestModel result = captor.getValue();
        assertRequestModels(expected, result);
    }

    @Test
    public void testTrackDeepLink_setsClickedFlag_onIntentBundle() {
        Intent originalIntent = mock(Intent.class);
        when(mockActivity.getIntent()).thenReturn(originalIntent);

        Intent currentIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://demo-mobileengage.emarsys.net/something?fancy_url=1&ems_dl=1_2_3_4_5"));

        deepLinkInternal.trackDeepLinkOpen(mockActivity, currentIntent);

        verify(originalIntent).putExtra("ems_deep_link_tracked", true);
    }

    @Test
    public void testTrackDeepLink_doesNotCallRequestManager_whenTrackedFlagIsSet() {
        Intent intentFromActivity = mock(Intent.class);
        when(mockActivity.getIntent()).thenReturn(intentFromActivity);
        when(intentFromActivity.getBooleanExtra("ems_deep_link_tracked", false)).thenReturn(true);

        Intent currentIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://demo-mobileengage.emarsys.net/something?fancy_url=1&ems_dl=1_2_3_4_5"));

        deepLinkInternal.trackDeepLinkOpen(mockActivity, currentIntent);

        verify(manager, times(0)).submit(any(RequestModel.class));
    }

    @Test
    public void testTrackDeepLink_doesNotCallRequestManager_whenDataIsNull() {
        Intent intent = new Intent();

        deepLinkInternal.trackDeepLinkOpen(mockActivity, intent);

        verify(manager, times(0)).submit(any(RequestModel.class));
    }

    @Test
    public void testTrackDeepLink_doesNotCallRequestManager_whenUriDoesNotContainEmsDlQueryParameter() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://demo-mobileengage.emarsys.net/something?fancy_url=1&other=1_2_3_4_5"));

        deepLinkInternal.trackDeepLinkOpen(mockActivity, intent);

        verify(manager, times(0)).submit(any(RequestModel.class));
    }

    private void assertRequestModels(RequestModel expected, RequestModel result) {
        assertEquals(expected.getUrl(), result.getUrl());
        assertEquals(expected.getMethod(), result.getMethod());
        assertEquals(expected.getPayload(), result.getPayload());
    }
}