package io.evercam.androidapp;

import com.bugsense.trace.BugSenseHandler;
import com.hikvision.netsdk.NET_DVR_TIME;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.video.VideoActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.DatePicker;
import android.widget.TimePicker;

public class LocalStorageActivity extends Activity
{
	private final String TAG = "evercamplay-LocalStorageActivity";
	private final String KEY_STATE_PORT = "playPort";

	private EvercamCamera evercamCamera;
	private SurfaceView surfaceView;
	private HikvisionSdk hikvisionSdk;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_local_storage);

		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.initAndStartSession(this, Constants.bugsense_ApiKey);
		}

		EvercamPlayApplication.sendScreenAnalytics(this, getString(R.string.screen_local_storage));

		surfaceView = (SurfaceView) findViewById(R.id.surface_hikvision);

		evercamCamera = VideoActivity.evercamCamera;

		hikvisionSdk = new HikvisionSdk(surfaceView, evercamCamera);

		loginToDevice();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		outState.putInt(KEY_STATE_PORT, hikvisionSdk.playPort);
		super.onSaveInstanceState(outState);
		Log.i(TAG, "onSaveInstanceState");
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		hikvisionSdk.playPort = savedInstanceState.getInt(KEY_STATE_PORT);
		super.onRestoreInstanceState(savedInstanceState);
		Log.i(TAG, "onRestoreInstanceState");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_playback, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		switch (item.getItemId())
		{
		case R.id.menu_start_datetime:
			showDateTimePickerDialog();
			return true;
		case android.R.id.home:
			this.finish();
			return true;

		default:
			return true;
		}
	}

	private void loginToDevice()
	{
		boolean loginSuccess = hikvisionSdk.login();
		if (loginSuccess)
		{

			showDateTimePickerDialog();
		}
	}

	private void showDateTimePickerDialog()
	{
		final View dialogLayout = getLayoutInflater().inflate(R.layout.date_time_layout, null);

		final DatePicker datePicker = (DatePicker) dialogLayout.findViewById(R.id.datePicker);
		final TimePicker timePicker = (TimePicker) dialogLayout.findViewById(R.id.timePicker);
		timePicker.setIs24HourView(true);
		timePicker.setPadding(0, 0, 0, 0);
		datePicker.setPadding(0, 0, 0, 0);

		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
				.setView(dialogLayout)
				.setPositiveButton(R.string.play, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						int month = datePicker.getMonth();
						int day = datePicker.getDayOfMonth();
						int year = datePicker.getYear();

						int hour = timePicker.getCurrentHour();
						int min = timePicker.getCurrentMinute();

						String strMin = String.valueOf(min);
						String strHour = String.valueOf(hour);

						if (strMin.length() == 1)
						{
							strMin = "0" + strMin;
						}

						if (strHour.length() == 1)
						{
							strHour = "0" + strHour;
						}

						String date = month + "/" + day + "/" + year + " ";
						String time = strHour + ":" + strMin + " ";

						Log.d(TAG, "From: " + date + time);
						NET_DVR_TIME beginTime = new NET_DVR_TIME();
						// beginTime.dwYear = 2014;
						// beginTime.dwMonth = 9;
						// beginTime.dwDay = 29;
						// beginTime.dwHour = 0;
						// beginTime.dwMinute = 0;
						// beginTime.dwSecond = 0;
						beginTime.dwYear = year;
						beginTime.dwMonth = month;
						beginTime.dwDay = day;
						beginTime.dwHour = hour;
						beginTime.dwMinute = min;
						beginTime.dwSecond = 0;

						surfaceView.setVisibility(View.VISIBLE);
						hikvisionSdk.startPlayback(beginTime);
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				});

		dialogBuilder.create().show();
	}
}
