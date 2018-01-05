package com.emarsys.mobileengage.iam.webview;

import android.annotation.SuppressLint;

import com.emarsys.mobileengage.iam.dialog.IamDialog;

import java.util.concurrent.CountDownLatch;

@SuppressLint("ValidFragment")
public class TestIamDialog extends IamDialog {

    CountDownLatch latch;

    public static TestIamDialog create(String campaignId, CountDownLatch latch) {
        IamDialog iamDialog = IamDialog.create(campaignId);

        TestIamDialog testIamDialog = new TestIamDialog(latch);
        testIamDialog.setArguments(iamDialog.getArguments());

        return testIamDialog;
    }

    public TestIamDialog(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onResume() {
        super.onResume();
        latch.countDown();
    }

    @Override
    public void onStop() {
        super.onStop();
        latch.countDown();
    }
}