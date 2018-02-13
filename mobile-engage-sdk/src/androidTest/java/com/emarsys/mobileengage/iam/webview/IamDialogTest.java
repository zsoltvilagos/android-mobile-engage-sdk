package com.emarsys.mobileengage.iam.webview;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.rule.ActivityTestRule;
import android.webkit.WebView;

import com.emarsys.mobileengage.fake.FakeActivity;
import com.emarsys.mobileengage.iam.dialog.IamDialog;
import com.emarsys.mobileengage.iam.dialog.action.OnDialogShownAction;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static android.os.Build.VERSION_CODES.KITKAT;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SdkSuppress(minSdkVersion = KITKAT)
public class IamDialogTest {

    private TestIamDialog dialog;
    private CountDownLatch latch;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Rule
    public ActivityTestRule<FakeActivity> activityRule = new ActivityTestRule<>(FakeActivity.class);

    @Before
    public void init() throws InterruptedException {
        latch = new CountDownLatch(1);
        dialog = TestIamDialog.create("", latch);

        initWebViewProvider();
    }

    @After
    public void tearDown() {
        IamWebViewProvider.webView = null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate_campaignIdMustNotBeNull() {
        IamDialog.create(null);
    }

    @Test
    public void testCreate_shouldReturnImageDialogInstance() {
        assertNotNull(IamDialog.create(""));
    }

    @Test
    public void testCreate_shouldInitializeDialog_withCampaignId() {
        String campaignId = "123456789";
        IamDialog dialog = IamDialog.create(campaignId);

        Bundle result = dialog.getArguments();
        assertEquals(campaignId, result.getString("campaign_id"));
    }

    @Test
    public void testInitialization_setsDimAmountToZero() throws InterruptedException {
        displayDialog();
        latch.await();

        float expected = 0.0f;
        float actual = dialog.getDialog().getWindow().getAttributes().dimAmount;

        assertEquals(expected, actual, 0.0000001);
    }

    @Test
    public void testInitialization_setsDialogToFullscreen() throws InterruptedException {
        displayDialog();
        latch.await();

        float dialogWidth = activityRule.getActivity().getWindow().getAttributes().width;
        float dialogHeight = activityRule.getActivity().getWindow().getAttributes().height;

        float windowWidth = dialog.getDialog().getWindow().getAttributes().width;
        float windowHeight = dialog.getDialog().getWindow().getAttributes().height;

        assertEquals(windowWidth, dialogWidth, 0.0001);
        assertEquals(windowHeight, dialogHeight, 0.0001);
    }

    @Test
    public void testOnResume_callsActions_ifProvided() throws InterruptedException {
        Bundle args = new Bundle();
        args.putString("campaign_id", "123456789");
        dialog.setArguments(args);

        List<OnDialogShownAction> actions = createMockActions();
        dialog.setActions(actions);

        displayDialog();
        latch.await();

        for (OnDialogShownAction action : actions) {
            verify(action).execute(eq("123456789"));
        }
    }

    @Test
    public void testOnResume_callsActions_onlyOnce() throws InterruptedException {
        List<OnDialogShownAction> actions = createMockActions();
        dialog.setActions(actions);

        displayDialog();
        latch.await();

        dialog.latch = latch = new CountDownLatch(1);
        dismissDialog();
        latch.await();

        dialog.latch = latch = new CountDownLatch(1);
        displayDialog();
        latch.await();

        for (OnDialogShownAction action : actions) {
            verify(action, times(1)).execute(any(String.class));
        }
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

    private void dismissDialog() {
        final Activity activity = activityRule.getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
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

    private List<OnDialogShownAction> createMockActions() {
        return Arrays.asList(
                mock(OnDialogShownAction.class),
                mock(OnDialogShownAction.class),
                mock(OnDialogShownAction.class)
        );
    }

}