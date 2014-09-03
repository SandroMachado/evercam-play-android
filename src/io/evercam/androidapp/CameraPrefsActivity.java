package io.evercam.androidapp;

import java.util.ArrayList;

import io.evercam.androidapp.utils.Constants;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import com.bugsense.trace.BugSenseHandler;
import io.evercam.androidapp.R;

public class CameraPrefsActivity extends PreferenceActivity
{
	private static int screenWidth = 0;
	private static final String TAG = "evercamplay-CameraPrefsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.initAndStartSession(this, Constants.bugsense_ApiKey);
		}

		screenWidth = CamerasActivity.readScreenWidth(this);

		EvercamPlayApplication.sendScreenAnalytics(this, getString(R.string.screen_preference));

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new MyPreferenceFragment()).commit();
		this.setDefaultKeyMode(DEFAULT_KEYS_DISABLE);
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

	public class MyPreferenceFragment extends PreferenceFragment
	{
		public MyPreferenceFragment()
		{
			super();
		}

		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.camspreferences);

			setCameraNumbersForScreen(screenWidth);
		}

		private void setCameraNumbersForScreen(int screenWidth)
		{
			int maxCamerasPerRow = 3;
			if (screenWidth != 0)
			{
				maxCamerasPerRow = screenWidth / 350;
			}
			ArrayList<String> cameraNumberArrayList = new ArrayList<String>();
			for (int index = 1; index <= maxCamerasPerRow; index++)
			{
				cameraNumberArrayList.add(String.valueOf(index));
			}
			CharSequence[] charNumberValues = cameraNumberArrayList
					.toArray(new CharSequence[cameraNumberArrayList.size()]);
			ListPreference interfaceList = (ListPreference) getPreferenceManager().findPreference(
					Constants.KEY_CAMERA_PER_ROW);
			interfaceList.setEntries(charNumberValues);
			interfaceList.setEntryValues(charNumberValues);
		}
	}
}