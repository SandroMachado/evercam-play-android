package io.evercam.androidapp.feedback;

import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;

import com.firebase.client.Firebase;

import android.content.Context;

public class FirebaseHelper 
{
	private final String FIREBASE_URL = "https://evercam-play.firebaseio.com/stream-data";
	private Firebase rtspRef;
	private Firebase jpgRef;
	private String username;
	
	/**
	 * JSON structure:
	 * evercam-play
	 * ------stream-data
	 * ------------username
	 * --------------------jpg
	 * --------------------rtsp
	 */
	
	public FirebaseHelper(Context context)
	{
		Firebase.setAndroidContext(context);
		Firebase rootRef = new Firebase(FIREBASE_URL);
		AppUser defaultUser = AppData.defaultUser;
		
		if(defaultUser != null)
		{
			username = defaultUser.getUsername();
			//Replace 
			username = username.replace(".", "dot");
		}
		else
		{
			username = "unknown";
		}
		
		rtspRef = rootRef.child(username + "/rtsp");
		jpgRef = rootRef.child(username + "/jpg");
	}
	
	public void pushRtspItem(StreamFeedbackItem item)
	{
		Firebase pushRef = rtspRef.push();
		pushRef.setValue(item);
	}
	
	public void pushJpgItem(StreamFeedbackItem item)
	{
		Firebase pushRef = jpgRef.push();
		pushRef.setValue(item);
	}
}
