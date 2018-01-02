package com.emarsys.mobileengage.iam.webview;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;

import com.emarsys.core.activity.CurrentActivityWatchdog;
import com.emarsys.mobileengage.iam.IamDialog;

public class DefaultMessageLoadedListener implements MessageLoadedListener {

    IamDialog iamDialog;

    public DefaultMessageLoadedListener(IamDialog iamDialog) {
        this.iamDialog = iamDialog;
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
