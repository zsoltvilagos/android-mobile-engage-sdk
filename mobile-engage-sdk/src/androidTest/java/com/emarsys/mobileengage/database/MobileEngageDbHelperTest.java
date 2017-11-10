package com.emarsys.mobileengage.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class MobileEngageDbHelperTest {

    public static final String SQL_QUERY_IAM_TABLE_EXISTS = "SELECT name FROM sqlite_master WHERE type='table' AND name='displayed_iam';";
    private MobileEngageDbHelper dbHelper;

    @Before
    public void init() {
        Context context = InstrumentationRegistry.getContext();
        context.deleteDatabase("EmarsysMobileEngage.db");
        dbHelper = new MobileEngageDbHelper(context);
    }

    @Test
    public void onCreate() throws Exception {
        SQLiteDatabase db = dbHelper.getReadableCoreDatabase().getBackingDatabase();

        Cursor cursor = db.rawQuery(SQL_QUERY_IAM_TABLE_EXISTS, null);

        Assert.assertEquals(1, cursor.getCount());
        cursor.close();
    }

}