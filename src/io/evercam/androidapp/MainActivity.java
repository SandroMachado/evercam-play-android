package io.evercam.androidapp;

import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dal.DbAppUser;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.tasks.CheckInternetTask;
import io.evercam.androidapp.utils.AppData;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.PrefsManager;

import com.bugsense.trace.BugSenseHandler;

import io.evercam.androidapp.R;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/*
 * Main starting activity. 
 * Checks whether user should login first or load the cameras straight away
 * */
public class MainActivity extends Activity
{
	private static final String TAG = "evercamapp-MainActivity";

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		try
		{
			super.onCreate(savedInstanceState);

			if (Constants.isAppTrackingEnabled)
			{
				BugSenseHandler.initAndStartSession(this, Constants.bugsense_ApiKey);
			}

			setContentView(R.layout.mainactivitylayout);

			startApplication();

		}
		catch (Exception ex)
		{
			CustomedDialog.showUnexpectedErrorDialog(MainActivity.this);
			Log.e(TAG, Log.getStackTraceString(ex));
		}
	}

	private void startApplication()
	{
		try
		{
			new MainCheckInternetTask(MainActivity.this)
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		catch (Exception ex)
		{
			BugSenseHandler.sendException(ex);
			CustomedDialog.showUnexpectedErrorDialog(MainActivity.this);
			Log.e(TAG, Log.getStackTraceString(ex));
		}
	}

	private void startCamerasActivity()
	{
		int notificationID = 0;
		String strNotificationID = this.getIntent().getStringExtra(
				Constants.GCMNotificationIDString);

		if (strNotificationID != null && !strNotificationID.equals("")) notificationID = Integer
				.parseInt(strNotificationID);

		if (CamerasActivity.activity != null)
		{
			CamerasActivity.activity.finish();
		}

		Intent intent = new Intent(this, CamerasActivity.class);
		intent.putExtra(Constants.GCMNotificationIDString, notificationID);
		this.startActivity(intent);

		MainActivity.this.finish();
	}

	@Override
	public void onStart()
	{
		super.onStart();

		if (Constants.isAppTrackingEnabled)
		{
			if (Constants.isAppTrackingEnabled)
			{
				BugSenseHandler.startSession(this);
			}
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();

		if (Constants.isAppTrackingEnabled)
		{
			if (Constants.isAppTrackingEnabled)
			{
				BugSenseHandler.closeSession(this);
			}
		}
	}

	private boolean isUserLogged() throws Exception
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String defaultEmail = PrefsManager.getUserEmail(sharedPrefs);
		if (defaultEmail != null)
		{
			DbAppUser dbUser = new DbAppUser(this);
			AppUser defaultUser = dbUser.getAppUserByEmail(defaultEmail);
			AppData.defaultUser = defaultUser;
		}

		return (AppData.defaultUser != null);
	}

	class MainCheckInternetTask extends CheckInternetTask
	{

		public MainCheckInternetTask(Context context)
		{
			super(context);
		}

		@Override
		protected void onPostExecute(Boolean hasNetwork)
		{
			try
			{
				if (hasNetwork)
				{
					if (isUserLogged())
					{
						startCamerasActivity();
					}
					else
					{
						Intent slideIntent = new Intent(MainActivity.this, SlideActivity.class);
						startActivity(slideIntent);
					}
				}
				else
				{
					CustomedDialog.showInternetNotConnectDialog(MainActivity.this);
				}
			}
			catch (Exception e)
			{
				BugSenseHandler.sendException(e);
				CustomedDialog.showUnexpectedErrorDialog(MainActivity.this);
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}
	}
}