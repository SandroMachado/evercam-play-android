package io.evercam.androidapp.recordings;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.utils.Constants;


public class RecordingWebActivity extends Activity
{
    private final String TAG = "RecordingWebActivity";

    public static CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

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
}
