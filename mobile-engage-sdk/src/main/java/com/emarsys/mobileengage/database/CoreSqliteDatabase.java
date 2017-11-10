package com.emarsys.mobileengage.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public interface CoreSqliteDatabase {

    SQLiteDatabase getBackingDatabase();

    Cursor rawQuery(String sql, String[] selectionArgs);

    long insert(String table, String nullColumnHack, ContentValues values);

    int delete(String table, String whereClause, String[] whereArgs);

    void beginTransaction();

    void setTransactionSuccessful();

    void endTransaction();
}
