package io.evercam.androidapp.utils;

import io.evercam.androidapp.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.text.Spanned;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;

public class UIUtils
{
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

	public static AlertDialog getAlertDialog(Context ctx, String title, String message,
			Spanned messageText)
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ctx,
				io.evercam.androidapp.R.style.ThemeDialog);
		dialogBuilder.setTitle(title);
		TextView textView = new TextView(ctx);
		if ((message + "").length() > 0) dialogBuilder.setMessage(message);
		textView.setText(messageText);
		textView.setPadding(14, 10, 5, 21);

		Linkify.addLinks(textView, Linkify.ALL);
		dialogBuilder.setView(textView);
		dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		AlertDialog alertDialog = dialogBuilder.create();
		alertDialog.setCanceledOnTouchOutside(false);
		return alertDialog;
	}

	public static AlertDialog getAlertDialog(Context ctx, String title, String Message, View view)
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ctx,
				io.evercam.androidapp.R.style.ThemeDialog);
		dialogBuilder.setTitle(title);

		view.setPadding(14, 10, 5, 21);

		dialogBuilder.setView(view);
		dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		AlertDialog ad = dialogBuilder.create();
		ad.setCanceledOnTouchOutside(false);
		return ad;
	}
	
	//Alert dialog with single click
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
	
	public static AlertDialog getNoInternetDialog(final Context context,
			DialogInterface.OnClickListener negativeistener)
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context,
				io.evercam.androidapp.R.style.ThemeDialog);
		dialogBuilder.setInverseBackgroundForced(false);

		dialogBuilder.setTitle(R.string.msg_network_not_connected);
		dialogBuilder.setMessage(R.string.msg_try_network_again);
		dialogBuilder.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener(){
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

	public static AlertDialog.Builder getAlertDialogBuilderNoTitle(Context ctx)
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ctx,
				io.evercam.androidapp.R.style.ThemeDialogNoTitle);

		return dialogBuilder;
	}
}
