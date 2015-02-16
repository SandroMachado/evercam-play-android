package io.evercam.androidapp.recordings;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import io.evercam.API;
import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomProgressDialog;

public class RecordingWebView extends WebView
{
    private final String TAG = "RecordingWebView";
    private Context mContext;

    public RecordingWebView(Context context)
    {
        super(context);
        this.mContext = context;
    }

    public RecordingWebView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.mContext = context;
    }

    public void loadRecordingWidget(String cameraId)
    {
        getSettings().setJavaScriptEnabled(true);

        setWebViewClient(getWebViewClient());

        String customHtml = "<html><body><div evercam=\"snapshot-navigator" +
                "\"></div><script type=\"text/javascript\" src=\"https://dashboard" +
                ".evercam.io/snapshot.navigator.js?camera=" + cameraId + "&private=false&api_id="
                + API.getUserKeyPair()[1] +
                "&api_key=" + API.getUserKeyPair()[0] +
                "\"></script></body></html>";

        Log.d(TAG, customHtml);
        loadData(customHtml, "text/html", "UTF-8");
    }

    private WebViewClient getWebViewClient()
    {
        WebViewClient client = new WebViewClient()
        {
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                RecordingWebActivity.progressDialog.show(mContext.getString(R.string.msg_loading));
            }

            public void onPageFinished(WebView view, String url)
            {
                RecordingWebActivity.progressDialog.dismiss();
            }
        };
        return client;
    }
}
