package com.emarsys.mobileengage.iam.webview;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.emarsys.core.activity.CurrentActivityWatchdog;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.iam.dialog.IamDialog;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class DefaultMessageLoadedListener implements MessageLoadedListener {

    IamDialog iamDialog;

    public DefaultMessageLoadedListener(IamDialog iamDialog) {
        Assert.notNull(iamDialog, "IamDialog must not be null!");
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
