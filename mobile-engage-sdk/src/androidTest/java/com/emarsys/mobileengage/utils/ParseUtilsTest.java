package com.emarsys.mobileengage.utils;

import com.emarsys.mobileengage.inbox.Notification;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ParseUtilsTest {

    @Test
    public void parseNotificationList_withNull_returnsNull(){
        Assert.assertNull(ParseUtils.parseNotificationList(null));
    }

    @Test
    public void parseNotification_withNull_returnsNull() {
        Assert.assertNull(ParseUtils.parseNotification(null));
    }

    @Test
    public void parseNotification_withEmptyString_returnsNull() {
        Assert.assertNull(ParseUtils.parseNotification(""));
    }

    @Test
    public void parseNotification_withUnparsableString_returnsNull() {
        Assert.assertNull(ParseUtils.parseNotification("{\"key\":\"value\"}"));
    }

    @Test
    public void parseNotification_withCorrectString_returnsNotification() {
        String json = "{" +
                "\"id\":\"notificationId\", " +
                "\"title\":\"notification title\", " +
                "\"customData\": {" +
                "\"data1\":\"dataValue1\"," +
                "\"data2\":\"dataValue2\"" +
                "}," +
                "\"rootParams\": {" +
                "\"param1\":\"paramValue1\"," +
                "\"param2\":\"paramValue2\"" +
                "}," +
                "\"expirationTime\": 300, " +
                "\"receivedAt\":87647000" +
                "}";

        Map<String, String> customData = new HashMap<>();
        customData.put("data1", "dataValue1");
        customData.put("data2", "dataValue2");

        Map<String, String> rootParams = new HashMap<>();
        rootParams.put("param1", "paramValue1");
        rootParams.put("param2", "paramValue2");

        Notification expected = new Notification("notificationId", "notification title", customData, rootParams, 300, new Date(87647000));


        Notification result = ParseUtils.parseNotification(json);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void convertFlatJsonObject_withNull(){
        HashMap<Object, Object> expected = new HashMap<>();
        Map<String, String> result = ParseUtils.convertFlatJsonObject(null);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void convertFlatJsonObject_withEmptyJson(){
        HashMap<String, String> expected = new HashMap<>();
        Map<String, String> result = ParseUtils.convertFlatJsonObject(new JSONObject());
        Assert.assertEquals(expected, result);
    }

    @Test
    public void convertFlatJsonObject_withJson() throws JSONException {
        JSONObject json = new JSONObject().put("key1", "value1").put("key2", "value2").put("key3", "value3");

        HashMap<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");
        expected.put("key3", "value3");

        Map<String, String> result = ParseUtils.convertFlatJsonObject(json);

        Assert.assertEquals(expected, result);
    }
}