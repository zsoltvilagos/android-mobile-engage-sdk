package com.emarsys.mobileengage.service;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;

import com.emarsys.core.activity.ActivityLifecycleWatchdog;
import com.emarsys.core.concurrency.CoreSdkHandlerProvider;
import com.emarsys.core.util.FileUtils;
import com.emarsys.mobileengage.di.DependencyContainer;
import com.emarsys.mobileengage.di.DependencyInjection;
import com.emarsys.mobileengage.iam.InAppPresenter;
import com.emarsys.mobileengage.iam.PushToInAppAction;
import com.emarsys.mobileengage.testUtil.TimeoutUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import static android.os.Build.VERSION_CODES.KITKAT;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = KITKAT)
public class PushToInAppUtilsTest {

    private ActivityLifecycleWatchdog activityLifecycleWatchdog;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();


    @Before
    public void setUp() {
        DependencyContainer dependencyContainer = mock(DependencyContainer.class);
        activityLifecycleWatchdog = mock(ActivityLifecycleWatchdog.class);
        when(dependencyContainer.getActivityLifecycleWatchdog()).thenReturn(activityLifecycleWatchdog);
        when(dependencyContainer.getInAppPresenter()).thenReturn(mock(InAppPresenter.class));
        when(dependencyContainer.getCoreSdkHandler()).thenReturn(new CoreSdkHandlerProvider().provideHandler());
        DependencyInjection.setup(dependencyContainer);
    }

    @After
    public void tearDown() {
        DependencyInjection.tearDown();
    }

    @Test
    public void testHandlePreloadedInAppMessage_shouldCallAddTriggerOnActivityAction_whenFileUrlIsAvailable() throws JSONException, InterruptedException {
        Intent intent = new Intent();
        Bundle payload = new Bundle();
        JSONObject ems = new JSONObject();
        JSONObject inapp = new JSONObject();
        inapp.put("campaignId", "campaignId");
        inapp.put("fileUrl", FileUtils.download(InstrumentationRegistry.getTargetContext(), "https://www.emarsys.com"));
        ems.put("inapp", inapp.toString());
        payload.putString("ems", ems.toString());
        intent.putExtra("payload", payload);

        PushToInAppUtils.handlePreloadedInAppMessage(intent);

        waitForEventLoopToFinish();

        verify(activityLifecycleWatchdog).addTriggerOnActivityAction(any(PushToInAppAction.class));
    }

    @Test
    public void testHandlePreloadedInAppMessage_shouldCallAddTriggerOnActivityAction_whenFileUrlIsAvailableButTheFileIsMissing() throws JSONException, InterruptedException {
        Intent intent = new Intent();
        Bundle payload = new Bundle();
        JSONObject ems = new JSONObject();
        JSONObject inapp = new JSONObject();
        inapp.put("campaignId", "campaignId");
        String fireUrl = FileUtils.download(InstrumentationRegistry.getTargetContext(), "https://www.emarsys.com");
        new File(fireUrl).delete();
        inapp.put("fileUrl", fireUrl);
        inapp.put("url", "https://www.emarsys.com");
        ems.put("inapp", inapp.toString());
        payload.putString("ems", ems.toString());
        intent.putExtra("payload", payload);

        PushToInAppUtils.handlePreloadedInAppMessage(intent);

        waitForEventLoopToFinish();

        verify(activityLifecycleWatchdog).addTriggerOnActivityAction(any(PushToInAppAction.class));
    }

    @Test
    public void testHandlePreloadedInAppMessage_shouldCallAddTriggerOnActivityAction_whenFileUrlIsNotAvailable_butUrlIsAvailable() throws JSONException, InterruptedException {
        Intent intent = new Intent();
        Bundle payload = new Bundle();
        JSONObject ems = new JSONObject();
        JSONObject inapp = new JSONObject();
        inapp.put("campaignId", "campaignId");
        inapp.put("url", "https://www.emarsys.com");
        ems.put("inapp", inapp.toString());
        payload.putString("ems", ems.toString());
        intent.putExtra("payload", payload);

        PushToInAppUtils.handlePreloadedInAppMessage(intent);

        waitForEventLoopToFinish();

        verify(activityLifecycleWatchdog).addTriggerOnActivityAction(any(PushToInAppAction.class));
    }

    @Test
    public void testHandlePreloadedInAppMessage_shouldDeleteFile_afterPushToInAppActionIsScheduled() throws Exception {
        String fileUrl = FileUtils.download(InstrumentationRegistry.getTargetContext(), "https://www.emarsys.com");

        Intent intent = new Intent();
        Bundle payload = new Bundle();
        JSONObject ems = new JSONObject();
        JSONObject inapp = new JSONObject();
        inapp.put("campaignId", "campaignId");
        inapp.put("fileUrl", fileUrl);
        ems.put("inapp", inapp.toString());
        payload.putString("ems", ems.toString());
        intent.putExtra("payload", payload);

        assertEquals(true, new File(fileUrl).exists());

        PushToInAppUtils.handlePreloadedInAppMessage(intent);
        waitForEventLoopToFinish();

        verify(activityLifecycleWatchdog).addTriggerOnActivityAction(any(PushToInAppAction.class));

        assertEquals(false, new File(fileUrl).exists());
    }

    private void waitForEventLoopToFinish() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        DependencyInjection.getContainer().getCoreSdkHandler().post(new Runnable() {
            @Override
            public void run() {
                latch.countDown();
            }
        });

        latch.await();
    }
}