package io.evercam.androidapp.custom;

import io.evercam.androidapp.R;
import io.evercam.androidapp.video.VideoActivity;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.OnClickWrapper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

public class CustomToast
{
	public static void showInCenter(Context context, String message)
	{
		Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	public static void showInCenter(Context context, int message)
	{
		Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	public static void showInBottom(Context context, int message)
	{
		Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.BOTTOM, 0, 0);
		toast.show();
	}

	public static void showInCenterLong(Context context, String message)
	{
		Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	public static void showInCenterLong(Context context, int message)
	{
		Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	public static void showSnapshotTestResult(Context context, int message)
	{
		Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 200);
		toast.show();
	}
	
	public static void showSuperSnapshotSaved(final Activity activity)
	{
		/**
		 * The OnClickWrapper is needed to reattach SuperToast.OnClickListeners on orientation changes. 
		 * It does this via a unique String tag defined in the first parameter so each OnClickWrapper's tag 
		 * should be unique.
		 */
		OnClickWrapper onClickWrapper = new OnClickWrapper("snapshotsavedtoast", new SuperToast.OnClickListener() {

		    @Override
		    public void onClick(View view, Parcelable token) 
		    {
				Intent intent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("content://media/internal/images/media"));
				activity.startActivity(intent);
		    }
		});
		
		SuperActivityToast.cancelAllSuperActivityToasts();
		SuperActivityToast superActivityToast = new SuperActivityToast(activity, SuperToast.Type.BUTTON);
		superActivityToast.setDuration(SuperToast.Duration.EXTRA_LONG);
		superActivityToast.setText(activity.getString(R.string.msg_snapshot_saved));
		superActivityToast.setButtonIcon(R.drawable.icon_gallery, activity.getString(R.string.view_in_gallery));
		superActivityToast.setOnClickWrapper(onClickWrapper);
		superActivityToast.show();		
	}
	
	public static void showSuperToastShort(Activity activity, int message)
	{
		SuperActivityToast.cancelAllSuperActivityToasts();
		SuperActivityToast superActivityToast = new SuperActivityToast(activity);
		superActivityToast.setDuration(SuperToast.Duration.SHORT);
		superActivityToast.setText(activity.getString(message));
		superActivityToast.show();	
	}
}
