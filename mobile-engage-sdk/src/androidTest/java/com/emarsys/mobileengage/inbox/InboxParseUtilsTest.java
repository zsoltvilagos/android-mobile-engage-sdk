package com.emarsys.mobileengage.inbox;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InboxParseUtilsTest {

    @Test
    public void parseNotificationList_withNull_returnsEmptyList() {
        List<Notification> expected = new ArrayList<>();
        List<Notification> result = InboxParseUtils.parseNotificationList(null);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void parseNotificationList_withEmptyString_returnsEmptyList() {
        List<Notification> expected = new ArrayList<>();
        List<Notification> result = InboxParseUtils.parseNotificationList("");
        Assert.assertEquals(expected, result);
    }

    @Test
    public void parseNotificationList_withIncorrectString_returnsEmptyList() {
        List<Notification> expected = new ArrayList<>();
        List<Notification> result = InboxParseUtils.parseNotificationList("{\"key\":\"value\"}");
        Assert.assertEquals(expected, result);
    }

    @Test
    public void parseNotificationList_withCorrectString_returnsList() throws JSONException {
        List<Notification> expected = new ArrayList<>();

        Map<String, String> customData1 = new HashMap<>();
        customData1.put("data1", "dataValue1");
        customData1.put("data2", "dataValue2");

        JSONObject rootParams1 = new JSONObject();
        rootParams1.put("param1", "paramValue1");
        rootParams1.put("param2", "paramValue2");

        Map<String, String> customData2 = new HashMap<>();
        customData2.put("data3", "dataValue3");
        customData2.put("data4", "dataValue4");

        JSONObject rootParams2 = new JSONObject();
        rootParams2.put("param3", "paramValue3");
        rootParams2.put("param4", "paramValue4");


        Map<String, String> customData3 = new HashMap<>();
        customData3.put("data5", "dataValue5");
        customData3.put("data6", "dataValue6");

        JSONObject rootParams3 = new JSONObject();
        rootParams3.put("param5", "paramValue5");
        rootParams3.put("param6", "paramValue6");

        expected.add(new Notification("id1", "title1", customData1, rootParams1, 300, new Date(10000000)));
        expected.add(new Notification("id2", "title2", customData2, rootParams2, 200, new Date(30000000)));
        expected.add(new Notification("id3", "title3", customData3, rootParams3, 100, new Date(25000000)));

        String notificationString1 = "{" +
                "\"id\":\"id1\", " +
                "\"title\":\"title1\", " +
                "\"customData\": {" +
                "\"data1\":\"dataValue1\"," +
                "\"data2\":\"dataValue2\"" +
                "}," +
                "\"rootParams\": {" +
                "\"param1\":\"paramValue1\"," +
                "\"param2\":\"paramValue2\"" +
                "}," +
                "\"expirationTime\": 300, " +
                "\"receivedAt\":10000000" +
                "}";

        String notificationString2 = "{" +
                "\"id\":\"id2\", " +
                "\"title\":\"title2\", " +
                "\"customData\": {" +
                "\"data3\":\"dataValue3\"," +
                "\"data4\":\"dataValue4\"" +
                "}," +
                "\"rootParams\": {" +
                "\"param3\":\"paramValue3\"," +
                "\"param4\":\"paramValue4\"" +
                "}," +
                "\"expirationTime\": 200, " +
                "\"receivedAt\":30000000" +
                "}";

        String notificationString3 = "{" +
                "\"id\":\"id3\", " +
                "\"title\":\"title3\", " +
                "\"customData\": {" +
                "\"data5\":\"dataValue5\"," +
                "\"data6\":\"dataValue6\"" +
                "}," +
                "\"rootParams\": {" +
                "\"param5\":\"paramValue5\"," +
                "\"param6\":\"paramValue6\"" +
                "}," +
                "\"expirationTime\": 100, " +
                "\"receivedAt\":25000000" +
                "}";

        String json = "[" + notificationString1 + "," + notificationString2 + "," + notificationString3 + "]";

        List<Notification> result = InboxParseUtils.parseNotificationList(json);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void parseNotificationList_shouldBeResilient_toPartiallyIncorrectInput() throws JSONException {
        List<Notification> expected = new ArrayList<>();

        Map<String, String> customData = new HashMap<>();
        customData.put("data1", "dataValue1");
        customData.put("data2", "dataValue2");

        JSONObject rootParams = new JSONObject();
        rootParams.put("param1", "paramValue1");
        rootParams.put("param2", "paramValue2");

        expected.add(new Notification("id", "title", customData, rootParams, 300, new Date(10000000)));

        String notificationString = "{" +
                "\"id\":\"id\", " +
                "\"title\":\"title\", " +
                "\"customData\": {" +
                "\"data1\":\"dataValue1\"," +
                "\"data2\":\"dataValue2\"" +
                "}," +
                "\"rootParams\": {" +
                "\"param1\":\"paramValue1\"," +
                "\"param2\":\"paramValue2\"" +
                "}," +
                "\"expirationTime\": 300, " +
                "\"receivedAt\":10000000" +
                "}";

        String incorrect1 = "{" +
                "\"key\":\"value\"" +
                "}";

        String incorrect2 = "{" +
                "\"name\":\"Joel\"," +
                "\"age\":\"34\"" +
                "}";

        String json = "[" + incorrect1 + "," + notificationString + "," + incorrect2 + "]";

        List<Notification> result = InboxParseUtils.parseNotificationList(json);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void parseNotification_withNull_returnsNull() {
        Assert.assertNull(InboxParseUtils.parseNotification(null));
    }

    @Test
    public void parseNotification_withEmptyString_returnsNull() {
        Assert.assertNull(InboxParseUtils.parseNotification(""));
    }

    @Test
    public void parseNotification_withIncorrectString_returnsNull() {
        Assert.assertNull(InboxParseUtils.parseNotification("{\"key\":\"value\"}"));
    }

    @Test
    public void parseNotification_withCorrectString_returnsNotification() throws JSONException {
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

        JSONObject rootParams = new JSONObject();
        rootParams.put("param1", "paramValue1");
        rootParams.put("param2", "paramValue2");

        Notification expected = new Notification("notificationId", "notification title", customData, rootParams, 300, new Date(87647000));


        Notification result = InboxParseUtils.parseNotification(json);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void convertFlatJsonObject_withNull() {
        HashMap<Object, Object> expected = new HashMap<>();
        Map<String, String> result = InboxParseUtils.convertFlatJsonObject(null);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void convertFlatJsonObject_withEmptyJson() {
        HashMap<String, String> expected = new HashMap<>();
        Map<String, String> result = InboxParseUtils.convertFlatJsonObject(new JSONObject());
        Assert.assertEquals(expected, result);
    }

    @Test
    public void convertFlatJsonObject_withJson() throws JSONException {
        JSONObject json = new JSONObject().put("key1", "value1").put("key2", "value2").put("key3", "value3");

        HashMap<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");
        expected.put("key3", "value3");

        Map<String, String> result = InboxParseUtils.convertFlatJsonObject(json);

        Assert.assertEquals(expected, result);
    }
}