package io.evercam.android;

import io.evercam.API;
import io.evercam.ApiKeyPair;
import io.evercam.EvercamException;
import io.evercam.User;
import io.evercam.android.dal.dbAppUser;
import io.evercam.android.dto.AppUser;
import io.evercam.android.dto.Camera;
import io.evercam.android.utils.AppData;
import io.evercam.android.utils.CambaApiManager;
import io.evercam.android.utils.Constants;
import io.evercam.android.utils.PrefsManager;
import io.evercam.android.utils.PropertyReader;

import java.util.ArrayList;

import com.bugsense.trace.BugSenseHandler;

import io.evercam.android.R;

import com.google.analytics.tracking.android.EasyTracker;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.*;

public class LoginActivity extends ParentActivity
{
	public static final int loginVerifyRequestCode = 5; // code of the request
														// being sent to this
														// activity
	public static int loginResultSuccessCode = 5; // code of the success for the
													// above request code

	private EditText usernameEdit;
	private EditText passwordEdit;
	private String username;
	private String password;
	private LoginTask loginTask;
	private SharedPreferences sharedPrefs;
	private String developerAppKey;
	private String developerAppID;

	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		launchBugsense();
		
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);
		
		setEvercamDeveloperKeypair();

		Button btnLogin = (Button) findViewById(R.id.btnLogin);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString("AppUserEmail", null);
		editor.putString("AppUserPassword", null);
		editor.commit();

		usernameEdit = (EditText) findViewById(R.id.editUsername);
		passwordEdit = (EditText) findViewById(R.id.editPassword);

		btnLogin.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v)
			{
				attemptLogin();
			}
		});

	}

	public void attemptLogin()
	{
		if (loginTask != null)
		{
			return;
		}

		usernameEdit.setError(null);
		passwordEdit.setError(null);

		username = usernameEdit.getText().toString();
		password = passwordEdit.getText().toString();

		boolean cancel = false;
		View focusView = null;

		if (TextUtils.isEmpty(password))
		{
			passwordEdit.setError(getString(R.string.error_password_required));
			focusView = passwordEdit;
			cancel = true;
		}
		else if (password.contains(" "))
		{
			passwordEdit.setError(getString(R.string.error_invalid_password));
			focusView = passwordEdit;
			cancel = true;
		}

		if (TextUtils.isEmpty(username))
		{
			usernameEdit.setError(getString(R.string.error_username_required));
			focusView = usernameEdit;
			cancel = true;
		}
		else if (username.contains("@"))
		{
			usernameEdit.setError(getString(R.string.please_use_username));
			focusView = usernameEdit;
			cancel = true;
		}
		else if (username.contains(" "))
		{
			usernameEdit.setError(getString(R.string.error_invalid_username));
			focusView = usernameEdit;
			cancel = true;
		}

		if (cancel)
		{
			focusView.requestFocus();
		}
		else
		{
			showProgressDialog();
			loginTask = new LoginTask();
			loginTask.execute();
		}
	}
	
	public class LoginTask extends AsyncTask<Void, Void, Boolean>
	{
		private String errorMessage = "";

		@Override
		protected Boolean doInBackground(Void... params)
		{
			try
			{
				ApiKeyPair userKeyPair = API.requestUserKeyPairFromEvercam(username, password);
				String userApiKey = userKeyPair.getApiKey();
				String userApiId = userKeyPair.getApiId();
				PrefsManager.saveEvercamUserKeyPair(sharedPrefs, userApiKey, userApiId);
				API.setUserKeyPair(userApiKey, userApiId);
				User evercamUser = new User(username);
				return true;
			}
			catch (EvercamException e)
			{
				errorMessage = e.getMessage();
			}
			return false;
		}

		@Override
		protected void onPostExecute(final Boolean success)
		{
			loginTask = null;
			dismissProgressDialog();

			if (success)
			{
				try{
					AppData.AppUserEmail =  "liuting.du@mhlabs.net";
					AppData.AppUserPassword =  "kangtaooo";
				String cambaAPiKey = CambaApiManager.getCambaKey(AppData.AppUserEmail, AppData.AppUserPassword);
				AppData.cambaApiKey = cambaAPiKey;
				AppData.camesList = new ArrayList<Camera>(); // clear all cameras
				
					dbAppUser dbuser = new dbAppUser(LoginActivity.this);
//					delete the old user if already exisits
					if( dbuser.getAppUser("liuting.du@mhlabs.net") != null)
					{
						dbuser.deleteAppUserForEmail("liuting.du@mhlabs.net");
					}
					dbuser.updateAllIsDefaultFalse(); // set all users as non default
					
					// adding new logged in users
					AppUser newUser = new AppUser();
					newUser.setUserEmail("liuting.du@mhlabs.net");
					newUser.setUserPassword("kangtaooo");
					newUser.setApiKey(cambaAPiKey);
					newUser.setIsActive(true);
					newUser.setIsDefault(true);
					dbuser.addAppUser(newUser);
				SharedPreferences.Editor editor = sharedPrefs.edit(); 
				editor.putString("AppUserEmail", AppData.AppUserEmail); 
				editor.putString("AppUserPassword", AppData.AppUserPassword); 
				editor.commit(); 
				
				}catch(Exception e)
				{
					e.printStackTrace();
				}

				finishLoginActivity();
			}
			else
			{
				Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				passwordEdit.setText(null);
			}
		}

		@Override
		protected void onCancelled()
		{
			loginTask = null;
			dismissProgressDialog();
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
	
	private void launchBugsense()
	{
		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.initAndStartSession(this, Constants.bugsense_ApiKey);
		}
	}
	
	private void showProgressDialog()
	{
		progressDialog = new ProgressDialog(LoginActivity.this); 
		progressDialog.setMessage(getString(R.string.login_progress_signing_in));
		progressDialog.setCanceledOnTouchOutside(false); //can not be canceled
		progressDialog.show();
	}
	
	private void dismissProgressDialog()
	{
		if (progressDialog != null && progressDialog.isShowing()) 
		{
			progressDialog.dismiss();
		}
	}
	
	private void setEvercamDeveloperKeypair()
	{
		PropertyReader propertyReader = new PropertyReader(getApplicationContext());
		developerAppKey = propertyReader.getPropertyStr(PropertyReader.KEY_API_KEY);
		developerAppID = propertyReader.getPropertyStr(PropertyReader.KEY_API_ID);
		API.setDeveloperKeyPair(developerAppKey, developerAppID);
	}
	
	private void finishLoginActivity()
	{
		setResult(loginResultSuccessCode);
		this.finish();
	}
}