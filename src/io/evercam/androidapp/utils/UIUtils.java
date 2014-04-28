package io.evercam.androidapp.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Spanned;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;

public class UIUtils
{
	public static AlertDialog GetAlertDialog(Context ctx, String title, String message)
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(ctx,
				io.evercam.androidapp.R.style.ThemeDialog);// ,io.evercam.androidappapp.R.style.theme_devicedefault_dialog_borderless);
		adb.setTitle(title);
		adb.setMessage(message);
		adb.setPositiveButton("OK", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		AlertDialog ad = adb.create();
		ad.setCanceledOnTouchOutside(false);
		return ad;
	}

	public static AlertDialog GetAlertDialog(Context ctx, String title, String Message,
			Spanned messageText)
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(ctx,
				io.evercam.androidapp.R.style.ThemeDialog);// ,io.evercam.androidappapp.R.style.theme_devicedefault_dialog_borderless);
		adb.setTitle(title);
		TextView tv = new TextView(ctx);
		if ((Message + "").length() > 0) adb.setMessage(Message);
		tv.setText(messageText);
		tv.setPadding(14, 10, 5, 21);

		Linkify.addLinks(tv, Linkify.ALL);
		adb.setView(tv);
		adb.setPositiveButton("OK", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		AlertDialog ad = adb.create();
		ad.setCanceledOnTouchOutside(false);
		return ad;
	}

	public static AlertDialog GetAlertDialog(Context ctx, String title, String Message, View view)
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(ctx,
				io.evercam.androidapp.R.style.ThemeDialog);// ,io.evercam.androidappapp.R.style.theme_devicedefault_dialog_borderless);
		adb.setTitle(title);

		view.setPadding(14, 10, 5, 21);

		adb.setView(view);
		adb.setPositiveButton("OK", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		AlertDialog ad = adb.create();
		ad.setCanceledOnTouchOutside(false);
		return ad;
	}

	public static AlertDialog GetAlertDialog(Context ctx, String title, String message,
			DialogInterface.OnClickListener listener)
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(ctx,
				io.evercam.androidapp.R.style.ThemeDialog);// ,io.evercam.androidappapp.R.style.theme_devicedefault_dialog_borderless);//.Theme_DeviceDefault_Dialog);
		adb.setInverseBackgroundForced(false);

		adb.setTitle(title);
		adb.setMessage(message);
		adb.setPositiveButton("OK", listener);
		AlertDialog ad = adb.create();
		ad.setCanceledOnTouchOutside(false);
		ad.closeOptionsMenu();
		return ad;
	}

	public static AlertDialog GetAlertDialogNoTitleNoButton(Context ctx, View view)
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(ctx,
				io.evercam.androidapp.R.style.ThemeDialogNoTitle);// ,io.evercam.androidappapp.R.style.theme_devicedefault_dialog_borderless);

		view.setPadding(14, 10, 5, 21);

		adb.setView(view);
		AlertDialog ad = adb.create();
		ad.setCanceledOnTouchOutside(false);
		return ad;
	}

	public static AlertDialog.Builder GetAlertDialogBuilderNoTitle(Context ctx)
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(ctx,
				io.evercam.androidapp.R.style.ThemeDialogNoTitle);// ,io.evercam.androidappapp.R.style.theme_devicedefault_dialog_borderless);

		return adb;
	}

}
