package io.evercam.androidapp.tasks;

import io.evercam.Camera;
import io.evercam.CameraDetail;
import io.evercam.EvercamException;
import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.dal.DbCamera;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.video.VideoActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class AddCameraTask extends AsyncTask<Void, Void, EvercamCamera>
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
	protected void onPostExecute(EvercamCamera evercamCamera)
	{
		customProgressDialog.dismiss();
		if (evercamCamera != null)
		{
			CustomToast.showInBottom(activity, R.string.create_success);

			/**
			 * Successfully added a camera, so refresh camera list.
			 */
			Intent returnIntent = new Intent();
			activity.setResult(Constants.RESULT_TRUE, returnIntent);
			// activity.finish();

			/**
			 * Successfully added camera, show camera live view and finish add
			 * camera activity
			 */
			VideoActivity.startPlayingVideoForCamera(activity, evercamCamera.getCameraId());
			activity.finish();
		}
		else
		{
			CustomToast.showInCenterLong(activity, errorMessage);
		}
	}

	@Override
	protected EvercamCamera doInBackground(Void... params)
	{
		return createCamera(cameraDetail);
	}

	private EvercamCamera createCamera(CameraDetail detail)
	{
		try
		{
			Camera camera = Camera.create(detail);
			EvercamCamera evercamCamera = new EvercamCamera().convertFromEvercam(camera);
			DbCamera dbCamera = new DbCamera(activity);
			dbCamera.addCamera(evercamCamera);
			AppData.evercamCameraList.add(evercamCamera);

			return evercamCamera;
		}
		catch (EvercamException e)
		{
			errorMessage = e.getMessage();
			Log.e(TAG, "add camera to evercam: " + e.getMessage());
			return null;
		}
	}
}
