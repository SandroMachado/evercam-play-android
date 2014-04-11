package io.evercam.android;

import io.evercam.android.utils.AppData;
import io.evercam.android.utils.Commons;
import io.evercam.android.utils.Constants;
import io.evercam.android.utils.UIUtils;

import com.bugsense.trace.BugSenseHandler;
import io.evercam.android.R;
import com.google.analytics.tracking.android.EasyTracker;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.util.Log;

// Main starting activity. Checks whether user should login first or load the cameras straight away
public class mainActivity extends Activity
{

	static String debugValues = ""; // values of variables while debugging saved
									// into it
	static final String TAG = "mainActivity"; // Log tag being used to filter in
												// LogCat
	static boolean enableLogs = true;

	TestingTask testing = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		try
		{
			super.onCreate(savedInstanceState);

			if (Constants.isAppTrackingEnabled) if (Constants.isAppTrackingEnabled) BugSenseHandler
					.initAndStartSession(this, Constants.bugsense_ApiKey);

			this.setContentView(R.layout.mainactivitylayout);
			Log.i(TAG, "onCreate of MainActivity ");

			// **********//Debugging comments
			// testing = new TestingTask();
			// testing.execute("");
			// Boolean msg = true;
			// if(msg == true)
			// {
			// Log.i(TAG,
			// "Exiting from main application without startig login activity");
			// return;
			// }
			// ###########END Debugging

			int vcode = 0;
			boolean isReleaseNotesShown = false;
			try
			{
				PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
				vcode = pInfo.versionCode;
				SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
				isReleaseNotesShown = sharedPrefs.getBoolean(
						this.getString(R.string.is_release_notes_shown) + vcode, false);

			}
			catch (Exception e)
			{
			}

			if (vcode > 0 && isReleaseNotesShown)
			{
				startActivityWork();
			}
			else
			{
				Intent act = new Intent(mainActivity.this, ReleaseNotesActivity.class);
				startActivity(act);
				this.finish();
			}

		}
		catch (Exception ex)
		{
			UIUtils.GetAlertDialog(mainActivity.this, "Error Occured", ex.toString()).show();
			if (enableLogs) Log.i(TAG, Log.getStackTraceString(ex));
		}

	}

	private void startActivityWork()
	{
		try
		{
			if (!Commons.isOnline(this))
			{
				try
				{
					UIUtils.GetAlertDialog(mainActivity.this, "Network not connected",
							"Please connect to internet and try again",
							new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									dialog.dismiss();
									mainActivity.this.finish();
								}
							}).show();
					return;
				}
				catch (Exception ex)
				{
					if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ex);
				}
			}

			// get the username and password saved in application and pass to
			// CambaApiManager so that they can be used at the time of login
			// authentication
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			AppData.AppUserEmail = sharedPrefs.getString("AppUserEmail", null);
			AppData.AppUserPassword = sharedPrefs.getString("AppUserPassword", null);

			// if username and password not found, pass the same to login
			// activity
			if (AppData.AppUserEmail == null || AppData.AppUserEmail == ""
					|| AppData.AppUserPassword == null || AppData.AppUserPassword == "")
			{
				Intent login = new Intent(mainActivity.this, LoginActivity.class);
				startActivityForResult(login, LoginActivity.loginVerifyRequestCode);
			}
			else
			// username password found. pass to cams activity and verify if the
			// username password is valid and get the cameras data
			{
				startCamsActivity();
			}

		}
		catch (Exception ex)
		{
			UIUtils.GetAlertDialog(mainActivity.this, "Error Occured", ex.toString()).show();
			if (enableLogs) Log.i(TAG, Log.getStackTraceString(ex));
		}

	}

	// login activity result. perform the acction according to result...
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		try
		{
			// Some simple checks to ensure that we are recieving results for
			// our desired intention and that it was successfull
			if (requestCode == LoginActivity.loginVerifyRequestCode
					&& resultCode == LoginActivity.loginResultSuccessCode)
			{
				startCamsActivity();
			}
			else
			{
				mainActivity.this.finish();
			}
		}
		catch (Exception ex)
		{
			UIUtils.GetAlertDialog(mainActivity.this, "Error Occured", ex.toString()).show();
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ex);
		}
	}

	// start the cameras activity
	private void startCamsActivity()
	{
		int notificationID = 0;
		try
		{
			String strNotificationID = this.getIntent().getStringExtra(
					Constants.GCMNotificationIDString);

			if (strNotificationID != null && !strNotificationID.equals("")) notificationID = Integer
					.parseInt(strNotificationID);

			Log.i(TAG, "main activitiy strNotificationID [" + strNotificationID
					+ "], notificationID [" + notificationID + "]");

		}
		catch (Exception e)
		{
		}

		if (CamsActivity._activity != null)
		{
			try
			{
				CamsActivity._activity.finish();
			}
			catch (Exception e)
			{
				Log.e(TAG, e.toString(), e);
			}
		}

		Intent i = new Intent(this, CamsActivity.class);
		i.putExtra(Constants.GCMNotificationIDString, notificationID);
		this.startActivity(i);

		// this.startActivity(new
		// Intent(this,io.evercam.android.rtspvideo.RTSPVideoViewActivity.class));

		mainActivity.this.finish();

	}

	@Override
	public void onStart()
	{
		super.onStart();

		if (Constants.isAppTrackingEnabled)
		{
			EasyTracker.getInstance().activityStart(this);
			if (Constants.isAppTrackingEnabled) BugSenseHandler.startSession(this);
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();

		if (Constants.isAppTrackingEnabled)
		{
			EasyTracker.getInstance().activityStop(this);
			if (Constants.isAppTrackingEnabled) BugSenseHandler.closeSession(this);
		}
	}

	// ********************** TESTING *******************************
	// Check whether connected to Internet or not

	// Task used for testing only. Only at the time of debugging
	private class TestingTask extends AsyncTask<String, String, String>
	{
		@Override
		protected String doInBackground(String... arg0)
		{
			try
			{
				// TODO: put the code that you want to test here

				return null;

			}

			catch (Exception ex)
			{
				// UIUtils.GetAlertDialog(mainActivity.this,
				// "Error Occured In Task", ex.toString() ).show();
				Log.e(TAG, ex.getMessage() + "::" + Log.getStackTraceString(ex) + "_::");
			}
			return null;
		}

		@Override
		public void onPostExecute(String status)
		{

		}
	}
	// ###################### TESTING ###############################
}