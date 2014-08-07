package io.evercam.androidapp;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

import io.evercam.CameraBuilder;
import io.evercam.CameraDetail;
import io.evercam.EvercamException;
import io.evercam.Model;
import io.evercam.Vendor;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;

import com.bugsense.trace.BugSenseHandler;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
	private Button addButton;
	private TreeMap<String, String> vendorMap;
	private TreeMap<String, String> modelMap;
	
	//User's input
	private String cameraId;
	private String cameraName;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.initAndStartSession(this, Constants.bugsense_ApiKey);
		}

		EvercamPlayApplication.sendScreenAnalytics(this, getString(R.string.screen_add_camera));
		
		setContentView(R.layout.activity_add_camera);
		
		//Initial UI elements
		initialScreen();
		
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
		cameraIdEdit = (EditText)findViewById(R.id.add_id_edit);
		cameraNameEdit = (EditText)findViewById(R.id.add_name_edit);
		vendorSpinner = (Spinner) findViewById(R.id.vendor_spinner);
		modelSpinner = (Spinner) findViewById(R.id.model_spinner);
		addButton = (Button)findViewById(R.id.button_add_camera);
		
		new RequestVendorListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		addButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v)
			{
				if(launchLocalCheck())
				{
					
				}
				else
				{
					
				}
			}
		});
	}
	
	/**
	 * Read and validate user input from user interface.
	 */
	private boolean launchLocalCheck()
	{
		cameraId = cameraIdEdit.getText().toString();
		cameraName = cameraNameEdit.getText().toString();
		return true;
	}
	
	/**
	 * Build camera detail object for creating camera API request.
	 * Return null if EvercamException occurred
	 */
	private CameraDetail buildCamera()
	{
		CameraBuilder cameraBuilder = null;
		try
		{
			cameraBuilder = new CameraBuilder(cameraId, cameraName,false);
		}
		catch (EvercamException e)
		{
			Log.e(TAG, e.toString());
		}
		return cameraBuilder.build();
	}
	
	private void buildVendorSpinner(ArrayList<Vendor> vendorList)
	{
		vendorMap = new TreeMap<String, String>();

		for(Vendor vendor : vendorList)
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

		Set<String> set = vendorMap.keySet();
		String[] vendorArray = Commons.joinStringArray(
				new String[] { getResources().getString(R.string.select_vendor) },
				set.toArray(new String[0]));
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, vendorArray);
		spinnerArrayAdapter.setDropDownViewResource(R.layout.country_spinner);
		vendorSpinner.setAdapter(spinnerArrayAdapter);
	}
	
	private void buildModelSpinner(ArrayList<Model> modelList)
	{
		modelMap = new TreeMap<String, String>();

		for(Model model: modelList)
		{
			try
			{
				modelMap.put(model.getName(), model.getName());
			}
			catch (EvercamException e)
			{
				Log.e(TAG, e.toString());
			}
		}

		Set<String> set = modelMap.keySet();
		String[] vendorArray = Commons.joinStringArray(
				new String[] { getResources().getString(R.string.select_model) },
				set.toArray(new String[0]));
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, vendorArray);
		spinnerArrayAdapter.setDropDownViewResource(R.layout.country_spinner);
		modelSpinner.setAdapter(spinnerArrayAdapter);
	}
	
	class RequestVendorListTask extends AsyncTask<Void, Void, ArrayList<Vendor>>
	{
		
		@Override
		protected void onPostExecute(ArrayList<Vendor> vendorList)
		{
			if(vendorList != null)
			{
				buildVendorSpinner(vendorList);
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
	
	class RequestModelListTask extends AsyncTask<Void, Void, ArrayList<Model>>
	{
		private String vendorId;
		
		public RequestModelListTask(String vendorId)
		{
			this.vendorId = vendorId;
		}
		
		@Override
		protected ArrayList<Model> doInBackground(Void... params)
		{
			//FIXME: Request model list from Evercam.
//			try
//			{
//				
//				return Vendor.getById(vendorId).
//			}
//			catch (EvercamException e)
//			{
//				Log.e(TAG, e.toString());
//			}
			return null;
		}
		
		@Override
		protected void onPostExecute(ArrayList<Model> modelList)
		{
			if(modelList != null)
			{
				buildModelSpinner(modelList);
			}
		}
	}
}
