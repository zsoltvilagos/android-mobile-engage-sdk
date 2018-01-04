package com.emarsys.mobileengage.testUtil.mockito;

import android.os.Looper;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;

public class UiThreadSpy<T> implements Answer<T> {

    private Thread thread;
    private CountDownLatch latch;
    private T result;

    public UiThreadSpy() {
        this(null);
    }

    public UiThreadSpy(T result) {
        latch = new CountDownLatch(1);
        this.result = result;
    }

    @Override
    public T answer(InvocationOnMock invocation) throws Throwable {
        thread = Thread.currentThread();
        latch.countDown();
        return result;
    }

    public Thread getThread() throws InterruptedException {
        latch.await();
        return thread;
    }

    public void assertCalledOnMainThread() throws InterruptedException {
        Thread expected = Looper.getMainLooper().getThread();
        Thread result = getThread();
        assertEquals(expected, result);
    }
}
