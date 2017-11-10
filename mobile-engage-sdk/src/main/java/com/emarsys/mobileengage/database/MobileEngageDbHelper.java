package com.emarsys.mobileengage.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.emarsys.mobileengage.iam.DisplayedIamContract;


public class MobileEngageDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "EmarsysMobileEngage.db";

    public MobileEngageDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DisplayedIamContract.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * @deprecated use getReadableCoreDatabase
     */
    @Override
    @Deprecated
    public SQLiteDatabase getReadableDatabase() {
        return null;
    }

    public CoreSqliteDatabase getReadableCoreDatabase() {
        return new DelegatingCoreSqliteDatabase(super.getReadableDatabase());
    }

    /**
     * @deprecated use getWritableCoreDatabase
     */
    @Override
    @Deprecated
    public SQLiteDatabase getWritableDatabase() {
        return null;
    }

    public CoreSqliteDatabase getWritableCoreDatabase() {
        return new DelegatingCoreSqliteDatabase(super.getWritableDatabase());
    }
}
