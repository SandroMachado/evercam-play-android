package io.evercam.androidapp;

import io.evercam.API;
import io.evercam.ApiKeyPair;
import io.evercam.EvercamException;
import io.evercam.User;
import io.evercam.androidapp.custom.CustomAdapter;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dal.*;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.tasks.CheckInternetTask;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.PrefsManager;
import io.evercam.androidapp.utils.PropertyReader;

import java.util.ArrayList;
import java.util.List;

import com.bugsense.trace.BugSenseHandler;
import io.evercam.androidapp.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

public class ManageAccountsActivity extends ParentActivity
{
	static String TAG = "evercamplay-ManageAccountsActivity";

	private AlertDialog alertDialog = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.initAndStartSession(this, Constants.bugsense_ApiKey);
		}

		EvercamPlayApplication.sendScreenAnalytics(this, getString(R.string.screen_manage_account));

		if (this.getActionBar() != null)
		{
			this.getActionBar().setHomeButtonEnabled(true);
			this.getActionBar().setTitle(R.string.accounts);
			this.getActionBar().setIcon(R.drawable.ic_navigation_back);
		}

		setContentView(R.layout.manage_account_activity);

		// create and start the task to show all user accounts
		ListView listview = (ListView) findViewById(R.id.email_list);
		if (AppData.appUsers != null && AppData.appUsers.size() != 0)
		{
			ListAdapter listAdapter = new CustomAdapter(ManageAccountsActivity.this,
					R.layout.manageaccountsactivity_listitem,
					R.layout.manageaccountsactivity_listitem_add_new_user_account,
					R.id.manageactivity_listitemtextvie, (ArrayList<AppUser>) AppData.appUsers);
			listview.setAdapter(listAdapter);
		}
		else
		{
			showAllAccounts();
		}

		listview.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				ListView listview = (ListView) findViewById(R.id.email_list);

				final AppUser user = (AppUser) listview.getItemAtPosition(position);

				if (user.getId() < 0) // add new user item
				{
					showAddUserDialogue(null, null, false);
					return;
				}

				final View ed_dialog_layout = getLayoutInflater().inflate(
						R.layout.manage_account_option_list, null);

				final AlertDialog dialog = CustomedDialog.getAlertDialogNoTitle(
						ManageAccountsActivity.this, ed_dialog_layout);
				dialog.show();

				Button openDefault = (Button) ed_dialog_layout.findViewById(R.id.btn_open_account);
				Button setDefault = (Button) ed_dialog_layout
						.findViewById(R.id.btn_set_default_account);
				Button delete = (Button) ed_dialog_layout.findViewById(R.id.btn_delete_account);

				if (user.getIsDefault())
				{
					openDefault.setEnabled(false);
					setDefault.setEnabled(false);
				}
				else
				{
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
				}

				if (AppData.appUsers != null && AppData.appUsers.size() == 2)
				{
					// If only one user exists, don't allow to remove this user
					delete.setEnabled(false);
				}
				else
				{
					delete.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v)
						{
							CustomedDialog.getConfirmRemoveDialog(ManageAccountsActivity.this,
									new DialogInterface.OnClickListener(){

										@Override
										public void onClick(DialogInterface warningDialog, int which)
										{
											try
											{
												DbAppUser users = new DbAppUser(
														ManageAccountsActivity.this);
												users.deleteAppUserByEmail(user.getEmail());
												if (users.getDefaultUsersCount() == 0
														&& users.getAppUsersCount() > 0)
												{
													int maxid = users.getMaxID();
													AppUser user = users.getAppUserByID(maxid);
													user.setIsDefault(true);
													users.updateAppUser(user);
													PrefsManager.saveUserEmail(
															PreferenceManager
																	.getDefaultSharedPreferences(ManageAccountsActivity.this),
															user.getEmail());
													AppData.defaultUser = user;
												}

												showAllAccounts();
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

	// Tells that what item has been selected from options. We need to call the
	// relevant code for that item.
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		try
		{
			// Handle item selection
			switch (item.getItemId())
			{
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

	private void showAddUserDialogue(String username, String password, boolean isdefault)
	{
		final View dialog_layout = getLayoutInflater().inflate(
				R.layout.manageaccountsactivity_adduser_dialogue, null);
		// View title_layout = getLayoutInflater().inflate(
		// R.layout.manageaccountsactivity_adduser_dialogue_title, null);
		alertDialog = new AlertDialog.Builder(this)
				// .setCustomTitle(title_layout)
				.setView(dialog_layout).setCancelable(false)
				.setNegativeButton(R.string.cancel, null)
				.setPositiveButton((getString(R.string.add)), null).create();

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
						new AccountCheckInternetTask(ManageAccountsActivity.this, dialog_layout)
								.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					}
				});
			}
		});

		alertDialog.show();
	}

	private void launchLogin(View view)
	{
		boolean isDefault = false;
		EditText usernameEdit = (EditText) view.findViewById(R.id.username_edit);
		EditText passwordEdit = (EditText) view.findViewById(R.id.user_password);

		String username = usernameEdit.getText().toString();
		String password = passwordEdit.getText().toString();

		if (TextUtils.isEmpty(username))
		{
			CustomToast.showInCenter(this, R.string.error_username_required);
			return;
		}
		else if (username.contains(" "))
		{
			CustomToast.showInCenter(this, R.string.error_invalid_username);
			return;
		}

		if (TextUtils.isEmpty(password))
		{
			CustomToast.showInCenter(this, R.string.error_password_required);
			return;
		}
		else if (password.contains(" "))
		{
			CustomToast.showInCenter(this, R.string.error_invalid_password);
			return;
		}
		
		AddAccountTask task = new AddAccountTask(username, password, isDefault, alertDialog);
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public void setDefaultUser(final String userId, final Boolean closeActivity,
			final AlertDialog dialogToDismiss)
	{
		try
		{
			DbAppUser dbUser = new DbAppUser(ManageAccountsActivity.this);

			List<AppUser> appUsers = dbUser.getAllAppUsers(1000);
			for (int count = 0; count < appUsers.size(); count++)
			{
				AppUser user = appUsers.get(count);
				if ((user.getId() + "").equalsIgnoreCase(userId))
				{
					if (!user.getIsDefault())
					{
						user.setIsDefault(true);
						dbUser.updateAppUser(user);
						PrefsManager.saveUserEmail(PreferenceManager
								.getDefaultSharedPreferences(ManageAccountsActivity.this),
								user.getEmail());
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
			
			if (closeActivity)
				{
					ManageAccountsActivity.this.finish();
				}
				else
				{
					showAllAccounts();
				}

				if (dialogToDismiss != null && dialogToDismiss.isShowing())
				{
					dialogToDismiss.dismiss();
				}
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString());
			BugSenseHandler.sendException(e);
			EvercamPlayApplication.sendEventAnalytics(this, R.string.category_error,
					R.string.action_error_manage_account, R.string.label_error_set_default);
			CustomedDialog.showUnexpectedErrorDialog(ManageAccountsActivity.this);
		}

		
//		new AsyncTask<String, String, String>(){
//
//			@Override
//			protected String doInBackground(String... params)
//			{
//				try
//				{
//					DbAppUser dbUser = new DbAppUser(ManageAccountsActivity.this);
//
//					List<AppUser> appUsers = dbUser.getAllAppUsers(1000);
//					for (int count = 0; count < appUsers.size(); count++)
//					{
//						AppUser user = appUsers.get(count);
//						if ((user.getId() + "").equalsIgnoreCase(params[0]))
//						{
//							if (!user.getIsDefault())
//							{
//								user.setIsDefault(true);
//								dbUser.updateAppUser(user);
//								PrefsManager.saveUserEmail(PreferenceManager
//										.getDefaultSharedPreferences(ManageAccountsActivity.this),
//										user.getEmail());
//								AppData.defaultUser = user;
//							}
//						}
//						else if (user.getIsDefault())
//						{
//							user.setIsDefault(false);
//							dbUser.updateAppUser(user);
//						}
//					}
//
//					AppData.appUsers = dbUser.getAllAppUsers(1000);
//
//					return null;
//				}
//				catch (Exception e)
//				{
//					Log.e(TAG, e.toString());
//					BugSenseHandler.sendException(e);
//					return e.getLocalizedMessage();
//				}
//			}
//
//			@Override
//			protected void onPostExecute(String error)
//			{
//				if (error != null && error.length() > 0)
//				{
//					CustomedDialog.showUnexpectedErrorDialog(ManageAccountsActivity.this);
//				}
//				if (closeActivity)
//				{
//					ManageAccountsActivity.this.finish();
//				}
//				else
//				{
//					showAllAccounts();
//				}
//
//				if (dialogToDismiss != null && dialogToDismiss.isShowing())
//				{
//					dialogToDismiss.dismiss();
//				}
//			}
//
//		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, userId);
	}

	private void showAllAccounts()
	{
		try
		{
			DbAppUser dbuser = new DbAppUser(ManageAccountsActivity.this);
			AppData.appUsers = dbuser.getAllAppUsers(100);

			ListAdapter listAdapter = new CustomAdapter(ManageAccountsActivity.this,
					R.layout.manageaccountsactivity_listitem,
					R.layout.manageaccountsactivity_listitem_add_new_user_account,
					R.id.manageactivity_listitemtextvie, (ArrayList<AppUser>) AppData.appUsers);
			ListView listview = (ListView) findViewById(R.id.email_list);
			listview.setAdapter(null);
			listview.setAdapter(listAdapter);
		}
		catch (Exception e)
		{
			if (Constants.isAppTrackingEnabled)
			{
				BugSenseHandler.sendException(e);
			}
		}
	}

	private class AddAccountTask extends AsyncTask<String, Void, Boolean>
	{
		String username;
		String password;
		boolean isDefault = false;
		AlertDialog alertDialog = null;
		AppUser newUser;
		String errorMessage = null;

		public AddAccountTask(String username, String password, boolean isDefault,
				AlertDialog alertDialog)
		{
			this.username = username;
			this.password = password;
			this.isDefault = isDefault;
			this.alertDialog = alertDialog;
		}

		@Override
		protected Boolean doInBackground(String... values)
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
				return true;
			}
			catch (EvercamException e)
			{
				if (e.getMessage().contains(getString(R.string.prefix_invalid))
						|| e.getMessage().contains(getString(R.string.prefix_no_user)))
				{
					errorMessage = e.getMessage();
				}
				else
				{
					// Do nothing, show alert dialog in onPostExecute
				}
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean success)
		{
			if (!success)
			{
				ProgressBar progressBar = (ProgressBar) alertDialog
						.findViewById(R.id.pb_loadinguser);
				progressBar.setVisibility(View.GONE);

				if (errorMessage != null)
				{
					CustomToast.showInCenter(getApplicationContext(), errorMessage);
				}
				else
				{
					EvercamPlayApplication.sendEventAnalytics(ManageAccountsActivity.this, R.string.category_error,
							R.string.action_error_manage_account, R.string.label_error_login);
					CustomedDialog.showUnexpectedErrorDialog(ManageAccountsActivity.this);
				}

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
						showAllAccounts();

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
			//Show the progress bar before the task starts
			ProgressBar progressBar = (ProgressBar) alertDialog.findViewById(R.id.pb_loadinguser);
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(Boolean hasNetwork)
		{
			if (hasNetwork)
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
