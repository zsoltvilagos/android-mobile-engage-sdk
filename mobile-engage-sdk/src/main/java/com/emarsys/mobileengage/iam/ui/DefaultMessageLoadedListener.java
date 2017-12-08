package com.emarsys.mobileengage.iam.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;

import com.emarsys.core.activity.CurrentActivityWatchdog;

public class DefaultMessageLoadedListener implements MessageLoadedListener {

    IamDialog iamDialog;

    public DefaultMessageLoadedListener() {
        iamDialog = new IamDialog();
    }

    @Override
    public void onMessageLoaded() {
        Activity currentActivity = CurrentActivityWatchdog.getCurrentActivity();
        FragmentManager fragmentManager = currentActivity.getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(IamDialog.TAG);
        if (fragment == null) {
            iamDialog.show(fragmentManager, IamDialog.TAG);
        }
    }

}
