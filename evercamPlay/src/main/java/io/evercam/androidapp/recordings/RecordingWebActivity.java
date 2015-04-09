package io.evercam.androidapp.recordings;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.bugsense.trace.BugSenseHandler;

import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.feedback.FeedbackSender;
import io.evercam.androidapp.utils.Constants;


public class RecordingWebActivity extends Activity
{
    private final String TAG = "RecordingWebActivity";

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

        if(this.getActionBar() != null)
        {
            this.getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
        {
            String cameraId = bundle.getString(Constants.BUNDLE_KEY_CAMERA_ID);

            progressDialog = new CustomProgressDialog(this);

            RecordingWebView webView = (RecordingWebView) findViewById(R.id.recordings_webview);
            webView.loadRecordingWidget(cameraId);
        }
        else
        {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if(id == android.R.id.home)
        {
            finish();
        }
        return super.onOptionsItemSelected(item);
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
