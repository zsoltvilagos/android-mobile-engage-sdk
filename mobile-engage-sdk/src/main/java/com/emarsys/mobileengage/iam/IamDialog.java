package com.emarsys.mobileengage.iam;

import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.emarsys.mobileengage.R;
import com.emarsys.mobileengage.iam.webview.IamWebViewProvider;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class IamDialog extends DialogFragment {

    public static final String TAG = "IAM_DIALOG_TAG";

    public IamDialog() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.mobile_engage_in_app_message, container, false);

        WebView webView = new IamWebViewProvider().provideWebView();

        FrameLayout webViewContainer = v.findViewById(R.id.mobileEngageInAppMessageContainer);

        webViewContainer.addView(webView);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0.0f;
        window.setAttributes(windowParams);

        getDialog().getWindow()
                .setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT);
    }
}
