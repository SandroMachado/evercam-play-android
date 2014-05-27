package io.evercam.androidapp;

import io.evercam.API;
import io.evercam.ApiKeyPair;
import io.evercam.EvercamException;
import io.evercam.User;
import io.evercam.androidapp.R;
import io.evercam.androidapp.dal.DbAppUser;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.utils.AppData;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.PrefsManager;
import io.evercam.androidapp.utils.PropertyReader;
import io.evercam.androidapp.utils.UIUtils;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.google.analytics.tracking.android.EasyTracker;

public class LoginActivity extends ParentActivity
{
	public static final int loginVerifyRequestCode = 5;
	public static int loginResultSuccessCode = 5;

	private EditText usernameEdit;
	private EditText passwordEdit;
	private String username;
	private String password;
	private LoginTask loginTask;
	private SharedPreferences sharedPrefs;
	private String TAG = "evercamapp-LoginActivity";
	private ProgressDialog progressDialog;
	private TextView signUpLink;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		launchBugsense();

		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);
		setUnderLine();
		
		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.initAndStartSession(LoginActivity.this, Constants.bugsense_ApiKey);
		}
		
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

		signUpLink.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				if (Commons.isOnline(LoginActivity.this))
				{
					Intent signupIntent = new Intent();
					signupIntent.setClass(LoginActivity.this, SignUpActivity.class);
					startActivity(signupIntent);
				}
				else
				{
					showInternetNotConnectDialog();
				}
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
			Log.v(TAG, "before login task started");
			loginTask = new LoginTask();
			loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	public class LoginTask extends AsyncTask<Void, Void, Boolean>
	{
		private String errorMessage = "LoginTaskMessage";
		private AppUser newUser = null;

		@Override
		protected Boolean doInBackground(Void... params)
		{
			Log.v(TAG, "login task started");
			
			try
			{
				ApiKeyPair userKeyPair = API.requestUserKeyPairFromEvercam(username, password);
				String userApiKey = userKeyPair.getApiKey();
				String userApiId = userKeyPair.getApiId();
				API.setUserKeyPair(userApiKey, userApiId);
				Log.v(TAG, "key and id done");
				User evercamUser = new User(username);
				Log.v(TAG, "got new user");
				newUser = new AppUser();
				newUser.setUsername(username);
				newUser.setPassword(password);
				newUser.setIsDefault(true);
				newUser.setCountry(evercamUser.getCountry());
				newUser.setEmail(evercamUser.getEmail());
				newUser.setApiKey(userApiKey);
				newUser.setApiId(userApiId);
				Log.v(TAG, "new app user done");
				return true;
			}
			catch (EvercamException e)
			{
				Log.e(TAG, e.toString());
				errorMessage = e.getMessage();
			}
			return false;
		}

		@Override
		protected void onPostExecute(final Boolean success)
		{
			Log.v(TAG, "login task finished");
			loginTask = null;
			dismissProgressDialog();

			if (success)
			{
				DbAppUser dbUser = new DbAppUser(LoginActivity.this);

				if (dbUser.getAppUserByUsername(newUser.getUsername()) != null)
				{
					dbUser.deleteAppUserByUsername(newUser.getUsername());
				}
				dbUser.updateAllIsDefaultFalse();

				dbUser.addAppUser(newUser);
				AppData.defaultUser = newUser;
				PrefsManager.saveUserEmail(sharedPrefs, newUser.getEmail());
				
				startCamerasActivity();
				// AppData.camesList = new ArrayList<Camera>();
			}
			else
			{
				Toast toast = Toast.makeText(getApplicationContext(), errorMessage,
						Toast.LENGTH_SHORT);
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
			BugSenseHandler.startSession(this);
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();

		if (Constants.isAppTrackingEnabled)
		{
			EasyTracker.getInstance().activityStop(this);
			BugSenseHandler.closeSession(this);
		}
	}

	@Override
	protected void onRestart()
	{
		super.onRestart();
		String defaultEmail = PrefsManager.getUserEmail(sharedPrefs);
		if (defaultEmail != null)
		{
			try
			{
				DbAppUser dbUser = new DbAppUser(this);
				AppUser defaultUser;
				defaultUser = dbUser.getAppUserByEmail(defaultEmail);
				AppData.defaultUser = defaultUser;
			}
			catch (NumberFormatException e)
			{
				e.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		if (AppData.defaultUser != null)
		{
			Intent intent = new Intent(this, CamerasActivity.class);
			startActivity(intent);
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
		progressDialog.setCanceledOnTouchOutside(false); // can not be canceled
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
		String developerAppKey = propertyReader.getPropertyStr(PropertyReader.KEY_API_KEY);
		String developerAppID = propertyReader.getPropertyStr(PropertyReader.KEY_API_ID);
		API.setDeveloperKeyPair(developerAppKey, developerAppID);
	}

	private void setUnderLine()
	{
		signUpLink = (TextView) findViewById(R.id.signupLink);
		SpannableString spanString = new SpannableString(this.getResources().getString(
				R.string.create_evercam_account));
		spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
		signUpLink.setText(spanString);
	}

	private void showInternetNotConnectDialog()
	{
		UIUtils.getNoInternetDialog(this, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				finish();
			}
		}).show();
	}
	
	private void startCamerasActivity()
	{
		if (CamerasActivity.activity != null)
		{
			try
			{
				CamerasActivity.activity.finish();
			}
			catch (Exception e)
			{
				Log.e(TAG, e.toString(), e);
			}
		}

		Intent intent = new Intent(this, CamerasActivity.class);
		this.startActivity(intent);

	}
}