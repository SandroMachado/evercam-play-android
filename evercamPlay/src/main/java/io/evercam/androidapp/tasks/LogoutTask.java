package io.evercam.androidapp.tasks;

import io.evercam.androidapp.CamerasActivity;
import io.evercam.androidapp.MainActivity;
import io.evercam.androidapp.dal.DbAppUser;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.utils.PrefsManager;

import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class LogoutTask extends AsyncTask<String, String, String>
{
	private final String TAG = "evercamplay-LogoutTask";
	private CamerasActivity cameraActivity;

	public LogoutTask(CamerasActivity cameraActivity)
	{
		this.cameraActivity = cameraActivity;
	}

	@Override
	protected String doInBackground(String... params)
	{
		try
		{
			// delete saved username and password
			SharedPreferences sharedPrefs = PreferenceManager
					.getDefaultSharedPreferences(cameraActivity);
			PrefsManager.removeUserEmail(sharedPrefs);

			// clear realtime default app data
			AppData.defaultUser = null;
			AppData.evercamCameraList.clear();

			// delete app user
			DbAppUser dbUser = new DbAppUser(cameraActivity);
			List<AppUser> list = dbUser.getAllAppUsers(10000);

			if (list != null && list.size() > 0)
			{
				for (AppUser user : list)
				{
					Log.v(TAG, "delete user ");
					dbUser.deleteAppUserByEmail(user.getEmail());
				}
			}
		}
		catch (Exception e)
		{
			Log.e(TAG, "Error: delete user" + e.toString());
		}
		return null;
	}

	@Override
	protected void onPostExecute(String result)
	{
		cameraActivity.startActivity(new Intent(cameraActivity, MainActivity.class));
	}
}