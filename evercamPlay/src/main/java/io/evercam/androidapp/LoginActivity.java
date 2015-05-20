package io.evercam.androidapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import io.evercam.API;
import io.evercam.ApiKeyPair;
import io.evercam.EvercamException;
import io.evercam.User;
import io.evercam.androidapp.authentication.EvercamAccount;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.tasks.CheckInternetTask;
import io.evercam.androidapp.utils.Constants;

public class LoginActivity extends ParentActivity
{
    private EditText usernameEdit;
    private EditText passwordEdit;
    private String username;
    private String password;
    private LoginTask loginTask;
    private String TAG = "evercamplay-LoginActivity";
    private CustomProgressDialog customProgressDialog;

    private enum InternetCheckType
    {
        LOGIN, SIGNUP
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        customProgressDialog = new CustomProgressDialog(this);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login);

        Button btnLogin = (Button) findViewById(R.id.btnLogin);

        TextView signUpLink = (TextView) findViewById(R.id.signupLink);
        usernameEdit = (EditText) findViewById(R.id.editUsername);
        passwordEdit = (EditText) findViewById(R.id.editPassword);

        btnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new LoginCheckInternetTask(LoginActivity.this,
                        InternetCheckType.LOGIN).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        signUpLink.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                new LoginCheckInternetTask(LoginActivity.this,
                        InternetCheckType.SIGNUP).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        hideLogoIfNecessary();
    }

    /**
     * (Currently only for portrait mode)
     * Hide Evercam logo when soft keyboard shows up, and show the logo when keyboard is hidden
     */
    public void adjustLoginFormForKeyboardChange()
    {
        final View activityRootView = findViewById(R.id.login_form);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView
                        .getHeight();
                ImageView logoImageView = (ImageView) findViewById(R.id.icon_imgview);
                //Log.d(TAG, activityRootView.getRootView().getHeight() + " - " +
                // activityRootView.getHeight() + " = " + heightDiff);
                if(heightDiff > activityRootView.getRootView().getHeight() / 3)
                {
                    logoImageView.setVisibility(View.GONE);
                }
                else
                {
                    logoImageView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * Hide logo when landscape, or when soft keyboard showing up in portrait
     */
    public void hideLogoIfNecessary()
    {
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            ImageView logoImageView = (ImageView) findViewById(R.id.icon_imgview);
            logoImageView.setVisibility(View.GONE);
        }
        else
        {
            adjustLoginFormForKeyboardChange();
        }
    }

    public void attemptLogin()
    {
        if(loginTask != null)
        {
            return;
        }

        usernameEdit.setError(null);
        passwordEdit.setError(null);

        username = usernameEdit.getText().toString();
        password = passwordEdit.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if(TextUtils.isEmpty(username))
        {
            CustomToast.showInCenter(getApplicationContext(), R.string.error_username_required);
            focusView = usernameEdit;
            cancel = true;
        }
        else if(!username.matches(Constants.REGULAR_EXPRESSION_USERNAME))
        {
            CustomToast.showInCenter(getApplicationContext(), R.string.error_invalid_username);
            focusView = usernameEdit;
            cancel = true;
        }
        else if(TextUtils.isEmpty(password))
        {
            CustomToast.showInCenter(getApplicationContext(), R.string.error_password_required);
            focusView = passwordEdit;
            cancel = true;
        }
        else if(password.contains(" "))
        {
            CustomToast.showInCenter(getApplicationContext(), R.string.error_invalid_password);
            focusView = passwordEdit;
            cancel = true;
        }

        if(cancel)
        {
            focusView.requestFocus();
        }
        else
        {
            customProgressDialog.show(getString(R.string.login_progress_signing_in));

            //Hide soft keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context
                    .INPUT_METHOD_SERVICE);
            if(getCurrentFocus() != null)
            {
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }

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
                newUser = new AppUser(evercamUser);
                newUser.setApiKeyPair(userApiKey, userApiId);
                return true;
            }
            catch(EvercamException e)
            {
                Log.e(TAG, e.toString());

                if(e.getMessage().contains(getString(R.string.prefix_invalid)) || e.getMessage()
                        .contains(getString(R.string.prefix_no_user)))
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

            if(success)
            {
                AppData.defaultUser = newUser;
                new EvercamAccount(LoginActivity.this).add(newUser);

                setResult(Constants.RESULT_TRUE);
                startCamerasActivity();

                getMixpanel().identifyUser(newUser.getUsername());
                getMixpanel().sendEvent(R.string.mixpanel_event_sign_in, null);
            }
            else
            {
                if(errorMessage != null)
                {
                    CustomToast.showInCenter(getApplicationContext(), errorMessage);
                }
                else
                {
                    EvercamPlayApplication.sendCaughtException(LoginActivity.this,
                            getString(R.string.exception_error_login));
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == Constants.REQUEST_CODE_SIGN_UP)
        {
            if(resultCode == Constants.RESULT_TRUE)
            {
                setResult(Constants.RESULT_TRUE);

                finish();
            }
        }
    }

    private void startCamerasActivity()
    {
        if(CamerasActivity.activity != null)
        {
            try
            {
                CamerasActivity.activity.finish();
            }
            catch(Exception e)
            {
                Log.e(TAG, e.toString(), e);
            }
        }

        Intent intent = new Intent(this, CamerasActivity.class);
        this.startActivity(intent);
        this.finish();
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
            if(hasNetwork)
            {
                if(type == InternetCheckType.LOGIN)
                {
                    attemptLogin();
                }
                else if(type == InternetCheckType.SIGNUP)
                {
                    Intent signupIntent = new Intent(LoginActivity.this, SignUpActivity.class);
                    startActivityForResult(signupIntent, Constants.REQUEST_CODE_SIGN_UP);
                }
            }
            else
            {
                CustomedDialog.showInternetNotConnectDialog(LoginActivity.this);
            }
        }
    }
}