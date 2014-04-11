package io.evercam.android;

import io.evercam.android.utils.Commons;
import io.evercam.android.utils.Constants;
import com.bugsense.trace.BugSenseHandler;
import io.evercam.android.R;
import com.google.analytics.tracking.android.EasyTracker;

import android.os.Bundle;
import android.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.text.Html;
import android.text.util.Linkify;
import android.view.View;
import android.widget.*;

// 	This activity verifies the login and requests the cams data from the api 
public class ReleaseNotesActivity extends ParentActivity
{
	public String TAG = "ReleaseNotesActivity";

	Button btnReleaseNotes = null;

	static boolean enableLogs = true;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (Constants.isAppTrackingEnabled) if (Constants.isAppTrackingEnabled) BugSenseHandler
				.initAndStartSession(this, Constants.bugsense_ApiKey);

		setContentView(R.layout.release_notes_activity_layout); 

		try
		{

			TextView tvReleaseNotes = (TextView) findViewById(R.id.txtreleasenotes);
			btnReleaseNotes = (Button) findViewById(R.id.btn_release_notes_ok);

			tvReleaseNotes.setPadding(25, 14, 14, 14);

			btnReleaseNotes.setOnClickListener(new Button.OnClickListener(){
				@Override
				public void onClick(View v)
				{
					onNotesRead();
					// TODO Auto-generated method stub

				}
			});

			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			String version = pInfo.versionName;
			String data = Commons.readRawTextFile(R.raw.release_notes, this).replace("@@version",
					version);
			tvReleaseNotes.setText(Html.fromHtml(data));
			Linkify.addLinks(tvReleaseNotes, Linkify.EMAIL_ADDRESSES);

		}
		catch (Exception e)
		{
		}

	}

	private void onNotesRead()
	{
		try
		{
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			int vcode = pInfo.versionCode;

			SharedPreferences sharedPrefs = PreferenceManager
					.getDefaultSharedPreferences(ReleaseNotesActivity.this);
			SharedPreferences.Editor editor = sharedPrefs.edit();
			editor.putBoolean(ReleaseNotesActivity.this.getString(R.string.is_release_notes_shown)
					+ vcode, true);
			editor.commit();

			Intent intent = new Intent(ReleaseNotesActivity.this, MainActivity.class);
			startActivity(intent);

			ReleaseNotesActivity.this.finish();
		}
		catch (Exception e)
		{
		}

	}

	@Override
	public void onWindowFocusChanged(boolean hasfocus)
	{
		ScrollView svreleasenotes = (ScrollView) findViewById(R.id.svreleasenotes);
		svreleasenotes.getLayoutParams().height = svreleasenotes.getMeasuredHeight()
				- btnReleaseNotes.getMeasuredHeight();
	}

	@Override
	public void onBackPressed()
	{
		onNotesRead();
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

}