package io.evercam.androidapp.custom;

import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;
import io.evercam.androidapp.R;
import com.google.analytics.tracking.android.EasyTracker;

public class AboutDialog extends Activity
{
	private static String TAG = "evercamapp-AboutDialog";

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.initAndStartSession(this, Constants.bugsense_ApiKey);
		}

		try
		{

			setContentView(R.layout.aboutlayoutnew);

			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			String version = packageInfo.versionName;

			TextView infoTextView = (TextView) findViewById(R.id.info_text);
			infoTextView.setText(Html.fromHtml(Commons.readRawTextFile(R.raw.info, this).replace(
					"@@version", version)));
			// tvInfo.setLinkTextColor(Color.WHITE);
			Linkify.addLinks(infoTextView, Linkify.ALL);

			TextView tvLegal = (TextView) findViewById(R.id.legal_text);
			tvLegal.setText(Html.fromHtml(Commons.readRawTextFile(R.raw.legal, this)));
			// tvLegal.setLinkTextColor(Color.WHITE);
			Linkify.addLinks(tvLegal, Linkify.ALL);

			Button btnClose = (Button) findViewById(R.id.btnclose);
			btnClose.setOnClickListener(new Button.OnClickListener(){
				@Override
				public void onClick(View v)
				{
					AboutDialog.this.finish();
				}
			});

			ImageView iconImageView = (ImageView) this.findViewById(R.id.ivshowlogoabout);
			iconImageView.setImageResource(R.drawable.evercam_play_192x192);

		}
		catch (Exception ex)
		{
			Log.e(TAG, ex.toString(), ex);
			if (Constants.isAppTrackingEnabled)
			{
				BugSenseHandler.sendException(ex);
			}
		}
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
