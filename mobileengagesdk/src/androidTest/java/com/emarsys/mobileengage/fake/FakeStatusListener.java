package com.emarsys.mobileengage.fake;

import android.os.Looper;

import com.emarsys.mobileengage.MobileEngageStatusListener;

import java.util.concurrent.CountDownLatch;

public class FakeStatusListener implements MobileEngageStatusListener {

    public int onStatusLogCount;
    public int onErrorCount;
    public String errorId;
    public String successId;
    public CountDownLatch latch;
    public Exception errorCause;
    public String successLog;

    @Override
    public void onError(String id, Exception cause) {
        errorId = id;
        errorCause = cause;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            onErrorCount++;
        }
        if (latch != null) {
            latch.countDown();
        }
    }

    @Override
    public void onStatusLog(String id, String log) {
        successId = id;
        successLog = log;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            onStatusLogCount++;
        }
        if (latch != null) {
            latch.countDown();
        }
    }

}
