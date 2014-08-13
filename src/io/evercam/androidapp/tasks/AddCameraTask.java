package io.evercam.androidapp.tasks;

import java.util.HashMap;
import com.bugsense.trace.BugSenseHandler;

import io.evercam.Camera;
import io.evercam.CameraDetail;
import io.evercam.EvercamException;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.CustomToast;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class AddCameraTask extends AsyncTask<Void, Void, Boolean>
{
	private final String TAG = "evercamplay-AddCameraTask";
	private CameraDetail cameraDetail;
	private Activity activity;
	private CustomProgressDialog customProgressDialog;

	public AddCameraTask(CameraDetail cameraDetail, Activity activity)
	{
		this.cameraDetail = cameraDetail;
		this.activity = activity;
	}

	@Override
	protected void onPreExecute()
	{
		customProgressDialog = new CustomProgressDialog(activity);
		customProgressDialog.show("starts");
	}

	@Override
	protected void onPostExecute(Boolean success)
	{
		customProgressDialog.dismiss();
		if(success)
		{
			CustomToast.showInCenter(activity, "success");
		}
		else
		{
			CustomToast.showInCenter(activity, "error");
		}
	}

	@Override
	protected Boolean doInBackground(Void... params)
	{
		return createCamera(cameraDetail);
	}

	private boolean createCamera(CameraDetail detail)
	{
		try
		{
			Log.d(TAG, "Creating camera" + detail.toString());
			Camera camera = Camera.create(detail);
			Log.d(TAG, "After creating camera");
			if(camera!=null)
			{
			Log.d(TAG, camera.toString());
			}
			else
			{
				Log.d(TAG, "Camera is null");
			}
			return true;
		}
		catch (EvercamException e)
		{
			Log.e(TAG, "add camera to evercam: " + e.getMessage());
			//sendBugReportWithCameraDetails(e, detail);
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
