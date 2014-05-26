package io.evercam.androidapp;

import io.evercam.androidapp.dal.DbAppUser;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.utils.AppData;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.PrefsManager;
import io.evercam.androidapp.utils.UIUtils;

import com.bugsense.trace.BugSenseHandler;
import io.evercam.androidapp.R;
import com.google.analytics.tracking.android.EasyTracker;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.DialogInterface;
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
			UIUtils.getAlertDialog(MainActivity.this, "Error Occured", ex.toString()).show();
			Log.e(TAG, Log.getStackTraceString(ex));
		}
	}

	private void startApplication()
	{
		try
		{
			if (!Commons.isOnline(this))
			{
				try
				{
					UIUtils.getNoInternetDialog(this, new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
							finish();
						}
					}).show();
					return;
				}
				catch (Exception ex)
				{
					if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ex);
				}
			}
			else
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
		}
		catch (Exception ex)
		{
			UIUtils.getAlertDialog(MainActivity.this, "Error Occured", ex.toString()).show();
			Log.e(TAG, Log.getStackTraceString(ex));
		}

	}

	private void startCamerasActivity()
	{
		int notificationID = 0;
		try
		{
			String strNotificationID = this.getIntent().getStringExtra(
					Constants.GCMNotificationIDString);

			if (strNotificationID != null && !strNotificationID.equals("")) notificationID = Integer
					.parseInt(strNotificationID);

		}
		catch (Exception e)
		{
		}

		if (CamerasActivity.activity != null)
		{
			try
			{
				CamerasActivity.activity.finish();
			}
			catch (Exception e)
			{
				Log.e(TAG, e.toString(), e);
			}
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
			EasyTracker.getInstance().activityStart(this);
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
			EasyTracker.getInstance().activityStop(this);
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
}