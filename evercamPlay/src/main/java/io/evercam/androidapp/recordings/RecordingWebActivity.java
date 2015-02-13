package io.evercam.androidapp.recordings;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ProgressBar;

import io.evercam.androidapp.R;
import io.evercam.androidapp.utils.Constants;


public class RecordingWebActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_web);

        Bundle bundle = getIntent().getExtras();
        String cameraId = bundle.getString(Constants.BUNDLE_KEY_CAMERA_ID);

        RecordingWebView webView = (RecordingWebView) findViewById(R.id.recordings_webview);
     //   ProgressBar progressBar = (ProgressBar) findViewById(R.id.recordings_progress);
        webView.loadRecordingWidget(cameraId);
    }
}
