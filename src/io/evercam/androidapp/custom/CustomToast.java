package io.evercam.androidapp.custom;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class CustomToast
{
	public static void showInCenter(Context context, String message)
	{
		Toast toast = Toast.makeText(context, message,
				Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	
	public static void showInCenter(Context context, int message)
	{
		Toast toast = Toast.makeText(context, message,
				Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
}
