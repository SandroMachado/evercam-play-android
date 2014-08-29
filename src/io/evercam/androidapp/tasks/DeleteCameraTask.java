package io.evercam.androidapp.tasks;

import io.evercam.Camera;
import io.evercam.EvercamException;
import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.utils.Constants;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class DeleteCameraTask extends AsyncTask<Void, Void, Boolean>
{
	private final String TAG = "evercamplay-DeleteCameraTask";
	private String cameraId;
	private CustomProgressDialog customProgressDialog;
	private Activity activity;

	public DeleteCameraTask(String cameraId, Activity activity)
	{
		this.cameraId = cameraId;
		this.activity = activity;
	}

	@Override
	protected void onPreExecute()
	{
		customProgressDialog = new CustomProgressDialog(activity);
		customProgressDialog.show(activity.getString(R.string.deleting_camera));
	}

	@Override
	protected Boolean doInBackground(Void... params)
	{
		try
		{
			if (Camera.delete(cameraId))
			{
				return true;
			}
		}
		catch (EvercamException e)
		{
			Log.e(TAG, e.toString());
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean success)
	{
		customProgressDialog.dismiss();
		if (success)
		{
			CustomToast.showInBottom(activity, R.string.msg_delete_success);
			activity.setResult(Constants.RESULT_TRUE);
			activity.finish();
		}
		else
		{
			CustomToast.showInBottom(activity, R.string.msg_delete_failed);
		}
	}
}
