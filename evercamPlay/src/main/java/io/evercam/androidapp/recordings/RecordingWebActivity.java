package io.evercam.androidapp.recordings;

import android.app.Activity;
import android.os.Bundle;

import com.bugsense.trace.BugSenseHandler;

import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.utils.Constants;


public class RecordingWebActivity extends Activity
{
    public static CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(Constants.isAppTrackingEnabled)
        {
            BugSenseHandler.initAndStartSession(this, Constants.bugsense_ApiKey);
        }

        setContentView(R.layout.activity_recording_web);

        Bundle bundle = getIntent().getExtras();
        String cameraId = bundle.getString(Constants.BUNDLE_KEY_CAMERA_ID);

        progressDialog = new CustomProgressDialog(this);

        RecordingWebView webView = (RecordingWebView) findViewById(R.id.recordings_webview);
        webView.loadRecordingWidget(cameraId);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if(Constants.isAppTrackingEnabled)
        {
            BugSenseHandler.startSession(this);
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if(Constants.isAppTrackingEnabled)
        {
            BugSenseHandler.closeSession(this);
        }
    }
}
