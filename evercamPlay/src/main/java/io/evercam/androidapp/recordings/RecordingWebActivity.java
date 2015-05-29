package io.evercam.androidapp.recordings;

import android.os.Bundle;

import io.evercam.androidapp.R;
import io.evercam.androidapp.WebActivity;
import io.evercam.androidapp.utils.Constants;


public class RecordingWebActivity extends WebActivity
{
    private final String TAG = "RecordingWebActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recording_web);

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
        String cameraId = bundle.getString(Constants.BUNDLE_KEY_CAMERA_ID);

        RecordingWebView webView = (RecordingWebView) findViewById(R.id.recordings_webview);
        webView.webActivity = this;
        webView.loadRecordingWidget(cameraId);
    }
}
