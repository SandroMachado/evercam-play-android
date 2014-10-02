package io.evercam.androidapp;

import com.bugsense.trace.BugSenseHandler;
import com.hikvision.netsdk.NET_DVR_TIME;

import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.ProgressView;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceView;
import android.widget.DatePicker;
import android.widget.TimePicker;

public class LocalStorageActivity extends Activity
{
	private final String TAG = "evercamplay-LocalStorageActivity";
	private final String KEY_STATE_PORT = "playPort";

	private EvercamCamera evercamCamera;
	private SurfaceView surfaceView;
	private ProgressView progressView;
	private HikvisionSdk hikvisionSdk;

	private CustomProgressDialog customProgressDialog;
	Handler handler = new Handler();

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

		evercamCamera = VideoActivity.evercamCamera;

		if (this.getActionBar() != null)
		{
			this.getActionBar().setHomeButtonEnabled(true);
			this.getActionBar().setTitle(
					getString(R.string.title_activity_local_storage) + " - "
							+ evercamCamera.getName());
		}

		surfaceView = (SurfaceView) findViewById(R.id.surface_hikvision);
		progressView = (ProgressView) findViewById(R.id.local_storage_spinner);

		int screenWidth = CamerasActivity.readScreenWidth(this);
		int screenHeight = CamerasActivity.readScreenHeight(this);
		if (screenWidth < screenHeight)
		{
			android.widget.RelativeLayout.LayoutParams params = new android.widget.RelativeLayout.LayoutParams(
					screenWidth, screenWidth / 3 * 2);
			surfaceView.setLayoutParams(params);
		}

		hikvisionSdk = new HikvisionSdk(surfaceView, evercamCamera, this);

		customProgressDialog = new CustomProgressDialog(this);
		customProgressDialog.show(getString(R.string.msg_connecting_camera));

		new LoginTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

		// finish();
		// hikvisionSdk.cleanUp();

		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.closeSession(this);
		}
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
						dialog.dismiss();
						surfaceView.setVisibility(View.VISIBLE);
						showProgressView();
						hikvisionSdk.startPlayback(getTimeFromPicker(datePicker, timePicker));
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

	private NET_DVR_TIME getTimeFromPicker(DatePicker datePicker, TimePicker timePicker)
	{
		int month = datePicker.getMonth() + 1;
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

		NET_DVR_TIME beginTime = new NET_DVR_TIME();
		beginTime.dwYear = year;
		beginTime.dwMonth = month;
		beginTime.dwDay = day;
		beginTime.dwHour = hour;
		beginTime.dwMinute = min;
		beginTime.dwSecond = 0;

		return beginTime;
	}

	public void showProgressView()
	{
		progressView.setVisibility(View.VISIBLE);
	}

	public void hideProgressView()
	{
		handler.postDelayed(new Runnable(){

			@Override
			public void run()
			{
				progressView.setVisibility(View.INVISIBLE);
			}

		}, 2000);
	}

	class LoginTask extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Void... params)
		{
			boolean loginSuccess = hikvisionSdk.login();
			if (loginSuccess)
			{
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean success)
		{
			customProgressDialog.dismiss();

			if (success)
			{
				showDateTimePickerDialog();
			}
		}
	}
}
