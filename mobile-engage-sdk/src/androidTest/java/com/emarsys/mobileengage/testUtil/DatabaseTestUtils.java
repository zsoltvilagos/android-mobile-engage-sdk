package com.emarsys.mobileengage.testUtil;

import android.support.test.InstrumentationRegistry;

public class DatabaseTestUtils {
    private DatabaseTestUtils() {
    }

    public static void deleteMobileEngageDatabase() {
        InstrumentationRegistry.getContext().deleteDatabase("EmarsysMobileEngage.db");
    }
}
