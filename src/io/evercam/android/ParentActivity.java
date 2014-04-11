package io.evercam.android;

import android.app.Activity;
import android.os.Bundle;

import android.view.Window;

public class ParentActivity extends Activity
{

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		try
		{
			// getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
			// Log.e(TAG, "parent:" + this.getActionBar().getHeight());
			// if(this.getActionBar() != null)
			// this.getActionBar().setDisplayShowHomeEnabled(true);
			// if(this.getActionBar() != null)
			// this.getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		catch (Exception e)
		{

		}

	}
	// @Override
	// public void onStart() {
	// super.onStart();
	// EasyTracker.getInstance().activityStart(this); // Add this method.
	// }
	//
	// @Override
	// public void onStop() {
	// super.onStop();
	// EasyTracker.getInstance().activityStop(this); // Add this method.
	// }
}