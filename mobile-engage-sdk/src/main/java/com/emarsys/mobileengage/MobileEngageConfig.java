package com.emarsys.mobileengage;

import android.app.Application;
import android.support.annotation.NonNull;

import com.emarsys.core.util.Assert;

public class MobileEngageConfig {

    private final Application application;
    private final String applicationCode;
    private final String applicationPassword;
    private final MobileEngageStatusListener statusListener;
    private final boolean idlingResourceEnabled;

    MobileEngageConfig(Application application,
                       String applicationCode,
                       String applicationPassword,
                       MobileEngageStatusListener statusListener,
                       boolean idlingResourceEnabled) {
        Assert.notNull(application, "Application must not be null");
        Assert.notNull(applicationCode, "ApplicationCode must not be null");
        Assert.notNull(applicationPassword, "ApplicationPassword must not be null");
        this.application = application;
        this.applicationCode = applicationCode;
        this.applicationPassword = applicationPassword;
        this.statusListener = statusListener;
        this.idlingResourceEnabled = idlingResourceEnabled;
    }

    public Application getApplication() {
        return application;
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

    public boolean isIdlingResourceEnabled() {
        return idlingResourceEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MobileEngageConfig that = (MobileEngageConfig) o;

        if (idlingResourceEnabled != that.idlingResourceEnabled) return false;
        if (application != null ? !application.equals(that.application) : that.application != null)
            return false;
        if (applicationCode != null ? !applicationCode.equals(that.applicationCode) : that.applicationCode != null)
            return false;
        if (applicationPassword != null ? !applicationPassword.equals(that.applicationPassword) : that.applicationPassword != null)
            return false;
        return statusListener != null ? statusListener.equals(that.statusListener) : that.statusListener == null;

    }

    @Override
    public int hashCode() {
        int result = application != null ? application.hashCode() : 0;
        result = 31 * result + (applicationCode != null ? applicationCode.hashCode() : 0);
        result = 31 * result + (applicationPassword != null ? applicationPassword.hashCode() : 0);
        result = 31 * result + (statusListener != null ? statusListener.hashCode() : 0);
        result = 31 * result + (idlingResourceEnabled ? 1 : 0);
        return result;
    }

    public static class Builder {
        private Application application;
        private String applicationCode;
        private String applicationPassword;
        private MobileEngageStatusListener statusListener;
        private boolean idlingResourceEnabled;

        public Builder from(MobileEngageConfig baseConfig) {
            Assert.notNull(baseConfig, "BaseConfig must not be null");
            application = baseConfig.getApplication();
            applicationCode = baseConfig.getApplicationCode();
            applicationPassword = baseConfig.getApplicationPassword();
            statusListener = baseConfig.getStatusListener();
            idlingResourceEnabled = baseConfig.isIdlingResourceEnabled();
            return this;
        }

        public Builder application(@NonNull Application application) {
            this.application = application;
            return this;
        }

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

        public Builder enableIdlingResource(boolean enabled) {
            idlingResourceEnabled = enabled;
            return this;
        }

        public MobileEngageConfig build() {
            return new MobileEngageConfig(
                    application,
                    applicationCode,
                    applicationPassword,
                    statusListener,
                    idlingResourceEnabled
            );
        }
    }
}