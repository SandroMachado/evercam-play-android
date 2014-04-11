package io.evercam.android;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.*;

import io.evercam.android.custom.AboutDialog;
import io.evercam.android.custom.CameraLayout;
import io.evercam.android.dal.dbAppUser;
import io.evercam.android.dal.dbCamera;
import io.evercam.android.dal.dbNotifcation;
import io.evercam.android.dto.AppUser;
import io.evercam.android.dto.Camera;
import io.evercam.android.dto.CameraNotification;
import io.evercam.android.dto.ImageLoadingStatus;
import io.evercam.android.exceptions.ConnectivityException;
import io.evercam.android.exceptions.CredentialsException;
import io.evercam.android.slidemenu.*;
import io.evercam.android.utils.AppData;
import io.evercam.android.utils.CambaApiManager;
import io.evercam.android.utils.Commons;
import io.evercam.android.utils.Constants;
import io.evercam.android.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

import com.bugsense.trace.BugSenseHandler;
import io.evercam.android.R;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gcm.GCMRegistrar;

import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class CamsActivity extends ParentActivity implements
		SlideMenuInterface.OnSlideMenuItemClickListener
{

	private static final String TAG = "CamsActivity"; // TAG is used for
														// logging. Filter when
														// searching from logs
														// in LogCat

	RegisterGCMAlertsServiceTask RegisterTask = null;

	ProgressDialog pdLoading;
	SlidingDrawer sdDrawer;

	private MenuItem refresh;

	private SlideMenu slidemenu;

	private int TotalCamerasInGrid = 0;

	public int slideoutMenuAnimationTime = 255;

	public static CamsActivity _activity = null;

	private boolean isUsersAccountsActivityStarted = false;

	static boolean enableLogs = true; // Whether log for the events or not

	public static int camerasperrow = 2; // lstgridcamerasperrow // tells
											// whether how many cameras are
											// being shown in one row

	Bundle _savedInstanceState = null; // instance state that tells about the
										// previous activity instance state

	void addUsersToDropdownActionBar()
	{
		boolean taskStarted = false;

		while (!taskStarted)
		{

			try
			{

				StopAllCameraViews();

				if (AppData.AppUserEmail == null || AppData.AppUserEmail.length() == 0)
				{
					startActivity(new Intent(this, mainActivity.class));
					CamsActivity.this.finish();
					return;
				}

				// Task for loading users from database
				new AsyncTask<String, String, String[]>(){
					int defaultUserIndex = 0;

					@Override
					protected String[] doInBackground(String... arg0)
					{
						try
						{
							// get database dal class
							dbAppUser dbuser = new dbAppUser(CamsActivity.this);
							AppData.appUsers = dbuser.getAllAppUsers(1000);

							// If it is the first time called when application
							// has been installed. Then user might be not in
							// database but in preferences. So we need to add it
							// to database as well.
							AppUser old = dbuser.getAppUser(AppData.AppUserEmail);
							if (old == null)
							{
								AppUser user = new AppUser();
								user.setUserEmail(AppData.AppUserEmail + "");
								user.setUserPassword(AppData.AppUserPassword + "");
								user.setApiKey(AppData.cambaApiKey + "");
								user.setIsActive(true);
								user.setIsDefault(true);
								dbuser.addAppUser(user);
							}

							final String[] userAccounts = new String[AppData.appUsers.size()];

							for (int i = 0; i < AppData.appUsers.size(); i++)
							{
								userAccounts[i] = AppData.appUsers.get(i).getUserEmail();
								if (AppData.appUsers.get(i).getIsDefault()) defaultUserIndex = i;
							}

							return userAccounts;

						}
						catch (Exception e)
						{
							Log.e(TAG, e.getMessage(), e);
							if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
						}
						return null;
					}

					@Override
					protected void onPostExecute(String[] userAccounts)
					{
						try
						{
							ArrayAdapter<String> adapter = new ArrayAdapter<String>(
									CamsActivity.this,
									android.R.layout.simple_spinner_dropdown_item, userAccounts);
							CamsActivity.this.getActionBar().setNavigationMode(
									ActionBar.NAVIGATION_MODE_LIST); // dropdown
																		// list
																		// navigation
																		// for
																		// the
																		// action
																		// bar
							OnNavigationListener navigationListener = new OnNavigationListener(){
								@Override
								public boolean onNavigationItemSelected(int itemPosition,
										long itemId)
								{
									try
									{
										// set all current users default to
										// false
										for (AppUser u : AppData.appUsers)
											u.setIsDefault(false);

										// set all db app users as false
										dbAppUser db = new dbAppUser(CamsActivity.this);
										db.updateAllIsDefaultFalse();

										// set selected user's default to true
										AppUser user = AppData.appUsers.get(itemPosition);
										user.setIsDefault(true);
										Commons.setDefaultUserForApp(CamsActivity.this,
												user.getUserEmail(), user.getUserPassword(),
												user.getApiKey(), true);
										db.updateAppUser(user);

										// load cameras for default user
										AppData.camesList = new dbCamera(CamsActivity.this)
												.getAllCamerasForEmailID(AppData.AppUserEmail, 500);
										RemoveAllCameraViews();
										AddAllCameraViews(true);

										// start the task for default user to
										// refresh data
										GetUserCamsData task = new GetUserCamsData();
										task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
										if (TotalCamerasInGrid == 0 && refresh != null)
										{
											refresh.setActionView(null);
											refresh.setActionView(R.layout.actionbar_indeterminate_progress);
										}

									}
									catch (Exception e)
									{
										Log.e(TAG, e.getMessage(), e);
										if (Constants.isAppTrackingEnabled) BugSenseHandler
												.sendException(e);
									}
									return false;
								}
							};

							getActionBar().setListNavigationCallbacks(adapter, navigationListener);
							// getActionBar().setSelectedNavigationItem(-1);
							getActionBar().setSelectedNavigationItem(defaultUserIndex);

						}
						catch (Exception e)
						{
							Log.e(TAG, e.getMessage(), e);
							if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
						}
					}

				}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");

				taskStarted = true;
			}
			catch (Exception e)
			{
				Log.e(TAG, e.getMessage(), e);
				if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
			}
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (Constants.isAppTrackingEnabled) if (Constants.isAppTrackingEnabled) BugSenseHandler
				.initAndStartSession(this, Constants.bugsense_ApiKey);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		try
		{
			if (this.getActionBar() != null)
			{
				this.getActionBar().setHomeButtonEnabled(true);
				this.getActionBar().setDisplayShowTitleEnabled(false);
				this.getActionBar().setIcon(R.drawable.ic_device_access_storage);
			}

			setContentView(R.layout.camslayoutwithslide); // inflate the cams
															// screen

			_savedInstanceState = savedInstanceState;

			addUsersToDropdownActionBar();

			slidemenu = (SlideMenu) findViewById(R.id.slideMenu);
			slidemenu.init(this, R.menu.slide, this, slideoutMenuAnimationTime);

			int notificationID = 0;
			try
			{
				_activity = this;

				notificationID = this.getIntent().getIntExtra(Constants.GCMNotificationIDString, 0);
				this.getIntent().putExtra(Constants.GCMNotificationIDString, 0);
			}
			catch (Exception e)
			{
			}
			if (notificationID > 0) CamsActivity.this.onSlideMenuItemClick(notificationID);
			Log.i(TAG, "notificationID [" + notificationID + "]");

		}
		catch (Exception e)
		{
			if (enableLogs) Log.e(TAG, e.toString(), e);
			UIUtils.GetAlertDialog(
					CamsActivity.this,
					"Error Occured",
					Constants.ErrorMessageGeneric + e.toString() + "::"
							+ Log.getStackTraceString(e)).show();
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		try
		{
			// draw the options defined in the following file
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.camsmenulayout, menu);

			refresh = menu.findItem(R.id.menurefresh);

			if (refresh != null && (AppData.camesList == null || AppData.camesList.size() == 0))
			{
				refresh.setActionView(null);
				refresh.setActionView(R.layout.actionbar_indeterminate_progress);
			}

			if (enableLogs) Log.i(TAG, "Options Activity Started in onPrepareOptionsMenu event");
			return true;
		}
		catch (Exception ex)
		{
			if (enableLogs) Log.e(TAG, ex.toString());
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ex);
		}
		return true;
	}

	// Tells that the item has been selected from the menu. Now check and get
	// the selected item and perform the relevent action
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		try
		{
			switch (item.getItemId())
			{
			case R.id.menurefresh: // need to refresh the application
				if (refresh != null) refresh
						.setActionView(R.layout.actionbar_indeterminate_progress);

				GetUserCamsData task = new GetUserCamsData();
				task.reload = true; // be default do not refesh until there is
									// any change in cameras in database
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");

				return true;

			case android.R.id.home: // this is the app icon of the actionbar
				slidemenu.show();

			default:
				return super.onOptionsItemSelected(item);
			}
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString(), e);
			UIUtils.GetAlertDialog(
					this,
					"Error Occured",
					"Some error occured while saving your options. Technical details are: "
							+ e.toString());
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
		}
		return super.onOptionsItemSelected(item);

	}

	@Override
	public void onSlideMenuItemClick(int itemId)
	{

		try
		{
			switch (itemId)
			{
			case R.id.slidemenu_logout: // Need to logout and return to the
										// login activty.

				// delete saved username and apssword
				SharedPreferences sharedPrefs = PreferenceManager
						.getDefaultSharedPreferences(CamsActivity.this);
				SharedPreferences.Editor editor = sharedPrefs.edit();
				editor.putString("AppUserEmail", null);
				editor.putString("AppUserPassword", null);
				editor.commit();
				// start login activity
				startActivity(new Intent(this, mainActivity.class));

				new LogoutActivitiesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");

				break;

			case R.id.slidemenu_about: // show the about dialog

				new Handler().postDelayed(new Runnable(){
					@Override
					public void run()
					{
						startActivity(new Intent(CamsActivity.this, AboutDialog.class));
					}
				}, slideoutMenuAnimationTime);

				break;

			case R.id.slidemenu_settings:

				new Handler().postDelayed(new Runnable(){
					@Override
					public void run()
					{
						startActivity(new Intent(CamsActivity.this, CamsPrefsActivity.class));
					}
				}, slideoutMenuAnimationTime);

				break;

			case R.id.slidemenu_manage: // show the manage dialog

				new Handler().postDelayed(new Runnable(){
					@Override
					public void run()
					{
						startActivity(new Intent(CamsActivity.this, ManageAccountsActivity.class));
						isUsersAccountsActivityStarted = true;
					}
				}, slideoutMenuAnimationTime);
				break;

			default: // starting the notification activity

				NotificationActivity.NotificationID = itemId;
				new MarkNotificationAsReadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
						itemId + "");
				new Handler().postDelayed(new Runnable(){
					@Override
					public void run()
					{
						Intent i = new Intent(new Intent(CamsActivity.this,
								NotificationActivity.class));
						startActivity(i);
					}
				}, slideoutMenuAnimationTime);
				break;

			}
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString(), e);
			UIUtils.GetAlertDialog(
					this,
					"Error Occured",
					"Some error occured while saving your options. Technical details are: "
							+ e.toString());
		}
	}

	class LogoutActivitiesTask extends AsyncTask<String, String, String>
	{
		@Override
		protected String doInBackground(String... params)
		{
			try
			{

				// delete saved username and apssword
				SharedPreferences sharedPrefs = PreferenceManager
						.getDefaultSharedPreferences(CamsActivity.this);
				SharedPreferences.Editor editor = sharedPrefs.edit();
				editor.putString("AppUserEmail", null);
				editor.putString("AppUserPassword", null);
				editor.commit();

				// Un register from gcm server
				GCMRegistrar.setRegisteredOnServer(CamsActivity.this, false);

				// delete all app users
				dbAppUser dbu = new dbAppUser(CamsActivity.this);
				List<AppUser> list = dbu.getAllAppUsers(10000);
				if (list != null && list.size() > 0)
				{
					for (AppUser user : list)
					{
						dbu.deleteAppUserForEmail(user.getUserEmail());
					}
				}

				// unregister all users
				if (list != null && list.size() > 0)
				{
					// get information to be posted for unregister on camba
					// server request
					String regId = GCMRegistrar.getRegistrationId(CamsActivity.this); // registration
																						// id
																						// for
																						// this
					String AppUserEmail = null;
					String AppUserPassword = null;
					String Operation = null;
					String Manufacturer = null;
					String Model = null;
					String SerialNo = null;
					String ImeiNo = null;
					String Fingureprint = null;
					String MacAddress = null;
					String BlueToothName = null;
					String AppVersion = null;

					try
					{

						AppUserPassword = sharedPrefs.getString("AppUserPassword", null);
						Operation = "2";
						Manufacturer = android.os.Build.MANUFACTURER;
						Model = android.os.Build.MODEL;
						SerialNo = android.os.Build.SERIAL;
						ImeiNo = ((android.telephony.TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
								.getDeviceId();
						Fingureprint = android.os.Build.FINGERPRINT;
						WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
						WifiInfo info = manager.getConnectionInfo();
						MacAddress = info.getMacAddress();
						BlueToothName = BluetoothAdapter.getDefaultAdapter().getName();
						AppVersion = (getPackageManager().getPackageInfo(getPackageName(), 0)).versionName;
					}
					catch (Exception ee)
					{
					}

					for (AppUser user : list)
					{
						dbu.deleteAppUserForEmail(user.getUserEmail());
						AppUserEmail = user.getUserEmail();
						try
						{
							CambaApiManager.registerDeviceForUsername(AppUserEmail,
									AppUserPassword, regId, Operation, BlueToothName, Manufacturer,
									Model, SerialNo, ImeiNo, Fingureprint, MacAddress, AppVersion);
						}
						catch (Exception e)
						{
						}
					}
				}
			}
			catch (Exception eee)
			{
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result)
		{
			CamsActivity.this.finish();

		}
	}

	private class MarkNotificationAsReadTask extends AsyncTask<String, String, String>
	{
		@Override
		protected String doInBackground(String... id)
		{
			String message = "";
			try
			{
				dbNotifcation helper = new dbNotifcation(CamsActivity.this);

				CameraNotification notif = helper.getCameraNotification(Integer.parseInt(id[0]));
				notif.setIsRead(true);
				helper.updateCameraNotification(notif);

			}
			catch (Exception e)
			{
				Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
				message = e.toString();
			}
			return message;
		}

	}

	@Override
	public void onRestart()
	{
		super.onRestart();
		try
		{
			if (isUsersAccountsActivityStarted)
			{
				isUsersAccountsActivityStarted = false;
				addUsersToDropdownActionBar();
			}

			int camsOldValue = camerasperrow;
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			camerasperrow = Integer.parseInt(sharedPrefs.getString("lstgridcamerasperrow", "2"));

			if (camsOldValue != camerasperrow)
			{
				RemoveAllCameraViews();
				AddAllCameraViews(false); // do not reload cameras beacuse it
											// may be an activity for orentation
											// changed or notification might
											// have arrived.
			}

		}
		catch (Exception e)
		{
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
		}
	}

	// This will show the progress dialog
	void ShowLoadingDialog(String message)
	{
		if (pdLoading == null) pdLoading = new ProgressDialog(CamsActivity.this);

		if (!pdLoading.isShowing())
		{
			pdLoading.setCancelable(false);
			pdLoading.show();
		}
		pdLoading.setMessage(message);
	}

	// Stop All Camera Views
	void StopAllCameraViews()
	{
		io.evercam.android.custom.FlowLayout camsLineView = (io.evercam.android.custom.FlowLayout) this
				.findViewById(R.id.camsLV);
		for (int i = 0; i < camsLineView.getChildCount(); i++)
		{
			LinearLayout pview = (LinearLayout) camsLineView.getChildAt(i);
			CameraLayout cl = (CameraLayout) pview.getChildAt(0); // CameraLayout
																	// is on 0th
																	// index
			cl.StopAllActivity();
		}

	}

	boolean resizeCameras()
	{
		try
		{

			Display display = getWindowManager().getDefaultDisplay();
			int screen_width = display.getWidth();

			io.evercam.android.custom.FlowLayout camsLineView = (io.evercam.android.custom.FlowLayout) this
					.findViewById(R.id.camsLV);
			for (int i = 0; i < camsLineView.getChildCount(); i++)
			{
				LinearLayout pview = (LinearLayout) camsLineView.getChildAt(i);
				CameraLayout cl = (CameraLayout) pview.getChildAt(0); // CameraLayout
																		// is on
																		// 0th
																		// index

				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
				params.width = ((i + 1 % camerasperrow == 0) ? (screen_width - (i % camerasperrow)
						* (screen_width / camerasperrow)) : screen_width / camerasperrow);
				params.height = (int) (params.width / (1.25));
				cl.setLayoutParams(params);
			}

			return true;
		}
		catch (Exception e)
		{
			if (enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
			UIUtils.GetAlertDialog(CamsActivity.this, "Error Occured",
					Constants.ErrorMessageGeneric).show();
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);

		}
		return false;
	}

	// Remove all the cameras so that all activites being performed can be
	// stopped
	boolean RemoveAllCameraViews()
	{
		try
		{

			StopAllCameraViews();

			io.evercam.android.custom.FlowLayout camsLineView = (io.evercam.android.custom.FlowLayout) this
					.findViewById(R.id.camsLV);
			camsLineView.removeAllViews();

			TotalCamerasInGrid = 0;

			return true;
		}
		catch (Exception e)
		{
			if (enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
			UIUtils.GetAlertDialog(CamsActivity.this, "Error Occured",
					Constants.ErrorMessageGeneric).show();
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);

		}
		return false;
	}

	// Add all the cameras as per the rules
	boolean AddAllCameraViews(boolean reloadImages)
	{
		try
		{

			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			camerasperrow = Integer.parseInt(sharedPrefs.getString("lstgridcamerasperrow", ""
					+ camerasperrow));

			io.evercam.android.custom.FlowLayout camsLineView = (io.evercam.android.custom.FlowLayout) this
					.findViewById(R.id.camsLV);

			Display display = getWindowManager().getDefaultDisplay();
			int screen_width = display.getWidth();

			int index = 0;
			TotalCamerasInGrid = 0;
			for (Camera caml : AppData.camesList)
			{
				LinearLayout pview = new LinearLayout(this);

				int j = index + 1;

				if (reloadImages) caml.loadingStatus = ImageLoadingStatus.not_started;
				CameraLayout cl = new CameraLayout(this, caml);// ,imageReceivedFrom);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
				params.width = ((j % camerasperrow == 0) ? (screen_width - (index % camerasperrow)
						* (screen_width / camerasperrow)) : screen_width / camerasperrow);
				params.height = (int) (params.width / (1.25));
				cl.setLayoutParams(params);

				pview.addView(cl);
				camsLineView.addView(pview, new io.evercam.android.custom.FlowLayout.LayoutParams(
						0, 0));

				Log.i("sajjad", caml.toString());

				index++;
				TotalCamerasInGrid++;
			}

			_savedInstanceState = null; // it will be set again when oncreate is
										// called again.

			if (this.getActionBar() != null) this.getActionBar().setHomeButtonEnabled(true);

			StartgCMRegisterActions();

			if (refresh != null) refresh.setActionView(null);

			return true;
		}
		catch (Exception e)
		{
			if (enableLogs) Log.e(TAG, e.toString(), e);
			UIUtils.GetAlertDialog(CamsActivity.this, "Error Occured",
					Constants.ErrorMessageGeneric).show();
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);

		}
		return false;
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		try
		{
			StopGcmRegisterActions();
			RemoveAllCameraViews();
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString(), e);
		}
	}

	// This class gets users cameras list from the api with proper login
	private class GetUserCamsData extends AsyncTask<String, Void, String>
	{
		public boolean reload = false;

		@Override
		protected String doInBackground(String... login)
		{
			try
			{
				boolean updateDB = false;

				SharedPreferences sharedPrefs = PreferenceManager
						.getDefaultSharedPreferences(CamsActivity.this);
				AppData.AppUserEmail = sharedPrefs.getString("AppUserEmail", null);
				AppData.AppUserPassword = sharedPrefs.getString("AppUserPassword", null);

				ArrayList<Camera> cambaList = CambaApiManager.getCameraListAndSetKey();
				for (Camera cam : cambaList)
				{
					cam.setUserEmail(AppData.AppUserEmail);
					Log.i("sajjad125", cam.toString());
				}

				for (Camera cam : cambaList)
				{
					if (!AppData.camesList.contains(cam))
					{
						updateDB = true;
						break;
					}

				}
				if (!updateDB) for (Camera cam1 : AppData.camesList)
				{
					if (!cambaList.contains(cam1))
					{
						updateDB = true;
						break;
					}
				}
				if (updateDB)
				{
					this.reload = true;
					AppData.camesList = cambaList;

					dbCamera dbcam = new dbCamera(CamsActivity.this);
					dbcam.deleteCameraForEmail(AppData.AppUserEmail); // delete
																		// all
																		// with
																		// the
																		// email

					for (Camera cam : AppData.camesList)
					{
						Log.i(TAG, cam.toString());
						dbcam.addCamera(cam);
					}
				}

			}

			catch (CredentialsException ce)
			{
				if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ce);
				return Constants.ErrorMessageInvalidCredentialsAndLogout;

			}
			catch (ConnectivityException e)
			{
				if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
				return Constants.ErrorMessageNoConnectivity;
			}
			catch (Exception e)
			{
				if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
				if (AppData.camesList == null && AppData.camesList.size() == 0) return Constants.ErrorMessageRefreshCamerasWhenNoCamerasExist;
				else return Constants.ErrorMessageRefreshCamerasWhenCamerasLoadedFromLocalDB;
			}
			return null;
		}

		@Override
		protected void onPostExecute(final String result)
		{
			if (result == null)
			{
				if (this.reload || TotalCamerasInGrid != AppData.camesList.size())
				{
					CamsActivity.this.RemoveAllCameraViews();
					CamsActivity.this.AddAllCameraViews(true);

				}
			}
			else
			{
				if (!CamsActivity.this.isFinishing()) UIUtils.GetAlertDialog(CamsActivity.this,
						"Error Occured", result, new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								dialog.dismiss();
								// CamsActivity.this.finish(); // cannot finish
								// because if we finish, user will not be able
								// to login back again.
								if ((result + "")
										.equalsIgnoreCase(Constants.ErrorMessageInvalidCredentialsAndLogout))
								{
									// delete saved username and apssword
									SharedPreferences sharedPrefs = PreferenceManager
											.getDefaultSharedPreferences(CamsActivity.this);
									SharedPreferences.Editor editor = sharedPrefs.edit();
									editor.putString("AppUserEmail", null);
									editor.putString("AppUserPassword", null);
									editor.commit();

									startActivity(new Intent(CamsActivity.this, mainActivity.class));
									new LogoutActivitiesTask().executeOnExecutor(
											AsyncTask.THREAD_POOL_EXECUTOR, "");
								}
								if (CamsActivity.this.refresh != null) CamsActivity.this.refresh
										.setActionView(null);
							}
						}).show();

			}

		}
	}

	private class RegisterGCMAlertsServiceTask extends AsyncTask<String, Void, String>
	{
		@Override
		protected String doInBackground(String... usernames)
		{
			String message = "";
			String TAG = "RegisterTask";
			try
			{
				GCMRegistrar.checkDevice(CamsActivity.this);
				Log.i(TAG, "Device Checked");
				GCMRegistrar.checkManifest(CamsActivity.this);
				Log.i(TAG, "Manifest Checked");
				String regId = GCMRegistrar.getRegistrationId(CamsActivity.this); // registration
																					// id
																					// for
																					// this
				String AppUserEmail = null;
				String AppUserPassword = null;
				String Operation = null;
				String Manufacturer = null;
				String Model = null;
				String SerialNo = null;
				String ImeiNo = null;
				String Fingureprint = null;
				String MacAddress = null;
				String BlueToothName = null;
				String AppVersion = null;

				try
				{

					SharedPreferences sharedPrefs = PreferenceManager
							.getDefaultSharedPreferences(CamsActivity.this);
					AppUserEmail = sharedPrefs.getString("AppUserEmail", null);
					AppUserPassword = sharedPrefs.getString("AppUserPassword", null);
					Operation = "1";
					Manufacturer = android.os.Build.MANUFACTURER;
					Model = android.os.Build.MODEL;
					SerialNo = android.os.Build.SERIAL;
					ImeiNo = ((android.telephony.TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
							.getDeviceId();
					Fingureprint = android.os.Build.FINGERPRINT;
					WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
					WifiInfo info = manager.getConnectionInfo();
					MacAddress = info.getMacAddress();
					BlueToothName = BluetoothAdapter.getDefaultAdapter().getName();
					AppVersion = (getPackageManager().getPackageInfo(getPackageName(), 0)).versionName;
				}
				catch (Exception ee)
				{
				}

				Log.i(TAG, "regId [" + regId + "] ");
				// *
				if (regId.equals(""))
				{ // New Registration on GCM Server
					// Automatically registers application on startup.
					GCMRegistrar.register(CamsActivity.this, Constants.GCM_SENDER_ID);
					return "Device registered successfully on GCM Server. It will be registered on camba server shortly.";

				}
				else if (!GCMRegistrar.isRegisteredOnServer(CamsActivity.this)) // Registered
																				// on
																				// GCM
																				// Server
																				// and
																				// now
																				// going
																				// to
																				// register
																				// on
																				// Camba
																				// Server
				{
					if (CambaApiManager.registerDeviceForUsername(AppUserEmail, AppUserPassword,
							regId, Operation, BlueToothName, Manufacturer, Model, SerialNo, ImeiNo,
							Fingureprint, MacAddress, AppVersion))
					{
						return "Device successfully registerd on GCM Server and Camba Server.";
					}
					else
					{
						return "Device failed to register on Camba server.";
					}

				}
				else
				{

					Log.i(TAG, "Already registered on Camba Server for Alerts against DeviceID ["
							+ regId + "], Username [" + AppData.AppUserEmail + "]");

					CambaApiManager.registerDeviceForUsername(AppUserEmail, AppUserPassword, regId,
							Operation, BlueToothName, Manufacturer, Model, SerialNo, ImeiNo,
							Fingureprint, MacAddress, AppVersion);
					GCMRegistrar.setRegisteredOnServer(CamsActivity.this, true);

					// return
					// "Device is already registered on GCM Server with ID ["+regId+"] but was unable to register on camba server.";
					return regId;
				}

			}
			catch (Exception e)
			{
				if (enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
				message = e.toString();
				if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
			}
			catch (Error e)
			{
				if (Constants.isAppTrackingEnabled) BugSenseHandler.sendExceptionMessage(TAG,
						"Error", new Exception(Log.getStackTraceString(e)));
			}
			return message;
		}

		@Override
		protected void onPostExecute(String result)
		{
		}

	}

	boolean mHandleMessageReceiverRegistered = false;

	private final void StartgCMRegisterActions()
	{
		try
		{
			Log.i(TAG, "StartgCMRegisterActions called");

			RegisterTask = new RegisterGCMAlertsServiceTask();
			RegisterTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");

			registerReceiver(mHandleMessageReceiver, new IntentFilter("CambaGCMAlert"));

			mHandleMessageReceiverRegistered = true;

			Log.i(TAG,
					"registerReceiver(mHandleMessageReceiver,new IntentFilter(\"CambaGCMAlert\"));");
		}
		catch (Exception e)
		{
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
		}

	}

	private final void StopGcmRegisterActions()
	{

		Log.i(TAG, "StopGcmRegisterActions called");

		if (mHandleMessageReceiverRegistered) // unregister only if registered
												// otherwise
												// illegalArgumentException
		unregisterReceiver(mHandleMessageReceiver);

	}

	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent)
		{

			Log.i(TAG, "AlertMessage Received ");

			String AlertMessage = intent.getStringExtra("AlertMessage");
			Log.i(TAG, "AlertMessage [" + AlertMessage + "]");

			String ApiCamera = intent.getStringExtra("ApiCamera");
			Log.i(TAG, "ApiCamera [" + ApiCamera + "]");

			Camera cam = CambaApiManager.ParseJsonObject(ApiCamera);

		}
	};

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		try
		{
			Log.i("sajjadpp", "onConfigurationChanged called");

			super.onConfigurationChanged(newConfig);

			resizeCameras();

		}
		catch (Exception e)
		{
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
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
}
