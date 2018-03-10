package com.emarsys.mobileengage.iam.webview;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.emarsys.core.activity.CurrentActivityWatchdog;
import com.emarsys.core.database.repository.Repository;
import com.emarsys.core.database.repository.SqlSpecification;
import com.emarsys.core.timestamp.TimestampProvider;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.iam.dialog.IamDialog;

import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class DefaultMessageLoadedListener implements MessageLoadedListener {

    IamDialog iamDialog;
    long loadingTimeStart;
    Repository<Map<String, Object>, SqlSpecification> logRepository;
    private TimestampProvider timestampProvider;

    public DefaultMessageLoadedListener(
            IamDialog iamDialog,
            Repository<Map<String, Object>, SqlSpecification> logRepository,
            long loadingTimeStart,
            TimestampProvider timestampProvider) {
        Assert.notNull(iamDialog, "IamDialog must not be null!");
        Assert.notNull(logRepository, "LogRepository must not be null!");
        Assert.notNull(timestampProvider, "TimestampProvider must not be null!");
        this.iamDialog = iamDialog;
        this.logRepository = logRepository;
        this.loadingTimeStart = loadingTimeStart;
        this.timestampProvider = timestampProvider;
    }

    @Override
    public void onMessageLoaded() {
        Activity currentActivity = CurrentActivityWatchdog.getCurrentActivity();
        if (currentActivity != null) {
            FragmentManager fragmentManager = currentActivity.getFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(IamDialog.TAG);
            if (fragment == null) {
                iamDialog.show(fragmentManager, IamDialog.TAG);
            }
        }
    }
}


