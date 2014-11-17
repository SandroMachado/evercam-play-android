package io.evercam.androidapp.email;

import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.utils.PropertyReader;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;

import com.github.sendgrid.SendGrid;

public class FeedbackSender 
{
	private final String TAG = "evercamplay-FeedbackSender";
	private final String TO_EMAIL = "play@evercam.io";
	private final String TITLE_FEEDBACK = "Evercam Play Feedback";
	private final String FROM_UNKNOWN = "unknown@evercam.io";
	private Context context;
	private SendGrid sendgrid;
	
	public FeedbackSender(Context context)
	{
		this.context = context;
		PropertyReader propertyReader = new PropertyReader(context);
		String sandGridUsername = propertyReader.getPropertyStr(PropertyReader.KEY_SENDGRID_USERNAME);
		String sandGridPassword = propertyReader.getPropertyStr(PropertyReader.KEY_SENDGRID_PASSWORD);
		sendgrid = new SendGrid(sandGridUsername, sandGridPassword);
	}
	
	public void send(String feedbackString)
	{
		AppUser user = AppData.defaultUser;
		sendgrid.addTo(TO_EMAIL);
		sendgrid.setFrom(FROM_UNKNOWN);
		sendgrid.setSubject(TITLE_FEEDBACK);
		sendgrid.setText(feedbackString + "\n\nVersion: " + getAppVersion() + "\nDevice: " + getDeviceName());
		if(user != null)
		{
			sendgrid.setFrom(user.getEmail());
		}
		String response = sendgrid.send();
		Log.d(TAG, "Sendgrid response: " + response);
	}
	
	public String getAppVersion()
	{
		String version = "";
		PackageInfo packageInfo;
		try 
		{
			packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			version = packageInfo.versionName;
		} catch (NameNotFoundException e) 
		{
			e.printStackTrace();
		}
		return version;
	}
	
	public String getDeviceName() 
	{
		  String manufacturer = Build.MANUFACTURER;
		  String model = Build.MODEL;
		  if (model.startsWith(manufacturer)) 
		  {
		    return capitalize(model);
		  } 
		  else 
		  {
		    return capitalize(manufacturer) + " " + model;
		  }
	}

	private String capitalize(String s) 
	{
	  if (s == null || s.length() == 0) 
	  {
	    return "";
	  }
	  char first = s.charAt(0);
	  if (Character.isUpperCase(first)) 
	  {
	    return s;
	  } else 
	  {
	    return Character.toUpperCase(first) + s.substring(1);
	  }
	} 
}
