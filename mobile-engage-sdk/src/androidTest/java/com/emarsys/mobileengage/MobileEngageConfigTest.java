package com.emarsys.mobileengage;

import android.app.Application;
import android.support.test.InstrumentationRegistry;

import com.emarsys.mobileengage.testUtil.ApplicationTestUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class MobileEngageConfigTest {
    private String APP_ID = "appID";
    private String SECRET = "5678987654345678654";
    private MobileEngageStatusListener statusListenerMock;
    private Application application;
    private Application applicationDebug;
    private Application applicationRelease;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Before
    public void init(){
        application = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        applicationDebug = ApplicationTestUtils.applicationDebug();
        applicationRelease = ApplicationTestUtils.applicationRelease();
        statusListenerMock =  mock(MobileEngageStatusListener.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_applicationShouldNotBeNull() throws Exception {
        new MobileEngageConfig(null, APP_ID, SECRET, statusListenerMock, true, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_applicationCodeShouldNotBeNull() throws Exception {
        new MobileEngageConfig(application, null, SECRET, statusListenerMock, true, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_secretShouldNotBeNull() throws Exception {
        new MobileEngageConfig(application, APP_ID, null, statusListenerMock, true, false);
    }

    @Test
    public void testBuilder_withMandatoryArguments() {
        MobileEngageConfig expected = new MobileEngageConfig(applicationDebug, APP_ID, SECRET, null, true, false);

        MobileEngageConfig result = new MobileEngageConfig.Builder()
                .application(applicationDebug)
                .credentials(APP_ID, SECRET)
                .build();

        assertEquals(expected, result);
    }

    @Test
    public void testBuilder_withAllArguments() {
        MobileEngageConfig expected = new MobileEngageConfig(applicationDebug, APP_ID, SECRET, statusListenerMock, true, true);

        MobileEngageConfig result = new MobileEngageConfig.Builder()
                .application(applicationDebug)
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
        MobileEngageConfig expected = new MobileEngageConfig(applicationDebug, APP_ID, SECRET, statusListenerMock, true, true);

        MobileEngageConfig result = new MobileEngageConfig.Builder()
                .from(expected)
                .build();

        assertEquals(expected, result);
    }

    @Test
    public void testDebugApplication(){
        MobileEngageConfig result = new MobileEngageConfig.Builder()
                .application(applicationDebug)
                .credentials("", "")
                .build();
        assertTrue(result.isDebugMode());
    }

    @Test
    public void testReleaseApplication(){
        MobileEngageConfig result = new MobileEngageConfig.Builder()
                .application(applicationRelease)
                .credentials("", "")
                .build();
        assertFalse(result.isDebugMode());
    }
}