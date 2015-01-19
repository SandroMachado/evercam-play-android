package io.evercam.androidapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.EvercamApiHelper;
import io.evercam.androidapp.video.VideoActivity;

public class ViewCameraActivity extends Activity
{
    private final String TAG = "evercamplay-ViewCameraActivity";
    private LinearLayout canEditDetailLayout;
    private TextView cameraIdTextView;
    private TextView cameraNameTextView;
    private TextView cameraOwnerTextView;
    private TextView cameraVendorTextView;
    private TextView cameraModelTextView;
    private TextView cameraUsernameTextView;
    private TextView cameraPasswordTextView;
    private TextView cameraSnapshotUrlTextView;
    private TextView cameraInternalHostTextView;
    private TextView cameraInternalHttpTextView;
    private TextView cameraInternalRtspTextView;
    private TextView cameraExternalHostTextView;
    private TextView cameraExternalHttpTextView;
    private TextView cameraExternalRtspTextView;

    private EvercamCamera evercamCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(Constants.isAppTrackingEnabled)
        {
            BugSenseHandler.initAndStartSession(this, Constants.bugsense_ApiKey);
        }

        EvercamApiHelper.setEvercamDeveloperKeypair(this);

        EvercamPlayApplication.sendScreenAnalytics(this, getString(R.string.screen_view_camera));
        evercamCamera = VideoActivity.evercamCamera;

        setContentView(R.layout.activity_view_camera);

        if(this.getActionBar() != null)
        {
            this.getActionBar().setDisplayHomeAsUpEnabled(true);
            this.getActionBar().setIcon(R.drawable.icon_50x50);
        }

        // Initial UI elements
        initialScreen();
        fillCameraDetails(evercamCamera);

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

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return true;
    }

    private void initialScreen()
    {
        canEditDetailLayout = (LinearLayout) findViewById(R.id.can_edit_detail_layout);

        cameraIdTextView = (TextView) findViewById(R.id.view_id_value);
        cameraNameTextView = (TextView) findViewById(R.id.view_name_value);
        cameraOwnerTextView = (TextView) findViewById(R.id.view_owner_value);
        cameraVendorTextView = (TextView) findViewById(R.id.view_vendor_value);
        cameraModelTextView = (TextView) findViewById(R.id.view_model_value);

        //'Can edit' fields
        cameraUsernameTextView = (TextView) findViewById(R.id.view_username_value);
        cameraPasswordTextView = (TextView) findViewById(R.id.view_password_value);
        cameraSnapshotUrlTextView = (TextView) findViewById(R.id.view_jpg_url_value);
        cameraInternalHostTextView = (TextView) findViewById(R.id.view_internal_host_value);
        cameraInternalHttpTextView = (TextView) findViewById(R.id.view_internal_http_value);
        cameraInternalRtspTextView = (TextView) findViewById(R.id.view_internal_rtsp_value);
        cameraExternalHostTextView = (TextView) findViewById(R.id.view_external_host_value);
        cameraExternalHttpTextView = (TextView) findViewById(R.id.view_external_http_value);
        cameraExternalRtspTextView = (TextView) findViewById(R.id.view_external_rtsp_value);
    }

    private void fillCameraDetails(EvercamCamera camera)
    {
        if(camera != null)
        {
            cameraIdTextView.setText(camera.getCameraId());
            cameraNameTextView.setText(camera.getName());
            cameraOwnerTextView.setText(camera.getRealOwner());
            if(camera.getVendor().isEmpty())
            {
                setAsNotSpecified(cameraVendorTextView);
            }
            else
            {
                cameraVendorTextView.setText(camera.getVendor());
            }
            if(camera.getModel().isEmpty())
            {
                setAsNotSpecified(cameraModelTextView);
            }
            else
            {
                cameraModelTextView.setText(camera.getModel());
            }

            if(evercamCamera.canEdit())
            {
                canEditDetailLayout.setVisibility(View.VISIBLE);

            }
            else
            {
                canEditDetailLayout.setVisibility(View.GONE);
            }

            //Show more details if user has the rights
            fillCanEditDetails(camera);
        }
    }

    private void fillCanEditDetails(EvercamCamera camera)
    {
        if(camera.canEdit())
        {
            canEditDetailLayout.setVisibility(View.VISIBLE);

            if(camera.getUsername().isEmpty())
            {
                setAsNotSpecified(cameraUsernameTextView);

            }
            else
            {
                cameraUsernameTextView.setText(camera.getUsername());
            }

            if(camera.getPassword().isEmpty())
            {
                setAsNotSpecified(cameraPasswordTextView);

            }
            else
            {
                cameraPasswordTextView.setText(camera.getPassword());
            }

            if(camera.getJpgPath().isEmpty())
            {
                setAsNotSpecified(cameraSnapshotUrlTextView);
            }
            else
            {
                cameraSnapshotUrlTextView.setText(camera.getJpgPath());
            }

            if(camera.getExternalHost().isEmpty())
            {
                setAsNotSpecified(cameraExternalHostTextView);
            }
            else
            {
                cameraExternalHostTextView.setText(camera.getExternalHost());
            }

            if(camera.getInternalHost().isEmpty())
            {
                setAsNotSpecified(cameraInternalHostTextView);
            }
            else
            {
                cameraInternalHostTextView.setText(camera.getInternalHost());
            }

            int externalHttp = camera.getExternalHttp();
            int externalRtsp = camera.getExternalRtsp();
            int internalHttp = camera.getInternalHttp();
            int internalRtsp = camera.getInternalRtsp();

            if(externalHttp != 0)
            {
                cameraExternalHttpTextView.setText(String.valueOf(externalHttp));
            }
            else
            {
                setAsNotSpecified(cameraExternalHttpTextView);
            }
            if(externalRtsp != 0)
            {
                cameraExternalRtspTextView.setText(String.valueOf(externalRtsp));
            }
            else
            {
                setAsNotSpecified(cameraExternalRtspTextView);
            }
            if(internalHttp != 0)
            {
                cameraInternalHttpTextView.setText(String.valueOf(camera.getInternalHttp()));
            }
            else
            {
                setAsNotSpecified(cameraInternalHttpTextView);
            }
            if(internalRtsp != 0)
            {
                cameraExternalRtspTextView.setText(String.valueOf(camera.getInternalRtsp()));
            }
            else
            {
                setAsNotSpecified(cameraInternalRtspTextView);
            }
        }
    }

    private void setAsNotSpecified(TextView textView)
    {
        textView.setText(R.string.not_specified);
        textView.setTextColor(Color.GRAY);
    }
}
