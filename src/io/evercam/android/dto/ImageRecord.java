package io.evercam.android.dto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.util.Log;

//Camera object having information related to one camera received from the camba api

public class ImageRecord
{

	static final String TAG = "ImageRecord";

	private String DT;
	private int motion;
	private String Url;

	public ImageRecord()
	{
	}

	public ImageRecord(String _DT, int _motion, String _Url)
	{
		DT = _DT;
		motion = _motion;
		Url = _Url;

	}

	public String getDT()
	{
		return DT;
	}

	public int getmotion()
	{
		return motion;
	}

	public String getUrl()
	{
		return Url;
	}

	public void setDT(String _Dt)
	{
		DT = _Dt;
	}

	public void setmotion(int _motion)
	{
		motion = _motion;
	}

	public void setUrl(String _Url)
	{
		Url = _Url;
	}

	public String getFDT()
	{

		try
		{
			Date startTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(DT);

			Calendar c = Calendar.getInstance();

			c.setTime(startTime);

			// c.add(Calendar.MINUTE, minutes);

			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			String output = sdf1.format(c.getTime());
			return output;

		}
		catch (ParseException e)
		{
			return null;
		}

	}

	public void SetCameraProperty(ImageRecord obj, String propertyName, String propertyValue)
	{
		Log.i(TAG, "\"" + propertyName + "\":\"" + propertyValue + "\"");
		if (propertyValue == null || propertyValue.trim().length() == 0)
		{
			return;
		}
		else if (propertyName.equalsIgnoreCase("date"))
		{
			obj.DT = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("motion"))
		{
			try
			{
				obj.motion = Integer.parseInt(propertyValue);
			}
			catch (Exception e)
			{
			}
		}
		else if (propertyName.equalsIgnoreCase("Url"))
		{
			obj.Url = propertyValue;
		}

	}

}
