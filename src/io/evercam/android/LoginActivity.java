package io.evercam.android;

import io.evercam.android.dal.dbAppUser;
import io.evercam.android.dto.AppUser;
import io.evercam.android.dto.Camera;
import io.evercam.android.exceptions.ConnectivityException;
import io.evercam.android.exceptions.CredentialsException;
import io.evercam.android.utils.AppData;
import io.evercam.android.utils.CLog;
import io.evercam.android.utils.CambaApiManager;
import io.evercam.android.utils.Constants;
import io.evercam.android.utils.UIUtils;

import java.util.ArrayList;

import com.bugsense.trace.BugSenseHandler;
import io.evercam.android.R;
import com.google.analytics.tracking.android.EasyTracker;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.*;

// 	This activity verifies the login and requests the cams data from the api 
public class LoginActivity extends ParentActivity
{
	public static final int loginVerifyRequestCode = 5; // code of the request
														// being sent to this
														// activity
	public static int loginResultSuccessCode = 5; // code of the success for the
													// above request code

	private String TAG = "LoginActivity";
	private EditText emailEdit;
	private EditText passwordEdit;

	private static boolean enableLogs = true;
	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (Constants.isAppTrackingEnabled)
		{
			if (Constants.isAppTrackingEnabled)
			{
				BugSenseHandler.initAndStartSession(this, Constants.bugsense_ApiKey);
			}
		}

		getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.login);

		Button btnLogin = (Button) findViewById(R.id.btnLogin);
		btnLogin.setFocusable(true);

		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(LoginActivity.this);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString("AppUserEmail", null);
		editor.putString("AppUserPassword", null);
		editor.commit();

		progressDialog = new ProgressDialog(LoginActivity.this); 
		progressDialog.setMessage("Please wait...");
		progressDialog.setCanceledOnTouchOutside(false); // on touching other
															// place
		// except dialog this dialog
		// will not be cancelled.

		// Initialize Control Pointers
		emailEdit = (EditText) findViewById(R.id.editUsername);
		passwordEdit = (EditText) findViewById(R.id.editPassword);

		// create the login button handler. When clicked, performed the
		// authentication
		btnLogin.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v)
			{
				Button btnLogin = (Button) findViewById(R.id.btnLogin);
				try
				{
					btnLogin.setEnabled(false);
					btnLogin.setClickable(false);

					progressDialog.show();

					AppData.AppUserEmail = emailEdit.getText().toString();
					AppData.AppUserPassword = passwordEdit.getText().toString();

					LoginTask loginTask = new LoginTask(); // create and start the task for
												// the login authentication
					loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
							new String[] { emailEdit.getText().toString(),
									passwordEdit.getText().toString() });

				}
				catch (Exception e)
				{
					if (enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
					UIUtils.GetAlertDialog(LoginActivity.this, "Error Occured", e.toString())
							.show();
					if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
				}
				btnLogin.setEnabled(true);

			}
		});

	}

	// task to perfrom login authentication and get the data
	private class LoginTask extends AsyncTask<String, Void, String>
	{
		// public boolean loginError = false; // whether login error incurred or
		// not

		public String dialogTitle = "Error Occured";

		@Override
		protected String doInBackground(String... login)
		{
			String response = "";
			try
			{
				// username and password already saved in CambaApiManager. So no
				// need to pass and just call the method and get the api data
				// response =
				// CambaApiManager.getCameraData();//login[0],login[1]);

				String cambaAPiKey = CambaApiManager.getCambaKey(AppData.AppUserEmail,
						AppData.AppUserPassword);
				AppData.cambaApiKey = cambaAPiKey;
				AppData.camesList = new ArrayList<Camera>(); // clear all
																// cameras

				try
				{
					io.evercam.android.dal.dbAppUser dbuser = new dbAppUser(LoginActivity.this);
					// delete the old user if already exisits
					if (dbuser.getAppUser(emailEdit.getText().toString()) != null)
					{
						dbuser.deleteAppUserForEmail(emailEdit.getText().toString());
					}
					dbuser.updateAllIsDefaultFalse(); // set all users as non
														// default

					// adding new logged in users
					AppUser newUser = new AppUser();
					newUser.setUserEmail(emailEdit.getText().toString());
					newUser.setUserPassword(passwordEdit.getText().toString());
					newUser.setApiKey(cambaAPiKey);
					newUser.setIsActive(true);
					newUser.setIsDefault(true);
					dbuser.addAppUser(newUser);
				}
				catch (Exception e)
				{
					if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
				}

			}
			catch (CredentialsException ce)
			{
				if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + ce.toString() + "::"
						+ ce.getServerHtml(), ce);
				dialogTitle = "Incorrect Credentials";
				response = ce.getMessage();
				if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ce);
			}
			catch (ConnectivityException ce)
			{
				dialogTitle = "Internet Connectivity Error";
				if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + ce.toString() + "::"
						+ ce.getServerHtml(), ce);
				response = ce.getMessage();
				CLog.email(LoginActivity.this, ce.getMessage(), ce);
			}
			catch (Exception e)
			{
				if (enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e), e);
				response = Constants.ErrorMessageGeneric;// e.getMessage();
				if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
			}
			return response;
		}

		@Override
		protected void onPostExecute(String message)
		{

			if (progressDialog != null) progressDialog.dismiss();
			if (message == null || message.toString().length() == 0)
			{
				// Save the credentials in CambaApiManager for future use
				AppData.AppUserEmail = emailEdit.getText().toString();
				AppData.AppUserPassword = passwordEdit.getText().toString();

				// save the credentials for future use for auto login on startup
				// if(chkRememberMe.isChecked())
				// {
				SharedPreferences sharedPrefs = PreferenceManager
						.getDefaultSharedPreferences(LoginActivity.this);
				SharedPreferences.Editor editor = sharedPrefs.edit();
				editor.putString("AppUserEmail", AppData.AppUserEmail);
				editor.putString("AppUserPassword", AppData.AppUserPassword);
				editor.commit();
				// }

				setResult(loginResultSuccessCode); // tell that user has logged
													// in successfully. Moreover
													// API data has also been
													// fetched

				LoginActivity.this.finish();
				return;

			}

			else
			{
				UIUtils.GetAlertDialog(LoginActivity.this, dialogTitle, message).show();
			}
			((Button) findViewById(R.id.btnLogin)).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.btnLogin)).setEnabled(true);
			((Button) findViewById(R.id.btnLogin)).setClickable(true);
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();

		if (Constants.isAppTrackingEnabled)
		{
			EasyTracker.getInstance().activityStart(this);
			if (Constants.isAppTrackingEnabled) BugSenseHandler.startSession(this);
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();

		if (Constants.isAppTrackingEnabled)
		{
			EasyTracker.getInstance().activityStop(this);
			if (Constants.isAppTrackingEnabled) BugSenseHandler.closeSession(this);
		}
	}
}