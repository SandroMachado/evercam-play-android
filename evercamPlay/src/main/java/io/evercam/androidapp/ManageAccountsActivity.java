package io.evercam.androidapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.bugsense.trace.BugSenseHandler;

import io.evercam.API;
import io.evercam.ApiKeyPair;
import io.evercam.EvercamException;
import io.evercam.User;
import io.evercam.androidapp.authentication.EvercamAccount;
import io.evercam.androidapp.custom.CustomAdapter;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.tasks.CheckInternetTask;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.PrefsManager;

public class ManageAccountsActivity extends ParentActivity
{
    private static String TAG = "evercamplay-ManageAccountsActivity";

    private AlertDialog alertDialog = null;
    private String oldDefaultUser = "";
    private CustomProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(Constants.isAppTrackingEnabled)
        {
            BugSenseHandler.initAndStartSession(this, Constants.bugsense_ApiKey);
        }

        if(this.getActionBar() != null)
        {
            this.getActionBar().setDisplayHomeAsUpEnabled(true);
            this.getActionBar().setTitle(R.string.accounts);
            this.getActionBar().setIcon(R.drawable.icon_50x50);
        }

        setContentView(R.layout.manage_account_activity);

        progressDialog = new CustomProgressDialog(ManageAccountsActivity.this);

        // create and start the task to show all user accounts
        ListView listview = (ListView) findViewById(R.id.email_list);
        if(AppData.appUsers != null && AppData.appUsers.size() != 0)
        {
            oldDefaultUser = AppData.defaultUser.getUsername();
            ListAdapter listAdapter = new CustomAdapter(ManageAccountsActivity.this,
                    R.layout.manage_account_list_item,
                    R.layout.manage_account_list_item_new_user, R.id.account_item_email,
                    AppData.appUsers);
            listview.setAdapter(listAdapter);
        }
        else
        {
            showAllAccounts();
        }

        listview.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                ListView listview = (ListView) findViewById(R.id.email_list);

                final AppUser user = (AppUser) listview.getItemAtPosition(position);

                if(user.getId() < 0) // add new user item
                {
                    showAddUserDialogue(null, null, false);
                    return;
                }

                final View ed_dialog_layout = getLayoutInflater().inflate(R.layout
                        .manage_account_option_list, null);

                final AlertDialog dialog = CustomedDialog.getAlertDialogNoTitle
                        (ManageAccountsActivity.this, ed_dialog_layout);
                dialog.show();

                Button openDefault = (Button) ed_dialog_layout.findViewById(R.id.btn_open_account);
                Button setDefault = (Button) ed_dialog_layout.findViewById(R.id
                        .btn_set_default_account);
                Button delete = (Button) ed_dialog_layout.findViewById(R.id.btn_delete_account);

