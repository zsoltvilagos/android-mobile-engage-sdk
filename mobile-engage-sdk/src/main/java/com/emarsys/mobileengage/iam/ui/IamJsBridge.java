package com.emarsys.mobileengage.iam.ui;

import android.webkit.JavascriptInterface;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.iam.DialogOwner;

public class IamJsBridge {

    private IamDialog dialog;

    public IamJsBridge(DialogOwner dialogOwner) {
        Assert.notNull(dialogOwner, "DialogOwner must not be null!");
        this.dialog = dialogOwner.getIamDialog();
    }

    @JavascriptInterface
    public void close(String json) {
        dialog.dismiss();
    }
}
