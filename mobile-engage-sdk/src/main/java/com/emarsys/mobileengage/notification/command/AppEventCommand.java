package com.emarsys.mobileengage.notification.command;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.EventHandler;
import com.emarsys.mobileengage.MobileEngage;

import org.json.JSONObject;

public class AppEventCommand implements Runnable {

    private String name;
    private JSONObject payload;

    public AppEventCommand(String name, JSONObject payload) {
        Assert.notNull(name, "Name must not be null!");
        this.name = name;
        this.payload = payload;
    }

    public String getName() {
        return name;
    }

    public JSONObject getPayload() {
        return payload;
    }

    @Override
    public void run() {
        EventHandler notificationEventHandler = MobileEngage.getConfig().getNotificationEventHandler();
        if (notificationEventHandler != null) {
            notificationEventHandler.handleEvent(name, payload);
        }
    }
}
