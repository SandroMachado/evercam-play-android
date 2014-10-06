package io.evercam.androidapp;

import io.evercam.API;
import io.evercam.ApiKeyPair;
import io.evercam.EvercamException;
import io.evercam.User;
import io.evercam.UserDetail;
import io.evercam.androidapp.account.AccountUtils;
import io.evercam.androidapp.account.UserProfile;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dal.DbAppUser;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.tasks.CheckInternetTask;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.PrefsManager;
import io.evercam.androidapp.utils.PropertyReader;

import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

import com.bugsense.trace.BugSenseHandler;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class SignUpActivity extends Activity
{
	private final String TAG = "evercamplay-SignUpActivity";
	// Auto filled profiles
	private String filledFirstname = "";
	private String filledLastname = "";
	private String filledEmail = "";

	private EditText firstnameEdit;
	private EditText lastnameEdit;
	private EditText usernameEdit;
	private EditText emailEdit;
	private EditText passwordEdit;
	private EditText repasswordEdit;
	private Button signupBtn;
	private Spinner countrySpinner;
	private TreeMap<String, String> countryMap;
	private View signUpFormView;
	private View signUpStatusView;
	private CreateUserTask createUserTask;
	private View focusView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.initAndStartSession(SignUpActivity.this, Constants.bugsense_ApiKey);
		}

		EvercamPlayApplication.sendScreenAnalytics(this, getString(R.string.screen_sign_up));

		setContentView(R.layout.activity_sign_up);

		setEvercamDeveloperKeypair();
		readFromAccount();
		initialPage();
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

	private void initialPage()
	{
		signUpFormView = findViewById(R.id.signup_form);
		signUpStatusView = findViewById(R.id.signup_status);
		firstnameEdit = (EditText) findViewById(R.id.forename_edit);
		lastnameEdit = (EditText) findViewById(R.id.lastname_edit);
		usernameEdit = (EditText) findViewById(R.id.username_edit);
		emailEdit = (EditText) findViewById(R.id.email_edit);
		passwordEdit = (EditText) findViewById(R.id.password_edit);
		repasswordEdit = (EditText) findViewById(R.id.repassword_edit);
		signupBtn = (Button) findViewById(R.id.sign_up_button);
		countrySpinner = (Spinner) findViewById(R.id.country_spinner);

		fillDefaultProfile();

		setSpinnerAdapter();
		signupBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v)
			{
				new SignUpCheckInternetTask(SignUpActivity.this)
						.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		});
	}

	private UserDetail checkDetails()
	{
		UserDetail user = new UserDetail();
		String firstname = firstnameEdit.getText().toString();
		String lastname = lastnameEdit.getText().toString();
		String username = usernameEdit.getText().toString();
		String email = emailEdit.getText().toString();
		String countryname = countrySpinner.getSelectedItem().toString();
		String password = passwordEdit.getText().toString();
		String repassword = repasswordEdit.getText().toString();

		if (TextUtils.isEmpty(firstname))
		{
			CustomToast.showInCenter(this, R.string.error_firstname_required);
			focusView = firstnameEdit;
			return null;
		}
		else
		{
			user.setFirstname(firstname);
		}

		if (TextUtils.isEmpty(lastname))
		{
			CustomToast.showInCenter(this, R.string.error_lastname_required);
			focusView = lastnameEdit;
			return null;
		}
		else
		{
			user.setLastname(lastname);
		}

		if (countryname.equals(getResources().getString(R.string.spinnerFistItem)))
		{
			CustomToast.showInCenter(this, R.string.countryNotSelected);
			return null;
		}
		else
		{
			String countrycode = countryMap.get(countryname).toLowerCase(Locale.UK);
			user.setCountrycode(countrycode);
		}

		if (TextUtils.isEmpty(username))
		{
			CustomToast.showInCenter(this, R.string.error_username_required);
			focusView = usernameEdit;
			return null;
		}
		else if (username.length() < 3)
		{
			CustomToast.showInCenter(this, R.string.username_too_short);
			focusView = usernameEdit;
			return null;
		}
		else if (username.contains(" "))
		{
			CustomToast.showInCenter(this, R.string.error_invalid_username);
			focusView = usernameEdit;
			return null;
		}
		else
		{
			user.setUsername(username);
		}

		if (TextUtils.isEmpty(email))
		{
			CustomToast.showInCenter(this, R.string.error_email_required);
			focusView = emailEdit;
			return null;
		}
		else if (!email.contains("@"))
		{
			CustomToast.showInCenter(this, R.string.invalidEmail);
			focusView = emailEdit;
			return null;
		}
		else
		{
			user.setEmail(email);
		}

		if (TextUtils.isEmpty(password))
		{
			CustomToast.showInCenter(this, R.string.error_password_required);
			focusView = passwordEdit;
			return null;
		}

		if (TextUtils.isEmpty(repassword))
		{
			CustomToast.showInCenter(this, R.string.error_confirmpassword_required);
			focusView = repasswordEdit;
			return null;
		}
		else if (!password.equals(repassword))
		{
			CustomToast.showInCenter(this, R.string.passwordNotMatch);
			return null;
		}
		else
		{
			user.setPassword(password);
		}
		return user;
	}

	private void initCountryMap()
	{
		countryMap = new TreeMap<String, String>();

		for (String countryCode : Locale.getISOCountries())
		{
			Locale locale = new Locale("", countryCode);
			countryMap.put(locale.getDisplayName(), countryCode);
		}
	}

	private void setSpinnerAdapter()
	{
		initCountryMap();
		Set<String> set = countryMap.keySet();
		String[] countryArray = Commons.joinStringArray(
				new String[] { getResources().getString(R.string.spinnerFistItem) },
				set.toArray(new String[0]));
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(SignUpActivity.this,
				android.R.layout.simple_spinner_item, countryArray);
		spinnerArrayAdapter.setDropDownViewResource(R.layout.country_spinner);
		countrySpinner.setAdapter(spinnerArrayAdapter);
	}

	private void readFromAccount()
	{
		try
		{
			UserProfile profile = AccountUtils.getUserProfile(this);
			if (profile.primaryEmail() != null)
			{
				filledEmail = profile.primaryEmail();
			}
			else if (profile.possibleEmails().size() > 0)
			{
				filledEmail = profile.possibleEmails().get(0);
			}

			if (profile.possibleNames().size() > 0)
			{
				String name = profile.possibleNames().get(0);
				String[] nameArray = name.split("\\s+");
				if (nameArray.length >= 2)
				{
					filledFirstname = nameArray[0];
					filledLastname = nameArray[1];
				}
			}
		}
		catch (Exception e)
		{
			// If exceptions happen here, will not influence app functionality.
			// Just catch it to avoid crashing.
			Log.e(TAG, e.toString());
		}
	}

	private void fillDefaultProfile()
	{
		firstnameEdit.setText(filledFirstname);
		lastnameEdit.setText(filledLastname);
		emailEdit.setText(filledEmail);
	}

	private void showProgress(boolean show)
	{
		signUpStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		signUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
	}

	private void setEvercamDeveloperKeypair()
	{
		PropertyReader propertyReader = new PropertyReader(getApplicationContext());
		String developerAppKey = propertyReader.getPropertyStr(PropertyReader.KEY_API_KEY);
		String developerAppID = propertyReader.getPropertyStr(PropertyReader.KEY_API_ID);
		API.setDeveloperKeyPair(developerAppKey, developerAppID);
	}

	private void attemptSignUp()
	{
		UserDetail userDetail = checkDetails();
		if (userDetail != null)
		{
			if (createUserTask != null)
			{
				createUserTask = null;
			}
			createUserTask = new CreateUserTask(userDetail);
			createUserTask.execute();
		}
		else
		{
			if (focusView != null)
			{
				focusView.requestFocus();
			}
		}
	}

	public class CreateUserTask extends AsyncTask<Void, Void, String>
	{
		private UserDetail userDetail;
		private AppUser newUser = null;
	
		public CreateUserTask(UserDetail userDetail)
		{
			this.userDetail = userDetail;
		}
	
		@Override
		protected void onPostExecute(String message)
		{
			if (message == null)
			{
				EvercamPlayApplication.sendEventAnalytics(SignUpActivity.this, 
						R.string.category_sign_up, R.string.action_signup_success, 
						R.string.label_signup_successful);
				DbAppUser dbUser = new DbAppUser(SignUpActivity.this);
	
				if (dbUser.getAppUserByUsername(newUser.getUsername()) != null)
				{
					dbUser.deleteAppUserByUsername(newUser.getUsername());
				}
				dbUser.updateAllIsDefaultFalse();
	
				dbUser.addAppUser(newUser);
				AppData.defaultUser = newUser;
				SharedPreferences sharedPrefs = PreferenceManager
						.getDefaultSharedPreferences(SignUpActivity.this);
				PrefsManager.saveUserEmail(sharedPrefs, newUser.getEmail());
				CustomToast.showInCenter(SignUpActivity.this, R.string.confirmSignUp);
				showProgress(false);
				startActivity(new Intent(SignUpActivity.this, MainActivity.class));
			}
			else
			{
				showProgress(false);
				CustomToast.showInCenter(SignUpActivity.this, message);
			}
		}
	
		@Override
		protected void onPreExecute()
		{
			showProgress(true);
		}
	
		@Override
		protected String doInBackground(Void... args)
		{
			try
			{
				User.create(userDetail);
				ApiKeyPair userKeyPair = API.requestUserKeyPairFromEvercam(
						userDetail.getUsername(), userDetail.getPassword());
				String userApiKey = userKeyPair.getApiKey();
				String userApiId = userKeyPair.getApiId();
				SharedPreferences sharedPrefs = PreferenceManager
						.getDefaultSharedPreferences(SignUpActivity.this);
				PrefsManager.saveEvercamUserKeyPair(sharedPrefs, userApiKey, userApiId);
				API.setUserKeyPair(userApiKey, userApiId);
				User evercamUser = new User(userDetail.getUsername());
				newUser = new AppUser();
				newUser.setUsername(userDetail.getUsername());
				newUser.setPassword(userDetail.getPassword());
				newUser.setIsDefault(true);
				newUser.setCountry(evercamUser.getCountry());
				newUser.setEmail(evercamUser.getEmail());
				newUser.setApiKey(userApiKey);
				newUser.setApiId(userApiId);
				return null;
			}
			catch (EvercamException e)
			{
				return e.getMessage();
			}
		}
	}

	class SignUpCheckInternetTask extends CheckInternetTask
	{
		public SignUpCheckInternetTask(Context context)
		{
			super(context);
		}
	
		@Override
		protected void onPostExecute(Boolean hasNetwork)
		{
			if (hasNetwork)
			{
				attemptSignUp();
			}
			else
			{
				CustomedDialog.showInternetNotConnectDialog(SignUpActivity.this);
			}
		}
	}
}
