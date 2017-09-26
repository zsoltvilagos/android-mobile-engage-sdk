package com.emarsys.mobileengage.config;

import android.app.Application;
import android.support.test.InstrumentationRegistry;

import com.emarsys.mobileengage.MobileEngageStatusListener;
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
    private OreoConfig mockOreoConfig;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Before
    public void init(){
        application = (Application) InstrumentationRegistry.getTargetContext().getApplicationContext();
        applicationDebug = ApplicationTestUtils.applicationDebug();
        applicationRelease = ApplicationTestUtils.applicationRelease();
        statusListenerMock =  mock(MobileEngageStatusListener.class);
        mockOreoConfig = mock(OreoConfig.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_applicationShouldNotBeNull() {
        new MobileEngageConfig(null, APP_ID, SECRET, statusListenerMock, true, false, mockOreoConfig);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_applicationCodeShouldNotBeNull() {
        new MobileEngageConfig(application, null, SECRET, statusListenerMock, true, false, mockOreoConfig);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_secretShouldNotBeNull() {
        new MobileEngageConfig(application, APP_ID, null, statusListenerMock, true, false, mockOreoConfig);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_oreoConfigShouldNotBeNull() {
        new MobileEngageConfig(application, APP_ID, SECRET, statusListenerMock, true, false, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_oreoConfigParameter_channelNameShouldNotBeNull_whenEnabled() {
        new MobileEngageConfig(application, APP_ID, SECRET, statusListenerMock, true, false, new OreoConfig(true, null, "description"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_oreoConfigParameter_channelDescriptionShouldNotBeNull_whenEnabled() {
        new MobileEngageConfig(application, APP_ID, SECRET, statusListenerMock, true, false, new OreoConfig(true, "name", null));
    }

    @Test
    public void testBuilder_withMandatoryArguments() {
        MobileEngageConfig expected = new MobileEngageConfig(
                applicationDebug,
                APP_ID,
                SECRET,
                null,
                true,
                false,
                new OreoConfig(false));

        MobileEngageConfig result = new MobileEngageConfig.Builder()
                .application(applicationDebug)
                .credentials(APP_ID, SECRET)
                .disableDefaultChannel()
                .build();

        assertEquals(expected, result);
    }

    @Test
    public void testBuilder_withAllArguments() {
        MobileEngageConfig expected = new MobileEngageConfig(
                applicationDebug,
                APP_ID,
                SECRET,
                statusListenerMock,
                true,
                true,
                new OreoConfig(true, "defaultChannelName", "defaultChannelDescription"));

        MobileEngageConfig result = new MobileEngageConfig.Builder()
                .application(applicationDebug)
                .credentials(APP_ID, SECRET)
                .statusListener(statusListenerMock)
                .enableIdlingResource(true)
                .enableDefaultChannel("defaultChannelName", "defaultChannelDescription")
                .build();

        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilder_from_shouldNotAcceptNull(){
        new MobileEngageConfig.Builder().from(null);
    }

    @Test
    public void testBuilder_from(){
        MobileEngageConfig expected = new MobileEngageConfig(
                applicationDebug,
                APP_ID,
                SECRET,
                statusListenerMock,
                true,
                true,
                new OreoConfig(false));

        MobileEngageConfig result = new MobileEngageConfig.Builder()
                .from(expected)
                .build();

        assertEquals(expected, result);
    }

    @Test
    public void testBuilder_withDebugApplication(){
        MobileEngageConfig result = new MobileEngageConfig.Builder()
                .application(applicationDebug)
                .credentials("", "")
                .disableDefaultChannel()
                .build();
        assertTrue(result.isDebugMode());
    }

    @Test
    public void testBuilder_withReleaseApplication(){
        MobileEngageConfig result = new MobileEngageConfig.Builder()
                .application(applicationRelease)
                .credentials("", "")
                .disableDefaultChannel()
                .build();
        assertFalse(result.isDebugMode());
    }
}