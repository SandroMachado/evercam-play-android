package io.evercam.androidapp;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

import java.io.IOException;
import java.util.ArrayList;

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
import io.evercam.androidapp.tasks.CheckKeyExpirationTask;
import io.evercam.androidapp.utils.Constants;

public class ManageAccountsActivity extends ParentActivity
{
    private static String TAG = "ManageAccountsActivity";

    private AlertDialog alertDialog = null;
    private String oldDefaultUser = "";
    private CustomProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(this.getActionBar() != null)
        {
            this.getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.manage_account_activity);

        progressDialog = new CustomProgressDialog(ManageAccountsActivity.this);

        // create and start the task to show all user accounts
        ListView listview = (ListView) findViewById(R.id.email_list);

        if(AppData.defaultUser != null)
        {
            oldDefaultUser = AppData.defaultUser.getUsername();
        }

        showAllAccounts();

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

                final View optionListView = getLayoutInflater().inflate(R.layout
                        .manage_account_option_list, null);

                final AlertDialog dialog = CustomedDialog.getAlertDialogNoTitle
                        (ManageAccountsActivity.this, optionListView );
                dialog.show();

                Button openDefault = (Button) optionListView .findViewById(R.id.btn_open_account);
                Button delete = (Button) optionListView.findViewById(R.id.btn_delete_account);

                openDefault.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //Check if stored API key and ID before switching account
                        new CheckKeyExpirationTaskAccount(user, optionListView, dialog)
                                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                });

                delete.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        CustomedDialog.getConfirmRemoveDialog(ManageAccountsActivity.this,
                                new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface warningDialog, int which)
                                    {
                                        if(AppData.appUsers != null && AppData.appUsers.size() == 2)
                                        {
                                            // If only one user exists, log out the user
                                            CamerasActivity.logOutUser(ManageAccountsActivity.this);
                                        }
                                        else
                                        {
                                            new EvercamAccount(ManageAccountsActivity.this)
                                                    .remove(user.getEmail(),
                                                            new AccountManagerCallback<Boolean>()
                                                    {
                                                        @Override
                                                        public void run
                                                                (AccountManagerFuture<Boolean>
                                                                         future)
                                                        {
                                                            // This is the line that
                                                            // actually
                                                            // starts the
                                                            // call to remove the account.
                                                            try
                                                            {
                                                                boolean isAccountDeleted = future
                                                                        .getResult();
                                                                if(isAccountDeleted)
                                                                {
                                                                    showAllAccounts();
                                                                }
                                                            }
                                                            catch(OperationCanceledException e)
                                                            {
                                                                e.printStackTrace();
                                                            }
                                                            catch(IOException e)
                                                            {
                                                                e.printStackTrace();
                                                            }
                                                            catch(AuthenticatorException e)
                                                            {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    });
                                        }
                                        dialog.dismiss();
                                    }
                                }, R.string.msg_confirm_remove).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();

        // Finish this activity on restart because there are lots of opportunities
        // that the account has been changed, and it's hard to handle.
        // Finishing it is a simpler way.
        finish();
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
     * @param closeActivity   after updating, close the account manage activity or not
     * @param dialogToDismiss the account manage dialog that is showing
     */
    public void updateDefaultUser(final String userEmail, final Boolean closeActivity,
                                  final AlertDialog dialogToDismiss)
    {
        EvercamAccount evercamAccount = new EvercamAccount(this);
        evercamAccount.updateDefaultUser(userEmail);
        AppData.appUsers = evercamAccount.retrieveUserList();

        getMixpanel().identifyUser(AppData.defaultUser.getUsername());

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
        ArrayList<AppUser> appUsers = new EvercamAccount(this).retrieveUserList();

        ListAdapter listAdapter = new CustomAdapter(ManageAccountsActivity.this,
                R.layout.manage_account_list_item, R.layout.manage_account_list_item_new_user,
                R.id.account_item_email, appUsers);
        ListView listview = (ListView) findViewById(R.id.email_list);
        listview.setAdapter(null);
        listview.setAdapter(listAdapter);
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
            try
            {
                ApiKeyPair userKeyPair = API.requestUserKeyPairFromEvercam(username, password);
                String userApiKey = userKeyPair.getApiKey();
                String userApiId = userKeyPair.getApiId();
                API.setUserKeyPair(userApiKey, userApiId);
                User evercamUser = new User(username);
                newUser = new AppUser(evercamUser);
                newUser.setApiKeyPair(userApiKey, userApiId);

                // Save new user
                new EvercamAccount(ManageAccountsActivity.this).add(newUser);

                return true;
            }
            catch(EvercamException e)
            {
                if(e.getMessage().contains(getString(R.string.prefix_invalid)) || e.getMessage()
                        .contains(getString(R.string.prefix_no_user)))
                {
                    errorMessage = e.getMessage();
                }
                else
                {
                    // Do nothing, show alert dialog in onPostExecute
                }
            }
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

                getMixpanel().identifyUser(newUser.getUsername());
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

    class CheckKeyExpirationTaskAccount extends CheckKeyExpirationTask
    {
        public CheckKeyExpirationTaskAccount(AppUser appUser, View viewToDismiss, AlertDialog
                dialogToDismiss)
        {
            super(appUser, viewToDismiss, dialogToDismiss);
        }

        @Override
        protected void onPostExecute(Boolean isExpired)
        {
            if(isExpired)
            {
                new EvercamAccount(ManageAccountsActivity.this).remove(appUser.getEmail(), null);

                finish();
                Intent slideIntent = new Intent(ManageAccountsActivity.this, SlideActivity.class);
                startActivity(slideIntent);
            }
            else
            {
                progressDialog.show(ManageAccountsActivity.this.getString(R.string.switching_account));

                updateDefaultUser(appUser.getEmail(), true, dialogToDismiss);

                getMixpanel().identifyUser(appUser.getUsername());

                viewToDismiss.setEnabled(false);
                viewToDismiss.setClickable(false);
            }
        }
    }
}
