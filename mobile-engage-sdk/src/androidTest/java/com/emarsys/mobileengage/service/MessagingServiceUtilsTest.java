package com.emarsys.mobileengage.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.NotificationCompat;

import com.emarsys.core.resource.MetaDataReader;
import com.emarsys.mobileengage.config.OreoConfig;
import com.emarsys.mobileengage.inbox.model.Notification;
import com.emarsys.mobileengage.inbox.model.NotificationCache;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.MockSettings;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class MessagingServiceUtilsTest {

    static {
        mock(Context.class);
        mock(PackageManager.class);
    }

    private static final String TITLE = "title";
    private static final String DEFAULT_TITLE = "This is a default title";
    private static final String BODY = "body";
    private static final String CHANNEL_ID = "channelId";

    private Context context;
    private List<Notification> notificationCache;
    private OreoConfig enabledOreoConfig;
    private OreoConfig disabledOreoConfig;
    private MetaDataReader metaDataReader;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() throws Exception {
        context = InstrumentationRegistry.getContext();

        Field cacheField = NotificationCache.class.getDeclaredField("internalCache");
        cacheField.setAccessible(true);
        notificationCache = (List) cacheField.get(null);
        notificationCache.clear();

        metaDataReader = mock(MetaDataReader.class);

        enabledOreoConfig = new OreoConfig(true, "name", "description");
        disabledOreoConfig = new OreoConfig(false);
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
        assertNotNull(MessagingServiceUtils.createNotification(context, new HashMap<String, String>(), disabledOreoConfig, metaDataReader));
    }

    @Test
    @SdkSuppress(minSdkVersion = LOLLIPOP)
    public void createNotification_withBigTextStyle_withTitleAndBody() {
        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);
        input.put("body", BODY);

        android.app.Notification result = MessagingServiceUtils.createNotification(context, input, disabledOreoConfig, metaDataReader);

        assertEquals(TITLE, result.extras.getString(NotificationCompat.EXTRA_TITLE));
        assertEquals(TITLE, result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_TEXT));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT));

        assertNull(result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT));
    }

    @Test
    @SdkSuppress(minSdkVersion = LOLLIPOP)
    public void createNotification_withBigTextStyle_withTitle_withoutBody() {
        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);

        android.app.Notification result = MessagingServiceUtils.createNotification(context, input, disabledOreoConfig, metaDataReader);

        assertEquals(TITLE, result.extras.getString(NotificationCompat.EXTRA_TITLE));
        assertEquals(TITLE, result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG));

        assertNull(result.extras.getString(NotificationCompat.EXTRA_TEXT));
        assertNull(result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT));
        assertNull(result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT));
    }

    @Test
    @SdkSuppress(minSdkVersion = LOLLIPOP)
    public void createNotification_withBigTextStyle_withoutTitle_withBody() {
        Map<String, String> input = new HashMap<>();
        input.put("body", BODY);

        android.app.Notification result = MessagingServiceUtils.createNotification(context, input, disabledOreoConfig, metaDataReader);

        String expectedTitle = expectedBasedOnApiLevel(getApplicationName(), "");

        assertEquals(expectedTitle, result.extras.getString(NotificationCompat.EXTRA_TITLE));
        assertEquals(expectedTitle, result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_TEXT));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT));

        assertNull(result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT));
    }

    @Test
    @SdkSuppress(minSdkVersion = LOLLIPOP)
    public void createNotification_withBigTextStyle_withoutTitle_withBody_withDefaultTitle() {
        Map<String, String> input = new HashMap<>();
        input.put("body", BODY);
        input.put("u", "{\"test_field\":\"\",\"ems_default_title\":\"" + DEFAULT_TITLE + "\",\"image\":\"https:\\/\\/media.giphy.com\\/media\\/ktvFa67wmjDEI\\/giphy.gif\",\"deep_link\":\"lifestylelabels.com\\/mobile\\/product\\/3245678\",\"sid\":\"sid_here\"}");

        android.app.Notification result = MessagingServiceUtils.createNotification(context, input, disabledOreoConfig, metaDataReader);

        String expectedTitle = expectedBasedOnApiLevel(DEFAULT_TITLE, "");

        assertEquals(expectedTitle, result.extras.getString(NotificationCompat.EXTRA_TITLE));
        assertEquals(expectedTitle, result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_TEXT));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT));

        assertNull(result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT));
    }

    @Test
    @SdkSuppress(minSdkVersion = LOLLIPOP)
    public void testCreateNotification_withBigPictureStyle_whenImageIsAvailable() {
        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);
        input.put("body", BODY);
        input.put("image_url", "https://ems-denna.herokuapp.com/images/Emarsys.png");

        android.app.Notification result = MessagingServiceUtils.createNotification(context, input, disabledOreoConfig, metaDataReader);

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
        input.put("image_url", "https://fa.il/img.jpg");

        android.app.Notification result = MessagingServiceUtils.createNotification(context, input, disabledOreoConfig, metaDataReader);

        assertEquals(TITLE, result.extras.getString(NotificationCompat.EXTRA_TITLE));
        assertEquals(TITLE, result.extras.getString(NotificationCompat.EXTRA_TITLE_BIG));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_TEXT));
        assertEquals(BODY, result.extras.getString(NotificationCompat.EXTRA_BIG_TEXT));

        assertNull(result.extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT));
    }

    @Test
    public void testCreateNotification_setsNotificationColor() throws PackageManager.NameNotFoundException {
        Integer expected = Color.MAGENTA;
        when(metaDataReader.getIntOrNull(any(Context.class), any(String.class))).thenReturn(expected);

        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);
        input.put("body", BODY);

        android.app.Notification result = MessagingServiceUtils.createNotification(context, input, disabledOreoConfig, metaDataReader);
        assertEquals(Color.MAGENTA, result.color);
    }

    @Test
    public void testCreateNotification_doesNotSet_notificationColor_when() throws PackageManager.NameNotFoundException {
        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);
        input.put("body", BODY);

        android.app.Notification result = MessagingServiceUtils.createNotification(context, input, disabledOreoConfig, metaDataReader);
        assertEquals(android.app.Notification.COLOR_DEFAULT, result.color);
    }

    @Test
    @SdkSuppress(minSdkVersion = O)
    public void testCreateNotification_withChannelId() {
        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);
        input.put("body", BODY);
        input.put("channel_id", CHANNEL_ID);

        android.app.Notification result = MessagingServiceUtils.createNotification(context, input, disabledOreoConfig, metaDataReader);

        assertEquals(CHANNEL_ID, result.getChannelId());
    }

    @Test
    @SdkSuppress(minSdkVersion = O)
    public void testCreateNotification_withoutChannelId_withDefaultChannelEnabled() {
        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);
        input.put("body", BODY);

        android.app.Notification result = MessagingServiceUtils.createNotification(context, input, enabledOreoConfig, metaDataReader);

        String expected = "ems_me_default";

        assertEquals(expected, result.getChannelId());
    }

    @Test
    @SdkSuppress(minSdkVersion = O)
    public void testCreateNotification_withoutChannelId_withDefaultChannelDisabled() {
        Map<String, String> input = new HashMap<>();
        input.put("title", TITLE);
        input.put("body", BODY);

        android.app.Notification result = MessagingServiceUtils.createNotification(context, input, disabledOreoConfig, metaDataReader);

        assertNull(result.getChannelId());
    }

    @Test
    @SdkSuppress(minSdkVersion = O)
    public void testCreateChannel() {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.deleteNotificationChannel("ems_me_default");
        assertNull(manager.getNotificationChannel("ems_me_default"));

        MessagingServiceUtils.createDefaultChannel(context, enabledOreoConfig);

        NotificationChannel channel = manager.getNotificationChannel("ems_me_default");
        assertNotNull(channel);
        assertEquals(NotificationManager.IMPORTANCE_DEFAULT, channel.getImportance());
        assertEquals(enabledOreoConfig.getDefaultChannelName(), channel.getName());
        assertEquals(enabledOreoConfig.getDefaultChannelDescription(), channel.getDescription());
    }

    @Test
    @SdkSuppress(minSdkVersion = O)
    public void testCreateChannel_overridesPrevious() {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.deleteNotificationChannel("ems_me_default");
        assertNull(manager.getNotificationChannel("ems_me_default"));
        MessagingServiceUtils.createDefaultChannel(context, enabledOreoConfig);
        NotificationChannel channel = manager.getNotificationChannel("ems_me_default");
        assertNotNull(channel);

        OreoConfig updatedConfig = new OreoConfig(true, "updatedName", "updatedDescription");
        MessagingServiceUtils.createDefaultChannel(context, updatedConfig);

        NotificationChannel updatedChannel = manager.getNotificationChannel("ems_me_default");
        assertEquals(updatedConfig.getDefaultChannelName(), updatedChannel.getName());
        assertEquals(updatedConfig.getDefaultChannelDescription(), updatedChannel.getDescription());
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

        String expectedFrom23 = "";

        String expected = expectedBasedOnApiLevel(DEFAULT_TITLE, expectedFrom23);

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