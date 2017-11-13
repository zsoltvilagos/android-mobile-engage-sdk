package com.emarsys.mobileengage.iam.lifecycle;

import android.app.Activity;
import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class IamActivityLifecycleListenerTest {

    private IamActivityLifecycleListener listener;
    private Activity activity;
    private Activity nextActivity;

    @Before
    public void init() {
        listener = new IamActivityLifecycleListener();
        activity = mock(Activity.class);
        nextActivity = mock(Activity.class);
    }

    @Test
    public void testGetCurrentActivity_shouldStoreTheActivity_whenCallingOnResumed() {
        listener.onActivityResumed(activity);

        assertEquals(activity, listener.getCurrentActivity());
    }

    @Test
    public void testGetCurrentActivity_newerActivity_shouldOverride_thePrevious() {
        listener.onActivityResumed(activity);
        listener.onActivityResumed(nextActivity);
        listener.onActivityPaused(activity);

        assertEquals(nextActivity, listener.getCurrentActivity());
    }

    @Test
    public void testGetCurrentActivity_shouldReturnNull_whenCurrentActivityPauses_andThereIsNoNewerActivity() {
        listener.onActivityResumed(activity);
        listener.onActivityPaused(activity);

        assertNull(listener.getCurrentActivity());
    }

    @Test
    public void testGetCurrentActivity_otherLifecycleCallbacks_shouldBeIgnored() {
        Bundle bundle = new Bundle();

        listener.onActivityCreated(activity, bundle);
        listener.onActivityStarted(activity);
        listener.onActivityStopped(activity);
        listener.onActivitySaveInstanceState(activity, bundle);
        listener.onActivityDestroyed(activity);

        assertNull(listener.getCurrentActivity());
    }
}