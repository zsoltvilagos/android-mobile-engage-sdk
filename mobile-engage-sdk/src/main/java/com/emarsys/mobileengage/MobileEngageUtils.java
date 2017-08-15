package com.emarsys.mobileengage;

import android.support.test.espresso.IdlingResource;

import com.emarsys.mobileengage.util.MobileEngageIdlingResource;

public class MobileEngageUtils {

    static MobileEngageIdlingResource idlingResource;
    static boolean idlingResourceEnabled;

    public static void setup(MobileEngageConfig config) {
        idlingResourceEnabled = config.isIdlingResourceEnabled();
        idlingResource = idlingResourceEnabled ? new MobileEngageIdlingResource("mobile-engage-idling-resource") : null;
    }

    public static IdlingResource getIdlingResource() {
        return idlingResource;
    }

    static void incrementIdlingResource() {
        if(idlingResourceEnabled){
            idlingResource.increment();
        }
    }

    static void decrementIdlingResource() {
        if(idlingResourceEnabled){
            idlingResource.decrement();
        }
    }
}
