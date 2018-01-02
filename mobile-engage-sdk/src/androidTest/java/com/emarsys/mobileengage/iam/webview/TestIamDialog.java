package com.emarsys.mobileengage.iam.webview;

import android.annotation.SuppressLint;

import com.emarsys.mobileengage.iam.IamDialog;

import java.util.concurrent.CountDownLatch;

@SuppressLint("ValidFragment")
public class TestIamDialog extends IamDialog {

    CountDownLatch latch;

    public TestIamDialog(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onStart() {
        super.onStart();
        latch.countDown();
    }

}