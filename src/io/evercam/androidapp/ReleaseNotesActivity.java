package io.evercam.androidapp;

import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.PrefsManager;

import com.bugsense.trace.BugSenseHandler;

import android.os.Bundle;
import android.content.Intent;
import android.text.Html;
import android.text.util.Linkify;
import android.view.View;
import android.widget.*;

// 	This activity verifies the login and requests the cams data from the api 
public class ReleaseNotesActivity extends ParentActivity
{
	public String TAG = "evercamplay-ReleaseNotesActivity";
	private Button btnReleaseNotes;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.initAndStartSession(this, Constants.bugsense_ApiKey);
		}

		setContentView(R.layout.release_notes_activity_layout);

		TextView textViewNotes = (TextView) findViewById(R.id.txtreleasenotes);
		btnReleaseNotes = (Button) findViewById(R.id.btn_release_notes_ok);

		textViewNotes.setPadding(25, 14, 14, 14);

		btnReleaseNotes.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v)
			{
				onNotesRead();
			}
		});
		//
		// PackageInfo pInfo =
		// getPackageManager().getPackageInfo(getPackageName(), 0);
		// String version = pInfo.versionName;
		// String data =
		// Commons.readRawTextFile(R.raw.release_notes,this).replace("@@version",
		// version);
		String data = Commons.readRawTextFile(R.raw.release_notes, this);
		textViewNotes.setText(Html.fromHtml(data));
		Linkify.addLinks(textViewNotes, Linkify.EMAIL_ADDRESSES);

	}

	private void onNotesRead()
	{
		int versionCode = Commons.getAppVersionCode(this);
		PrefsManager.setReleaseNotesShown(this, versionCode);

		Intent act = new Intent(ReleaseNotesActivity.this, MainActivity.class);
		startActivity(act);
		ReleaseNotesActivity.this.finish();
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