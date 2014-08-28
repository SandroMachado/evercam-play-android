package io.evercam.androidapp;

import android.os.Bundle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.*;

import io.evercam.androidapp.custom.AboutDialog;
import io.evercam.androidapp.custom.CameraLayout;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dal.DbAppUser;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.dto.ImageLoadingStatus;
import io.evercam.androidapp.slidemenu.*;
import io.evercam.androidapp.tasks.CheckInternetTask;
import io.evercam.androidapp.tasks.LoadCameraListTask;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.PrefsManager;

import com.bugsense.trace.BugSenseHandler;
import io.evercam.androidapp.R;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class CamerasActivity extends ParentActivity implements
		SlideMenuInterface.OnSlideMenuItemClickListener
{
	public static CamerasActivity activity = null;
	public MenuItem refresh;

	public static boolean stopImageLoading = false;

	private static final String TAG = "evercamplay-CamerasActivity";

	private SlideMenu slideMenu;
	private int totalCamerasInGrid = 0;
	private int slideoutMenuAnimationTime = 255;
	public boolean isUsersAccountsActivityStarted = false;
	private static int camerasPerRow = 2;
	public boolean reloadCameraList = false;

	public CustomProgressDialog reloadProgressDialog;

	private enum InternetCheckType
	{
		START, RESTART
	};

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.initAndStartSession(this, Constants.bugsense_ApiKey);
		}

		EvercamPlayApplication.sendScreenAnalytics(this, getString(R.string.screen_camera_list));

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if (this.getActionBar() != null)
		{
			this.getActionBar().setHomeButtonEnabled(true);
			this.getActionBar().setDisplayShowTitleEnabled(false);
			this.getActionBar().setIcon(R.drawable.evercam_play_192x192);
		}

		setContentView(R.layout.camslayoutwithslide);

		// Disable add user to drop down list to hide user Email
		// Start loading camera list directly.
		// addUsersToDropdownActionBar();
		new CamerasCheckInternetTask(CamerasActivity.this, InternetCheckType.START)
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		// Disable slide menu until the functionality is required.
		// slideMenu = (SlideMenu) findViewById(R.id.slideMenu);
		// slideMenu.init(this, R.menu.slide, this,
		// slideoutMenuAnimationTime);

		// int notificationID = 0;

		activity = this;
		//
		// notificationID =
		// this.getIntent().getIntExtra(Constants.GCMNotificationIDString, 0);
		// this.getIntent().putExtra(Constants.GCMNotificationIDString, 0);
		//
		// if (notificationID > 0)
		// {
		// CamerasActivity.this.onSlideMenuItemClick(notificationID);
		// }
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

			if (refresh != null
					&& (AppData.evercamCameraList == null || AppData.evercamCameraList.size() == 0))
			{
				refresh.setActionView(null);
				refresh.setActionView(R.layout.actionbar_indeterminate_progress);
			}
			return true;
		}
		catch (Exception ex)
		{
			Log.e(TAG, ex.toString());
			if (Constants.isAppTrackingEnabled)
			{
				BugSenseHandler.sendException(ex);
			}
		}
		return true;
	}

	// Tells that the item has been selected from the menu. Now check and get
	// the selected item and perform the relevant action
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menurefresh:

			EvercamPlayApplication.sendEventAnalytics(this, R.string.category_menu,
					R.string.action_refresh, R.string.label_list_refresh);

			if (refresh != null) refresh.setActionView(R.layout.actionbar_indeterminate_progress);

			LoadCameraListTask loadTask = new LoadCameraListTask(AppData.defaultUser,
					CamerasActivity.this);
			loadTask.reload = true; // be default do not refresh until there
									// is
			// any change in cameras in database
			loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

			return true;

		case R.id.menu_add_camera:

			showAddCameraOptionsDialog();

			return true;

		case R.id.menu_settings:
			EvercamPlayApplication.sendEventAnalytics(this, R.string.category_menu,
					R.string.action_settings, R.string.label_settings);

			startActivity(new Intent(CamerasActivity.this, CameraPrefsActivity.class));

			return true;

		case R.id.menu_manage_accounts:
			EvercamPlayApplication.sendEventAnalytics(this, R.string.category_menu,
					R.string.action_manage_account, R.string.label_account);

			startActivity(new Intent(CamerasActivity.this, ManageAccountsActivity.class));
			isUsersAccountsActivityStarted = true;
			stopImageLoading = true;

			return true;

		case R.id.menu_about:
			EvercamPlayApplication.sendEventAnalytics(this, R.string.category_menu,
					R.string.action_about, R.string.label_about);

			startActivity(new Intent(CamerasActivity.this, AboutDialog.class));

			return true;

		case R.id.menu_logout:
			showSignOutDialog();

			return true;

			// Temporarily disable slide menu
			// case android.R.id.home:
			// slideMenu.show();

		default:
			return super.onOptionsItemSelected(item);
		}

	}

	@Override
	public void onSlideMenuItemClick(int itemId)
	{
		if (slideMenu.isShown())
		{
			slideMenu.hide();
		}

		switch (itemId)
		{
		// case R.id.slidemenu_logout:
		// EvercamPlayApplication.sendEventAnalytics(this,
		// R.string.category_menu,
		// R.string.action_logout, R.string.label_user_logout);
		// logOutUser();
		//
		// break;
		//
		// case R.id.slidemenu_about:
		// EvercamPlayApplication.sendEventAnalytics(this,
		// R.string.category_menu,
		// R.string.action_about, R.string.label_about);
		// new Handler().postDelayed(new Runnable(){
		// @Override
		// public void run()
		// {
		// startActivity(new Intent(CamerasActivity.this,
		// AboutDialog.class));
		// }
		// }, slideoutMenuAnimationTime);
		//
		// break;

		// case R.id.slidemenu_settings:
		// EvercamPlayApplication.sendEventAnalytics(this,
		// R.string.category_menu,
		// R.string.action_settings, R.string.label_settings);
		// new Handler().postDelayed(new Runnable(){
		// @Override
		// public void run()
		// {
		// startActivity(new Intent(CamerasActivity.this,
		// CameraPrefsActivity.class));
		// }
		// }, slideoutMenuAnimationTime);
		//
		// break;
		//
		// case R.id.slidemenu_manage:
		// EvercamPlayApplication.sendEventAnalytics(this,
		// R.string.category_menu,
		// R.string.action_manage_account, R.string.label_account);
		// new Handler().postDelayed(new Runnable(){
		// @Override
		// public void run()
		// {
		// startActivity(new Intent(CamerasActivity.this,
		// ManageAccountsActivity.class));
		// isUsersAccountsActivityStarted = true;
		// }
		// }, slideoutMenuAnimationTime);
		// break;

		// default: // starting the notification activity
		//
		// NotificationActivity.NotificationID = itemId;
		// new
		// MarkNotificationAsReadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
		// itemId + "");
		// new Handler().postDelayed(new Runnable(){
		// @Override
		// public void run()
		// {
		// Intent i = new Intent(new Intent(CamerasActivity.this,
		// NotificationActivity.class));
		// startActivity(i);
		// }
		// }, slideoutMenuAnimationTime);
		// break;

		}
	}

	@Override
	public void onRestart()
	{
		super.onRestart();

		if (isUsersAccountsActivityStarted || reloadCameraList)
		{
			// If returned from account management, the
			// default user could possibly changed,
			// so remove all cameras and reload.

			// addUsersToDropdownActionBar();
			removeAllCameraViews();
			startLoadingCameras();
			isUsersAccountsActivityStarted = false;
			reloadCameraList = false;
		}
		else
		{
			new CamerasCheckInternetTask(CamerasActivity.this, InternetCheckType.RESTART)
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	@Override
	public void onBackPressed()
	{

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == Constants.REQUEST_CODE_ADD_CAMERA
				|| requestCode == Constants.REQUEST_CODE_DELETE_CAMERA)
		{
			if (resultCode == Constants.RESULT_TRUE)
			{
				reloadCameraList = true;
			}
			else
			{
				reloadCameraList = false;
			}
		}
	}

	private void startLoadingCameras()
	{
		stopImageLoading = false;

		reloadProgressDialog = new CustomProgressDialog(this);
		if (isUsersAccountsActivityStarted || reloadCameraList)
		{
			reloadProgressDialog.show(getString(R.string.loading_cameras));
		}

		LoadCameraListTask loadTask = new LoadCameraListTask(AppData.defaultUser,
				CamerasActivity.this);
		loadTask.reload = true; // be default do not refresh until there
								// is
		// any change in cameras in database
		loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	// Stop All Camera Views
	public void stopAllCameraViews()
	{
		io.evercam.androidapp.custom.FlowLayout camsLineView = (io.evercam.androidapp.custom.FlowLayout) this
				.findViewById(R.id.camsLV);
		for (int count = 0; count < camsLineView.getChildCount(); count++)
		{
			LinearLayout linearLayout = (LinearLayout) camsLineView.getChildAt(count);
			CameraLayout cameraLayout = (CameraLayout) linearLayout.getChildAt(0);
			cameraLayout.stopAllActivity();
		}
	}

	boolean resizeCameras()
	{
		try
		{
			int screen_width = readScreenWidth(this);

			io.evercam.androidapp.custom.FlowLayout camsLineView = (io.evercam.androidapp.custom.FlowLayout) this
					.findViewById(R.id.camsLV);
			for (int i = 0; i < camsLineView.getChildCount(); i++)
			{
				LinearLayout pview = (LinearLayout) camsLineView.getChildAt(i);
				CameraLayout cameraLayout = (CameraLayout) pview.getChildAt(0);

				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
				params.width = ((i + 1 % camerasPerRow == 0) ? (screen_width - (i % camerasPerRow)
						* (screen_width / camerasPerRow)) : screen_width / camerasPerRow);
				params.height = (int) (params.width / (1.25));
				cameraLayout.setLayoutParams(params);
			}
			return true;
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
			if (Constants.isAppTrackingEnabled)
			{
				BugSenseHandler.sendException(e);
			}
			EvercamPlayApplication.sendEventAnalytics(this, R.string.category_error,
					R.string.action_error_camera_list, R.string.label_error_resize);
			CustomedDialog.showUnexpectedErrorDialog(CamerasActivity.this);
		}
		return false;
	}

	// Remove all the cameras so that all activities being performed can be
	// stopped
	public boolean removeAllCameraViews()
	{
		try
		{
			stopAllCameraViews();

			io.evercam.androidapp.custom.FlowLayout camsLineView = (io.evercam.androidapp.custom.FlowLayout) this
					.findViewById(R.id.camsLV);
			camsLineView.removeAllViews();

			totalCamerasInGrid = 0;

			return true;
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
			if (Constants.isAppTrackingEnabled) 
			{BugSenseHandler.sendException(e);
			
			}
		EvercamPlayApplication.sendEventAnalytics(this, R.string.category_error,
				R.string.action_error_camera_list, R.string.label_error_remove_cameras);
			CustomedDialog.showUnexpectedErrorDialog(CamerasActivity.this);
		}
		return false;
	}

	// Add all the cameras as per the rules
	public boolean addAllCameraViews(boolean reloadImages)
	{
		try
		{
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			camerasPerRow = PrefsManager.getCameraPerRow(sharedPrefs, camerasPerRow);

			io.evercam.androidapp.custom.FlowLayout camsLineView = (io.evercam.androidapp.custom.FlowLayout) this
					.findViewById(R.id.camsLV);

			int screen_width = readScreenWidth(this);

			int index = 0;
			totalCamerasInGrid = 0;

			for (EvercamCamera evercamCamera : AppData.evercamCameraList)
			{
				LinearLayout cameraListLayout = new LinearLayout(this);

				int indexPlus = index + 1;

				if (reloadImages) evercamCamera.loadingStatus = ImageLoadingStatus.not_started;
				CameraLayout cameraLayout = new CameraLayout(this, evercamCamera);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
				params.width = ((indexPlus % camerasPerRow == 0) ? (screen_width - (index % camerasPerRow)
						* (screen_width / camerasPerRow))
						: screen_width / camerasPerRow);
				params.height = (int) (params.width / (1.25));
				cameraLayout.setLayoutParams(params);

				cameraListLayout.addView(cameraLayout);
				camsLineView.addView(cameraListLayout,
						new io.evercam.androidapp.custom.FlowLayout.LayoutParams(0, 0));
				index++;
				totalCamerasInGrid++;
			}

			if (this.getActionBar() != null) this.getActionBar().setHomeButtonEnabled(true);

			// startgCMRegisterActions();

			if (refresh != null) refresh.setActionView(null);

			return true;
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString(), e);
			if (Constants.isAppTrackingEnabled) 
			{	
				BugSenseHandler.sendException(e);
			}
			EvercamPlayApplication.sendEventAnalytics(this, R.string.category_error,
					R.string.action_error_camera_list, R.string.label_error_add_cameras);
			CustomedDialog.showUnexpectedErrorDialog(CamerasActivity.this);
		}
		return false;
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		try
		{
			stopGcmRegisterActions();
			removeAllCameraViews();
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString(), e);
		}
	}

	boolean mHandleMessageReceiverRegistered = false;

	private final void stopGcmRegisterActions()
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
		}
	};

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		try
		{
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
			if (Constants.isAppTrackingEnabled) BugSenseHandler.startSession(this);
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();

		if (Constants.isAppTrackingEnabled)
		{
			if (Constants.isAppTrackingEnabled) BugSenseHandler.closeSession(this);
		}
	}

	private void showAddCameraOptionsDialog()
	{
		final View optionsView = getLayoutInflater()
				.inflate(R.layout.add_camera_options_list, null);
		final AlertDialog dialog = CustomedDialog.getAlertDialogNoTitle(CamerasActivity.this,
				optionsView);
		dialog.show();

		Button addCameraButton = (Button) optionsView.findViewById(R.id.btn_add_ip_camera);
		Button scanCameraButton = (Button) optionsView.findViewById(R.id.btn_scan_for_camera);

		addCameraButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v)
			{
				dialog.dismiss();

				EvercamPlayApplication.sendEventAnalytics(CamerasActivity.this,
						R.string.category_menu, R.string.action_add_camera,
						R.string.label_add_camera_manually);

				startActivityForResult(
						new Intent(CamerasActivity.this, AddEditCameraActivity.class),
						Constants.REQUEST_CODE_ADD_CAMERA);
			}
		});

		scanCameraButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v)
			{
				dialog.dismiss();

				EvercamPlayApplication.sendEventAnalytics(CamerasActivity.this,
						R.string.category_menu, R.string.action_add_camera,
						R.string.label_add_camera_scan);

				startActivityForResult(new Intent(CamerasActivity.this, ScanActivity.class),
						Constants.REQUEST_CODE_ADD_CAMERA);
			}
		});
	}

	private void logOutUser()
	{
		try
		{
			// delete saved username and password
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			PrefsManager.removeUserEmail(sharedPrefs);

			// Instead of deleting all users, only delete the default one
			DbAppUser dbUser = new DbAppUser(this);
			dbUser.deleteAppUserByEmail(AppData.defaultUser.getEmail());

			// clear real-time default app data
			AppData.defaultUser = null;
			AppData.appUsers.clear();
			AppData.evercamCameraList.clear();
		}
		catch (Exception e)
		{
			Log.e(TAG, "Error: sign out" + e.toString());
		}
		startActivity(new Intent(this, MainActivity.class));
	}

	private void showSignOutDialog()
	{
		CustomedDialog.getConfirmLogoutDialog(this, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				EvercamPlayApplication.sendEventAnalytics(CamerasActivity.this,
						R.string.category_menu, R.string.action_logout, R.string.label_user_logout);
				logOutUser();
			}
		}).show();

	}

	class CamerasCheckInternetTask extends CheckInternetTask
	{
		InternetCheckType type;

		public CamerasCheckInternetTask(Context context, InternetCheckType type)
		{
			super(context);
			this.type = type;
		}

		@Override
		protected void onPostExecute(Boolean hasNetwork)
		{
			if (hasNetwork)
			{
				if (type == InternetCheckType.START)
				{
					startLoadingCameras();
				}
				else if (type == InternetCheckType.RESTART)
				{
					try
					{
						int camsOldValue = camerasPerRow;
						SharedPreferences sharedPrefs = PreferenceManager
								.getDefaultSharedPreferences(CamerasActivity.this);
						camerasPerRow = PrefsManager.getCameraPerRow(sharedPrefs, 2);

						if (camsOldValue != camerasPerRow)
						{
							removeAllCameraViews();
							addAllCameraViews(false);
						}

					}
					catch (Exception e)
					{
						if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
					}
				}
			}
			else
			{
				CustomedDialog.showInternetNotConnectDialog(CamerasActivity.this);
			}
		}
	}

	public static int readScreenWidth(Activity activity)
	{
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return size.x;
	}
}
