package com.emarsys.mobileengage.testUtil;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.emarsys.core.queue.sqlite.DbHelper;

public class TestDbHelper extends DbHelper {
    public static final String TABLE_NAME = "request";

    public TestDbHelper(Context context) {
        super(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.execSQL(String.format("DROP TABLE IF EXISTS %s;", TABLE_NAME));
        onCreate(db);
    }
}
