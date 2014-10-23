package io.evercam.androidapp.tasks;

import io.evercam.androidapp.ScanActivity;
import io.evercam.androidapp.utils.NetInfo;

import io.evercam.network.EvercamDiscover;
import io.evercam.network.discovery.DiscoveredCamera;
import io.evercam.network.discovery.ScanRange;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.util.Log;

public class ScanForCameraTask extends AsyncTask<Void, Void, ArrayList<DiscoveredCamera>>
{
	private final String TAG = "evercamplay-ScanForCameraTask";
	private ScanActivity scanActivity;
	private NetInfo netInfo;

	public ScanForCameraTask(ScanActivity scanActivity)
	{
		this.scanActivity = scanActivity;
		netInfo = new NetInfo(scanActivity);
	}

	@Override
	protected ArrayList<DiscoveredCamera> doInBackground(Void... params)
	{
		ArrayList<DiscoveredCamera> cameraList = null;
		try
		{
			EvercamDiscover evercamDiscover = new EvercamDiscover();
			ScanRange scanRange = new ScanRange(netInfo.getGatewayIp(), netInfo.getNetmaskIp());
			cameraList = evercamDiscover.discoverAllAndroid(scanRange);
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getLocalizedMessage());
			e.printStackTrace();
		}

		return cameraList;
	}

	@Override
	protected void onPostExecute(ArrayList<DiscoveredCamera> cameraList)
	{
		scanActivity.showProgress(false);

		scanActivity.showScanResults(cameraList);
	}
}
