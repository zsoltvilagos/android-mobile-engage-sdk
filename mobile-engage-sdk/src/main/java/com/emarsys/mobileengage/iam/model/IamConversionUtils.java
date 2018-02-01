package com.emarsys.mobileengage.iam.model;

import com.emarsys.core.util.TimestampUtils;
import com.emarsys.mobileengage.iam.model.buttonclicked.ButtonClicked;
import com.emarsys.mobileengage.iam.model.displayediam.DisplayedIam;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class IamConversionUtils {

    public static JSONArray buttonClicksToArray(List<ButtonClicked> buttonClicks) {
        JSONArray result = new JSONArray();
        for (ButtonClicked buttonClick : buttonClicks) {
            result.put(buttonClickToJson(buttonClick));
        }
        return result;
    }

    public static JSONObject buttonClickToJson(ButtonClicked buttonClicked) {
        JSONObject result = new JSONObject();
        try {
            result.put("message_id", buttonClicked.getCampaignId());
            result.put("button_id", buttonClicked.getButtonId());
            result.put("timestamp", TimestampUtils.formatTimestampWithUTC(buttonClicked.getTimestamp()));
        } catch (JSONException ignore) {
        }
        return result;
    }

    public static JSONArray displayedIamsToArray(List<DisplayedIam> displayedIams) {
        JSONArray result = new JSONArray();
        for (DisplayedIam displayedIam : displayedIams) {
            result.put(displayedIamToJson(displayedIam));
        }
        return result;
    }

    public static JSONObject displayedIamToJson(DisplayedIam displayedIam) {
        JSONObject result = new JSONObject();
        try {
            result.put("message_id", displayedIam.getCampaignId());
            result.put("timestamp", TimestampUtils.formatTimestampWithUTC(displayedIam.getTimestamp()));
        } catch (JSONException ignore) {
        }
        return result;
    }

}
