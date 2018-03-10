package com.emarsys.mobileengage.iam.webview;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.test.filters.SdkSuppress;

import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.mobileengage.iam.dialog.IamDialog;
import com.emarsys.mobileengage.testUtil.CurrentActivityWatchdogTestUtils;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Map;

import static android.os.Build.VERSION_CODES.KITKAT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SdkSuppress(minSdkVersion = KITKAT)
public class DefaultMessageLoadedListenerTest {

    public static final long TIMESTAMP = 900_800L;

    static {
        mock(Activity.class);
        mock(FragmentManager.class);
        mock(Fragment.class);
        mock(IamDialog.class);
    }

    private FragmentManager fragmentManager;
    private DefaultMessageLoadedListener listener;
    private IamDialog dialog;
    private Repository<Map<String, Object>, SqlSpecification> logRepositoryMock;
    private TimestampProvider timestampProvider;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() throws Exception {

        Activity currentActivity = mock(Activity.class);
        fragmentManager = mock(FragmentManager.class);
        when(currentActivity.getFragmentManager()).thenReturn(fragmentManager);
        CurrentActivityWatchdogTestUtils.setActivityWatchdogState(currentActivity);
        dialog = mock(IamDialog.class);

        logRepositoryMock = mock(Repository.class);
        timestampProvider = mock(TimestampProvider.class);

        listener = new DefaultMessageLoadedListener(dialog, logRepositoryMock, TIMESTAMP, timestampProvider);
    }

    @After
    public void tearDown() throws Exception {
        CurrentActivityWatchdogTestUtils.resetCurrentActivityWatchdog();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_iamDialog_shouldNotBeNull() {
        new DefaultMessageLoadedListener(null, logRepositoryMock, TIMESTAMP, timestampProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_logRepository_shouldNotBeNull() {
        new DefaultMessageLoadedListener(dialog, null, TIMESTAMP, timestampProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_timestampProvider_shouldNotBeNull() {
        new DefaultMessageLoadedListener(dialog, logRepositoryMock, TIMESTAMP, null);
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