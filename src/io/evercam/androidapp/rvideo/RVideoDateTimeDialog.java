package io.evercam.androidapp.rvideo;

import io.evercam.androidapp.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import com.bugsense.trace.BugSenseHandler;
import io.evercam.androidapp.R;
import com.google.analytics.tracking.android.EasyTracker;

public class RVideoDateTimeDialog extends Activity
{
	private static String TAG = "RVideoDateTimeSelections";
	public static Date startTime = null;

	private static void intializeDateTime()
	{
		if (startTime == null)
		{
			Calendar c = Calendar.getInstance();
			c.add(Calendar.HOUR_OF_DAY, -1);
			startTime = c.getTime();
		}
	}

	public static String GetDateTimeString()
	{
		// return Year + "-" + ((Month+1) < 10? "0" + (Month+1) : (Month+1)) +
		// "-" + (Day < 10? "0" + Day : Day) + "  " + (Hour < 10? "0" + Hour :
		// Hour) + ":" + (Minute < 10? "0" + Minute : Minute) + ":" + (Second <
		// 10? "0" + Second : Second);
		// return (startTime.toString());
		intializeDateTime();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String output = sdf1.format(startTime);
		return output;
	}

	public static String GetDateTimeStringFullNoSpaces()
	{
		intializeDateTime();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String output = sdf1.format(startTime);
		return output;
	}

	public static Boolean setTime(String FDT)
	{
		if (FDT != null && FDT.length() == 17)
		{
			try
			{
				startTime = new SimpleDateFormat("yyyyMMddHHmmssSSS").parse(FDT);
				return true;
			}
			catch (ParseException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	public static String GetDateTimeStringFullNoSpacesAddMinutes(int minutes)
	{
		intializeDateTime();
		Calendar c = Calendar.getInstance();
		try
		{
			c.setTime(startTime);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		c.add(Calendar.MINUTE, minutes); // number of days to add, can also use
											// Calendar.DAY_OF_MONTH in place of
											// Calendar.DATE
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String output = sdf1.format(c.getTime());
		return output;
	}

	public static boolean IsDateTimeChanged = false;

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

			setContentView(R.layout.rvideodatetimelayout);

			if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) ((LinearLayout) this
					.findViewById(R.id.rvideodatetimelayout_relativeLaout))
					.setOrientation(LinearLayout.VERTICAL);
			intializeDateTime();

			final TimePicker tpRecordingTime = (TimePicker) findViewById(R.id.tpRecordingTime);
			tpRecordingTime.setIs24HourView(true);

			Calendar c = Calendar.getInstance();
			c.setTime(startTime);

			tpRecordingTime.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));

			tpRecordingTime.setCurrentMinute(c.get(Calendar.MINUTE));

			final DatePicker dpRecordingDate = (DatePicker) findViewById(R.id.dpRecordingDate);

			dpRecordingDate.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
					c.get(Calendar.DAY_OF_MONTH), null);

			Button btnSelectRecordingDateTime = (Button) findViewById(R.id.btnSelectRecordingDateTime);

			btnSelectRecordingDateTime.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v)
				{
					Calendar c = Calendar.getInstance();
					c.setTime(startTime);

					c.set(Calendar.YEAR, dpRecordingDate.getYear());
					c.set(Calendar.MONTH, dpRecordingDate.getMonth());
					c.set(Calendar.DAY_OF_MONTH, dpRecordingDate.getDayOfMonth());
					c.set(Calendar.HOUR_OF_DAY, tpRecordingTime.getCurrentHour());
					c.set(Calendar.MINUTE, tpRecordingTime.getCurrentMinute());
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);

					startTime = c.getTime();

					RVideoDateTimeDialog.this.finish();
				}
			});

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
