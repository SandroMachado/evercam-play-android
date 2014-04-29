package io.evercam.androidapp.tasks;

import io.evercam.androidapp.CamerasActivity;
import io.evercam.androidapp.MainActivity;
import io.evercam.androidapp.dal.DbAppUser;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.utils.AppData;
import io.evercam.androidapp.utils.PrefsManager;

import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.google.android.gcm.GCMRegistrar;

public class LogoutTask extends AsyncTask<String, String, String>
{
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
			// delete saved username and apssword
			SharedPreferences sharedPrefs = PreferenceManager
					.getDefaultSharedPreferences(cameraActivity);
			PrefsManager.removeUserEmail(sharedPrefs);

			// clear realtime default app data
			AppData.defaultUser = null;
			AppData.evercamCameraList.clear();

			// Un register from gcm server
			GCMRegistrar.setRegisteredOnServer(cameraActivity, false);

			// delete app user
			DbAppUser dbUser = new DbAppUser(cameraActivity);
			List<AppUser> list = dbUser.getAllAppUsers(10000);
			if (list != null && list.size() > 0)
			{
				for (AppUser user : list)
				{
					dbUser.deleteAppUserByEmail(user.getEmail());
				}
			}

			// unregister all users
			if (list != null && list.size() > 0)
			{
				// get information to be posted for unregister on camba
				// server request
				String regId = GCMRegistrar.getRegistrationId(cameraActivity);
				String AppUserEmail = null;
				String AppUserPassword = null;
				String Operation = null;
				String Manufacturer = null;
				String Model = null;
				String SerialNo = null;
				String ImeiNo = null;
				String Fingureprint = null;
				String MacAddress = null;
				String BlueToothName = null;
				String AppVersion = null;

				try
				{

					AppUserPassword = sharedPrefs.getString("AppUserPassword", null);
					Operation = "2";
					Manufacturer = android.os.Build.MANUFACTURER;
					Model = android.os.Build.MODEL;
					SerialNo = android.os.Build.SERIAL;
					ImeiNo = ((android.telephony.TelephonyManager) cameraActivity
							.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
					Fingureprint = android.os.Build.FINGERPRINT;
					WifiManager manager = (WifiManager) cameraActivity
							.getSystemService(Context.WIFI_SERVICE);
					WifiInfo info = manager.getConnectionInfo();
					MacAddress = info.getMacAddress();
					BlueToothName = BluetoothAdapter.getDefaultAdapter().getName();
					AppVersion = (cameraActivity.getPackageManager().getPackageInfo(
							cameraActivity.getPackageName(), 0)).versionName;
				}
				catch (Exception ee)
				{
				}

				for (AppUser user : list)
				{
					dbUser.deleteAppUserByEmail(user.getEmail());
					AppUserEmail = user.getEmail();
					try
					{
						// CambaApiManager.registerDeviceForUsername(AppUserEmail,
						// AppUserPassword, regId, Operation, BlueToothName,
						// Manufacturer,
						// Model, SerialNo, ImeiNo, Fingureprint, MacAddress,
						// AppVersion);
					}
					catch (Exception e)
					{
					}
				}
			}
		}
		catch (Exception eee)
		{
		}
		return null;
	}

	@Override
	protected void onPostExecute(String result)
	{
		cameraActivity.startActivity(new Intent(cameraActivity, MainActivity.class));
	}
}