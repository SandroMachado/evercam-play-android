package io.evercam.androidapp;

import android.os.Bundle;
import android.webkit.WebView;

import io.evercam.androidapp.utils.Constants;

public class AboutWebActivity extends WebActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web);

        if(bundle != null)
        {
            loadPage();
        }
        else
        {
            finish();
        }
    }

    @Override
    protected void loadPage()
    {
        WebView webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(getWebViewClient());
        String url = bundle.getString(Constants.BUNDLE_KEY_URL);
        webView.loadUrl(url);
    }
}
