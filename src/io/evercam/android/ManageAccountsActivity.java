package io.evercam.android;

import io.evercam.android.custom.CustomAdapter;
import io.evercam.android.dal.*;
import io.evercam.android.dto.AppUser;
import io.evercam.android.exceptions.ConnectivityException;
import io.evercam.android.exceptions.CredentialsException;
import io.evercam.android.utils.AppData;
import io.evercam.android.utils.CLog;
import io.evercam.android.utils.CambaApiManager;
import io.evercam.android.utils.Commons;
import io.evercam.android.utils.Constants;
import io.evercam.android.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

import com.bugsense.trace.BugSenseHandler;
import io.evercam.android.R;
import com.google.analytics.tracking.android.EasyTracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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
import android.widget.TextView;

public class ManageAccountsActivity extends ParentActivity
{
	static String TAG = "ManageAccountsActivity";

	private AlertDialog alertDialog = null;

	public class ShowAllAccounts extends AsyncTask<String, String, Boolean>
	{
		@Override
		protected Boolean doInBackground(String... params)
		{
			try
			{
				DbAppUser dbuser = new DbAppUser(ManageAccountsActivity.this);
				AppData.appUsers = dbuser.getAllAppUsers(100);
				return true;
			}
			catch (Exception e)
			{
				if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean showAccounts)
		{
			if (showAccounts)
			{
				ListAdapter adp = new CustomAdapter(ManageAccountsActivity.this,
						R.layout.manageaccountsactivity_listitem,
						R.layout.manageaccountsactivity_listitem_add_new_user_account,
						R.id.manageactivity_listitemtextvie, (ArrayList<AppUser>) AppData.appUsers);
				ListView listview = (ListView) findViewById(R.id.email_list);
				listview.setAdapter(null);
				listview.setAdapter(adp);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (Constants.isAppTrackingEnabled)
		{
			if (Constants.isAppTrackingEnabled) BugSenseHandler.initAndStartSession(this,
					Constants.bugsense_ApiKey);
		}

		if (this.getActionBar() != null)
		{
			this.getActionBar().setHomeButtonEnabled(true);
			this.getActionBar().setTitle("Accounts");
			this.getActionBar().setIcon(R.drawable.ic_navigation_back);
		}

		setContentView(R.layout.manageaccountsactivity_layout);

		// create and start the task to show all useraccounts
		ListView listview = (ListView) findViewById(R.id.email_list);
		if (AppData.appUsers != null)
		{
			ListAdapter adp = new CustomAdapter(ManageAccountsActivity.this,
					R.layout.manageaccountsactivity_listitem,
					R.layout.manageaccountsactivity_listitem_add_new_user_account,
					R.id.manageactivity_listitemtextvie, (ArrayList<AppUser>) AppData.appUsers);
			listview.setAdapter(adp);
		}
		else
		{
			new ShowAllAccounts().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
		}

		listview.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{

				ListView listview = (ListView) findViewById(R.id.email_list);

				final AppUser user = (AppUser) listview.getItemAtPosition(position);
				Log.i("sajjad", "LongClick{" + user.toString() + "}[" + view.toString() + "]["
						+ position + "][" + id + "]");

				if (user.getId() < 0) // add new user item
				{
					showAddEditUserDialogue(null, null, false, false);
					return;
				}

				final View ed_dialog_layout = getLayoutInflater().inflate(
						R.layout.manageaccountsactivity_listitemoptions, null);

				final AlertDialog dialog = UIUtils.GetAlertDialogNoTitleNoButton(
						ManageAccountsActivity.this, ed_dialog_layout);
				dialog.show();

				Button cancel = (Button) ed_dialog_layout.findViewById(R.id.btn_cancel);
				Button openDefault = (Button) ed_dialog_layout.findViewById(R.id.btn_open_account);
				Button setDefault = (Button) ed_dialog_layout
						.findViewById(R.id.btn_set_default_account);
				Button delete = (Button) ed_dialog_layout.findViewById(R.id.btn_delete_account);
				Button edit = (Button) ed_dialog_layout.findViewById(R.id.btn_edit_account);

				cancel.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v)
					{
						dialog.dismiss();
					}
				});

				openDefault.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v)
					{
						setDefaultUser(user.getId() + "", true, dialog);
						ed_dialog_layout.setEnabled(false);
						ed_dialog_layout.setClickable(false);
						// dialog.dismiss();
						// finish activity
					}
				});

				setDefault.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v)
					{
						setDefaultUser(user.getId() + "", false, dialog);
						ed_dialog_layout.setEnabled(false);
						ed_dialog_layout.setClickable(false);
						// dialog.dismiss();
					}
				});

				delete.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v)
					{
						try
						{
							DbAppUser users = new DbAppUser(ManageAccountsActivity.this);
							users.deleteAppUserForEmail(user.getEmail());
							if (users.getDefaultUsersCount() == 0 && users.getAppUsersCount() > 0)
							{
								int maxid = users.getMaxID();
								AppUser user = users.getAppUserByID(maxid);
								user.setIsDefault(true);
								users.updateAppUser(user);

								Commons.setDefaultUserForApp(ManageAccountsActivity.this,
										user.getEmail(), user.getPassword(),
										user.getApiKey(), true);
							}
							else if (users.getAppUsersCount() == 0)
							{
								Commons.setDefaultUserForApp(ManageAccountsActivity.this, null,
										null, null, true);
							}
							new ShowAllAccounts().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
									"");
							dialog.dismiss();
						}
						catch (Exception e)
						{
							if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
						}
					}
				});

				// / Edit

				edit.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v)
					{
						dialog.dismiss();
						showAddEditUserDialogue(user.getEmail(), user.getPassword(),
								user.getIsDefault(), true);
					}
				});

			}

		});

	}

	private void showAddEditUserDialogue(String _email, String _password, boolean _isdefault,
			boolean _isedit)
	{
		final View dialog_layout = getLayoutInflater().inflate(
				R.layout.manageaccountsactivity_adduser_dialogue, null);
		View title_layout = getLayoutInflater().inflate(
				R.layout.manageaccountsactivity_adduser_dialogue_title, null);
		alertDialog = new AlertDialog.Builder(this).setCustomTitle(title_layout)
				.setView(dialog_layout).setCancelable(false).setNegativeButton("Cancel", null)
				.setPositiveButton((_isedit ? "Save" : "Add"), null).create();

		if (_isedit)
		{
			((TextView) title_layout.findViewById(R.id.txt_adduser_title)).setText("Edit Account");
		}
		if (_email != null)
		{
			((EditText) dialog_layout.findViewById(R.id.user_email)).setText(_email);
		}
		if (_password != null)
		{
			((EditText) dialog_layout.findViewById(R.id.user_password)).setText(_password);
		}
		// if(_isdefault) {((CheckBox)
		// dialog_layout.findViewById(R.id.chk_isdefault)).setChecked(true);}

		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.setOnShowListener(new DialogInterface.OnShowListener(){
			@Override
			public void onShow(DialogInterface dialog)
			{
				Button b = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
				b.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View view)
					{
						String Email, Password;
						boolean isDefault = false;
						EditText email = (EditText) dialog_layout.findViewById(R.id.user_email);
						EditText password = (EditText) dialog_layout
								.findViewById(R.id.user_password);
						// CheckBox chkdefault = (CheckBox)
						// dialog_layout.findViewById(R.id.chk_isdefault);

						Email = email.getText().toString();
						Password = password.getText().toString();

						if (Email == null || Email.length() == 0)
						{
							TextView tverror = (TextView) dialog_layout
									.findViewById(R.id.txt_error_user_authentication);
							tverror.setVisibility(View.VISIBLE);
							tverror.setText("Please enter email address.");
							return;
						}
						if (Password == null || Password.length() == 0)
						{
							TextView tverror = (TextView) dialog_layout
									.findViewById(R.id.txt_error_user_authentication);
							tverror.setVisibility(View.VISIBLE);
							tverror.setText("Please enter password");
							return;
						}
						// isDefault = chkdefault.isChecked();
						ProgressBar pb = (ProgressBar) alertDialog
								.findViewById(R.id.pb_loadinguser);
						pb.setVisibility(View.VISIBLE);
						AddEditUserAccountTask task = new AddEditUserAccountTask(Email, Password,
								isDefault, alertDialog);
						task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
					}
				});
			}
		});

		alertDialog.show();

	}

	private class AddEditUserAccountTask extends AsyncTask<String, Void, String>
	{
		String Email = "";
		String Password = "";
		boolean isDefault = false;
		AlertDialog ad = null;
		String key = "";

		public AddEditUserAccountTask(String _email, String _password, boolean _isDefault,
				AlertDialog _ad)
		{
			Email = _email;
			Password = _password;
			isDefault = _isDefault;
			ad = _ad;

		}

		@Override
		protected String doInBackground(String... values)
		{

			try
			{
				key = CambaApiManager.getCambaKey(Email, Password);
				// key = "sajjad";
			}
			catch (CredentialsException ce)
			{
				Log.e(TAG,
						"Login Error: Server returned:" + ce.toString() + "::" + ce.getServerHtml(),
						ce);
				if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ce);
				return ce.getMessage();

			}
			catch (ConnectivityException ce)
			{
				Log.e(TAG,
						"Login Error: Server returned:" + ce.toString() + "::" + ce.getServerHtml(),
						ce);
				CLog.email(ManageAccountsActivity.this, ce.getMessage(), ce);
				return ce.getMessage();
			}
			catch (Exception e)
			{
				Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e), e);
				if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
				return Constants.ErrorMessageGeneric;
			}
			return null;

		}

		@Override
		protected void onPostExecute(String result)
		{

			if (result != null) // exception occured
			{
				ProgressBar pb = (ProgressBar) ad.findViewById(R.id.pb_loadinguser);
				pb.setVisibility(View.GONE);
				TextView tverror = (TextView) ad.findViewById(R.id.txt_error_user_authentication);
				tverror.setVisibility(View.VISIBLE);
				tverror.setText(result);
				return;
			}
			else
			{
				new AsyncTask<String, String, String>()
				{
					@Override
					protected String doInBackground(String... params)
					{
						try
						{
							io.evercam.android.dal.DbAppUser dbuser = new DbAppUser(
									ManageAccountsActivity.this);
							AppUser oldUser = dbuser.getAppUser(Email);
							int defaultUsersCount = dbuser.getDefaultUsersCount();
							if (oldUser != null)
							{
								dbuser.deleteAppUserForEmail(Email);
								if (oldUser.getIsDefault() || defaultUsersCount == 0) isDefault = true;
							}
							else if (defaultUsersCount == 0)
							{
								isDefault = true;
							}

							// adding new user
							AppUser newUser = new AppUser();
							newUser.setEmail(Email);
							newUser.setPassword(Password);
							newUser.setApiKey(key);
							newUser.setIsDefault(isDefault);
							if (isDefault)
							{
								dbuser.updateAllIsDefaultFalse();
								Commons.setDefaultUserForApp(ManageAccountsActivity.this,
										newUser.getEmail(), newUser.getPassword(),
										newUser.getApiKey(), true);
							}
							dbuser.addAppUser(newUser);
						}
						catch (Exception e12)
						{
							if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e12);
						}

						return null;
					}

					@Override
					protected void onPostExecute(String result)
					{
						new ShowAllAccounts().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
						ad.dismiss();
					}
				}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
			}
		}
	}

	// preferences options for this screen
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		try
		{
			if (this != null) return true; // not showing any menu because now
											// add new user menu option comes in
											// the list being shown in activity
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.menu_manageaccounts_activity, menu);

			return true;
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString(), e);
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
		}
		return true;
	}

	// Tells that what item has been selected from options. We need to call the
	// relevent code for that item.
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		try
		{
			// Handle item selection
			switch (item.getItemId())
			{
			case R.id.menu_account_add:
				// careate add account dialoge
				showAddEditUserDialogue(null, null, false, false);
				return true;
			case android.R.id.home:
				this.finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
			return true;
		}
	}

	public void setDefaultUser(String userID, final Boolean closeActivity,
			final AlertDialog dialogToDismiss)
	{
		// creating a new task that will update the db's
		new AsyncTask<String, String, String>(){

			@Override
			protected String doInBackground(String... params)
			{
				try
				{
					DbAppUser dbuser = new DbAppUser(ManageAccountsActivity.this);

					List<AppUser> appUsers = dbuser.getAllActiveAppUsers(1000);
					AppUser defaultUser = null;
					for (int i = 0; i < appUsers.size(); i++)
					{
						AppUser u = appUsers.get(i);
						if ((u.getId() + "").equalsIgnoreCase(params[0]))
						{
							if (!u.getIsDefault())
							{
								u.setIsDefault(true);
								dbuser.updateAppUser(u);
								defaultUser = u;
							}

						}
						else if (u.getIsDefault())
						{
							u.setIsDefault(false);
							dbuser.updateAppUser(u);
						}

					}
					Commons.setDefaultUserForApp(ManageAccountsActivity.this,
							defaultUser.getEmail(), defaultUser.getPassword(),
							defaultUser.getApiKey(), false);

					AppData.appUsers = dbuser.getAllActiveAppUsers(1000);

					return null;
				}
				catch (Exception e)
				{
					return e.getLocalizedMessage();
				}
			}

			@Override
			protected void onPostExecute(String error)
			{
				if (error != null && error.length() > 0)
				{
					UIUtils.GetAlertDialog(ManageAccountsActivity.this, "Error Occured", error);
				}
				if (closeActivity) ManageAccountsActivity.this.finish();
				else new ShowAllAccounts().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");

				if (dialogToDismiss != null) dialogToDismiss.dismiss();
			}

		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, userID);
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
