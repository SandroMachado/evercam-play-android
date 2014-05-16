package io.evercam.androidapp;

import io.evercam.API;
import io.evercam.ApiKeyPair;
import io.evercam.EvercamException;
import io.evercam.User;
import io.evercam.androidapp.custom.CustomAdapter;
import io.evercam.androidapp.dal.*;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.utils.AppData;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.PropertyReader;
import io.evercam.androidapp.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

import com.bugsense.trace.BugSenseHandler;
import io.evercam.androidapp.R;
import com.google.analytics.tracking.android.EasyTracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
	static String TAG = "evercamapp-ManageAccountsActivity";

	private AlertDialog alertDialog = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.initAndStartSession(this, Constants.bugsense_ApiKey);
		}

		if (this.getActionBar() != null)
		{
			this.getActionBar().setHomeButtonEnabled(true);
			this.getActionBar().setTitle(R.string.accounts);
			this.getActionBar().setIcon(R.drawable.ic_navigation_back);
		}

		setContentView(R.layout.manageaccountsactivity_layout);

		// create and start the task to show all user accounts
		ListView listview = (ListView) findViewById(R.id.email_list);
		if (AppData.appUsers != null)
		{
			ListAdapter listAdapter = new CustomAdapter(ManageAccountsActivity.this,
					R.layout.manageaccountsactivity_listitem,
					R.layout.manageaccountsactivity_listitem_add_new_user_account,
					R.id.manageactivity_listitemtextvie, (ArrayList<AppUser>) AppData.appUsers);
			listview.setAdapter(listAdapter);
		}
		else
		{
			new ShowAllAccountsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}

		listview.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				ListView listview = (ListView) findViewById(R.id.email_list);

				final AppUser user = (AppUser) listview.getItemAtPosition(position);

				if (user.getId() < 0) // add new user item
				{
					showAddEditUserDialogue(null, null, false, false);
					return;
				}

				final View ed_dialog_layout = getLayoutInflater().inflate(
						R.layout.manageaccountsactivity_listitemoptions, null);

				final AlertDialog dialog = UIUtils.getAlertDialogNoTitleNoButton(
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
					}
				});

				setDefault.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v)
					{
						setDefaultUser(user.getId() + "", false, dialog);
						ed_dialog_layout.setEnabled(false);
						ed_dialog_layout.setClickable(false);
					}
				});

				delete.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v)
					{
						try
						{
							DbAppUser users = new DbAppUser(ManageAccountsActivity.this);
							users.deleteAppUserByEmail(user.getEmail());
							if (users.getDefaultUsersCount() == 0 && users.getAppUsersCount() > 0)
							{
								int maxid = users.getMaxID();
								AppUser user = users.getAppUserByID(maxid);
								user.setIsDefault(true);
								users.updateAppUser(user);

							}
							new ShowAllAccountsTask()
									.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
							dialog.dismiss();
						}
						catch (Exception e)
						{
							if (Constants.isAppTrackingEnabled)
							{
								BugSenseHandler.sendException(e);
							}
						}
					}
				});

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
	
