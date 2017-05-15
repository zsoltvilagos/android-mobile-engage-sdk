package com.emarsys.mobileengage.fake;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TestDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME_SUFFIX = "_EmarsysCoreQueueTest.db";

    public static final String TABLE_NAME = "request";
    public static final String COLUMN_NAME_REQUEST_ID = "request_id";
    public static final String COLUMN_NAME_METHOD = "method";
    public static final String COLUMN_NAME_URL = "url";
    public static final String COLUMN_NAME_HEADERS = "headers";
    public static final String COLUMN_NAME_PAYLOAD = "payload";
    public static final String COLUMN_NAME_TIMESTAMP = "timestamp";

    public TestDbHelper(Context context, Class<?> cls) {
        super(context, cls.getName() + DATABASE_NAME_SUFFIX, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s (" +
                        "%s TEXT," +
                        "%s TEXT," +
                        "%s TEXT," +
                        "%s BLOB," +
                        "%s BLOB," +
                        "%s INTEGER" +
                        ");",
                TABLE_NAME,
                COLUMN_NAME_REQUEST_ID,
                COLUMN_NAME_METHOD,
                COLUMN_NAME_URL,
                COLUMN_NAME_HEADERS,
                COLUMN_NAME_PAYLOAD,
                COLUMN_NAME_TIMESTAMP));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL(String.format("DELETE FROM %s;", TABLE_NAME));
    }
}
