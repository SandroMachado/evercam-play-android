package io.evercam.androidapp.custom;

import io.evercam.androidapp.CamerasActivity;
import io.evercam.androidapp.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomedDialog
{
	/**
	 * Alert dialog with title, message, and OK button.
	 */
	public static AlertDialog getAlertDialog(Context ctx, String title, String message)
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ctx,
				io.evercam.androidapp.R.style.ThemeDialog);
		dialogBuilder.setTitle(title);
		dialogBuilder.setMessage(message);
		dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){

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

	/**
	 * Helper method to show unexpected error dialog.
	 */
	public static void showUnexpectedErrorDialog(Context context)
	{
		getAlertDialog(context, context.getString(R.string.msg_error_occurred),
				context.getString(R.string.msg_exception)).show();
	}

	/**
	 * Alert dialog with single click listener
	 */
	public static AlertDialog getAlertDialog(Context ctx, String title, String message,
			DialogInterface.OnClickListener listener)
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ctx,
				io.evercam.androidapp.R.style.ThemeDialog);
		dialogBuilder.setInverseBackgroundForced(false);

		dialogBuilder.setTitle(title);
		dialogBuilder.setMessage(message);
		dialogBuilder.setPositiveButton(R.string.ok, listener);
		AlertDialog dialogWithOneButton = dialogBuilder.create();
		dialogWithOneButton.setCanceledOnTouchOutside(false);
		dialogWithOneButton.closeOptionsMenu();
		return dialogWithOneButton;
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
	 */
	private static AlertDialog getStandartStyledDialog(final Activity activity, int title,
			int message, DialogInterface.OnClickListener positiveListener,
			DialogInterface.OnClickListener negativeListener)
	{
		final View dialogLayout = activity.getLayoutInflater().inflate(
				R.layout.single_message_dialogue, null);
		TextView titleTextView = ((TextView) dialogLayout.findViewById(R.id.text_title));
		TextView messageTextView = ((TextView) dialogLayout.findViewById(R.id.text_message));
		titleTextView.setText(title);
		messageTextView.setText(message);

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity).setView(dialogLayout)
				.setCancelable(false).setPositiveButton(R.string.yes, positiveListener)
				.setNegativeButton(R.string.no, negativeListener);
		AlertDialog alertDialog = dialogBuilder.create();
		return alertDialog;
	}
	
	/**
	 * Return the styled dialog with title and message to ask for confirmation
	 * to create camera. 
	 */
	public static AlertDialog getConfirmCreateDialog(Activity activity,
			DialogInterface.OnClickListener positiveListener,
			DialogInterface.OnClickListener negativeListener)
	{
		AlertDialog comfirmCreateDialog = getStandartStyledDialog(activity, 
				R.string.dialog_title_warning, R.string.msg_confirm_create, 
				positiveListener, negativeListener);
		return comfirmCreateDialog;
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
		snapshotImageView.setBackgroundDrawable(drawable);
		snapshotDialog.setView(snapshotView);

		Window window = snapshotDialog.getWindow();
		WindowManager.LayoutParams layoutParams = window.getAttributes();

		layoutParams.y = -CamerasActivity.readScreenHeight(activity) / 9;
		window.setAttributes(layoutParams);
		return snapshotDialog;
	}
}
