package com.emarsys.mobileengage.service;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.app.NotificationCompat;

import com.emarsys.mobileengage.inbox.model.Notification;
import com.emarsys.mobileengage.inbox.model.NotificationCache;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MessagingServiceUtilsTest {
    private static final String TITLE = "title";
    private static final String DEFAULT_TITLE = "This is a default title";
    private static final String BODY = "body";

    private Context context;
    private List<Notification> notificationCache;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Before
    @SuppressWarnings("unchecked")
    public void init() throws Exception {
        context = InstrumentationRegistry.getContext();

        Field cacheField = NotificationCache.class.getDeclaredField("internalCache");
        cacheField.setAccessible(true);
        notificationCache = (List) cacheField.get(null);
        notificationCache.clear();
    }

    @Test
    public void getSmallIconResourceId_shouldBePositive() {
        assertTrue(MessagingServiceUtils.getSmallIconResourceId(context) > 0);
    }

    @Test
    public void isMobileEngageMessage_shouldBeFalse_withEmptyData() {
        Map<String, String> remoteMessageData = new HashMap<>();
        assertFalse(MessagingServiceUtils.isMobileEngageMessage(remoteMessageData));
    }

    @Test
    public void isMobileEngageMessage_shouldBeTrue_withDataWhichContainsTheCorrectKey() {
        Map<String, String> remoteMessageData = new HashMap<>();
        remoteMessageData.put("ems_msg", "value");
        assertTrue(MessagingServiceUtils.isMobileEngageMessage(remoteMessageData));
    }

    @Test
    public void isMobileEngageMessage_shouldBeFalse_withDataWithout_ems_msg() {
        Map<String, String> remoteMessageData = new HashMap<>();
        remoteMessageData.put("key1", "value1");
        remoteMessageData.put("key2", "value2");
        assertFalse(MessagingServiceUtils.isMobileEngageMessage(remoteMessageData));
    }

    @Test
    public void createIntent() {
        Map<String, String> remoteMessageData = new HashMap<>();
        remoteMessageData.put("key1", "value1");
        remoteMessageData.put("key2", "value2");

        Intent resultIntent = MessagingServiceUtils.createIntent(remoteMessageData, context);
        assertEquals("value1", resultIntent.getBundleExtra("payload").getString("key1"));
        assertEquals("value2", resultIntent.getBundleExtra("payload").getString("key2"));
    }

    @Test
    public void createNotification_shouldNotBeNull() {
        assertNotNull(MessagingServiceUtils.createNotification(new HashMap<String, String>(), context));
    }

    @Test
    @SdkSuppress(minSdkVersion = LOLLIPOP)
    public void createNotification_withBigTextStyle_withTitleAndBody() {
        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);
        input.put("body", BODY);

        android.app.Notification result = MessagingServiceUtils.createNotification(input, context);

        assertEquals(TITLE, result.extras.getString(NotificationCompat.EXTRA_TITLE));
        assertEquals(TITLE, result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_TEXT));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT));
    }

    @Test
    @SdkSuppress(minSdkVersion = LOLLIPOP)
    public void createNotification_withBigTextStyle_withTitle_withoutBody() {
        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);

        android.app.Notification result = MessagingServiceUtils.createNotification(input, context);

        assertEquals(TITLE, result.extras.getString(NotificationCompat.EXTRA_TITLE));
        assertEquals(TITLE, result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG));
        assertNull(result.extras.getString(NotificationCompat.EXTRA_TEXT));
        assertNull(result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT));
    }

    @Test
    @SdkSuppress(minSdkVersion = LOLLIPOP)
    public void createNotification_withBigTextStyle_withoutTitle_withBody() {
        Map<String, String> input = new HashMap<>();
        input.put("body", BODY);

        android.app.Notification result = MessagingServiceUtils.createNotification(input, context);

        String expectedTitle = expectedBasedOnApiLevel(getApplicationName(), "");

        assertEquals(expectedTitle, result.extras.getString(NotificationCompat.EXTRA_TITLE));
        assertEquals(expectedTitle, result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_TEXT));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT));
    }

    @Test
    @SdkSuppress(minSdkVersion = LOLLIPOP)
    public void createNotification_withBigTextStyle_withoutTitle_withBody_withDefaultTitle() {
        Map<String, String> input = new HashMap<>();
        input.put("body", BODY);
        input.put("u", "{\"test_field\":\"\",\"ems_default_title\":\"" + DEFAULT_TITLE + "\",\"image\":\"https:\\/\\/media.giphy.com\\/media\\/ktvFa67wmjDEI\\/giphy.gif\",\"deep_link\":\"lifestylelabels.com\\/mobile\\/product\\/3245678\",\"sid\":\"sid_here\"}");

        android.app.Notification result = MessagingServiceUtils.createNotification(input, context);

        String expectedTitle = expectedBasedOnApiLevel(DEFAULT_TITLE, "");

        assertEquals(expectedTitle, result.extras.getString(NotificationCompat.EXTRA_TITLE));
        assertEquals(expectedTitle, result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_TEXT));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT));
    }

    @Test
    @SdkSuppress(minSdkVersion = LOLLIPOP)
    public void testCreateNotification_withBigPictureStyle_whenImageIsAvailable() {
        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);
        input.put("body", BODY);
        input.put("imageUrl", "https://www.emarsys.com/wp-content/themes/emarsys/images/home-page/press-releases-header.jpg");

        android.app.Notification result = MessagingServiceUtils.createNotification(input, context);

        assertEquals(TITLE, result.extras.getString(NotificationCompat.EXTRA_TITLE));
        assertEquals(TITLE, result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT));
        assertNotNull(result.extras.get(NotificationCompat.EXTRA_PICTURE));
        assertNotNull(result.extras.get(NotificationCompat.EXTRA_LARGE_ICON));
        assertNull(result.extras.get(NotificationCompat.EXTRA_LARGE_ICON_BIG));
    }

    @Test
    @SdkSuppress(minSdkVersion = LOLLIPOP)
    public void testCreateNotification_withBigTextStyle_whenImageCannotBeLoaded() {
        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);
        input.put("body", BODY);
        input.put("imageUrl", "https://fa.il/img.jpg");

        android.app.Notification result = MessagingServiceUtils.createNotification(input, context);

        assertEquals(TITLE, result.extras.getString(NotificationCompat.EXTRA_TITLE));
        assertEquals(TITLE, result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_TEXT));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT));
    }

    @Test
    public void testGetTitle_withTitleSet() {
        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);

        assertEquals(TITLE, MessagingServiceUtils.getTitle(input, context));
    }

    @Test
    public void testGetTitle_shouldReturnAppName_whenTitleNotSet() {
        Map<String, String> input = new HashMap<>();
        input.put("key1", "value1");
        input.put("key2", "value2");

        String expectedBefore23 = getApplicationName();

        String expectedFrom23 = "";

        String expected = expectedBasedOnApiLevel(expectedBefore23, expectedFrom23);

        assertEquals(expected, MessagingServiceUtils.getTitle(input, context));
    }

    @Test
    public void testGetTitle_shouldReturnAppName_whenTitleIsEmpty() {
        Map<String, String> input = new HashMap<>();
        input.put("key1", "value1");
        input.put("key2", "value2");
        input.put("title", "");

        String expectedBefore23 = getApplicationName();

        String expectedFrom23 = "";

        String expected = expectedBasedOnApiLevel(expectedBefore23, expectedFrom23);

        assertEquals(expected, MessagingServiceUtils.getTitle(input, context));
    }

    @Test
    public void testGetTitle_shouldReturnDefaultTitle_whenDefaultTitleSet() {
        Map<String, String> input = new HashMap<>();
        input.put("key1", "value1");
        input.put("key2", "value2");
        input.put("u", "{\"test_field\":\"\",\"ems_default_title\":\"" + DEFAULT_TITLE + "\",\"image\":\"https:\\/\\/media.giphy.com\\/media\\/ktvFa67wmjDEI\\/giphy.gif\",\"deep_link\":\"lifestylelabels.com\\/mobile\\/product\\/3245678\",\"sid\":\"sid_here\"}");

        String expectedBefore23 = DEFAULT_TITLE;

        String expectedFrom23 = "";

        String expected = expectedBasedOnApiLevel(expectedBefore23, expectedFrom23);

        assertEquals(expected, MessagingServiceUtils.getTitle(input, context));
    }


    @Test
    public void testGetTitle_defaultTitleShouldNotOverrideTitle() {
        Map<String, String> input = new HashMap<>();
        input.put("key1", "value1");
        input.put("key2", "value2");
        input.put("title", TITLE);
        input.put("u", "{\"test_field\":\"\",\"ems_default_title\":\"" + DEFAULT_TITLE + "\",\"image\":\"https:\\/\\/media.giphy.com\\/media\\/ktvFa67wmjDEI\\/giphy.gif\",\"deep_link\":\"lifestylelabels.com\\/mobile\\/product\\/3245678\",\"sid\":\"sid_here\"}");

        assertEquals(TITLE, MessagingServiceUtils.getTitle(input, context));
    }

    @Test
    public void testCacheNotification_shouldCacheNotification() {
        Map<String, String> remoteData = new HashMap<>();
        remoteData.put("ems_msg", "true");
        remoteData.put("u", "{\"test_field\":\"\",\"image\":\"https:\\/\\/media.giphy.com\\/media\\/ktvFa67wmjDEI\\/giphy.gif\",\"deep_link\":\"lifestylelabels.com\\/mobile\\/product\\/3245678\",\"sid\":\"sid_here\"}");
        remoteData.put("id", "21022.150123121212.43223434c3b9");
        remoteData.put("inbox", "true");
        remoteData.put("title", "hello there");
        remoteData.put("rootParam1", "param_param");

        Map<String, String> customData = new HashMap<>();
        customData.put("test_field", "");
        customData.put("image", "https://media.giphy.com/media/ktvFa67wmjDEI/giphy.gif");
        customData.put("deep_link", "lifestylelabels.com/mobile/product/3245678");
        customData.put("sid", "sid_here");

        long before = System.currentTimeMillis();
        MessagingServiceUtils.cacheNotification(remoteData);
        long after = System.currentTimeMillis();

        assertEquals(1, notificationCache.size());

        Notification result = notificationCache.get(0);

        assertEquals("21022.150123121212.43223434c3b9", result.getId());
        assertEquals("sid_here", result.getSid());
        assertEquals("hello there", result.getTitle());
        assertEquals(customData, result.getCustomData());
        Assert.assertTrue(before <= result.getReceivedAt());
        Assert.assertTrue(result.getReceivedAt() <= after);
    }

    private String expectedBasedOnApiLevel(String before23, String fromApi23) {
        if (Build.VERSION.SDK_INT < 23) {
            return before23;
        } else {
            return fromApi23;
        }
    }

    private String getApplicationName() {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

}