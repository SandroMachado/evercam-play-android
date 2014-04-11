package io.evercam.android.custom;

import io.evercam.android.utils.Commons;
import io.evercam.android.utils.Constants;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;
import io.evercam.android.R;
import com.google.analytics.tracking.android.EasyTracker;

public class AboutDialog extends Activity
{
	private static String TAG = "AboutDialog";

	/**
	 * Standard Android on create method that gets called when the activity
	 * initialized.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (Constants.isAppTrackingEnabled) if (Constants.isAppTrackingEnabled) BugSenseHandler
				.initAndStartSession(this, Constants.bugsense_ApiKey);

		try
		{

			setContentView(R.layout.aboutlayoutnew);
			// setTheme(android.R.style.Theme_Holo_Light_Dialog_NoActionBar)

			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			String version = pInfo.versionName;

			TextView tvInfo = (TextView) findViewById(R.id.info_text);
			tvInfo.setText(Html.fromHtml(Commons.readRawTextFile(R.raw.info, this).replace(
					"@@version", version)));
			// tvInfo.setLinkTextColor(Color.WHITE);
			Linkify.addLinks(tvInfo, Linkify.ALL);

			TextView tvLegal = (TextView) findViewById(R.id.legal_text);
			tvLegal.setText(Html.fromHtml(Commons.readRawTextFile(R.raw.legal, this)));
			// tvLegal.setLinkTextColor(Color.WHITE);
			Linkify.addLinks(tvLegal, Linkify.ALL);

			Button btnClose = (Button) findViewById(R.id.btnclose);
			btnClose.setOnClickListener(new Button.OnClickListener(){
				@Override
				public void onClick(View v)
				{
					// TODO Auto-generated method stub
					AboutDialog.this.finish();
				}
			});

			ImageView iv = (ImageView) this.findViewById(R.id.ivshowlogoabout);
			Bitmap bmp = BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_192x192);
			bmp = Bitmap.createScaledBitmap(bmp, bmp.getWidth() * 2, bmp.getHeight() * 2, true);
			iv.setImageBitmap(bmp);

		}
		catch (Exception ex)
		{
			Log.e(TAG, ex.toString(), ex);
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ex);
		}
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
