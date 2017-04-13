package com.emarsys.mobileengage;

public class ApploginParameters {
    private int contactFieldId = -1;
    private String contactFieldValue;

    public ApploginParameters() {
    }

    public ApploginParameters(int contactFieldId, String contactFieldValue) {
        this.contactFieldId = contactFieldId;
        this.contactFieldValue = contactFieldValue;
    }

    public int getContactFieldId() {
        return contactFieldId;
    }

    public String getContactFieldValue() {
        return contactFieldValue;
    }

    boolean hasCredentials() {
        return contactFieldId != -1 && contactFieldValue != null;
    }

}
