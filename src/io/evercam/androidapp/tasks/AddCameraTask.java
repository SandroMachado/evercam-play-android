package io.evercam.androidapp.tasks;

import java.util.HashMap;
import com.bugsense.trace.BugSenseHandler;

import io.evercam.Camera;
import io.evercam.CameraDetail;
import io.evercam.EvercamException;
import android.os.AsyncTask;
import android.util.Log;

public class AddCameraTask extends AsyncTask<Void, Void, Boolean>
{
	private final String TAG = "evercamplay-AddCameraTask";
	private CameraDetail cameraDetail;

	public AddCameraTask(CameraDetail cameraDetail)
	{
		this.cameraDetail = cameraDetail;
	}

	@Override
	protected void onPreExecute()
	{

	}

	@Override
	protected void onPostExecute(Boolean success)
	{

	}

	@Override
	protected Boolean doInBackground(Void... params)
	{
		createCamera(cameraDetail);
		return false;
	}

	private boolean createCamera(CameraDetail detail)
	{
		try
		{
			Camera camera = Camera.create(detail);
			return true;
		}
		catch (EvercamException e)
		{
			Log.e(TAG, "add camera to evercam: " + e.getMessage());
			sendBugReportWithCameraDetails(e, detail);
			return false;
		}
	}

	private void sendBugReportWithCameraDetails(EvercamException e, CameraDetail detail)
	{
		HashMap<String, String> errorDetails = new HashMap<String, String>();
		errorDetails.put("Camera timezone:", detail.getTimezone());
		errorDetails.put("Camera id", detail.getId());
		errorDetails.put("Camera mac address", detail.getMacAddress());
		BugSenseHandler.sendExceptionMap(errorDetails, e);
	}
}
