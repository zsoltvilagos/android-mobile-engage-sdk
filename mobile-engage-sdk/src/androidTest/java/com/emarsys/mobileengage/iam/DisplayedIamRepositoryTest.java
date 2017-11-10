package com.emarsys.mobileengage.iam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static com.emarsys.mobileengage.iam.DisplayedIamContract.COLUMN_NAME_CAMPAIGN_ID;
import static com.emarsys.mobileengage.iam.DisplayedIamContract.COLUMN_NAME_EVENT_NAME;
import static com.emarsys.mobileengage.iam.DisplayedIamContract.COLUMN_NAME_TIMESTAMP;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DisplayedIamRepositoryTest {

    private DisplayedIamRepository iamRepository;
    private DisplayedIam displayedIam1;
    private DisplayedIam displayedIam2;

    @Before
    public void init() {
        Context context = InstrumentationRegistry.getContext();
        context.deleteDatabase("EmarsysMobileEngage.db");
        iamRepository = new DisplayedIamRepository(context);
        displayedIam1 = new DisplayedIam("campaign1", new Date().getTime(), "event1");
        displayedIam2 = new DisplayedIam("campaign2", new Date().getTime() + 1000, "event2");
    }

    @Test
    public void testContentValuesFromItem() {
        ContentValues expected = new ContentValues();
        expected.put(COLUMN_NAME_CAMPAIGN_ID, displayedIam1.getCampaignId());
        expected.put(COLUMN_NAME_TIMESTAMP, displayedIam1.getTimestamp());
        expected.put(COLUMN_NAME_EVENT_NAME, displayedIam1.getEventName());

        ContentValues result = iamRepository.contentValuesFromItem(displayedIam1);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testItemFromCursor() {
        Cursor cursor = mock(Cursor.class);

        when(cursor.getColumnIndex(COLUMN_NAME_CAMPAIGN_ID)).thenReturn(0);
        when(cursor.getString(0)).thenReturn(displayedIam1.getCampaignId());
        when(cursor.getColumnIndex(COLUMN_NAME_TIMESTAMP)).thenReturn(1);
        when(cursor.getLong(1)).thenReturn(displayedIam1.getTimestamp());
        when(cursor.getColumnIndex(COLUMN_NAME_EVENT_NAME)).thenReturn(2);
        when(cursor.getString(2)).thenReturn(displayedIam1.getEventName());

        DisplayedIam result = iamRepository.itemFromCursor(cursor);
        DisplayedIam expected = displayedIam1;

        Assert.assertEquals(expected, result);
    }

}