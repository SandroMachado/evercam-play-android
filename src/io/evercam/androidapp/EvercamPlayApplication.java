package io.evercam.androidapp;

import android.app.Activity;
import android.app.Application;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

public class EvercamPlayApplication extends Application
{
	private static final String PROPERTY_ID = "UA-52483995-1";

	private static final String TAG = "evercamplay-EvercamPlayApplication";

	public enum TrackerName
	{
		APP_TRACKER, // Tracker used only in this app.
		GLOBAL_TRACKER, // Tracker used by all the apps from a company.
	}

	HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

	public EvercamPlayApplication()
	{
		super();
	}

	synchronized Tracker getTracker(TrackerName trackerId)
	{
		if (!mTrackers.containsKey(trackerId))
		{
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics
					.newTracker(R.xml.app_tracker) : analytics.newTracker(PROPERTY_ID);
			mTrackers.put(trackerId, t);

		}
		return mTrackers.get(trackerId);
	}

	/**
	 * Send screen view to Google Analytics from activity with screen name.
	 * @param activity
	 * @param screenName The screen name that shows in Google dashboard.
	 */
	public static void sendScreenAnalytics(Activity activity, String screenName)
	{
		Tracker tracker = ((EvercamPlayApplication) activity.getApplication())
				.getTracker(TrackerName.APP_TRACKER);
		tracker.setScreenName(screenName);
		tracker.send(new HitBuilders.AppViewBuilder().build());
	}
}