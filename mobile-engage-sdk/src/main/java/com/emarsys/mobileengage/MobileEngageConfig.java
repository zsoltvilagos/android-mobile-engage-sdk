package com.emarsys.mobileengage;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;

import com.emarsys.core.util.Assert;

public class MobileEngageConfig {

    private final Application application;
    private final String applicationCode;
    private final String applicationPassword;
    private final MobileEngageStatusListener statusListener;
    private final boolean isDebugMode;
    private final boolean idlingResourceEnabled;

    MobileEngageConfig(Application application,
                       String applicationCode,
                       String applicationPassword,
                       MobileEngageStatusListener statusListener,
                       boolean isDebugMode,
                       boolean idlingResourceEnabled) {
        Assert.notNull(application, "Application must not be null");
        Assert.notNull(applicationCode, "ApplicationCode must not be null");
        Assert.notNull(applicationPassword, "ApplicationPassword must not be null");
        this.application = application;
        this.applicationCode = applicationCode;
        this.applicationPassword = applicationPassword;
        this.statusListener = statusListener;
        this.isDebugMode = isDebugMode;
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

    public boolean isDebugMode() {
        return isDebugMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MobileEngageConfig config = (MobileEngageConfig) o;

        if (isDebugMode != config.isDebugMode) return false;
        if (idlingResourceEnabled != config.idlingResourceEnabled) return false;
        if (application != null ? !application.equals(config.application) : config.application != null)
            return false;
        if (applicationCode != null ? !applicationCode.equals(config.applicationCode) : config.applicationCode != null)
            return false;
        if (applicationPassword != null ? !applicationPassword.equals(config.applicationPassword) : config.applicationPassword != null)
            return false;
        return statusListener != null ? statusListener.equals(config.statusListener) : config.statusListener == null;

    }

    @Override
    public int hashCode() {
        int result = application != null ? application.hashCode() : 0;
        result = 31 * result + (applicationCode != null ? applicationCode.hashCode() : 0);
        result = 31 * result + (applicationPassword != null ? applicationPassword.hashCode() : 0);
        result = 31 * result + (statusListener != null ? statusListener.hashCode() : 0);
        result = 31 * result + (isDebugMode ? 1 : 0);
        result = 31 * result + (idlingResourceEnabled ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MobileEngageConfig{" +
                "application=" + application +
                ", applicationCode='" + applicationCode + '\'' +
                ", applicationPassword='" + applicationPassword + '\'' +
                ", statusListener=" + statusListener +
                ", isDebugMode=" + isDebugMode +
                ", idlingResourceEnabled=" + idlingResourceEnabled +
                '}';
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
            boolean isDebuggable = (0 != (application.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));

            return new MobileEngageConfig(
                    application,
                    applicationCode,
                    applicationPassword,
                    statusListener,
                    isDebuggable,
                    idlingResourceEnabled
            );
        }
    }
}