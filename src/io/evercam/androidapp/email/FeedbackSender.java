package io.evercam.androidapp.email;

import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.utils.DataCollector;
import io.evercam.androidapp.utils.PropertyReader;
import android.content.Context;
import android.util.Log;

import com.github.sendgrid.SendGrid;

public class FeedbackSender 
{
	private final String TAG = "evercamplay-FeedbackSender";
	private final String TO_EMAIL = "liuting@evercam.io";
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
		DataCollector dataCollector = new DataCollector(context);
		sendgrid.addTo(TO_EMAIL);
		sendgrid.setFrom(FROM_UNKNOWN);
		sendgrid.setSubject(TITLE_FEEDBACK);
		sendgrid.setText(feedbackString + "\n\nVersion: " + dataCollector.getAppVersion()
				+ "\nDevice: " + DataCollector.getDeviceName()
				+ "\nAndroid " + DataCollector.getAndroidVersion()
				+ "\nNetwork: " + dataCollector.getNetworkString());
		if(user != null)
		{
			sendgrid.setFrom(user.getEmail());
		}
		String response = sendgrid.send();
		Log.d(TAG, "Sendgrid response: " + response);
	}
}
