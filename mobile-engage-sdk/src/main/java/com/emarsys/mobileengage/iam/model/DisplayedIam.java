package com.emarsys.mobileengage.iam.model;

public class DisplayedIam {
    private String campaignId;
    private long timestamp;
    private String eventName;

    public DisplayedIam(String campaignId, long timestamp, String eventName) {
        this.campaignId = campaignId;
        this.timestamp = timestamp;
        this.eventName = eventName;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getEventName() {
        return eventName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DisplayedIam that = (DisplayedIam) o;

        if (timestamp != that.timestamp) return false;
        if (campaignId != null ? !campaignId.equals(that.campaignId) : that.campaignId != null)
            return false;
        return eventName != null ? eventName.equals(that.eventName) : that.eventName == null;

    }

    @Override
    public int hashCode() {
        int result = campaignId != null ? campaignId.hashCode() : 0;
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (eventName != null ? eventName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DisplayedIam{" +
                "campaignId='" + campaignId + '\'' +
                ", timestamp=" + timestamp +
                ", eventName='" + eventName + '\'' +
                '}';
    }
}
