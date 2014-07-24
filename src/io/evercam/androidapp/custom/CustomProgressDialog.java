package io.evercam.androidapp.custom;
import android.app.Activity;
import android.app.ProgressDialog;

/**
 * The progress dialog that can not be canceled.
 */
public class CustomProgressDialog
{
	private ProgressDialog progressDialog;
	
	public CustomProgressDialog(Activity activity)
	{
		progressDialog = new ProgressDialog(activity);
	}
	
	public void show(String message)
	{
		progressDialog.setMessage(message);
		progressDialog.setCanceledOnTouchOutside(false); // can not be canceled
		progressDialog.show();
	}

	public void dismiss()
	{
		if (progressDialog != null && progressDialog.isShowing())
		{
			progressDialog.dismiss();
		}
	}
}
