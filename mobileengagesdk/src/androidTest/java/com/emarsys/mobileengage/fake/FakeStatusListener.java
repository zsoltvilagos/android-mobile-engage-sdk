package com.emarsys.mobileengage.fake;

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

    public FakeStatusListener() {
    }

    public FakeStatusListener(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onError(String id, Exception cause) {
        errorId = id;
        errorCause = cause;
        onErrorCount++;
        if (latch != null) {
            latch.countDown();
        }
    }

    @Override
    public void onStatusLog(String id, String log) {
        successId = id;
        successLog = log;
        onStatusLogCount++;
        if (latch != null) {
            latch.countDown();
        }
    }

}
