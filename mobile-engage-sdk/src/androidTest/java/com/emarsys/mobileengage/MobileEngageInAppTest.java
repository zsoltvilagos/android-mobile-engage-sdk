package com.emarsys.mobileengage;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MobileEngageInAppTest {

    @Test
    public void testSetter_paused() {
        MobileEngage.InApp.setPaused(true);

        Assert.assertTrue(MobileEngage.InApp.isPaused());
    }

    @Test
    public void testSetter_resumed() {
        MobileEngage.InApp.setPaused(false);

        Assert.assertFalse(MobileEngage.InApp.isPaused());
    }

}