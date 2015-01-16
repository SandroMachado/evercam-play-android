package io.evercam.androidapp.custom;

import android.app.Activity;
import android.app.ProgressDialog;

import io.evercam.androidapp.EvercamPlayApplication;

/**
 * The progress dialog that can not be canceled.
 */
public class CustomProgressDialog
{
    private ProgressDialog progressDialog;
    private Activity activity;

    public CustomProgressDialog(Activity activity)
    {
        progressDialog = new ProgressDialog(activity);
        this.activity = activity;
    }

    public void show(String message)
    {
        progressDialog.setMessage(message);
        progressDialog.setCanceledOnTouchOutside(false); // can not be canceled
        progressDialog.show();
    }

    public void setMessage(String message)
    {
        progressDialog.setMessage(message);
    }

    public void dismiss()
    {
        try
        {
            if(progressDialog != null && progressDialog.isShowing())
            {
                progressDialog.dismiss();
            }
        }
        catch(IllegalArgumentException e)
        {
            // Could happen on screen orientation changes
            // Catch this and send a exception report, as not important.
            EvercamPlayApplication.sendCaughtExceptionNotImportant(activity, e);
        }
    }
}
