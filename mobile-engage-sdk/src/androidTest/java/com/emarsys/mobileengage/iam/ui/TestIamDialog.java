package com.emarsys.mobileengage.iam.ui;

import android.annotation.SuppressLint;

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