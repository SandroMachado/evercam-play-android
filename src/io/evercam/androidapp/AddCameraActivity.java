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
import io.evercam.Vendor;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.tasks.AddCameraTask;
import io.evercam.androidapp.tasks.TestSnapshotTask;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.EvercamApiHelper;
import io.evercam.network.camera.DiscoveredCamera;

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
import android.view.View;
import android.view.View.OnClickListener;

public class AddCameraActivity extends Activity
{
	private final String TAG = "evercamplay-AddCameraActivity";
	private EditText cameraIdEdit;
	private EditText cameraNameEdit;
	private Spinner vendorSpinner;
	private Spinner modelSpinner;
	private EditText usernameEdit;
	private EditText passwordEdit;
	private EditText externalHostEdit;
	private EditText externalHttpEdit;
	private EditText externalRtspEdit;
	private EditText jpgUrlEdit;
	private Button addButton;
	private Button testButton;
	private TreeMap<String, String> vendorMap;
	private TreeMap<String, String> modelMap;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.initAndStartSession(this, Constants.bugsense_ApiKey);
		}

		EvercamApiHelper.setEvercamDeveloperKeypair(this);

		EvercamPlayApplication.sendScreenAnalytics(this, getString(R.string.screen_add_camera));

		setContentView(R.layout.activity_add_camera);

		// Initial UI elements
		initialScreen();
		
		DiscoveredCamera camera = (DiscoveredCamera) getIntent().getSerializableExtra("camera");
		if(camera != null)
		{
			Log.d(TAG, camera.toString());
			//FIXME: fill discovered details
		}
		else
		{
			Log.d(TAG, "transfered camera is null");
		}

	}

	@Override
	public void onStart()
	{
		super.onStart();

		if (Constants.isAppTrackingEnabled)
		{
			if (Constants.isAppTrackingEnabled) BugSenseHandler.startSession(this);
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();

		if (Constants.isAppTrackingEnabled)
		{
			if (Constants.isAppTrackingEnabled) BugSenseHandler.closeSession(this);
		}
	}

	private void initialScreen()
	{
		cameraIdEdit = (EditText) findViewById(R.id.add_id_edit);
		cameraNameEdit = (EditText) findViewById(R.id.add_name_edit);
		vendorSpinner = (Spinner) findViewById(R.id.vendor_spinner);
		modelSpinner = (Spinner) findViewById(R.id.model_spinner);
		usernameEdit = (EditText) findViewById(R.id.add_username_edit);
		passwordEdit = (EditText) findViewById(R.id.add_password_edit);
		externalHostEdit = (EditText) findViewById(R.id.add_external_host_edit);
		externalHttpEdit = (EditText) findViewById(R.id.add_external_http_edit);
		externalRtspEdit = (EditText) findViewById(R.id.add_external_rtsp_edit);
		jpgUrlEdit = (EditText) findViewById(R.id.add_jpg_edit);
		addButton = (Button) findViewById(R.id.button_add_camera);
		testButton = (Button) findViewById(R.id.button_test_snapshot);

		buildVendorSpinner(null);
		buildModelSpinner(null);

		new RequestVendorListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		vendorSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
					int position, long id)
			{
				if (position == 0)
				{
					buildModelSpinner(new ArrayList<String>());
				}
				else
				{
					String vendorName = vendorSpinner.getSelectedItem().toString();
					String vendorId = vendorMap.get(vendorName).toLowerCase(Locale.UK);

					if (!vendorName.equals(getString(R.string.vendor_other)))
					{
						new RequestModelListTask(vendorId)
								.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					}
					else
					{
						modelSpinner.setEnabled(false);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
			}
		});

		modelSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
					int position, long id)
			{

				if (position == 0)
				{
					clearDefaults();
				}
				else
				{
					String vendorId = getVendorIdFromSpinner();
					String modelId = getModelIdFromSpinner();

					new RequestDefaultsTask(vendorId, modelId)
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
			}
		});
		addButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v)
			{
				CameraBuilder cameraBuilder = buildCameraWithLocalCheck();
				if (cameraBuilder != null)
				{
					new AddCameraTask(cameraBuilder.build(), AddCameraActivity.this)
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
				else
				{
					Log.e(TAG, "Camera is null");
				}
			}
		});
		
		testButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v)
			{
				launchTestSnapshot();
			}
		});
	}

	/**
	 * Read and validate user input from user interface.
	 */
	private CameraBuilder buildCameraWithLocalCheck()
	{
		CameraBuilder cameraBuilder = null;
		String cameraId = cameraIdEdit.getText().toString();

		String cameraName = cameraNameEdit.getText().toString();

		if (cameraId.isEmpty())
		{
			CustomToast.showInCenter(this, getString(R.string.id_required));
			return null;
		}
		if (cameraName.isEmpty())
		{
			CustomToast.showInCenter(this, getString(R.string.name_required));
			return null;
		}
		try
		{
			cameraBuilder = new CameraBuilder(cameraId, cameraName, false);
		}
		catch (EvercamException e)
		{
			Log.e(TAG, e.toString());
		}

		String vendorId = getVendorIdFromSpinner();
		if (!vendorId.isEmpty())
		{
			cameraBuilder.setVendor(vendorId);
		}

		String modelId = getModelIdFromSpinner();
		if (!modelId.isEmpty())
		{
			cameraBuilder.setModel(modelId);
		}

		String username = usernameEdit.getText().toString();
		if (!username.isEmpty())
		{
			cameraBuilder.setCameraUsername(username);
		}

		String password = passwordEdit.getText().toString();
		if (!password.isEmpty())
		{
			cameraBuilder.setCameraPassword(password);
		}

		String externalHost = externalHostEdit.getText().toString();
		if(externalHost.isEmpty())
		{
			CustomToast.showInCenter(this, getString(R.string.host_required));
			return null;
		}
		else
		{
			cameraBuilder.setExternalHost(externalHost);
		}

		String externalHttp = externalHttpEdit.getText().toString();
		if (!externalHttp.isEmpty())
		{
			int externalHttpInt = Integer.valueOf(externalHttp);
			cameraBuilder.setExternalHttpPort(externalHttpInt);
		}

		String externalRtsp = externalRtspEdit.getText().toString();
		if (!externalRtsp.isEmpty())
		{
			int externalRtspInt = Integer.valueOf(externalRtsp);
			cameraBuilder.setExternalRtspPort(externalRtspInt);
		}

		return cameraBuilder;
	}

	private void buildVendorSpinner(ArrayList<Vendor> vendorList)
	{
		if (vendorMap == null)
		{
			vendorMap = new TreeMap<String, String>();
		}

		if (vendorList != null)
		{
			for (Vendor vendor : vendorList)
			{
				try
				{
					vendorMap.put(vendor.getName(), vendor.getId());
				}
				catch (EvercamException e)
				{
					Log.e(TAG, e.toString());
				}
			}
		}

		Set<String> set = vendorMap.keySet();
		String[] vendorArray = Commons.joinStringArray(
				new String[] { getResources().getString(R.string.select_vendor) },
				set.toArray(new String[0]));
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, vendorArray);
		spinnerArrayAdapter.setDropDownViewResource(R.layout.country_spinner);
		vendorSpinner.setAdapter(spinnerArrayAdapter);
	}

	private void buildModelSpinner(ArrayList<String> modelList)
	{
		if (modelMap == null)
		{
			modelMap = new TreeMap<String, String>();
		}

		if (modelList == null)
		{
			modelSpinner.setEnabled(false);
		}
		else
		{
			if (modelList.size() == 0)
			{
				modelSpinner.setEnabled(false);
			}
			else
			{
				modelSpinner.setEnabled(true);
			}

			for (String modelId : modelList)
			{
				if (modelId.equals("*"))
				{
					modelMap.put(getString(R.string.model_default), modelId);
				}
				else
				{
					modelMap.put(modelId, modelId);
				}
			}
		}
		Set<String> set = modelMap.keySet();
		String[] fullModelArray = Commons.joinStringArray(
				new String[] { getResources().getString(R.string.select_model) },
				set.toArray(new String[0]));
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, fullModelArray);
		spinnerArrayAdapter.setDropDownViewResource(R.layout.country_spinner);
		modelSpinner.setAdapter(spinnerArrayAdapter);
	}

	private void fillDefaults(Model model)
	{
		try
		{
			// FIXME: Sometimes vendor with no default model, contains default
			// jpg url.
			// FIXME: Consider if no default values associated, clear defaults
			// that has been filled.
			Defaults defaults = model.getDefaults();
			Auth basicAuth = defaults.getAuth(Auth.TYPE_BASIC);
			usernameEdit.setText(basicAuth.getUsername());
			passwordEdit.setText(basicAuth.getPassword());
			jpgUrlEdit.setText(defaults.getJpgURL());
		}
		catch (EvercamException e)
		{
			Log.e(TAG, "Fill defaults: " + e.toString());
		}
	}

	private void clearDefaults()
	{
		usernameEdit.setText("");
		passwordEdit.setText("");
		jpgUrlEdit.setText("");
	}

	private String getVendorIdFromSpinner()
	{
		String vendorName = vendorSpinner.getSelectedItem().toString();
		if (vendorName.equals(getString(R.string.select_vendor)))
		{
			return "";
		}
		else
		{
			return vendorMap.get(vendorName).toLowerCase(Locale.UK);
		}

	}

	private String getModelIdFromSpinner()
	{
		String modelName = modelSpinner.getSelectedItem().toString();
		if (modelName.equals(getString(R.string.select_model)))
		{
			return "";
		}
		else
		{
			return modelName;
		}
	}
	
	private void launchTestSnapshot()
	{
		String username = usernameEdit.getText().toString();
		String password = passwordEdit.getText().toString();


		String externalHost = externalHostEdit.getText().toString();
		if(externalHost.isEmpty())
		{
			CustomToast.showInCenter(this, getString(R.string.host_required));
			return;
		}
		
		String externalHttp = externalHttpEdit.getText().toString();
		if (externalHttp.isEmpty())
		{
			CustomToast.showInCenter(this, getString(R.string.external_http_required));
			return;
		}
		
		String jpgUrl = jpgUrlEdit.getText().toString();
		if(!jpgUrl.startsWith("/"))
		{
			jpgUrl = "/" + jpgUrl;
		}

		String url = getString(R.string.prefix_http) + externalHost + ":" + externalHttp + jpgUrl;
		new TestSnapshotTask(url,username,password,this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	class RequestVendorListTask extends AsyncTask<Void, Void, ArrayList<Vendor>>
	{

		@Override
		protected void onPostExecute(ArrayList<Vendor> vendorList)
		{
			if (vendorList != null)
			{
				buildVendorSpinner(vendorList);
			}
			else
			{
				Log.e(TAG, "Vendor list is null");
			}
		}

		@Override
		protected ArrayList<Vendor> doInBackground(Void... params)
		{
			try
			{
				return Vendor.getAll();
			}
			catch (EvercamException e)
			{
				Log.e(TAG, e.toString());
			}
			return null;
		}
	}

	class RequestModelListTask extends AsyncTask<Void, Void, ArrayList<String>>
	{
		private String vendorId;

		public RequestModelListTask(String vendorId)
		{
			this.vendorId = vendorId;
		}

		@Override
		protected ArrayList<String> doInBackground(Void... params)
		{
			try
			{

				Vendor vendor = Vendor.getById(vendorId);
				return vendor.getModelNames();
			}
			catch (EvercamException e)
			{
				Log.e(TAG, e.toString());
			}
			return null;
		}

		@Override
		protected void onPostExecute(ArrayList<String> modelList)
		{
			if (modelList != null)
			{
				buildModelSpinner(modelList);
			}
		}
	}

	class RequestDefaultsTask extends AsyncTask<Void, Void, Model>
	{
		private String vendorId;
		private String modelId;

		public RequestDefaultsTask(String vendorId, String modelId)
		{
			this.vendorId = vendorId;
			this.modelId = modelId;
		}

		@Override
		protected void onPreExecute()
		{
			clearDefaults();
		}

		@Override
		protected Model doInBackground(Void... params)
		{
			try
			{
				return Model.getModel(vendorId, modelId);
			}
			catch (EvercamException e)
			{
				Log.e(TAG, e.toString());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Model model)
		{
			if (model != null)
			{
				fillDefaults(model);
			}
		}
	}
}
