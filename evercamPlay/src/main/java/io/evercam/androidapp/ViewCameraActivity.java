package io.evercam.androidapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.video.VideoActivity;

public class ViewCameraActivity extends ParentActivity
{
    private final String TAG = "evercamplay-ViewCameraActivity";
    private LinearLayout canEditDetailLayout;
    private TextView cameraIdTextView;
    private TextView cameraNameTextView;
    private TextView cameraOwnerTextView;
    private TextView cameraTimezoneTextView;
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
    private Button editLinkButton;

    private EvercamCamera evercamCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        evercamCamera = VideoActivity.evercamCamera;

        setContentView(R.layout.activity_view_camera);

        if(this.getActionBar() != null)
        {
            this.getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initial UI elements
        initialScreen();
        fillCameraDetails(evercamCamera);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        getMenuInflater().inflate(R.menu.menu_view_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem editItem = menu.findItem(R.id.menu_action_edit);

        if(evercamCamera != null)
        {
            if(evercamCamera.canEdit())
            {
                editItem.setVisible(true);
            }
            else
            {
                editItem.setVisible(false);
            }
        }
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.menu_action_edit:
                linkToEditCamera();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == Constants.REQUEST_CODE_PATCH_CAMERA)
        {
            //If camera details have been edited, return to live view
            if(resultCode == Constants.RESULT_TRUE)
            {
                setResult(Constants.RESULT_TRUE);
                finish();
            }
        }
    }

    private void initialScreen()
    {
        canEditDetailLayout = (LinearLayout) findViewById(R.id.can_edit_detail_layout);
        editLinkButton = (Button) findViewById(R.id.button_edit_camera_link);

        cameraIdTextView = (TextView) findViewById(R.id.view_id_value);
        cameraNameTextView = (TextView) findViewById(R.id.view_name_value);
        cameraOwnerTextView = (TextView) findViewById(R.id.view_owner_value);
        cameraTimezoneTextView = (TextView) findViewById(R.id.view_timezone_value);
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

        editLinkButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                linkToEditCamera();
            }
        });
    }

    private void fillCameraDetails(EvercamCamera camera)
    {
        if(camera != null)
        {
            cameraIdTextView.setText(camera.getCameraId());
            cameraNameTextView.setText(camera.getName());
            cameraOwnerTextView.setText(camera.getRealOwner());
            cameraTimezoneTextView.setText(camera.getTimezone());
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

            //Show more details if user has the rights
            fillCanEditDetails(camera);
        }
    }

    private void fillCanEditDetails(EvercamCamera camera)
    {
        if(camera.canEdit())
        {
            editLinkButton.setVisibility(View.VISIBLE);
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
                cameraInternalRtspTextView.setText(String.valueOf(camera.getInternalRtsp()));
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

    private void linkToEditCamera()
    {
        Intent intent = new Intent(ViewCameraActivity.this, AddEditCameraActivity.class);
        intent.putExtra(Constants.KEY_IS_EDIT, true);
        startActivityForResult(intent, Constants.REQUEST_CODE_PATCH_CAMERA);
    }
}
