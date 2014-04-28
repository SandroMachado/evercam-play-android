package io.evercam.androidapp;

import io.evercam.androidapp.utils.Constants;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import com.bugsense.trace.BugSenseHandler;
import io.evercam.androidapp.R;
import com.google.analytics.tracking.android.EasyTracker;

public class CameraPrefsActivity extends PreferenceActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.initAndStartSession(this, Constants.bugsense_ApiKey);
		}

		try
		{
			this.getActionBar().hide();
		}
		catch (Exception e)
		{
		}
		addPreferencesFromResource(R.layout.camspreferences);
		this.setDefaultKeyMode(DEFAULT_KEYS_DISABLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu m)
	{
		return false; // no menu options supported for this activity as it is
						// already the preferences activity
	}

	@Override
	public void onStart()
	{
		super.onStart();

		if (Constants.isAppTrackingEnabled)
		{
			EasyTracker.getInstance().activityStart(this);
			BugSenseHandler.startSession(this);
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();
		if (Constants.isAppTrackingEnabled)
		{
			EasyTracker.getInstance().activityStop(this);
			BugSenseHandler.closeSession(this);
		}
	}
}