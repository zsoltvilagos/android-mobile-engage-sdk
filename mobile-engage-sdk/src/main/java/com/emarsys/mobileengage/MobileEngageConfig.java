package com.emarsys.mobileengage;

import android.support.annotation.NonNull;

import com.emarsys.core.util.Assert;


public class MobileEngageConfig {

    private final String applicationCode;
    private final String applicationPassword;
    private final MobileEngageStatusListener statusListener;

    MobileEngageConfig(String applicationCode,
                       String applicationPassword,
                       MobileEngageStatusListener statusListener) {
        Assert.notNull(applicationCode, "ApplicationCode must not be null");
        Assert.notNull(applicationPassword, "ApplicationPassword must not be null");
        this.applicationCode = applicationCode;
        this.applicationPassword = applicationPassword;
        this.statusListener = statusListener;
    }

    public String getApplicationCode() {
        return applicationCode;
    }

    public String getApplicationPassword() {
        return applicationPassword;
    }

    public MobileEngageStatusListener getStatusListener() {
        return statusListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MobileEngageConfig that = (MobileEngageConfig) o;

        if (getApplicationCode() != null ? !getApplicationCode().equals(that.getApplicationCode()) : that.getApplicationCode() != null)
            return false;
        if (getApplicationPassword() != null ? !getApplicationPassword().equals(that.getApplicationPassword()) : that.getApplicationPassword() != null)
            return false;
        return getStatusListener() != null ? getStatusListener().equals(that.getStatusListener()) : that.getStatusListener() == null;

    }

    @Override
    public int hashCode() {
        int result = getApplicationCode() != null ? getApplicationCode().hashCode() : 0;
        result = 31 * result + (getApplicationPassword() != null ? getApplicationPassword().hashCode() : 0);
        result = 31 * result + (getStatusListener() != null ? getStatusListener().hashCode() : 0);
        return result;
    }

    public static class Builder {
        private String applicationCode;
        private String applicationPassword;
        private MobileEngageStatusListener statusListener;

        public Builder credentials(@NonNull String applicationCode,
                                   @NonNull String applicationPassword) {
            this.applicationCode = applicationCode;
            this.applicationPassword = applicationPassword;
            return this;
        }

        public Builder statusListener(@NonNull MobileEngageStatusListener statusListener) {
            this.statusListener = statusListener;
            return this;
        }

        public MobileEngageConfig build() {
            return new MobileEngageConfig(applicationCode, applicationPassword, statusListener);
        }
    }
}