package com.emarsys.mobileengage;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class MobileEngageConfigTest {
    private String APP_ID = "appID";
    private String SECRET = "5678987654345678654";
    private MobileEngageStatusListener statusListener;

    @Before
    public void init(){
        statusListener =  mock(MobileEngageStatusListener.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilder_applicationIdShouldNotBeNull() throws Exception {
        new MobileEngageConfig(null, SECRET, statusListener);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilder_secretShouldNotBeNull() throws Exception {
        new MobileEngageConfig(null, SECRET, statusListener);
    }

    @Test
    public void testBuilder_withMandatoryArguments() {
        MobileEngageConfig expected = new MobileEngageConfig(APP_ID, SECRET, null);

        MobileEngageConfig result = new MobileEngageConfig.Builder()
                .credentials(APP_ID, SECRET)
                .build();

        assertEquals(expected, result);
    }

    @Test
    public void testBuilder_withAllArguments() {
        MobileEngageConfig expected = new MobileEngageConfig(APP_ID, SECRET, statusListener);

        MobileEngageConfig result = new MobileEngageConfig.Builder()
                .credentials(APP_ID, SECRET)
                .statusListener(statusListener)
                .build();

        assertEquals(expected, result);
    }
}