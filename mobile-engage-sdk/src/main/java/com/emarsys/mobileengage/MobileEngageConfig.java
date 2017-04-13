package com.emarsys.mobileengage;

import android.support.annotation.NonNull;

import com.emarsys.core.util.Assert;


public class MobileEngageConfig {

    private final String applicationID;
    private final String applicationSecret;
    private final MobileEngageStatusListener statusListener;

    MobileEngageConfig(String applicationID,
                       String applicationSecret,
                       MobileEngageStatusListener statusListener) {
        Assert.notNull(applicationID, "ApplicationID must not be null");
        Assert.notNull(applicationSecret, "Token must not be null");
        this.applicationID = applicationID;
        this.applicationSecret = applicationSecret;
        this.statusListener = statusListener;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public String getApplicationSecret() {
        return applicationSecret;
    }

    public MobileEngageStatusListener getStatusListener() {
        return statusListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MobileEngageConfig that = (MobileEngageConfig) o;

        if (getApplicationID() != null ? !getApplicationID().equals(that.getApplicationID()) : that.getApplicationID() != null)
            return false;
        if (getApplicationSecret() != null ? !getApplicationSecret().equals(that.getApplicationSecret()) : that.getApplicationSecret() != null)
            return false;
        return getStatusListener() != null ? getStatusListener().equals(that.getStatusListener()) : that.getStatusListener() == null;

    }

    @Override
    public int hashCode() {
        int result = getApplicationID() != null ? getApplicationID().hashCode() : 0;
        result = 31 * result + (getApplicationSecret() != null ? getApplicationSecret().hashCode() : 0);
        result = 31 * result + (getStatusListener() != null ? getStatusListener().hashCode() : 0);
        return result;
    }

    public static class Builder {
        private String applicationID;
        private String applicationSecret;
        private MobileEngageStatusListener statusListener;

        public Builder credentials(@NonNull String applicationID,
                                   @NonNull String applicationSecret) {
            this.applicationID = applicationID;
            this.applicationSecret = applicationSecret;
            return this;
        }

        public Builder statusListener(@NonNull MobileEngageStatusListener statusListener) {
            this.statusListener = statusListener;
            return this;
        }

        public MobileEngageConfig build() {
            return new MobileEngageConfig(applicationID, applicationSecret, statusListener);
        }
    }
}