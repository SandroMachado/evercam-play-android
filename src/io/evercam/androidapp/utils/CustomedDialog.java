package io.evercam.androidapp.utils;

import io.evercam.androidapp.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;

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
	public static AlertDialog getNoInternetDialog(final Context context,
			DialogInterface.OnClickListener negativeistener)
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context,
				io.evercam.androidapp.R.style.ThemeDialog);
		dialogBuilder.setInverseBackgroundForced(false);

		dialogBuilder.setTitle(R.string.msg_network_not_connected);
		dialogBuilder.setMessage(R.string.msg_try_network_again);
		dialogBuilder.setPositiveButton(R.string.settings_capital,
				new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
						context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
					}
				});
		dialogBuilder.setNegativeButton(R.string.notNow, negativeistener);
		AlertDialog dialogNoInternet = dialogBuilder.create();
		dialogNoInternet.setCanceledOnTouchOutside(false);
		dialogNoInternet.closeOptionsMenu();
		return dialogNoInternet;
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
	 * The alert dialog for account management
	 */
	public static AlertDialog getAlertDialogNoTitleNoButton(Context ctx, View view)
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ctx,
				io.evercam.androidapp.R.style.ThemeDialogNoTitle);

		view.setPadding(14, 10, 5, 21);

		dialogBuilder.setView(view);
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
	
	public static AlertDialog getConfirmRemoveDialog(Activity activity,
			DialogInterface.OnClickListener listener)
	{
		AlertDialog comfirmLogoutDialog = new AlertDialog.Builder(activity)

		.setMessage(R.string.msg_confirm_remove).setPositiveButton(R.string.remove, listener)
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						return;
					}
				}).create();
		return comfirmLogoutDialog;
	}
}
