package com.emarsys.mobileengage.event.applogin;

import android.content.Context;
import android.content.SharedPreferences;

public class AppLoginStorage {
    public static final String SHARED_PREFERENCES_NAMESPACE = "ems_me_apploginstorage";
    public static final String APP_LOGIN_PAYLOAD_HASH_CODE_KEY = "appLoginPayloadHashCode";

    private SharedPreferences sharedPreferences;

    public AppLoginStorage(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAMESPACE, Context.MODE_PRIVATE);
    }

    public Integer getLastAppLoginPayloadHashCode() {
        if (sharedPreferences.contains(APP_LOGIN_PAYLOAD_HASH_CODE_KEY)) {
            return sharedPreferences.getInt(APP_LOGIN_PAYLOAD_HASH_CODE_KEY, 0);
        }
        return null;
    }

    public void setLastAppLoginPayloadHashCode(Integer hashCode) {
        sharedPreferences.edit().putInt(APP_LOGIN_PAYLOAD_HASH_CODE_KEY, hashCode).commit();
    }

    public void clear() {
        sharedPreferences.edit().clear().commit();
    }


}
