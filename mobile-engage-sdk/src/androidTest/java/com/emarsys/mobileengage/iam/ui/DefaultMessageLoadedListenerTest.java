package com.emarsys.mobileengage.iam.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;

import com.emarsys.core.activity.CurrentActivityWatchdog;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultMessageLoadedListenerTest {

    static {
        mock(Activity.class);
        mock(FragmentManager.class);
        mock(Fragment.class);
    }

    private FragmentManager fragmentManager;
    private Activity currentActivity;
    private DefaultMessageLoadedListener listener;
    private IamDialog dialog;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();


    @Before
    public void init() throws Exception {
        listener = new DefaultMessageLoadedListener();

        currentActivity = mock(Activity.class);
        fragmentManager = mock(FragmentManager.class);
        when(currentActivity.getFragmentManager()).thenReturn(fragmentManager);
        setActivityWatchdogState(true, currentActivity);
        dialog = mock(IamDialog.class);
        listener.iamDialog = dialog;
    }

    @After
    public void tearDown() throws Exception {
        setActivityWatchdogState(false, null);
    }

    @Test
    public void testConstructor_iamDialogShouldBeInitialized() {
        Assert.assertNotNull(new DefaultMessageLoadedListener().iamDialog);
    }

    @Test
    public void testOnMessageLoaded_shouldDisplayTheDialog() {
        listener.onMessageLoaded();

        verify(dialog).show(fragmentManager, IamDialog.TAG);
    }

    @Test
    public void testOnMessageLoaded_shouldNotCallShowOnDialog_whenThereIsAlreadyAFragmentWithSameTag() {
        Fragment fragment = mock(Fragment.class);
        when(fragmentManager.findFragmentByTag(IamDialog.TAG)).thenReturn(fragment);

        listener.onMessageLoaded();

        verify(dialog, times(0)).show(fragmentManager, IamDialog.TAG);
    }

    private void setActivityWatchdogState(boolean isRegistered, Activity activity) throws NoSuchFieldException, IllegalAccessException {
        Field activityField = CurrentActivityWatchdog.class.getDeclaredField("currentActivity");
        activityField.setAccessible(true);
        activityField.set(null, activity);

        Field isRegisteredField = CurrentActivityWatchdog.class.getDeclaredField("isRegistered");
        isRegisteredField.setAccessible(true);
        isRegisteredField.set(null, isRegistered);
    }

}