package com.emarsys.mobileengage;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.emarsys.core.request.RequestManager;

import org.json.JSONObject;

import java.util.Map;

public class MobileEngage {
    private static final String TAG = "MobileEngage";
    static MobileEngageInternal instance;

    static void setupWithRequestManager(Application application, MobileEngageConfig config, RequestManager manager) {
        instance = new MobileEngageInternal(application, config, manager);
    }

    public static void setup(@NonNull Application application, @NonNull MobileEngageConfig config) {
        setupWithRequestManager(application, config, new RequestManager());
    }

    public static void setPushToken(String pushToken){
        instance.setPushToken(pushToken);
    }

    public static void appLogin() {
        instance.appLogin();
    }

    public static void appLogin(int contactField,
                                @NonNull String contactFieldValue) {
        instance.appLogin(contactField, contactFieldValue);
    }

    public static void appLogout() {
        instance.appLogout();
    }

    public static void trackCustomEvent(@NonNull String eventName,
                                        @Nullable Map<String, String>  eventAttributes) {
        instance.trackCustomEvent(eventName, eventAttributes);
    }

}
