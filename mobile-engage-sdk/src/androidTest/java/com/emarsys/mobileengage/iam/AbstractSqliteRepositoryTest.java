package com.emarsys.mobileengage.iam;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.test.filters.SdkSuppress;

import com.emarsys.mobileengage.database.CoreSqliteDatabase;
import com.emarsys.mobileengage.database.MobileEngageDbHelper;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static android.os.Build.VERSION_CODES.KITKAT;
import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SdkSuppress(minSdkVersion = KITKAT)
public class AbstractSqliteRepositoryTest {

    private final static String TABLE_NAME = "table";

    private AbstractSqliteRepository<Object> repository;
    private MobileEngageDbHelper dbHelperMock;
    private CoreSqliteDatabase dbMock;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        dbMock = mock(CoreSqliteDatabase.class);

        dbHelperMock = mock(MobileEngageDbHelper.class);
        when(dbHelperMock.getReadableCoreDatabase()).thenReturn(dbMock);
        when(dbHelperMock.getWritableCoreDatabase()).thenReturn(dbMock);

        repository = mock(AbstractSqliteRepository.class, Mockito.CALLS_REAL_METHODS);
        repository.tableName = TABLE_NAME;
        repository.dbHelper = dbHelperMock;

    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdd_shouldNotAcceptNull() {
        repository.add(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemove_shouldNotAcceptNull() {
        repository.remove(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQuery_shouldNotAcceptNull() {
        repository.query(null);
    }

    @Test
    public void testAdd_shouldBehaveCorrectly() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("key", "value");
        when(repository.contentValuesFromItem(any())).thenReturn(contentValues);

        Object input = new Object();

        repository.add(input);

        verify(repository).contentValuesFromItem(input);
        verify(dbMock).beginTransaction();
        verify(dbMock).insert(TABLE_NAME, null, contentValues);
        verify(dbMock).setTransactionSuccessful();
        verify(dbMock).endTransaction();
    }

    @Test
    public void testQuery_shouldBehaveCorrectly() {
        SqlSpecification specification = mock(SqlSpecification.class);
        when(specification.getSql()).thenReturn("sql statement");
        when(specification.getArgs()).thenReturn(new String[]{"a", "b", "c"});

        Cursor cursor = mock(Cursor.class);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.isAfterLast()).thenReturn(false, false, false, true);

        when(dbMock.rawQuery(any(String.class), any(String[].class))).thenReturn(cursor);

        Object item1 = new Object();
        Object item2 = new Object();
        Object item3 = new Object();

        when(repository.itemFromCursor(cursor)).thenReturn(item1, item2, item3);

        List<Object> expected = Arrays.asList(item1, item2, item3);
        List<Object> result = repository.query(specification);

        verify(dbMock).rawQuery(specification.getSql(), specification.getArgs());
        assertEquals(expected, result);
    }

    @Test
    public void testRemove_shouldBehaveCorrectly() {
        SqlSpecification specification = mock(SqlSpecification.class);
        when(specification.getSql()).thenReturn("sql statement");
        when(specification.getArgs()).thenReturn(new String[]{"a", "b", "c"});

        repository.remove(specification);

        verify(dbMock).beginTransaction();
        verify(dbMock).delete(TABLE_NAME, specification.getSql(), specification.getArgs());
        verify(dbMock).setTransactionSuccessful();
        verify(dbMock).endTransaction();
    }

}