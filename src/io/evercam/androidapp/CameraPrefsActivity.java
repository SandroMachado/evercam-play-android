package io.evercam.androidapp;

import io.evercam.androidapp.utils.Constants;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.Menu;
import com.bugsense.trace.BugSenseHandler;
import io.evercam.androidapp.R;

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
		
		EvercamPlayApplication.sendScreenAnalytics(this, getString(R.string.screen_preference));

		this.getActionBar().hide();

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new MyPreferenceFragment()).commit();
		this.setDefaultKeyMode(DEFAULT_KEYS_DISABLE);
	}

	public static class MyPreferenceFragment extends PreferenceFragment
	{
		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.camspreferences);
		}
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
			BugSenseHandler.startSession(this);
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();
		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.closeSession(this);
		}
	}
}