//	Tells that what item has been selected from options. We need to call the relevent code for that item.
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try{
			// Handle item selection
			switch (item.getItemId()) {
			case R.id.menu_account_add:
				showAddEditUserDialogue(null,null,false,false);
				return true;
			case android.R.id.home:
				this.finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		}
		catch(Exception e)
		{
			Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
			if(Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
			return true;
		}
	}

	private void showAddEditUserDialogue(String username, String password, boolean isdefault,
			boolean isEdit)
	{
		final View dialog_layout = getLayoutInflater().inflate(
				R.layout.manageaccountsactivity_adduser_dialogue, null);
		View title_layout = getLayoutInflater().inflate(
				R.layout.manageaccountsactivity_adduser_dialogue_title, null);
		alertDialog = new AlertDialog.Builder(this).setCustomTitle(title_layout)
				.setView(dialog_layout).setCancelable(false)
				.setNegativeButton(R.string.cancel, null)
				.setPositiveButton((isEdit ? "Save" : "Add"), null).create();

		if (isEdit)
		{
			((TextView) title_layout.findViewById(R.id.txt_adduser_title)).setText("Edit Account");
		}
		if (username != null)
		{
			((EditText) dialog_layout.findViewById(R.id.username_edit)).setText(username);
		}
		if (password != null)
		{
			((EditText) dialog_layout.findViewById(R.id.user_password)).setText(password);
		}

		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.setOnShowListener(new DialogInterface.OnShowListener(){
			@Override
			public void onShow(DialogInterface dialog)
			{
				Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
				button.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View view)
					{
						boolean isDefault = false;
						EditText usernameEdit = (EditText) dialog_layout
								.findViewById(R.id.username_edit);
						EditText passwordEdit = (EditText) dialog_layout
								.findViewById(R.id.user_password);

						String username = usernameEdit.getText().toString();
						String password = passwordEdit.getText().toString();

						TextView textError = (TextView) dialog_layout
								.findViewById(R.id.txt_error_user_authentication);
						if (TextUtils.isEmpty(password))
						{
							showErrorMessageOnDialog(textError, R.string.error_password_required);
							return;
						}
						else if (password.contains(" "))
						{
							showErrorMessageOnDialog(textError, R.string.error_invalid_password);
							return;
						}

						if (TextUtils.isEmpty(username))
						{
							showErrorMessageOnDialog(textError, R.string.error_username_required);
							return;
						}
						else if (username.contains("@"))
						{
							showErrorMessageOnDialog(textError, R.string.please_use_username);
							return;
						}
						else if (username.contains(" "))
						{
							showErrorMessageOnDialog(textError, R.string.error_invalid_username);
							return;
						}
						ProgressBar progressBar = (ProgressBar) alertDialog
								.findViewById(R.id.pb_loadinguser);
						progressBar.setVisibility(View.VISIBLE);
						AddAccountTask task = new AddAccountTask(username, password, isDefault,
								alertDialog);
						task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					}
				});
			}
		});

		alertDialog.show();
	}

	public void setDefaultUser(final String userId, final Boolean closeActivity,
			final AlertDialog dialogToDismiss)
	{
		new AsyncTask<String, String, String>(){

			@Override
			protected String doInBackground(String... params)
			{
				try
				{
					DbAppUser dbUser = new DbAppUser(ManageAccountsActivity.this);

					List<AppUser> appUsers = dbUser.getAllAppUsers(1000);
					for (int count = 0; count < appUsers.size(); count++)
					{
						AppUser user = appUsers.get(count);
						if ((user.getId() + "").equalsIgnoreCase(params[0]))
						{
							if (!user.getIsDefault())
							{
								user.setIsDefault(true);
								dbUser.updateAppUser(user);
								AppData.defaultUser = user;
							}
						}
						else if (user.getIsDefault())
						{
							user.setIsDefault(false);
							dbUser.updateAppUser(user);
						}
					}

					AppData.appUsers = dbUser.getAllAppUsers(1000);

					return null;
				}
				catch (Exception e)
				{
					Log.e(TAG, e.toString());
					return e.getLocalizedMessage();
				}
			}

			@Override
			protected void onPostExecute(String error)
			{
				if (error != null && error.length() > 0)
				{
					UIUtils.getAlertDialog(ManageAccountsActivity.this, "Error Occured", error);
				}
				if (closeActivity) ManageAccountsActivity.this.finish();
				else new ShowAllAccountsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

				if (dialogToDismiss != null) dialogToDismiss.dismiss();
			}

		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, userId);
	}

	public class ShowAllAccountsTask extends AsyncTask<String, String, Boolean>
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
				if (Constants.isAppTrackingEnabled)
				{
					BugSenseHandler.sendException(e);
				}
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean showAccounts)
		{
			if (showAccounts)
			{
				ListAdapter listAdapter = new CustomAdapter(ManageAccountsActivity.this,
						R.layout.manageaccountsactivity_listitem,
						R.layout.manageaccountsactivity_listitem_add_new_user_account,
						R.id.manageactivity_listitemtextvie, (ArrayList<AppUser>) AppData.appUsers);
				ListView listview = (ListView) findViewById(R.id.email_list);
				listview.setAdapter(null);
				listview.setAdapter(listAdapter);
			}
		}
	}

	private class AddAccountTask extends AsyncTask<String, Void, String>
	{
		String username;
		String password;
		boolean isDefault = false;
		AlertDialog alertDialog = null;
		AppUser newUser;

		public AddAccountTask(String username, String password, boolean isDefault,
				AlertDialog alertDialog)
		{
			this.username = username;
			this.password = password;
			this.isDefault = isDefault;
			this.alertDialog = alertDialog;
		}

		@Override
		protected String doInBackground(String... values)
		{
			setEvercamDeveloperKeypair();
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

		@Override
		protected void onPostExecute(String result)
		{
			if (result != null)
			{
				ProgressBar progressBar = (ProgressBar) alertDialog
						.findViewById(R.id.pb_loadinguser);
				progressBar.setVisibility(View.GONE);
				TextView tverror = (TextView) alertDialog
						.findViewById(R.id.txt_error_user_authentication);
				tverror.setVisibility(View.VISIBLE);
				tverror.setText(result);
				return;
			}
			else
			{
				new AsyncTask<String, String, String>(){
					@Override
					protected String doInBackground(String... params)
					{
						try
						{
							DbAppUser dbuser = new DbAppUser(ManageAccountsActivity.this);
							AppUser oldUser = dbuser.getAppUserByEmail(newUser.getEmail());
							int defaultUsersCount = dbuser.getDefaultUsersCount();
							if (oldUser != null)
							{
								dbuser.deleteAppUserByEmail(newUser.getEmail());
								if (oldUser.getIsDefault() || defaultUsersCount == 0) isDefault = true;
							}

							// adding new user
							if (isDefault)
							{
								dbuser.updateAllIsDefaultFalse();
								newUser.setIsDefault(true);
							}
							dbuser.addAppUser(newUser);
						}
						catch (Exception e12)
						{
							if (Constants.isAppTrackingEnabled)
							{
								BugSenseHandler.sendException(e12);
							}
						}

						return null;
					}

					@Override
					protected void onPostExecute(String result)
					{
						new ShowAllAccountsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						alertDialog.dismiss();
					}
				}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		}
	}

	private void setEvercamDeveloperKeypair()
	{
		PropertyReader propertyReader = new PropertyReader(getApplicationContext());
		String developerAppKey = propertyReader.getPropertyStr(PropertyReader.KEY_API_KEY);
		String developerAppID = propertyReader.getPropertyStr(PropertyReader.KEY_API_ID);
		API.setDeveloperKeyPair(developerAppKey, developerAppID);
	}

	private void showErrorMessageOnDialog(TextView errorTextView, int message)
	{
		errorTextView.setVisibility(View.VISIBLE);
		errorTextView.setText(message);
	}
}
