package io.evercam.androidapp.custom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.OnClickWrapper;

import io.evercam.androidapp.R;

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

    public static void showSuperSnapshotSaved(final Activity activity, final Uri uri)
    {
        /**
         * The OnClickWrapper is needed to reattach SuperToast.OnClickListeners on orientation
         * changes.
         * It does this via a unique String tag defined in the first parameter so each
         * OnClickWrapper's tag
         * should be unique.
         */
        OnClickWrapper onClickWrapper = new OnClickWrapper("snapshotsavedtoast",
                new SuperToast.OnClickListener()
        {

            @Override
            public void onClick(View view, Parcelable token)
            {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                activity.startActivity(intent);
            }
        });

        SuperToast.cancelAllSuperToasts();
        SuperActivityToast superActivityToast = new SuperActivityToast(activity,
                SuperToast.Type.BUTTON);
        superActivityToast.setDuration(SuperToast.Duration.EXTRA_LONG);
        superActivityToast.setText(activity.getString(R.string.msg_snapshot_saved));
        superActivityToast.setButtonIcon(R.drawable.icon_gallery, activity.getString(R.string
                .view_in_gallery));
        superActivityToast.setOnClickWrapper(onClickWrapper);
        superActivityToast.show();
    }

    public static void showSuperToastShort(Activity activity, int message)
    {
        SuperActivityToast.cancelAllSuperActivityToasts();
        SuperToast superToast = new SuperToast(activity);
        superToast.setDuration(SuperToast.Duration.SHORT);
        superToast.setText(activity.getString(message));
        superToast.show();
    }

    public static void showSuperToastShort(Activity activity, String message)
    {
        SuperActivityToast.cancelAllSuperActivityToasts();
        SuperToast superToast = new SuperToast(activity);
        superToast.setDuration(SuperToast.Duration.SHORT);
        superToast.setText(message);
        superToast.show();
    }

    public static void showInCenterExtraLong(Context context, int message)
    {
        SuperToast superToast = new SuperToast(context);
        superToast.setDuration(SuperToast.Duration.EXTRA_LONG);
        superToast.setText(context.getString(message));
        superToast.setGravity(Gravity.CENTER, 0, 0);
        superToast.show();
    }
}
