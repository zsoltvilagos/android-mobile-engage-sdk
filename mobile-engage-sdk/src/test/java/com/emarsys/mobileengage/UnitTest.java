package com.emarsys.mobileengage;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

public class UnitTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Test
    public void test(){
        Assert.assertTrue(true);
    }
}