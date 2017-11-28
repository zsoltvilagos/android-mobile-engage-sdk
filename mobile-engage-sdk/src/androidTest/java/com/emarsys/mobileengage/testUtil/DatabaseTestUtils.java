package com.emarsys.mobileengage.testUtil;

import android.support.test.InstrumentationRegistry;

import com.emarsys.core.queue.sqlite.DbHelper;
import com.emarsys.mobileengage.database.MobileEngageDbHelper;

public class DatabaseTestUtils {

    public static final String EMARSYS_MOBILE_ENGAGE_DB = MobileEngageDbHelper.DATABASE_NAME;
    public static final String EMARSYS_CORE_QUEUE_DB = DbHelper.DATABASE_NAME;

    private DatabaseTestUtils() {
    }

    public static void deleteMobileEngageDatabase() {
        InstrumentationRegistry.getContext().deleteDatabase(EMARSYS_MOBILE_ENGAGE_DB);
    }

    public static void deleteCoreDatabase() {
        InstrumentationRegistry.getContext().deleteDatabase(EMARSYS_CORE_QUEUE_DB);
    }
}