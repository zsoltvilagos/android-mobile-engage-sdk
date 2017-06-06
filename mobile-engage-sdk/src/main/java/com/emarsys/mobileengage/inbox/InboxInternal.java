package com.emarsys.mobileengage.inbox;

import android.os.Handler;
import android.os.Looper;

import com.emarsys.core.util.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class InboxInternal {

    private final Handler handler;

    public InboxInternal() {
        this.handler = new Handler(Looper.getMainLooper());
    }

    public boolean fetchNotifications(final InboxResultListener<List<Notification>> resultListener) {
        Assert.notNull(resultListener, "ResultListener should not be null!");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                resultListener.onSuccess(Arrays.asList(
                        new Notification("id1", "title1", Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap(), 100, new Date(1000)),
                        new Notification("id2", "title2", Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap(), 200, new Date(2000)),
                        new Notification("id3", "title3", Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap(), 300, new Date(3000))

                ));
            }
        }, 100);
        return false;
    }

}
