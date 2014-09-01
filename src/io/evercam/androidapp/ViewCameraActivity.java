package io.evercam.androidapp;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

import io.evercam.Auth;
import io.evercam.CameraBuilder;
import io.evercam.Defaults;
import io.evercam.EvercamException;
import io.evercam.Model;
import io.evercam.PatchCameraBuilder;
import io.evercam.Vendor;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.tasks.AddCameraTask;
import io.evercam.androidapp.tasks.PatchCameraTask;
import io.evercam.androidapp.tasks.TestSnapshotTask;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.EvercamApiHelper;
import io.evercam.androidapp.video.VideoActivity;
import io.evercam.network.discovery.DiscoveredCamera;

import com.bugsense.trace.BugSenseHandler;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

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
			this.getActionBar().setHomeButtonEnabled(true);
			this.getActionBar().setIcon(R.drawable.ic_navigation_back);
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
			cameraVendorTextView.setText(camera.getVendor());
			cameraModelTextView.setText(camera.getModel());
		}
	}
}
