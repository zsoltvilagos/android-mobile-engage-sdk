package com.emarsys.mobileengage;

import android.app.Application;
import android.support.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class MobileEngageConfigTest {
    private String APP_ID = "appID";
    private String SECRET = "5678987654345678654";
    private MobileEngageStatusListener statusListenerMock;
    private Application application;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Before
    public void init(){
        application = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        statusListenerMock =  mock(MobileEngageStatusListener.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_applicationShouldNotBeNull() throws Exception {
        new MobileEngageConfig(null, APP_ID, SECRET, statusListenerMock, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_applicationCodeShouldNotBeNull() throws Exception {
        new MobileEngageConfig(application, null, SECRET, statusListenerMock, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_secretShouldNotBeNull() throws Exception {
        new MobileEngageConfig(application, APP_ID, null, statusListenerMock, false);
    }

    @Test
    public void testBuilder_withMandatoryArguments() {
        MobileEngageConfig expected = new MobileEngageConfig(application, APP_ID, SECRET, null, false);

        MobileEngageConfig result = new MobileEngageConfig.Builder()
                .application(application)
                .credentials(APP_ID, SECRET)
                .build();

        assertEquals(expected, result);
    }

    @Test
    public void testBuilder_withAllArguments() {
        MobileEngageConfig expected = new MobileEngageConfig(application, APP_ID, SECRET, statusListenerMock, true);

        MobileEngageConfig result = new MobileEngageConfig.Builder()
                .application(application)
                .credentials(APP_ID, SECRET)
                .statusListener(statusListenerMock)
                .enableIdlingResource(true)
                .build();

        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilder_from_shouldNotAcceptNull(){
        new MobileEngageConfig.Builder().from(null);
    }

    @Test
    public void testBuilder_from(){
        MobileEngageConfig expected = new MobileEngageConfig(application, APP_ID, SECRET, statusListenerMock, true);

        MobileEngageConfig result = new MobileEngageConfig.Builder()
                .from(expected)
                .build();

        assertEquals(expected, result);
    }
}