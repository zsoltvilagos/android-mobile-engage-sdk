package com.emarsys.mobileengage.deeplink;

import android.app.Activity;
import android.content.Intent;

import com.emarsys.mobileengage.MobileEngageInternal;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class DeepLinkActionTest {

    static {
        mock(Activity.class);
    }

    private MobileEngageInternal mockMobileEngageInternal;
    private DeepLinkAction action;

    @Before
    public void setUp() throws Exception {
        mockMobileEngageInternal = mock(MobileEngageInternal.class);
        action = new DeepLinkAction(mockMobileEngageInternal);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_mobileEngageInternalMustNotBeNull() {
        new DeepLinkAction(null);
    }

    @Test
    public void testExecute_callsMobileEngageInternal() throws Exception {
        Intent intent = mock(Intent.class);
        Activity activity = mock(Activity.class);
        when(activity.getIntent()).thenReturn(intent);

        action.execute(activity);

        verify(mockMobileEngageInternal).trackDeepLinkOpen(activity, intent);
    }

    @Test
    public void testExecute_neverCallsMobileEngageInternal_whenIntentFromActivityIsNull() {
        Activity activity = mock(Activity.class);

        action.execute(activity);

        verifyZeroInteractions(mockMobileEngageInternal);
    }

    @Test
    public void testExecute_neverCallsMobileEngageInternal_whenActivityIsNull() {
        action.execute(null);

        verifyZeroInteractions(mockMobileEngageInternal);
    }

}