package io.evercam.androidapp.tasks;

import java.util.ArrayList;
import java.util.Iterator;

import io.evercam.API;
import io.evercam.Camera;
import io.evercam.EvercamException;
import io.evercam.User;
import io.evercam.androidapp.CamerasActivity;
import io.evercam.androidapp.MainActivity;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.dal.DbCamera;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.utils.AppData;
import io.evercam.androidapp.utils.PrefsManager;
import io.evercam.androidapp.utils.CustomedDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class LoadCameraListTask extends AsyncTask<Void, Void, Boolean>
{
	private AppUser user;
	private CamerasActivity camerasActivity;
	private String TAG = "evercamplay-LoadCameraListTask";
	public boolean reload = false;
	private CustomProgressDialog customProgressDialog;

	public LoadCameraListTask(AppUser user, CamerasActivity camerasActivity)
	{
		this.user = user;
		this.camerasActivity = camerasActivity;
	}

	@Override
	protected void onPreExecute()
	{
//		customProgressDialog = new CustomProgressDialog(camerasActivity);
//		customProgressDialog.show(camerasActivity.getString(R.string.loading_cameras));
		API.setUserKeyPair(user.getApiKey(), user.getApiId());
	}

	@Override
	protected Boolean doInBackground(Void... params)
	{
		try
		{
			boolean updateDB = false;

			// Step 1: Load camera list from Evercam
			Log.d(TAG, "Step 1: Load camera list from Evercam");
			// FIXME: Time consuming at this line
			ArrayList<Camera> cameras = User.getCameras(user.getUsername(), true);
			ArrayList<EvercamCamera> evercamCameras = new ArrayList<EvercamCamera>();

			for (io.evercam.Camera camera : cameras)
			{
				evercamCameras.add(new EvercamCamera().convertFromEvercam(camera));
			}

			// Step 2: Check if any new cameras different from local saved
			// cameras.
			Log.d(TAG, "Step 2: Check if any new cameras different from local saved cameras.");
			for (EvercamCamera camera : evercamCameras)
			{
				if (!AppData.evercamCameraList.contains(camera))
				{
					updateDB = true;
					break;
				}
			}

			// Step 3: Check if any local camera no longer exists in Evercam
			Log.d(TAG, "Step 3: Check if any local camera no longer exists in Evercam");
			if (!updateDB)
			{
				for (EvercamCamera camera : AppData.evercamCameraList)
				{
					if (!evercamCameras.contains(camera))
					{
						updateDB = true;
						break;
					}
				}
			}

			// Step 4: If any different camera, replace all local camera data.
			Log.d(TAG, "Step 4: If any different camera, replace all local camera data.");
			if (updateDB)
			{
				reload = true;

				AppData.evercamCameraList = evercamCameras;
				DbCamera dbCamera = new DbCamera(camerasActivity);
				dbCamera.deleteCameraByOwner(user.getUsername());

				Iterator<EvercamCamera> iterator = AppData.evercamCameraList.iterator();
				while (iterator.hasNext())
				{
					dbCamera.addCamera(iterator.next());
				}
			}

			return true;
		}
		catch (EvercamException e)
		{
			Log.e(TAG, e.getMessage());
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean success)
	{
		Log.d(TAG, "Done");
	//	customProgressDialog.dismiss();
		if (success)
		{
			if (reload)
			{
				camerasActivity.removeAllCameraViews();
				camerasActivity.addAllCameraViews(true);
			}
		}
		else
		{
			if (!camerasActivity.isFinishing())
			{
				CustomedDialog.getAlertDialog(camerasActivity, "Error Occured", "",
						new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								dialog.dismiss();
								SharedPreferences sharedPrefs = PreferenceManager
										.getDefaultSharedPreferences(camerasActivity);
								PrefsManager.removeUserEmail(sharedPrefs);

								camerasActivity.startActivity(new Intent(camerasActivity,
										MainActivity.class));
								new LogoutTask(camerasActivity)
										.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

								if (camerasActivity.refresh != null)
								{
									camerasActivity.refresh.setActionView(null);
								}
							}
						}).show();
			}
		}
	}
}
