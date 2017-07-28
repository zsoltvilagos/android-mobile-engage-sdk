package com.emarsys.mobileengage.inbox.model;

import java.util.ArrayList;
import java.util.List;

public class NotificationCache {

    static List<Notification> internalCache = new ArrayList<>();

    public void cache(Notification notification) {
        if (notification != null) {
            internalCache.add(notification);
        }
    }
}
