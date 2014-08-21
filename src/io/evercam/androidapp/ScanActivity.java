package io.evercam.androidapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import io.evercam.androidapp.tasks.ScanForCameraTask;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.EvercamApiHelper;
import io.evercam.network.discovery.DiscoveredCamera;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class ScanActivity extends Activity
{
	private View scanProgressView;
	private View scanResultListView;
	private View scanResultNoCameraView;

	private ListView cameraListView;

	private ArrayList<HashMap<String, Object>> deviceArrayList;
	private SimpleAdapter deviceAdapter;
	private ArrayList<DiscoveredCamera> discoveredCameras;

	private final String ADAPTER_KEY_IP = "camera_id";
	private final String ADAPTER_KEY_MODEL = "camera_model";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);

		scanProgressView = findViewById(R.id.scan_status_layout);
		scanResultListView = findViewById(R.id.scan_result_layout);
		scanResultNoCameraView = findViewById(R.id.scan_result_no_camera_layout);

		cameraListView = (ListView) findViewById(R.id.scan_result_list);
		Button addManuallyButton = (Button) findViewById(R.id.button_add_camera_manually);

		deviceArrayList = new ArrayList<HashMap<String, Object>>();
		deviceAdapter = new SimpleAdapter(this, deviceArrayList, R.layout.scan_list_layout,
				new String[] { ADAPTER_KEY_IP, ADAPTER_KEY_MODEL }, new int[] { R.id.camera_ip,
						R.id.camera_model });
		cameraListView.setAdapter(deviceAdapter);

		cameraListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
			{
				@SuppressWarnings("unchecked")
				HashMap<String, Object> map = (HashMap<String, Object>) cameraListView
						.getItemAtPosition(position);
				String cameraIpText = (String) map.get(ADAPTER_KEY_IP);
				String cameraIp = cameraIpText.substring(0,cameraIpText.indexOf(":")-1);
				for (DiscoveredCamera camera : discoveredCameras)
				{
					if (camera.getIP().equals(cameraIp))
					{
						Intent intentAddCamera = new Intent(ScanActivity.this,
								AddCameraActivity.class);
						intentAddCamera.putExtra("camera", camera);
						startActivityForResult(intentAddCamera, Constants.REQUEST_CODE_ADD_CAMERA);
					}
				}
			}
		});

		addManuallyButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v)
			{
				startActivityForResult(new Intent(ScanActivity.this, AddCameraActivity.class),
						Constants.REQUEST_CODE_ADD_CAMERA);
			}
		});

		startDiscovery();
	}

	// Finish this activity and transfer the result from AddCameraActivity to
	// CamerasActivity.
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == Constants.REQUEST_CODE_ADD_CAMERA)
		{
			Intent returnIntent = new Intent();

			if (resultCode == Constants.RESULT_TRUE)
			{
				setResult(Constants.RESULT_TRUE, returnIntent);
				finish();
			}
			else
			{
				setResult(Constants.RESULT_FALSE, returnIntent);
				// If no camera found, finish this page, otherwise stay in
				// scanning page to let the user choose another.
				if (discoveredCameras == null)
				{
					finish();
				}
			}
		}
	}

	private void startDiscovery()
	{
		EvercamApiHelper.setEvercamDeveloperKeypair(this);
		new ScanForCameraTask(ScanActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public void showProgress(boolean show)
	{
		scanProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	public void showCameraListView(boolean show)
	{
		scanResultListView.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	public void showNoCameraView(boolean show)
	{
		scanResultNoCameraView.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	public void showScanResults(ArrayList<DiscoveredCamera> discoveredCameras)
	{
		if (discoveredCameras != null)
		{
			showCameraListView(true);
			showNoCameraView(false);
			this.discoveredCameras = discoveredCameras;
			for (DiscoveredCamera camera : discoveredCameras)
			{
				HashMap<String, Object> deviceMap = new HashMap<String, Object>();
				String ipTextShowing = camera.getIP();
				if(camera.hasHTTP())
				{
					ipTextShowing = ipTextShowing + ":" + camera.getHttp();
				}
				deviceMap.put(ADAPTER_KEY_IP, ipTextShowing);

				String vendor = camera.getVendor().toUpperCase(Locale.UK);

				if (camera.hasModel())
				{
					String model = camera.getModel().toUpperCase(Locale.UK);
					if (model.startsWith(vendor))
					{
						deviceMap.put(ADAPTER_KEY_MODEL, model);
					}
					else
					{
						deviceMap.put(ADAPTER_KEY_MODEL, vendor + " " + model);
					}
				}
				else
				{
					deviceMap.put(ADAPTER_KEY_MODEL, vendor);
				}
				deviceArrayList.add(deviceMap);
				deviceAdapter.notifyDataSetChanged();
			}
		}
		else
		{
			showCameraListView(false);
			showNoCameraView(true);
		}
	}
}
