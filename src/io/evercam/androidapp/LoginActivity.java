package io.evercam.androidapp;

import io.evercam.API;
import io.evercam.ApiKeyPair;
import io.evercam.EvercamException;
import io.evercam.User;
import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dal.DbAppUser;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.tasks.CheckInternetTask;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.EvercamApiHelper;
import io.evercam.androidapp.utils.PrefsManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.bugsense.trace.BugSenseHandler;

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
	private String TAG = "evercamplay-LoginActivity";
	private CustomProgressDialog customProgressDialog;
	private TextView signUpLink;

	private enum InternetCheckType
	{
		LOGIN, SIGNUP
	};

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		customProgressDialog = new CustomProgressDialog(this);

		launchBugsense();

		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);
		setUnderLine();

		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.initAndStartSession(LoginActivity.this, Constants.bugsense_ApiKey);
		}

		EvercamPlayApplication.sendScreenAnalytics(this, getString(R.string.screen_login));

		EvercamApiHelper.setEvercamDeveloperKeypair(this);

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
				new LoginCheckInternetTask(LoginActivity.this, InternetCheckType.LOGIN)
						.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		});

		signUpLink.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				new LoginCheckInternetTask(LoginActivity.this, InternetCheckType.SIGNUP)
						.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

		if (TextUtils.isEmpty(username))
		{
			CustomToast.showInCenter(getApplicationContext(), R.string.error_username_required);
			focusView = usernameEdit;
			cancel = true;
		}
		else if (username.contains(" "))
		{
			CustomToast.showInCenter(getApplicationContext(), R.string.error_invalid_username);
			focusView = usernameEdit;
			cancel = true;
		}
		else if (TextUtils.isEmpty(password))
		{
			CustomToast.showInCenter(getApplicationContext(), R.string.error_password_required);
			focusView = passwordEdit;
			cancel = true;
		}
		else if (password.contains(" "))
		{
			CustomToast.showInCenter(getApplicationContext(), R.string.error_invalid_password);
			focusView = passwordEdit;
			cancel = true;
		}

		if (cancel)
		{
			focusView.requestFocus();
		}
		else
		{
			customProgressDialog.show(getString(R.string.login_progress_signing_in));
			loginTask = new LoginTask();
			loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	public class LoginTask extends AsyncTask<Void, Void, Boolean>
	{
		private String errorMessage = null;
		private AppUser newUser = null;

		@Override
		protected Boolean doInBackground(Void... params)
		{
			try
			{
				ApiKeyPair userKeyPair = API.requestUserKeyPairFromEvercam(username, password);
				String userApiKey = userKeyPair.getApiKey();
				String userApiId = userKeyPair.getApiId();
				API.setUserKeyPair(userApiKey, userApiId);
				User evercamUser = new User(username);
				newUser = new AppUser();
				newUser.setUsername(username);
				newUser.setPassword(password);
				newUser.setIsDefault(true);
				newUser.setCountry(evercamUser.getCountry());
				newUser.setEmail(evercamUser.getEmail());
				newUser.setApiKey(userApiKey);
				newUser.setApiId(userApiId);
				return true;
			}
			catch (EvercamException e)
			{
				Log.e(TAG, e.toString());

				if (e.getMessage().contains(getString(R.string.prefix_invalid))
						|| e.getMessage().contains(getString(R.string.prefix_no_user)))
				{
					errorMessage = e.getMessage();
				}
				else
				{
					
				}
			}
			return false;
		}

		@Override
		protected void onPostExecute(final Boolean success)
		{
			loginTask = null;
			customProgressDialog.dismiss();

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
				if (errorMessage != null)
				{
					CustomToast.showInCenter(getApplicationContext(), errorMessage);
				}
				else
				{
					EvercamPlayApplication.sendEventAnalytics(LoginActivity.this, R.string.category_error,
							R.string.action_error_login, R.string.label_error_login);
					EvercamPlayApplication.sendCaughtException(LoginActivity.this, getString(R.string.label_error_login));
					CustomedDialog.showUnexpectedErrorDialog(LoginActivity.this);
				}

				passwordEdit.setText(null);
			}
		}

		@Override
		protected void onCancelled()
		{
			loginTask = null;
			customProgressDialog.dismiss();
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();

		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.startSession(this);
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();

		if (Constants.isAppTrackingEnabled)
		{
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

	private void setUnderLine()
	{
		signUpLink = (TextView) findViewById(R.id.signupLink);
		SpannableString spanString = new SpannableString(this.getResources().getString(
				R.string.create_account));
		spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
		signUpLink.setText(spanString);
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

	class LoginCheckInternetTask extends CheckInternetTask
	{
		InternetCheckType type;

		public LoginCheckInternetTask(Context context, InternetCheckType type)
		{
			super(context);
			this.type = type;
		}

		@Override
		protected void onPostExecute(Boolean hasNetwork)
		{
			if (hasNetwork)
			{
				if (type == InternetCheckType.LOGIN)
				{
					attemptLogin();
				}
				else if (type == InternetCheckType.SIGNUP)
				{
					Intent signupIntent = new Intent(LoginActivity.this, SignUpActivity.class);
					startActivity(signupIntent);
				}
			}
			else
			{
				CustomedDialog.showInternetNotConnectDialog(LoginActivity.this);
			}
		}
	}
}