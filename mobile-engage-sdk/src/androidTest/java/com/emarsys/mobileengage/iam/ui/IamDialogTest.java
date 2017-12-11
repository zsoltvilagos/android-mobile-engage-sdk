package com.emarsys.mobileengage.iam.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.webkit.WebView;

import com.emarsys.mobileengage.fake.FakeActivity;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.concurrent.CountDownLatch;


public class IamDialogTest {

    @SuppressLint("ValidFragment")
    private class TestIamDialog extends IamDialog {

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

    private TestIamDialog dialog;
    private CountDownLatch latch;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Rule
    public ActivityTestRule<FakeActivity> activityRule = new ActivityTestRule<>(FakeActivity.class);

    @Before
    public void init() throws InterruptedException {
        latch = new CountDownLatch(1);
        dialog = new TestIamDialog(latch);

        initWebViewProvider();
    }

    @After
    public void tearDown() {
        IamWebViewProvider.webView = null;
    }

    @Test
    public void testConstructor_setsDimAmountToZero() throws InterruptedException {
        displayDialog();
        latch.await();

        float expected = 0.0f;
        float actual = dialog.getDialog().getWindow().getAttributes().dimAmount;

        Assert.assertEquals(expected, actual, 0.0000001);
    }

    @Test
    public void testConstructor_setsDialogToFullscreen() throws InterruptedException {
        displayDialog();
        latch.await();

        float dialogWidth = activityRule.getActivity().getWindow().getAttributes().width;
        float dialogHeight = activityRule.getActivity().getWindow().getAttributes().height;

        float windowWidth = dialog.getDialog().getWindow().getAttributes().width;
        float windowHeight = dialog.getDialog().getWindow().getAttributes().height;

        Assert.assertEquals(windowWidth, dialogWidth, 0.0001);
        Assert.assertEquals(windowHeight, dialogHeight, 0.0001);
    }

    private void displayDialog() {
        final Activity activity = activityRule.getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.show(activity.getFragmentManager(), "testDialog");
            }
        });
    }

    private void initWebViewProvider() throws InterruptedException {
        final CountDownLatch initLatch = new CountDownLatch(1);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                IamWebViewProvider.webView = new WebView(InstrumentationRegistry.getTargetContext());
                initLatch.countDown();
            }
        });

        initLatch.await();
    }

}