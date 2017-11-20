package com.emarsys.mobileengage.iam.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;

import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.MobileEngage;

public class IamWebViewProvider {

    static WebView webView;

    public void loadMessageAsync(final String html, final MessageLoadedListener messageLoadedListener, final Object jsBridge) {
        Assert.notNull(html, "Html must not be null!");
        Assert.notNull(messageLoadedListener, "MessageLoadedListener must not be null!");
        Assert.notNull(jsBridge, "JsBridge must not be null!");

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @SuppressLint({"JavascriptInterface", "AddJavascriptInterface"})
            @Override
            public void run() {
                Context context = MobileEngage.getConfig().getApplication();
                webView = new WebView(context);

                webView.getSettings().setJavaScriptEnabled(true);
                webView.addJavascriptInterface(jsBridge, "Android");

                webView.setWebViewClient(new IamWebViewClient(messageLoadedListener));

                webView.loadData(html, "text/html", "UTF-8");
            }
        });
    }

    public WebView provideWebView() {
        return webView;
    }
}
