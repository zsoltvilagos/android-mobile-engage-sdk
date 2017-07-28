package com.emarsys.mobileengage.inbox.model;

import java.util.ArrayList;
import java.util.List;

public class NotificationCache {

    static List<Notification> internalCache = new ArrayList<>();

    public void cache(Notification notification) {
        if (notification != null) {
            internalCache.add(0, notification);
        }
    }

    public List<Notification> merge(List<Notification> fetchedList) {
        ArrayList<Notification> result = new ArrayList<>(internalCache);
        result.addAll(fetchedList);
        return result;
    }
}
