package io.evercam.androidapp.recordings;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import io.evercam.API;

public class RecordingWebView extends WebView
{
    private final String TAG = "RecordingWebView";

    public RecordingWebView(Context context)
    {
        super(context);
    }

    public RecordingWebView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public void loadRecordingWidget(String cameraId)
    {
        getSettings().setJavaScriptEnabled(true);

        setWebViewClient(new WebViewClient()
                         {
                             public void onPageStarted(WebView view, String url, Bitmap favicon)
                             {
                                 Log.d(TAG, "START");
                                // progressBar.setVisibility(View.VISIBLE);
                             }

                             public void onPageFinished(WebView view, String url)
                             {
                                // progressBar.setVisibility(View.GONE);
                                 Log.d(TAG, "stop");
                             }
                         });

        String customHtml = "<html><body><div evercam=\"snapshot-navigator" +
                "\"></div><script type=\"text/javascript\" src=\"https://dashboard" +
                ".evercam.io/snapshot.navigator.js?camera=" + cameraId + "&private=false&api_id=" + API.getUserKeyPair()[1] +
                "&api_key=" + API.getUserKeyPair()[0] +
                "\"></script></body></html>";

        Log.d(TAG, customHtml);
        loadData(customHtml, "text/html", "UTF-8");
    }
}