                if(user.getIsDefault())
                {
                    openDefault.setEnabled(false);
                    setDefault.setEnabled(false);
                }
                else
                {
                    openDefault.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            progressDialog.show(ManageAccountsActivity.this.getString(R.string
                                    .switching_account));
                            updateDefaultUser(user.getId() + "", true, dialog);
                            ed_dialog_layout.setEnabled(false);
                            ed_dialog_layout.setClickable(false);
                        }
                    });

                    setDefault.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            updateDefaultUser(user.getId() + "", false, dialog);
                            ed_dialog_layout.setEnabled(false);
                            ed_dialog_layout.setClickable(false);
                        }
                    });
                }

                if(AppData.appUsers != null && AppData.appUsers.size() == 2)
                {
                    // If only one user exists, don't allow to remove this user
                    delete.setEnabled(false);
                }
                else
                {
                    delete.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            CustomedDialog.getConfirmRemoveDialog(ManageAccountsActivity.this,
                                    new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface warningDialog,
                                                            int which)
                                        {
                                            new EvercamAccount(ManageAccountsActivity.this).remove(user.getEmail());
                                            showAllAccounts();
                                            dialog.dismiss();
                                        }
                                    }, R.string.msg_confirm_remove).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if(Constants.isAppTrackingEnabled)
        {
            BugSenseHandler.startSession(this);
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();

        if(Constants.isAppTrackingEnabled)
        {
            BugSenseHandler.closeSession(this);
        }
    }

    @Override
    public void onBackPressed()
    {
        if(!AppData.defaultUser.getUsername().equals(oldDefaultUser))
        {
            setResult(Constants.RESULT_ACCOUNT_CHANGED);
        }
        this.finish();
    }

    // Tells that what item has been selected from options. We need to call the
    // relevant code for that item.
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch(item.getItemId())
        {
            case android.R.id.home:

                if(AppData.defaultUser != null && oldDefaultUser != null)
                {
                    if(!AppData.defaultUser.getUsername().equals(oldDefaultUser))
                    {
                        setResult(Constants.RESULT_ACCOUNT_CHANGED);
                    }
                }
                else
                {
                    setResult(Constants.RESULT_ACCOUNT_CHANGED);
                }
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAddUserDialogue(String username, String password, boolean isdefault)
    {
        final View dialog_layout = getLayoutInflater().inflate(R.layout
                .manage_account_adduser_dialogue, null);

        alertDialog = new AlertDialog.Builder(this).setView(dialog_layout).setCancelable(false)
                .setNegativeButton(R.string.cancel, null).setPositiveButton((getString(R.string
                        .add)), null).create();

        if(username != null)
        {
            ((EditText) dialog_layout.findViewById(R.id.username_edit)).setText(username);
        }
        if(password != null)
        {
            ((EditText) dialog_layout.findViewById(R.id.user_password)).setText(password);
        }

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialog)
            {
                Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        new AccountCheckInternetTask(ManageAccountsActivity.this,
                                dialog_layout).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                });
            }
        });

        alertDialog.show();
    }

    private void launchLogin(View view)
    {
        EditText usernameEdit = (EditText) view.findViewById(R.id.username_edit);
        EditText passwordEdit = (EditText) view.findViewById(R.id.user_password);
        ProgressBar progressBar = (ProgressBar) alertDialog.findViewById(R.id.pb_loadinguser);

        String username = usernameEdit.getText().toString();
        String password = passwordEdit.getText().toString();

        if(TextUtils.isEmpty(username))
        {
            CustomToast.showInCenter(this, R.string.error_username_required);
            progressBar.setVisibility(View.GONE);
            return;
        }
        else if(username.contains(" "))
        {
            CustomToast.showInCenter(this, R.string.error_invalid_username);
            progressBar.setVisibility(View.GONE);
            return;
        }

        if(TextUtils.isEmpty(password))
        {
            CustomToast.showInCenter(this, R.string.error_password_required);
            progressBar.setVisibility(View.GONE);
            return;
        }
        else if(password.contains(" "))
        {
            CustomToast.showInCenter(this, R.string.error_invalid_password);
            progressBar.setVisibility(View.GONE);
            return;
        }

        AddAccountTask task = new AddAccountTask(username, password, alertDialog);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Update shared preference that stores default user's Email
     *
     * @param closeActivity after updating, close the account manage activity or not
     * @param dialogToDismiss the account manage dialog that is showing
     */
    public void updateDefaultUser(final String userEmail, final Boolean closeActivity,
                               final AlertDialog dialogToDismiss)
    {
//            DbAppUser dbUser = new DbAppUser(ManageAccountsActivity.this);
//
//            List<AppUser> appUsers = dbUser.getAllAppUsers(1000);
//            for(int count = 0; count < appUsers.size(); count++)
//            {
//                AppUser user = appUsers.get(count);
//                if((user.getId() + "").equalsIgnoreCase(userId))
//                {
//                    if(!user.getIsDefault())
//                    {
//                        user.setIsDefault(true);
//                        dbUser.updateAppUser(user);
//                        PrefsManager.saveUserEmail(PreferenceManager.getDefaultSharedPreferences
//                                (ManageAccountsActivity.this), user.getEmail());
//                        AppData.defaultUser = user;
//                    }
//                }
//                else if(user.getIsDefault())
//                {
//                    user.setIsDefault(false);
//                    dbUser.updateAppUser(user);
//                }
//            }

            PrefsManager.saveUserEmail(PreferenceManager.getDefaultSharedPreferences
                    (ManageAccountsActivity.this), userEmail);
            AppData.appUsers = new EvercamAccount(this).retrieveUserList();

            if(closeActivity)
            {
                if(!AppData.defaultUser.getUsername().equals(oldDefaultUser))
                {
                    setResult(Constants.RESULT_ACCOUNT_CHANGED);
                }
                ManageAccountsActivity.this.finish();
            }
            else
            {
                showAllAccounts();
            }

            if(dialogToDismiss != null && dialogToDismiss.isShowing())
            {
                dialogToDismiss.dismiss();
            }
    }

    private void showAllAccounts()
    {
        try
        {
            AppData.appUsers = new EvercamAccount(this).retrieveUserList();

            ListAdapter listAdapter = new CustomAdapter(ManageAccountsActivity.this,
                    R.layout.manage_account_list_item,
                    R.layout.manage_account_list_item_new_user, R.id.account_item_email,
                    AppData.appUsers);
            ListView listview = (ListView) findViewById(R.id.email_list);
            listview.setAdapter(null);
            listview.setAdapter(listAdapter);
        }
        catch(Exception e)
        {
            if(Constants.isAppTrackingEnabled)
            {
                BugSenseHandler.sendException(e);
            }
        }
    }

    private class AddAccountTask extends AsyncTask<String, Void, Boolean>
    {
        String username;
        String password;
        AlertDialog alertDialog = null;
        AppUser newUser;
        String errorMessage = null;
        ProgressBar progressBar;

        public AddAccountTask(String username, String password, AlertDialog alertDialog)
        {
            this.username = username;
            this.password = password;
            this.alertDialog = alertDialog;
            progressBar = (ProgressBar) alertDialog.findViewById(R.id.pb_loadinguser);
        }

        @Override
        protected Boolean doInBackground(String... values)
        {
//            try
//            {
                //TODO: Test how it works with adding user that already exists
//                DbAppUser dbUser = new DbAppUser(ManageAccountsActivity.this);
//                AppUser userFromEmail = dbUser.getAppUserByEmail(username);
//                AppUser userFromUsername = dbUser.getAppUserByUsername(username);
//                if(userFromEmail != null || userFromUsername != null)
//                {
//                    errorMessage = username + " " + getString(R.string.msg_account_already_added);
//                    return false;
//                }
//                else
//                {
                    try
                    {
                        ApiKeyPair userKeyPair = API.requestUserKeyPairFromEvercam(username,
                                password);
                        String userApiKey = userKeyPair.getApiKey();
                        String userApiId = userKeyPair.getApiId();
                        API.setUserKeyPair(userApiKey, userApiId);
                        User evercamUser = new User(username);
                        newUser = new AppUser();
                        newUser.setUsername(evercamUser.getUsername());
                        newUser.setPassword(password);
                        newUser.setCountry(evercamUser.getCountry());
                        newUser.setEmail(evercamUser.getEmail());
                        newUser.setApiKey(userApiKey);
                        newUser.setApiId(userApiId);

                        // Save new user
                        new EvercamAccount(ManageAccountsActivity.this).add(newUser);

                        return true;
                    }
                    catch(EvercamException e)
                    {
                        if(e.getMessage().contains(getString(R.string.prefix_invalid)) || e
                                .getMessage().contains(getString(R.string.prefix_no_user)))
                        {
                            errorMessage = e.getMessage();
                        }
                        else
                        {
                            // Do nothing, show alert dialog in onPostExecute
                        }
                    }
    //            }
//            }
//            catch(Exception e)
//            {
//                if(Constants.isAppTrackingEnabled)
//                {
//                    BugSenseHandler.sendException(e);
//                }
//            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success)
        {
            progressBar.setVisibility(View.GONE);
            if(!success)
            {
                if(errorMessage != null)
                {
                    CustomToast.showInCenter(getApplicationContext(), errorMessage);
                }
                else
                {
                    EvercamPlayApplication.sendCaughtException(ManageAccountsActivity.this,
                            getString(R.string.exception_error_login));
                    CustomedDialog.showUnexpectedErrorDialog(ManageAccountsActivity.this);
                }

                return;
            }
            else
            {
                showAllAccounts();
                alertDialog.dismiss();
            }
        }
    }

    class AccountCheckInternetTask extends CheckInternetTask
    {
        View dialogView;

        public AccountCheckInternetTask(Context context, View view)
        {
            super(context);
            this.dialogView = view;
        }

        @Override
        protected void onPreExecute()
        {
            // Show the progress bar before the task starts
            ProgressBar progressBar = (ProgressBar) alertDialog.findViewById(R.id.pb_loadinguser);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Boolean hasNetwork)
        {
            if(hasNetwork)
            {
                launchLogin(dialogView);
            }
            else
            {
                CustomedDialog.showInternetNotConnectDialog(ManageAccountsActivity.this);
            }
        }
    }
}
