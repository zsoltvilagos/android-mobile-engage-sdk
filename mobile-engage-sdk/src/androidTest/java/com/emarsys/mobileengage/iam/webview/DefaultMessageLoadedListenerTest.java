package com.emarsys.mobileengage.iam.webview;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.test.filters.SdkSuppress;

import com.emarsys.mobileengage.iam.dialog.IamDialog;
import com.emarsys.mobileengage.testUtil.CurrentActivityWatchdogTestUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static android.os.Build.VERSION_CODES.KITKAT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SdkSuppress(minSdkVersion = KITKAT)
public class DefaultMessageLoadedListenerTest {

    static {
        mock(Activity.class);
        mock(FragmentManager.class);
        mock(Fragment.class);
        mock(IamDialog.class);
    }

    private FragmentManager fragmentManager;
    private DefaultMessageLoadedListener listener;
    private IamDialog dialog;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();


    @Before
    public void init() throws Exception {
        listener = new DefaultMessageLoadedListener(mock(IamDialog.class));

        Activity currentActivity = mock(Activity.class);
        fragmentManager = mock(FragmentManager.class);
        when(currentActivity.getFragmentManager()).thenReturn(fragmentManager);
        CurrentActivityWatchdogTestUtils.setActivityWatchdogState(currentActivity);
        dialog = mock(IamDialog.class);
        listener.iamDialog = dialog;
    }

    @After
    public void tearDown() throws Exception {
        CurrentActivityWatchdogTestUtils.resetCurrentActivityWatchdog();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_iamDialogShouldNotBeNull() {
        new DefaultMessageLoadedListener(null);
    }

    @Test
    public void testConstructor_iamDialogShouldBeInitialized() {
        Assert.assertNotNull(new DefaultMessageLoadedListener(mock(IamDialog.class)).iamDialog);
    }

    @Test
    public void testOnMessageLoaded_shouldDisplayTheDialog() {
        listener.onMessageLoaded();

        verify(dialog).show(fragmentManager, IamDialog.TAG);
    }

    @Test
    public void testOnMessageLoaded_shouldNotShowDialog_whenThereIsNoAvailableActivity() throws Exception {
        CurrentActivityWatchdogTestUtils.setActivityWatchdogState(null);

        listener.onMessageLoaded();

        verify(dialog, times(0)).show(fragmentManager, IamDialog.TAG);
    }

    @Test
    public void testOnMessageLoaded_shouldNotShowDialog_whenThereIsAlreadyAFragmentWithSameTag() {
        Fragment fragment = mock(Fragment.class);
        when(fragmentManager.findFragmentByTag(IamDialog.TAG)).thenReturn(fragment);

        listener.onMessageLoaded();

        verify(dialog, times(0)).show(fragmentManager, IamDialog.TAG);
    }

}