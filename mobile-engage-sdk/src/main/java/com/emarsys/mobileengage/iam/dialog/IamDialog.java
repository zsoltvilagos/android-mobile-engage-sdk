package com.emarsys.mobileengage.iam.dialog;

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

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.R;
import com.emarsys.mobileengage.iam.webview.IamWebViewProvider;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class IamDialog extends DialogFragment {

    public static final String TAG = "MOBILE_ENGAGE_IAM_DIALOG_TAG";
    public static final String CAMPAIGN_ID = "campaign_id";

    private OnDialogShownAction action;
    private FrameLayout webViewContainer;
    private WebView webView;

    public static IamDialog create(String campaignId) {
        Assert.notNull(campaignId, "CampaignId must not be null!");
        IamDialog iamDialog = new IamDialog();
        Bundle bundle = new Bundle();
        bundle.putString(CAMPAIGN_ID, campaignId);
        iamDialog.setArguments(bundle);
        return iamDialog;
    }

    public IamDialog() {
    }

    public void setAction(OnDialogShownAction action) {
        this.action = action;
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

        webView = new IamWebViewProvider().provideWebView();
        webViewContainer = v.findViewById(R.id.mobileEngageInAppMessageContainer);
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

    @Override
    public void onResume() {
        super.onResume();
        Bundle args = getArguments();
        boolean notShown = !args.getBoolean("isShown", false);

        if (action != null && notShown) {
            String campaignId = args.getString(CAMPAIGN_ID);
            long timestamp = System.currentTimeMillis();
            action.execute(campaignId, timestamp);
            args.putBoolean("isShown", true);
        }
    }

    @Override
    public void onStop() {
        webViewContainer.removeView(webView);
        super.onStop();
    }
}
