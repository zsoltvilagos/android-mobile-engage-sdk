package com.emarsys.mobileengage.fake;

import android.os.Looper;

import com.emarsys.mobileengage.MobileEngageStatusListener;

public class FakeStatusListener implements MobileEngageStatusListener {

    public int onStatusLogCount;
    public int onErrorCount;
    public String errorId;
    public String successId;

    @Override
    public void onError(String id, Exception cause) {
        errorId = id;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            onErrorCount++;
        }
    }

    @Override
    public void onStatusLog(String id, String log) {
        successId = id;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            onStatusLogCount++;
        }
    }

}
