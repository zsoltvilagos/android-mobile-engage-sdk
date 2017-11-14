package com.emarsys.mobileengage.iam.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.emarsys.mobileengage.database.MobileEngageDbHelper;
import com.emarsys.mobileengage.iam.AbstractSqliteRepository;

public class DisplayedIamRepository extends AbstractSqliteRepository<DisplayedIam> {

    public DisplayedIamRepository(Context context) {
        super(DisplayedIamContract.TABLE_NAME, new MobileEngageDbHelper(context));
    }

    @Override
    protected ContentValues contentValuesFromItem(DisplayedIam item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DisplayedIamContract.COLUMN_NAME_CAMPAIGN_ID, item.getCampaignId());
        contentValues.put(DisplayedIamContract.COLUMN_NAME_TIMESTAMP, item.getTimestamp());
        contentValues.put(DisplayedIamContract.COLUMN_NAME_EVENT_NAME, item.getEventName());
        return contentValues;
    }

    @Override
    protected DisplayedIam itemFromCursor(Cursor cursor) {
        String campaignId = cursor.getString(cursor.getColumnIndex(DisplayedIamContract.COLUMN_NAME_CAMPAIGN_ID));
        long timestamp = cursor.getLong(cursor.getColumnIndex(DisplayedIamContract.COLUMN_NAME_TIMESTAMP));
        String eventName = cursor.getString(cursor.getColumnIndex(DisplayedIamContract.COLUMN_NAME_EVENT_NAME));
        return new DisplayedIam(campaignId, timestamp, eventName);
    }

}
