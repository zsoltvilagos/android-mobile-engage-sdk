package com.emarsys.mobileengage.iam;

import android.app.Application;
import android.content.pm.ApplicationInfo;

import com.emarsys.mobileengage.config.MobileEngageConfig;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IamInternalTest {
    Application application;
    MobileEngageConfig config;
    IamInternal iam;

    @Before
    public void init() {
        ApplicationInfo applicationInfo = new ApplicationInfo();

        application = mock(Application.class);
        when(application.getApplicationInfo()).thenReturn(applicationInfo);

        config = new MobileEngageConfig.Builder()
                .application(application)
                .credentials("id", "secret")
                .disableDefaultChannel()
                .build();

        iam = new IamInternal(config);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_configShouldNotBeNull() {
        new IamInternal(null);
    }

    @Test
    public void testConstructor_constructorInitializesFields() {
        assertNotNull(iam.activityLifecycleListener);
    }

    @Test
    public void testConstructor_registersLifecycleCallbackListener() {
        verify(application).registerActivityLifecycleCallbacks(iam.activityLifecycleListener);
    }
}