package io.evercam.androidapp;

import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.EvercamApiHelper;
import io.evercam.androidapp.video.VideoActivity;
import com.bugsense.trace.BugSenseHandler;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.view.MenuItem;

public class ViewCameraActivity extends Activity
{
	private final String TAG = "evercamplay-ViewCameraActivity";
	private TextView cameraIdTextView;
	private TextView cameraNameTextView;
	private TextView cameraOwnerTextView;
	private TextView cameraVendorTextView;
	private TextView cameraModelTextView;

	private EvercamCamera evercamCamera;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.initAndStartSession(this, Constants.bugsense_ApiKey);
		}

		EvercamApiHelper.setEvercamDeveloperKeypair(this);

		EvercamPlayApplication.sendScreenAnalytics(this, getString(R.string.screen_view_camera));
		evercamCamera = VideoActivity.evercamCamera;

		setContentView(R.layout.activity_view_camera);

		if (this.getActionBar() != null)
		{
			this.getActionBar().setDisplayHomeAsUpEnabled(true);
			this.getActionBar().setIcon(R.drawable.icon_50x50);
		}

		// Initial UI elements
		initialScreen();
		fillEditCameraDetails(evercamCamera);

	}

	@Override
	public void onStart()
	{
		super.onStart();

		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.startSession(this);
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();

		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.closeSession(this);
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		switch (item.getItemId())
		{
		case android.R.id.home:
			this.finish();
			return true;
		}
		return true;
	}

	private void initialScreen()
	{
		cameraIdTextView = (TextView) findViewById(R.id.view_id_value);
		cameraNameTextView = (TextView) findViewById(R.id.view_name_value);
		cameraOwnerTextView = (TextView) findViewById(R.id.view_owner_value);
		cameraVendorTextView = (TextView) findViewById(R.id.view_vendor_value);
		cameraModelTextView = (TextView) findViewById(R.id.view_model_value);
	}

	private void fillEditCameraDetails(EvercamCamera camera)
	{
		if (camera != null)
		{
			cameraIdTextView.setText(camera.getCameraId());
			cameraNameTextView.setText(camera.getName());
			cameraOwnerTextView.setText(camera.getRealOwner());
			if (camera.getVendor().isEmpty())
			{
				cameraVendorTextView.setText(R.string.view_not_set);
			}
			else
			{
				cameraVendorTextView.setText(camera.getVendor());
			}
			if (camera.getModel().isEmpty())
			{
				cameraModelTextView.setText(R.string.view_not_set);
			}
			else
			{
				cameraModelTextView.setText(camera.getModel());
			}
		}
	}
}
