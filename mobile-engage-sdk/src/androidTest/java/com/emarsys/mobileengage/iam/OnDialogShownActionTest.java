package com.emarsys.mobileengage.iam;

import android.os.Handler;

import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.mobileengage.database.MobileEngageDbHelper;
import com.emarsys.mobileengage.iam.dialog.OnDialogShownAction;
import com.emarsys.mobileengage.iam.model.DisplayedIam;
import com.emarsys.mobileengage.iam.model.DisplayedIamRepository;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;
import com.emarsys.mobileengage.testUtil.mockito.ThreadSpy;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.lang.reflect.Field;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class OnDialogShownActionTest {

    private static final String ID = "id";
    private static final long TIMESTAMP = 123;
    private static final DisplayedIam IAM = new DisplayedIam(ID, TIMESTAMP, "");

    private OnDialogShownAction action;
    private DisplayedIamRepository repository;
    private ThreadSpy threadSpy;
    private Handler handler;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException {
        threadSpy = new ThreadSpy();
        repository = mock(DisplayedIamRepository.class);
        handler = new CoreSdkHandlerProvider().provideHandler();

        MobileEngageDbHelper helper = mock(MobileEngageDbHelper.class, RETURNS_DEEP_STUBS);
        Field dbHelperField = AbstractSqliteRepository.class.getDeclaredField("dbHelper");
        dbHelperField.setAccessible(true);
        dbHelperField.set(repository, helper);

        doAnswer(threadSpy).when(repository).add(IAM);
        action = new OnDialogShownAction(handler, repository);
    }

    @After
    public void tearDown() {
        handler.getLooper().quit();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_handlerMustNotBeNull() {
        new OnDialogShownAction(null, repository);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_repositoryMustNotBeNull() {
        new OnDialogShownAction(handler, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecute_campaignIdMustNotBeNull() {
        action.execute(null, 0);
    }

    @Test
    public void testExecute_callsRepository() {
        action.execute(ID, TIMESTAMP);
        verify(repository, timeout(1000)).add(IAM);
    }

    @Test
    public void testExecute_callsRepository_onCoreSdkThread() throws InterruptedException {
        action.execute(ID, TIMESTAMP);
        threadSpy.verifyCalledOnCoreSdkThread();
    }

}