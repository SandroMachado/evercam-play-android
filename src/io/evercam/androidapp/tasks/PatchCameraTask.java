package io.evercam.androidapp.tasks;

import io.evercam.Camera;
import io.evercam.CameraDetail;
import io.evercam.EvercamException;
import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.dal.DbCamera;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.utils.AppData;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class PatchCameraTask extends AsyncTask<Void, Void, EvercamCamera>
{
	private final String TAG = "evercamplay-PatchCameraTask";
	private CameraDetail cameraDetail;
	private Activity activity;
	private CustomProgressDialog customProgressDialog;
	private String errorMessage;
	
	public PatchCameraTask(CameraDetail cameraDetail, Activity activity)
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
	protected EvercamCamera doInBackground(Void... params)
	{
		Log.d(TAG, cameraDetail.toString());
		return patchCamera(cameraDetail);
	}
	
	@Override
	protected void onPostExecute(EvercamCamera evercamCamera)
	{
		customProgressDialog.dismiss();
		if(evercamCamera != null)
		{
			CustomToast.showInBottom(activity, R.string.patch_success);
			
			/**
			 * Successfully added a camera, so refresh camera list.
			 */
//			Intent returnIntent = new Intent();
//			activity.setResult(Constants.RESULT_TRUE,returnIntent);
//			activity.finish();
			
			/**
			 * Successfully added camera, show camera live view
			 * and finish add camera activity
			 */
//			VideoActivity.startPlayingVideoForCamera(activity,
//					evercamCamera.getCameraId());
//			activity.finish();
		}
		else
		{
			CustomToast.showInCenterLong(activity, errorMessage);
		}
	}
	
	private EvercamCamera patchCamera(CameraDetail detail)
	{
		try
		{
			Camera patchedCamera = Camera.patch(detail);
			if(patchedCamera != null)
			{
			EvercamCamera evercamCamera = new EvercamCamera().convertFromEvercam(patchedCamera);
			DbCamera dbCamera = new DbCamera(activity);
			dbCamera.deleteCamera(evercamCamera.getCameraId());
			for(int index = 0; index < AppData.evercamCameraList.size(); index ++ )
			{
				if(AppData.evercamCameraList.get(index).getCameraId().equals(patchedCamera.getId()))
				{
					AppData.evercamCameraList.remove(index);
				}
			}
			dbCamera.addCamera(evercamCamera);
			AppData.evercamCameraList.add(evercamCamera);

			return evercamCamera;
			}
			else
			{
				Log.e(TAG, "Returned patch camera is null");
				return null;
			}
		}
		catch (EvercamException e)
		{
			errorMessage = e.getMessage();
			Log.e(TAG, "add camera to evercam: " + e.getMessage());
			return null;
		}
	}
}
