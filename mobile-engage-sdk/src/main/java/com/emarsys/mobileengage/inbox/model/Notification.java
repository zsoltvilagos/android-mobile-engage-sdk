package com.emarsys.mobileengage.inbox.model;

import android.support.annotation.NonNull;

import org.json.JSONObject;

import java.util.Date;
import java.util.Map;

public class Notification {
    private final String id;
    private final String sid;
    private final String title;
    private final Map<String, String> customData;
    private final JSONObject rootParams;
    private final int expirationTime;
    private final Date receivedAt;

    public Notification(String id, String sid, String title, Map<String, String> customData, JSONObject rootParams, int expirationTime, Date receivedAt) {
        this.id = id;
        this.sid = sid;
        this.title = title;
        this.customData = customData;
        this.rootParams = rootParams;
        this.expirationTime = expirationTime;
        this.receivedAt = receivedAt;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getSid() {
        return sid;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public Map<String, String> getCustomData() {
        return customData;
    }

    @NonNull
    public JSONObject getRootParams() {
        return rootParams;
    }

    @NonNull
    public int getExpirationTime() {
        return expirationTime;
    }

    @NonNull
    public Date getReceivedAt() {
        return receivedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Notification that = (Notification) o;

        if (expirationTime != that.expirationTime) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (sid != null ? !sid.equals(that.sid) : that.sid != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (customData != null ? !customData.equals(that.customData) : that.customData != null)
            return false;
        if (rootParams != null ? !rootParams.toString().equals(that.rootParams.toString()) : that.rootParams != null)
            return false;
        return receivedAt != null ? receivedAt.equals(that.receivedAt) : that.receivedAt == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (sid != null ? sid.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (customData != null ? customData.hashCode() : 0);
        result = 31 * result + (rootParams != null ? rootParams.hashCode() : 0);
        result = 31 * result + expirationTime;
        result = 31 * result + (receivedAt != null ? receivedAt.hashCode() : 0);
        return result;
    }
}
