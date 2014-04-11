/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.evercam.android;

import io.evercam.android.dal.dbNotifcation;
import io.evercam.android.dto.CameraNotification;
import io.evercam.android.utils.CLog;
import io.evercam.android.utils.CambaApiManager;
import io.evercam.android.utils.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import io.evercam.android.R;
import java.util.*;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService
{

	private static final String TAG = "GCMIntentService";

	public GCMIntentService()
	{
		super(Constants.GCM_SENDER_ID);
	}

	@Override
	protected void onRegistered(Context context, String registrationId)
	{
		try
		{
			Log.i(TAG, "Device registered: regId = " + registrationId);

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

				SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
				AppUserEmail = sharedPrefs.getString("AppUserEmail", null);
				AppUserPassword = sharedPrefs.getString("AppUserPassword", null);
				Operation = "1";
				Manufacturer = android.os.Build.MANUFACTURER;
				Model = android.os.Build.MODEL;
				SerialNo = android.os.Build.SERIAL;
				ImeiNo = ((android.telephony.TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
						.getDeviceId();
				Fingureprint = android.os.Build.FINGERPRINT;
				WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				WifiInfo info = manager.getConnectionInfo();
				MacAddress = info.getMacAddress();
				BlueToothName = BluetoothAdapter.getDefaultAdapter().getName();
				AppVersion = AppVersion = (getPackageManager().getPackageInfo(getPackageName(), 0)).versionName;
			}
			catch (Exception ee)
			{
			}

			if (AppUserEmail != null
					&& !AppUserEmail.equals("")
					&& CambaApiManager.registerDeviceForUsername(AppUserEmail, AppUserPassword,
							registrationId, Operation, BlueToothName, Manufacturer, Model,
							SerialNo, ImeiNo, Fingureprint, MacAddress, AppVersion))
			{
				GCMRegistrar.setRegisteredOnServer(context, true);
				Log.i(TAG, "GCM Device registration successful with id [" + registrationId + "]");

				String filename = Environment.getExternalStorageDirectory() + "/"
						+ "CambaGCMDeviceID" + ".txt";

				File file = new File(filename);
				if (file.exists()) file.delete();
				file.createNewFile();

				OutputStream os = new FileOutputStream(file);
				os.write(registrationId.getBytes());
				os.close();

				Log.i(TAG, "registration id [" + registrationId
						+ "] Successfully written to file [" + filename + "].");

			}
			else
			{
				GCMRegistrar.unregister(context);
				GCMRegistrar.setRegisteredOnServer(context, false); // unregister
				Log.i(TAG,
						"GCM Device Unregistered because failed to register on camba server."
								+ (AppUserEmail != null && !AppUserEmail.equals("") ? ""
										: "AppUserEmail is empty. User may have logged out or not logged in yet.")
								+ "  registrationId [" + registrationId + "]");
			}

		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString(), e);
			CLog.email(context, e.getMessage(), e);

		}

	}

	@Override
	protected void onUnregistered(Context context, String registrationId)
	{
		try
		{
			Log.i(TAG, "Device unregistered");
			if (GCMRegistrar.isRegisteredOnServer(context))
			{
				GCMRegistrar.setRegisteredOnServer(context, false); // unregister

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

					SharedPreferences sharedPrefs = PreferenceManager
							.getDefaultSharedPreferences(this);
					AppUserEmail = sharedPrefs.getString("AppUserEmail", null);
					AppUserPassword = sharedPrefs.getString("AppUserPassword", null);
					Operation = "2";
					Manufacturer = android.os.Build.MANUFACTURER;
					Model = android.os.Build.MODEL;
					SerialNo = android.os.Build.SERIAL;
					ImeiNo = ((android.telephony.TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
							.getDeviceId();
					Fingureprint = android.os.Build.FINGERPRINT;
					WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
					WifiInfo info = manager.getConnectionInfo();
					MacAddress = info.getMacAddress();
					BlueToothName = BluetoothAdapter.getDefaultAdapter().getName();
					AppVersion = (getPackageManager().getPackageInfo(getPackageName(), 0)).versionName;
				}
				catch (Exception ee)
				{
				}

				if (AppUserEmail != null && !AppUserEmail.equals(""))
				{
					CambaApiManager.registerDeviceForUsername(AppUserEmail, AppUserPassword,
							registrationId, Operation, BlueToothName, Manufacturer, Model,
							SerialNo, ImeiNo, Fingureprint, MacAddress, AppVersion);
					Log.i(TAG,
							"Un Registered from Camba Server is successfull. Registration id is ["
									+ registrationId + "] and email is [" + AppUserEmail + "]");
				}
				else
				{
					Log.i(TAG,
							"Unable to register from camba server because."
									+ (AppUserEmail != null && !AppUserEmail.equals("") ? ""
											: "AppUserEmail is empty. User may have logged out or not logged in yet.")
									+ "  registrationId [" + registrationId + "]");
				}

			}
			else
			{
				// This callback results from the call to unregister made on
				// ServerUtilities when the registration to the server failed.
				Log.i(TAG, "Ignoring unregister callback");
			}
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString(), e);
			CLog.email(context, e.getMessage(), e);
		}

	}

	@Override
	protected void onDeletedMessages(Context context, int total)
	{
		Log.i(TAG, "Received deleted messages notification");
		String message = getString(R.string.gcm_deleted, total);
		// displayMessage(context, message);
		// notifies user
		Intent i = new Intent("CambaGCMAlert");
		i.putExtra("GCMMethod", "onDeletedMessages");
		generateNotification(context, message, i);
	}

	@Override
	public void onError(Context context, String errorId)
	{
		Log.i(TAG, "Received error: " + errorId);
		// displayMessage(context, getString(R.string.gcm_error, errorId));
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId)
	{
		// log message
		Log.i(TAG, "Received recoverable error: " + errorId);
		// displayMessage(context, getString(R.string.gcm_recoverable_error,
		// errorId));
		return super.onRecoverableError(context, errorId);
	}

	/**
	 * Issues a notification to inform the user that server has sent a message.
	 */
	private static void generateNotification(Context context, String message, Intent intent)
	{
		int icon = R.drawable.icon_192x192;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon, message, when);
		String title = context.getString(R.string.app_name);

		Intent notificationIntent = new Intent(context, MainActivity.class);
		if (intent != null) notificationIntent.putExtras(intent.getExtras());
		notificationIntent.setAction("GCM");
		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, notificationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		notification.setLatestEventInfo(context, title, message, pIntent);

		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		// notificationManager.cancelAll();

		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.defaults |= Notification.DEFAULT_SOUND;

		notificationManager.notify(0, notification);

	}

	@Override
	protected void onMessage(Context context, Intent intent)
	{

		try
		{
			Log.i(TAG, "Received message");

			CameraNotification notif = new CameraNotification();
			notif.setIsRead(false);

			for (String key : intent.getExtras().keySet())
			{
				// Log.i(TAG, "Key [" + key + "]");
				String value = intent.getStringExtra(key);
				// Log.i(TAG, "Value [" + value + "]");
				if (key.equalsIgnoreCase("CameraID"))
				{
					notif.setCameraID(Integer.parseInt(value));
				}
				else if (key.equalsIgnoreCase("UserEmail"))
				{
					notif.setUserEmail(value);
				}
				else if (key.equalsIgnoreCase("AlertType"))
				{
					notif.setAlertTypeText(value);
					if (value.contains("GCM"))
					{
						notif.setAlertTypeID(Constants.ALert_GCMRegistration);
					}
					else if (value.contains("Online"))
					{
						notif.setAlertTypeID(Constants.ALert_CameraOnline);
					}
					else if (value.contains("Offline"))
					{
						notif.setAlertTypeID(Constants.ALert_CameraOffline);
					}
					else if (value.contains("Motion"))
					{
						notif.setAlertTypeID(Constants.ALert_CameraMD);
					}
				}
				if (key.equalsIgnoreCase("AlertMessage"))
				{
					notif.setAlertMessage(value);
				}
				if (key.equalsIgnoreCase("AlertTime"))
				{
					notif.setAlertTimeInteger(Long.parseLong(value.replace("-", "")
							.replace(" ", "").replace(":", "")));
				}
				if (key.equalsIgnoreCase("SnapUrls"))
				{
					int s = value.indexOf('[');
					int e = value.indexOf(']');
					notif.setSnapUrls((value.substring(s, e) + "").replace("\\/", "/")
							.replace("\"", "").replace(" ", ""));
				}
				if (key.equalsIgnoreCase("RecordingViewURL"))
				{
					notif.setRecordingViewURL(value);
				}
			}

			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			boolean CameraOnlineOfflineAlert = sharedPrefs.getBoolean(
					"pref_cp_CameraOnlineOfflineAlert", true);
			boolean CameraMDAlert = sharedPrefs.getBoolean("pref_cp_CameraMDAlert", true);

			String AppUserEmail = sharedPrefs.getString("AppUserEmail", null);

			if (notif.getAlertTypeText().toUpperCase(Locale.ENGLISH).contains("GCM"))
			{
				// add code that the this is registerd on cmaba server. No
				// action defined so far.
			}

			else if ((CameraMDAlert && notif.getAlertTypeText().toUpperCase(Locale.ENGLISH)
					.contains("MOTION"))
					|| (CameraOnlineOfflineAlert && (notif.getAlertTypeText()
							.toUpperCase(Locale.ENGLISH).contains("OFFLINE") || notif
							.getAlertTypeText().toUpperCase(Locale.ENGLISH).contains("ONLINE"))))
			{
				intent.putExtra("GCMMethod", "onMessage");

				dbNotifcation db = new dbNotifcation(context);
				synchronized (this)
				{ // only one
					if (db.getCameraNotificationCount(notif) == 0)
					{
						db.addCameraNotification(notif);
						String NotifyID = "" + db.getMaxID();
						intent.putExtra(Constants.GCMNotificationIDString, NotifyID); // type
																						// cast
																						// in
																						// string
																						// because
																						// we
																						// are
						// getting string from getextra in
						// main activity
						Log.i(TAG,
								"Generated and saved with id [" + NotifyID + "]" + notif.toString());

						if (AppUserEmail != null && notif.getUserEmail() != null
								&& AppUserEmail.equalsIgnoreCase(notif.getUserEmail()))
						{
							generateNotification(context, notif.getAlertMessage(), intent);
							Log.i(TAG, "Notification Generated");
						}
					}
				}

			}

		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString(), e);
			CLog.email(context, e.getMessage(), e);
		}

	}

}