package io.evercam.android.tasks;

import java.util.ArrayList;

import io.evercam.API;
import io.evercam.Camera;
import io.evercam.EvercamException;
import io.evercam.User;
import io.evercam.android.CamerasActivity;
import io.evercam.android.dto.AppUser;
import io.evercam.android.dto.EvercamCamera;
import io.evercam.android.utils.AppData;
import android.os.AsyncTask;
import android.util.Log;

public class LoadCameraListTask extends AsyncTask<Void, Void,Boolean>
{
	private AppUser user;
	private CamerasActivity camerasActivity;

	public LoadCameraListTask(AppUser user, CamerasActivity camerasActivity)
	{
		this.user = user;
		this.camerasActivity = camerasActivity;
	}

	@Override
	protected void onPreExecute()
	{
		API.setUserKeyPair(user.getApiKey(), user.getApiId());
	}

	@Override
	protected Boolean doInBackground(Void... params)
	{
		try
		{
			ArrayList<Camera> cameras = User.getCameras(user.getUsername());
			ArrayList<EvercamCamera> evercamCameras = new ArrayList<EvercamCamera>();
			for(io.evercam.Camera camera : cameras)
			{
				evercamCameras.add(new EvercamCamera(camera));
			}
			AppData.evercamCameraList = evercamCameras;
			return true;
		}
		catch (EvercamException e)
		{
			Log.e("evercamapp", "LoadCameraList: " + e.getMessage());
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean success)
	{
		if(success)
		{
			camerasActivity.addAllCameraViews(true);
		}
		else
		{
			
		}
	}
}
