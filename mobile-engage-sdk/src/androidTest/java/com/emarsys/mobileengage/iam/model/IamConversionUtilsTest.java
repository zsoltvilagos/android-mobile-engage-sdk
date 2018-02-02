package com.emarsys.mobileengage.iam.model;

import com.emarsys.core.util.TimestampUtils;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;

public class IamConversionUtilsTest {

    private ButtonClicked buttonClicked1;
    private ButtonClicked buttonClicked2;
    private ButtonClicked buttonClicked3;

    private DisplayedIam displayedIam1;
    private DisplayedIam displayedIam2;
    private DisplayedIam displayedIam3;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        buttonClicked1 = new ButtonClicked("campaign1", "button1", 200);
        buttonClicked2 = new ButtonClicked("campaign1", "button2", 400);
        buttonClicked3 = new ButtonClicked("campaign2", "button1", 2000);

        displayedIam1 = new DisplayedIam("campaign10", 500, "");
        displayedIam2 = new DisplayedIam("campaign20", 1000, "");
        displayedIam3 = new DisplayedIam("campaign30", 1500, "");
    }

    @Test
    public void testConvert_buttonClick() throws Exception {
        JSONObject json = IamConversionUtils.buttonClickToJson(buttonClicked1);

        JSONObject expected = new JSONObject()
                .put("message_id", buttonClicked1.getCampaignId())
                .put("button_id", buttonClicked1.getButtonId())
                .put("timestamp", TimestampUtils.formatTimestampWithUTC(buttonClicked1.getTimestamp()));

        assertEquals(expected.toString(), json.toString());
    }

    @Test
    public void testConvert_buttonClickList() throws Exception {
        JSONArray array = IamConversionUtils.buttonClicksToArray(Arrays.asList(
                buttonClicked1,
                buttonClicked2,
                buttonClicked3
        ));

        JSONArray expected = new JSONArray()
                .put(new JSONObject()
                        .put("message_id", buttonClicked1.getCampaignId())
                        .put("button_id", buttonClicked1.getButtonId())
                        .put("timestamp", TimestampUtils.formatTimestampWithUTC(buttonClicked1.getTimestamp())))
                .put(new JSONObject()
                        .put("message_id", buttonClicked2.getCampaignId())
                        .put("button_id", buttonClicked2.getButtonId())
                        .put("timestamp", TimestampUtils.formatTimestampWithUTC(buttonClicked2.getTimestamp())))
                .put(new JSONObject()
                        .put("message_id", buttonClicked3.getCampaignId())
                        .put("button_id", buttonClicked3.getButtonId())
                        .put("timestamp", TimestampUtils.formatTimestampWithUTC(buttonClicked3.getTimestamp())));

        assertEquals(expected.toString(), array.toString());
    }

    @Test
    public void testConvert_displayedIam() throws Exception {
        JSONObject json = IamConversionUtils.displayedIamToJson(displayedIam1);

        JSONObject expected = new JSONObject()
                .put("message_id", displayedIam1.getCampaignId())
                .put("timestamp", TimestampUtils.formatTimestampWithUTC(displayedIam1.getTimestamp()));

        assertEquals(expected.toString(), json.toString());
    }

    @Test
    public void testConvert_displayedIamList() throws Exception {
        JSONArray array = IamConversionUtils.displayedIamsToArray(Arrays.asList(
                displayedIam1,
                displayedIam2,
                displayedIam3
        ));

        JSONArray expected = new JSONArray()
                .put(new JSONObject()
                        .put("message_id", displayedIam1.getCampaignId())
                        .put("timestamp", TimestampUtils.formatTimestampWithUTC(displayedIam1.getTimestamp())))
                .put(new JSONObject()
                        .put("message_id", displayedIam2.getCampaignId())
                        .put("timestamp", TimestampUtils.formatTimestampWithUTC(displayedIam2.getTimestamp())))
                .put(new JSONObject()
                        .put("message_id", displayedIam3.getCampaignId())
                        .put("timestamp", TimestampUtils.formatTimestampWithUTC(displayedIam3.getTimestamp())));

        assertEquals(expected.toString(), array.toString());
    }
}