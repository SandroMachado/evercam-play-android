package io.evercam.androidapp.feedback;

import io.evercam.User;
import io.evercam.androidapp.EvercamPlayApplication;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.utils.DataCollector;
import io.evercam.androidapp.utils.PropertyReader;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.github.sendgrid.SendGrid;

public class FeedbackSender 
{
	private final String TAG = "evercamplay-FeedbackSender";
	private final String TO_EMAIL = "play@evercam.io";
	private final String TITLE_FEEDBACK = "Evercam Play Feedback";
	private final String FROM_UNKNOWN = "unknown@evercam.io";
	private Context context;
	private Activity activity;
	private SendGrid sendgrid;
	
	public FeedbackSender(Activity activity)
	{
		this.activity = activity;
		this.context = activity.getApplicationContext();;
		PropertyReader propertyReader = new PropertyReader(context);
		String sandGridUsername = propertyReader.getPropertyStr(PropertyReader.KEY_SENDGRID_USERNAME);
		String sandGridPassword = propertyReader.getPropertyStr(PropertyReader.KEY_SENDGRID_PASSWORD);
		sendgrid = new SendGrid(sandGridUsername, sandGridPassword);
	}
	
	public void send(String feedbackString)
	{
		AppUser user = AppData.defaultUser;
		String fullName = "";
		if(user != null)
		{
			sendgrid.setFrom(user.getEmail());
			
			try 
			{
				User evercamUser = new User(user.getUsername());
				fullName = evercamUser.getFirstName() + " " + evercamUser.getLastName();
			} catch (Exception e) 
			{
				EvercamPlayApplication.sendCaughtException(activity, e);
				Log.e(TAG, e.toString());
			}
		}
	
		DataCollector dataCollector = new DataCollector(context);
		sendgrid.addTo(TO_EMAIL);
		sendgrid.setFrom(FROM_UNKNOWN);
		sendgrid.setSubject(TITLE_FEEDBACK);
		sendgrid.setText(fullName + " says: \n\n" + feedbackString + "\n\nVersion: " + dataCollector.getAppVersion()
				+ "\nDevice: " + DataCollector.getDeviceName()
				+ "\nAndroid " + DataCollector.getAndroidVersion()
				+ "\nNetwork: " + dataCollector.getNetworkString());

		String response = sendgrid.send();
		Log.d(TAG, "Sendgrid response: " + response);
	}
}
