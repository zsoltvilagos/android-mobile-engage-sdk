package com.emarsys.mobileengage.util;

import android.graphics.Bitmap;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

public class ImageUtilsTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Test
    public void testLoadBitmapFromUrl_shouldReturnNullWhenImageUrlIsNull() throws Exception {
        Bitmap result = ImageUtils.loadBitmapFromUrl(null);
        Assert.assertNull(result);
    }

    @Test
    public void testLoadBitmapFromUrl_shouldReturnNullWhenImageUrlIsInvalid() throws Exception {
        Bitmap result = ImageUtils.loadBitmapFromUrl("");
        Assert.assertNull(result);
    }

    @Test
    public void testLoadBitmapFromUrl_shouldReturnBitmapWhenUrlIsValidWithHttpProtocol() throws Exception {
        Bitmap result = ImageUtils.loadBitmapFromUrl("http://www.emarsys.com/wp-content/themes/emarsys/images/home-page/press-releases-header.jpg");
        Assert.assertNotNull(result);
    }

    @Test
    public void testLoadBitmapFromUrl_shouldReturnBitmapWhenUrlIsValidWithHttpsProtocol() throws Exception {
        Bitmap result = ImageUtils.loadBitmapFromUrl("https://www.emarsys.com/wp-content/themes/emarsys/images/home-page/press-releases-header.jpg");
        Assert.assertNotNull(result);
    }

}