package io.evercam.androidapp.tasks;

import io.evercam.androidapp.ScanActivity;
import io.evercam.network.camera.DiscoveredCamera;
import io.evercam.network.ipscan.EvercamDiscover;
import io.evercam.network.ipscan.ScanRange;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.util.Log;

public class ScanForCameraTask extends AsyncTask<Void, Void, ArrayList<DiscoveredCamera>>
{
	private final String TAG = "evercamplay-ScanForCameraTask";
	private ScanActivity scanActivity;

	public ScanForCameraTask(ScanActivity scanActivity)
	{
		this.scanActivity = scanActivity;
	}
	
	@Override
	protected void onPreExecute()
	{
		
	}

	@Override
	protected ArrayList<DiscoveredCamera> doInBackground(Void... params)
	{
		ArrayList<DiscoveredCamera> cameraList = null;
		try
		{
			EvercamDiscover evercamDiscover = new EvercamDiscover();
			ScanRange scanRange = new ScanRange("192.168.1.122", "255.255.255.0");
			cameraList = evercamDiscover.discoverAllCamerasAndroid(scanRange);
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString());
		}
		return cameraList;
	}

	@Override
	protected void onPostExecute(ArrayList<DiscoveredCamera> cameraList)
	{
		scanActivity.showResult(true);
		
		if (cameraList != null)
		{
			for (DiscoveredCamera camera : cameraList)
			{
				Log.d(TAG, camera.toString());
			}
		}
		else
		{
			Log.e(TAG, "Camera list is null");
		}
	}
}
