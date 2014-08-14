package io.evercam.androidapp.tasks;

import java.util.HashMap;
import com.bugsense.trace.BugSenseHandler;

import io.evercam.Camera;
import io.evercam.CameraDetail;
import io.evercam.EvercamException;
import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.CustomToast;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class AddCameraTask extends AsyncTask<Void, Void, Boolean>
{
	private final String TAG = "evercamplay-AddCameraTask";
	private CameraDetail cameraDetail;
	private Activity activity;
	private CustomProgressDialog customProgressDialog;
	private String errorMessage;

	public AddCameraTask(CameraDetail cameraDetail, Activity activity)
	{
		this.cameraDetail = cameraDetail;
		this.activity = activity;
	}

	@Override
	protected void onPreExecute()
	{
		errorMessage = activity.getString(R.string.unknown_error);
		customProgressDialog = new CustomProgressDialog(activity);
		customProgressDialog.show(activity.getString(R.string.creating_camera));
	}

	@Override
	protected void onPostExecute(Boolean success)
	{
		customProgressDialog.dismiss();
		if(success)
		{
			CustomToast.showInCenter(activity, activity.getString(R.string.create_success));
		}
		else
		{
			CustomToast.showInCenterLong(activity, errorMessage);
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
			Camera camera = Camera.create(detail);

			return true;
		}
		catch (EvercamException e)
		{
			errorMessage = e.getMessage();
			Log.e(TAG, "add camera to evercam: " + e.getMessage());
			return false;
		}
	}
}
