package io.evercam.androidapp.custom;

import io.evercam.androidapp.CamerasActivity;
import io.evercam.androidapp.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomedDialog
{
	/**
	 * Helper method to show unexpected error dialog.
	 */
	public static void showUnexpectedErrorDialog(Activity activity)
	{
		getStandardStyledDialog(activity, R.string.msg_error_occurred,R.string.msg_exception,new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		}, null, R.string.ok, 0 ).show();
	}

	/**
	 * The dialog that prompt to connect Internet, with listener.
	 */
	public static AlertDialog getNoInternetDialog(final Activity activity,
			DialogInterface.OnClickListener negativeistener)
	{
		final View dialogLayout = activity.getLayoutInflater().inflate(
				R.layout.single_message_dialogue, null);
		TextView titleTextView = ((TextView) dialogLayout.findViewById(R.id.text_title));
		TextView messageTextView = ((TextView) dialogLayout.findViewById(R.id.text_message));
		titleTextView.setText(R.string.msg_network_not_connected);
		messageTextView.setText(R.string.msg_try_network_again);

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity)
				.setView(dialogLayout)
				.setCancelable(false)
				.setPositiveButton(R.string.settings_capital,
						new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								dialog.dismiss();
								activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
							}
						}).setNegativeButton(R.string.notNow, negativeistener);
		AlertDialog alertDialog = dialogBuilder.create();
		return alertDialog;
	}

	/**
	 * The single message dialog that contains title, a message, two buttons(Yes
	 * & No) and two listeners.
	 * 
	 * If int negativeButton == 0, it will be a dialog without negative button
	 */
	private static AlertDialog getStandardStyledDialog(final Activity activity, int title,
			int message, DialogInterface.OnClickListener positiveListener,
			DialogInterface.OnClickListener negativeListener, int positiveButton, int negativeButton)
	{
		final View dialogLayout = activity.getLayoutInflater().inflate(
				R.layout.single_message_dialogue, null);
		TextView titleTextView = ((TextView) dialogLayout.findViewById(R.id.text_title));
		TextView messageTextView = ((TextView) dialogLayout.findViewById(R.id.text_message));
		titleTextView.setText(title);
		messageTextView.setText(message);

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity).setView(dialogLayout)
				.setCancelable(false).setPositiveButton(positiveButton, positiveListener);
		if(negativeButton != 0)
		{
			dialogBuilder.setNegativeButton(negativeButton, negativeListener);
		}
		AlertDialog alertDialog = dialogBuilder.create();
		return alertDialog;
	}
	
	public static AlertDialog getCanNotPlayDialog(final Activity activity,DialogInterface.OnClickListener positiveListener )
	{
		return getStandardStyledDialog(activity, R.string.msg_unable_to_play, R.string.msg_please_check_camera,
				positiveListener, null, R.string.ok, 0);
	}

	/**
	 * Return the styled dialog with title and message to ask for confirmation
	 * to create camera.
	 */
	public static AlertDialog getConfirmCreateDialog(Activity activity,
			DialogInterface.OnClickListener positiveListener,
			DialogInterface.OnClickListener negativeListener)
	{
		AlertDialog comfirmCreateDialog = getStandardStyledDialog(activity,
				R.string.dialog_title_warning, R.string.msg_confirm_create, positiveListener,
				negativeListener, R.string.yes, R.string.no);
		return comfirmCreateDialog;
	}
	
	public static AlertDialog getConfirmQuitFeedbackDialog(Activity activity,
			DialogInterface.OnClickListener positiveListener)
	{
		AlertDialog comfirmFeedbackDialog = getStandardStyledDialog(activity,
				R.string.dialog_title_warning, R.string.msg_confirm_quit_feedback, positiveListener,
				new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						
					}}, R.string.yes, R.string.cancel);
		return comfirmFeedbackDialog;
	}

	/**
	 * The helper method to show Internet alert dialog and finish the activity.
	 */
	public static void showInternetNotConnectDialog(final Activity activity)
	{
		CustomedDialog.getNoInternetDialog(activity, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				activity.finish();
			}
		}).show();
	}

	/**
	 * The alert dialog with no title, but with a cancel button Used as add
	 * camera option dialog and account management.
	 */
	public static AlertDialog getAlertDialogNoTitle(Context ctx, View view)
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ctx,
				io.evercam.androidapp.R.style.ThemeDialogNoTitle);

		view.setPadding(14, 10, 5, 21);

		dialogBuilder.setView(view);
		dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		AlertDialog dialog = dialogBuilder.create();
		dialog.setCanceledOnTouchOutside(false);
		return dialog;
	}

	public static AlertDialog getConfirmLogoutDialog(Activity activity,
			DialogInterface.OnClickListener listener)
	{
		AlertDialog comfirmLogoutDialog = new AlertDialog.Builder(activity)

		.setMessage(R.string.msg_confirm_sign_out).setPositiveButton(R.string.yes, listener)
				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						return;
					}
				}).create();
		return comfirmLogoutDialog;
	}

	public static AlertDialog getConfirmCancelScanDialog(Activity activity,
			DialogInterface.OnClickListener listener)
	{
		AlertDialog comfirmCancelDialog = new AlertDialog.Builder(activity)

		.setMessage(R.string.msg_confirm_cancel_scan).setPositiveButton(R.string.yes, listener)
				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						return;
					}
				}).create();
		return comfirmCancelDialog;
	}
	
	public static AlertDialog getConfirmCancleAddCameraDialog(final Activity activity)
	{
		AlertDialog comfirmCancelDialog = new AlertDialog.Builder(activity)

		.setMessage(R.string.msg_confirm_cancel_add_camera).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				activity.finish();
			}
		})
				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						return;
					}
				}).create();
		return comfirmCancelDialog;
	}

	public static AlertDialog getConfirmRemoveDialog(Activity activity,
			DialogInterface.OnClickListener listener, int message)
	{
		AlertDialog comfirmLogoutDialog = new AlertDialog.Builder(activity)

		.setMessage(message).setPositiveButton(R.string.remove, listener)
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						return;
					}
				}).create();
		return comfirmLogoutDialog;
	}

	/**
	 * Return a pop up dialog that shows camera snapshot.
	 * 
	 * @param drawable
	 *            the image drawable returned to show in pop up dialog
	 */
	public static AlertDialog getSnapshotDialog(Activity activity, Drawable drawable)
	{
		AlertDialog snapshotDialog = new AlertDialog.Builder(activity).create();
		LayoutInflater mInflater = LayoutInflater.from(activity);
		final View snapshotView = mInflater.inflate(R.layout.test_snapshot_dialog, null);
		ImageView snapshotImageView = (ImageView) snapshotView
				.findViewById(R.id.test_snapshot_image);
		snapshotImageView.setImageDrawable(drawable);
		snapshotDialog.setView(snapshotView);

		Window window = snapshotDialog.getWindow();
		WindowManager.LayoutParams layoutParams = window.getAttributes();

		layoutParams.y = -CamerasActivity.readScreenHeight(activity) / 9;
		window.setAttributes(layoutParams);
		return snapshotDialog;
	}
	
	/**
	 * Return a pop up dialog that ask the user whether or not to save the snapshot
	 */
	public static AlertDialog getConfirmSnapshotDialog(Activity activity, Bitmap bitmap, DialogInterface.OnClickListener listener)
	{
		Builder snapshotDialogBuilder = new AlertDialog.Builder(activity);
		LayoutInflater mInflater = LayoutInflater.from(activity);
		final View snapshotView = mInflater.inflate(R.layout.confirm_snapshot_dialog, null);
		ImageView snapshotImageView = (ImageView) snapshotView
				.findViewById(R.id.confirm_snapshot_image);
		snapshotImageView.setImageBitmap(bitmap);
		snapshotDialogBuilder.setView(snapshotView);
		snapshotDialogBuilder.setPositiveButton(activity.getString(R.string.save), listener);
		snapshotDialogBuilder.setNegativeButton(activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				dialog.dismiss();
			}
		});
		AlertDialog snapshotDialog = snapshotDialogBuilder.create();
		snapshotDialog.setCanceledOnTouchOutside(false);

		return snapshotDialog;
	}
}
