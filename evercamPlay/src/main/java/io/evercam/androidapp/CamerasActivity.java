package io.evercam.androidapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.appstate.AppState;

import java.util.concurrent.RejectedExecutionException;

import io.evercam.androidapp.authentication.EvercamAccount;
import io.evercam.androidapp.custom.CameraLayout;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.CustomScrollView;
import io.evercam.androidapp.custom.CustomScrollView.OnScrollStoppedListener;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.dto.ImageLoadingStatus;
import io.evercam.androidapp.tasks.CheckInternetTask;
import io.evercam.androidapp.tasks.LoadCameraListTask;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.PrefsManager;
import io.evercam.androidapp.video.HomeShortcut;

public class CamerasActivity extends ParentActivity
{
    public static CamerasActivity activity = null;
    public MenuItem refresh;
    public String liveViewCameraId = "";

    private static final String TAG = "evercam-CamerasActivity";

    private int totalCamerasInGrid = 0;
    private static int camerasPerRow = 2;
    public boolean reloadCameraList = false;

    public CustomProgressDialog reloadProgressDialog;

    private enum InternetCheckType
    {
        START, RESTART
    }

    private String usernameOnStop = "";

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
            this.getActionBar().setHomeButtonEnabled(true);
            this.getActionBar().setDisplayShowTitleEnabled(false);
            this.getActionBar().setIcon(R.drawable.evercam_play_192x192);
        }
        setContentView(R.layout.camslayoutwithslide);

        readShortcutCameraId();

        activity = this;
        checkUser();

        /**
         * Use Handler here because we want the title bar/menu get loaded first.
         * When the app starts, it will load cameras to grid view twice:
         * 1. Load cameras that saved locally without image (disabled load image from cache
         * because it blocks UI.)
         * 2. When camera list returned from Evercam, show them on screen with thumbnails,
         * then request for snapshots in background seperately.
         */
        new Handler().postDelayed(new Runnable()
        {

            @Override
            public void run()
            {
                /**
                 * Sometimes Evercam returns the list less than 0.5 sec,
                 * so check it's returned or not before
                 * the first load to avoid loading it twice.
                 */
                io.evercam.androidapp.custom.FlowLayout camsLineView = (io.evercam.androidapp
                        .custom.FlowLayout) CamerasActivity.this.findViewById(R.id
                        .cameras_flow_layout);
                if(!(camsLineView.getChildCount() > 0))
                {
                    addAllCameraViews(false, false);
                }
            }
        }, 500);

        // Start loading camera list after menu created(because need the menu
        // showing as animation)
        new CamerasCheckInternetTask(CamerasActivity.this, InternetCheckType.START)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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
        // draw the options defined in the following file
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.camera_list_menu, menu);

        refresh = menu.findItem(R.id.menurefresh);
        refresh.setActionView(R.layout.actionbar_indeterminate_progress);

        return true;
    }

    // Tells that the item has been selected from the menu. Now check and get
    // the selected item and perform the relevant action
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int itemId = item.getItemId();
        if(itemId == R.id.menurefresh)
        {
            EvercamPlayApplication.sendEventAnalytics(this, R.string.category_menu,
                    R.string.action_refresh, R.string.label_list_refresh);

            if(refresh != null) refresh.setActionView(R.layout.actionbar_indeterminate_progress);

            startCameraLoadingTask();

        }
        else if(itemId == R.id.menu_add_camera)
        {
            showAddCameraOptionsDialog();
        }
        else if(itemId == R.id.menu_settings)
        {
            EvercamPlayApplication.sendEventAnalytics(this, R.string.category_menu,
                    R.string.action_settings, R.string.label_settings);

            startActivity(new Intent(CamerasActivity.this, CameraPrefsActivity.class));
        }
        else if(itemId == R.id.menu_manage_accounts)
        {
            EvercamPlayApplication.sendEventAnalytics(this, R.string.category_menu,
                    R.string.action_manage_account, R.string.label_account);

            startActivityForResult(new Intent(CamerasActivity.this, ManageAccountsActivity.class)
                    , Constants.REQUEST_CODE_MANAGE_ACCOUNT);
        }
        else if(itemId == R.id.menu_logout)
        {
            showSignOutDialog();
        }
        else if(itemId == R.id.menu_feedback)
        {
            startActivity(new Intent(CamerasActivity.this, FeedbackActivity.class));
        }
        else
        {
            return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onRestart()
    {
        super.onRestart();

        if(MainActivity.isUserLogged(this))
        {
            String restartedUsername = AppData.defaultUser.getUsername();

            //Reload camera list if default user has been changed
            if(!usernameOnStop.isEmpty() && !usernameOnStop.equals(restartedUsername))
            {
                new CamerasCheckInternetTask(CamerasActivity.this, InternetCheckType.START).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            else
            {
                try
                {
                    new CamerasCheckInternetTask(CamerasActivity.this, InternetCheckType.RESTART).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }
                catch(RejectedExecutionException e)
                {
                    EvercamPlayApplication.sendCaughtExceptionNotImportant(activity, e);
                }
            }
            usernameOnStop = "";
        }
        else
        {
            startActivity(new Intent(this, SlideActivity.class));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == Constants.REQUEST_CODE_ADD_CAMERA || requestCode == Constants
                .REQUEST_CODE_DELETE_CAMERA)
        {
            reloadCameraList = (resultCode == Constants.RESULT_TRUE);
        }
        else if(requestCode == Constants.REQUEST_CODE_MANAGE_ACCOUNT)
        {
            reloadCameraList = (resultCode == Constants.RESULT_ACCOUNT_CHANGED);
        }
    }

    private void startLoadingCameras()
    {
        reloadProgressDialog = new CustomProgressDialog(this);
        if(reloadCameraList)
        {
            reloadProgressDialog.show(getString(R.string.loading_cameras));
        }

        startCameraLoadingTask();
    }

    private void readShortcutCameraId()
    {
        Intent liveViewIntent = this.getIntent();
        if(liveViewIntent != null && liveViewIntent.getExtras() != null)
        {
            liveViewCameraId = liveViewIntent.getExtras().getString(HomeShortcut.KEY_CAMERA_ID, "");
        }
    }

    private void checkUser()
    {
        if(AppData.defaultUser == null)
        {
            AppData.defaultUser = new EvercamAccount(this).getDefaultUser();
        }
    }

    private void startCameraLoadingTask()
    {
        if(Commons.isOnline(this))
        {
            LoadCameraListTask loadTask = new LoadCameraListTask(AppData.defaultUser,
                    CamerasActivity.this);
            loadTask.reload = true; // be default do not refresh until there
            // is
            // any change in cameras in database
            loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else
        {
            CustomedDialog.showInternetNotConnectDialog(CamerasActivity.this);
        }
    }

    // Stop All Camera Views
    public void stopAllCameraViews()
    {
        io.evercam.androidapp.custom.FlowLayout camsLineView = (io.evercam.androidapp.custom
                .FlowLayout) this.findViewById(R.id.cameras_flow_layout);
        for(int count = 0; count < camsLineView.getChildCount(); count++)
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
            camerasPerRow = recalculateCameraPerRow();

            io.evercam.androidapp.custom.FlowLayout camsLineView = (io.evercam.androidapp.custom
                    .FlowLayout) this.findViewById(R.id.cameras_flow_layout);
            for(int i = 0; i < camsLineView.getChildCount(); i++)
            {
                LinearLayout pview = (LinearLayout) camsLineView.getChildAt(i);
                CameraLayout cameraLayout = (CameraLayout) pview.getChildAt(0);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(android.view
                        .ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams
                        .WRAP_CONTENT);
                params.width = ((i + 1 % camerasPerRow == 0) ? (screen_width - (i %
                        camerasPerRow) * (screen_width / camerasPerRow)) : screen_width /
                        camerasPerRow);
                params.width = params.width - 1; //1 pixels spacing between cameras
                params.height = (int) (params.width / (1.25));
                params.setMargins(1, 1, 0, 0); //1 pixels spacing between cameras
                cameraLayout.setLayoutParams(params);
            }
            return true;
        }
        catch(Exception e)
        {
            Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
            if(Constants.isAppTrackingEnabled)
            {
                BugSenseHandler.sendException(e);
            }

            EvercamPlayApplication.sendCaughtException(this, e);
            CustomedDialog.showUnexpectedErrorDialog(CamerasActivity.this);
        }
        return false;
    }

    private void updateCameraNames()
    {
        try
        {
            io.evercam.androidapp.custom.FlowLayout camsLineView = (io.evercam.androidapp.custom
                    .FlowLayout) this.findViewById(R.id.cameras_flow_layout);
            for(int i = 0; i < camsLineView.getChildCount(); i++)
            {
                LinearLayout pview = (LinearLayout) camsLineView.getChildAt(i);
                CameraLayout cameraLayout = (CameraLayout) pview.getChildAt(0);

                cameraLayout.updateTitleIfdifferent();
            }
        }
        catch(Exception e)
        {
            Log.e(TAG, e.toString());
            EvercamPlayApplication.sendCaughtException(this, e);
        }
    }

    // Remove all the cameras so that all activities being performed can be
    // stopped
    public boolean removeAllCameraViews()
    {
        try
        {
            stopAllCameraViews();

            io.evercam.androidapp.custom.FlowLayout camsLineView = (io.evercam.androidapp.custom
                    .FlowLayout) this.findViewById(R.id.cameras_flow_layout);
            camsLineView.removeAllViews();

            totalCamerasInGrid = 0;

            return true;
        }
        catch(Exception e)
        {
            Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
            if(Constants.isAppTrackingEnabled)
            {
                BugSenseHandler.sendException(e);

            }

            EvercamPlayApplication.sendCaughtException(this, e);
            CustomedDialog.showUnexpectedErrorDialog(CamerasActivity.this);
        }
        return false;
    }

    /**
     * Add all camera views to the main grid page
     *
     * @param reloadImages   reload camera images or not
     * @param showThumbnails show thumbnails that returned by Evercam or not, if true
     *                       and if thumbnail not available, it will request latest snapshot
     *                       instead. If false,
     *                       it will request neither thumbnail nor latest snapshot.
     */
    public boolean addAllCameraViews(final boolean reloadImages, final boolean showThumbnails)
    {
        try
        {
            // Recalculate camera per row
            camerasPerRow = recalculateCameraPerRow();

            final CustomScrollView scrollView = (CustomScrollView) this.findViewById(R.id
                    .cameras_scroll_view);

            io.evercam.androidapp.custom.FlowLayout camsLineView = (io.evercam.androidapp.custom
                    .FlowLayout) this.findViewById(R.id.cameras_flow_layout);

            final Rect bounds = readLiveBoundsOfScrollView();

            int screen_width = readScreenWidth(this);

            int index = 0;
            totalCamerasInGrid = 0;

            for(EvercamCamera evercamCamera : AppData.evercamCameraList)
            {
                final LinearLayout cameraListLayout = new LinearLayout(this);

                int indexPlus = index + 1;

                if(reloadImages) evercamCamera.loadingStatus = ImageLoadingStatus.not_started;

                final CameraLayout cameraLayout = new CameraLayout(this, evercamCamera,
                        showThumbnails);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(android.view
                        .ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams
                        .WRAP_CONTENT);
                params.width = ((indexPlus % camerasPerRow == 0) ? (screen_width - (index %
                        camerasPerRow) * (screen_width / camerasPerRow)) : screen_width /
                        camerasPerRow);
                params.width = params.width - 1; //1 pixels spacing between cameras
                params.height = (int) (params.width / (1.25));
                params.setMargins(1, 1, 0, 0); //1 pixels spacing between cameras
                cameraLayout.setLayoutParams(params);

                cameraListLayout.addView(cameraLayout);

                camsLineView.addView(cameraListLayout, new io.evercam.androidapp.custom
                        .FlowLayout.LayoutParams(0, 0));

                index++;

                /**
                 * If need to reload the images, read camera layout position and
                 * check the rectangle is within scope of the screen or not
                 */
                if(reloadImages)
                {
                    new Handler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Rect cameraBounds = new Rect();
                            cameraListLayout.getHitRect(cameraBounds);
                            if(Rect.intersects(cameraBounds, bounds))
                            {
                                cameraLayout.loadImage();
                            }
                        }
                    }, 300);
                }

                totalCamerasInGrid++;
            }

            if(this.getActionBar() != null) this.getActionBar().setHomeButtonEnabled(true);

            if(refresh != null) refresh.setActionView(null);

            // Only set up scroll listener if snapshots need to get reload
            if(reloadImages)
            {
                setScrollStopListenerFor(scrollView);
            }
            return true;
        }
        catch(Exception e)
        {
            Log.e(TAG, e.toString(), e);
            if(Constants.isAppTrackingEnabled)
            {
                BugSenseHandler.sendException(e);
            }

            EvercamPlayApplication.sendCaughtException(this, e);
            CustomedDialog.showUnexpectedErrorDialog(CamerasActivity.this);
        }
        return false;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        removeAllCameraViews();
    }

    /**
     * If screen get scrolled, for the moment of scroll stopping, load camera
     * snapshots within screen.
     */
    private void onScreenScrolled()
    {
        final Rect scrollViewBounds = readLiveBoundsOfScrollView();
        final io.evercam.androidapp.custom.FlowLayout camsLineView1 = (io.evercam.androidapp
                .custom.FlowLayout) CamerasActivity.this.findViewById(R.id.cameras_flow_layout);
        final int totalLayouts = camsLineView1.getChildCount();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                for(int index = 0; index < totalLayouts; index++)
                {
                    final LinearLayout cameraListLayout = (LinearLayout) camsLineView1.getChildAt
                            (index);
                    final CameraLayout cameraLayout = (CameraLayout) cameraListLayout.getChildAt(0);

                    if(cameraLayout.evercamCamera.loadingStatus == ImageLoadingStatus.not_started)

                    {
                        Rect cameraBounds = new Rect();
                        cameraListLayout.getHitRect(cameraBounds);
                        if(Rect.intersects(cameraBounds, scrollViewBounds))
                        {
                            CamerasActivity.this.runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    cameraLayout.loadImage();
                                }
                            });
                        }
                    }
                }
            }
        }).start();
    }

    private void setScrollStopListenerFor(final CustomScrollView scrollView)
    {
        scrollView.setOnTouchListener(new OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {

                if(event.getAction() == MotionEvent.ACTION_UP)
                {

                    scrollView.startScrollerTask();
                }

                return false;
            }
        });
        scrollView.setOnScrollStoppedListener(new OnScrollStoppedListener()
        {

            @Override
            public void onScrollStopped()
            {

                Log.d(TAG, "Scroll stopped");
                onScreenScrolled();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        resizeCameras();
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if(Constants.isAppTrackingEnabled)
        {
            if(Constants.isAppTrackingEnabled) BugSenseHandler.startSession(this);
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();

        if(AppData.defaultUser != null)
        {
            usernameOnStop = AppData.defaultUser.getUsername();
        }

        if(Constants.isAppTrackingEnabled)
        {
            if(Constants.isAppTrackingEnabled) BugSenseHandler.closeSession(this);
        }
    }

    private void showAddCameraOptionsDialog()
    {
        final View optionsView = getLayoutInflater().inflate(R.layout.add_camera_options_list,
                null);
        final AlertDialog dialog = CustomedDialog.getAlertDialogNoTitle(CamerasActivity.this,
                optionsView);
        dialog.show();

        Button addCameraButton = (Button) optionsView.findViewById(R.id.btn_add_ip_camera);
        Button scanCameraButton = (Button) optionsView.findViewById(R.id.btn_scan_for_camera);

        addCameraButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();

                EvercamPlayApplication.sendEventAnalytics(CamerasActivity.this,
                        R.string.category_menu, R.string.action_add_camera,
                        R.string.label_add_camera_manually);

                startActivityForResult(new Intent(CamerasActivity.this,
                        AddEditCameraActivity.class), Constants.REQUEST_CODE_ADD_CAMERA);
            }
        });

        scanCameraButton.setOnClickListener(new OnClickListener()
        {
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

    public static void logOutUser(Activity activity)
    {
        new EvercamAccount(activity).remove(AppData.defaultUser.getEmail(), null);

        // clear real-time default app data
        AppData.reset();

        activity.finish();
        activity.startActivity(new Intent(activity, SlideActivity.class));
    }

    private void showSignOutDialog()
    {
        CustomedDialog.getConfirmLogoutDialog(this, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                EvercamPlayApplication.sendEventAnalytics(CamerasActivity.this,
                        R.string.category_menu, R.string.action_logout, R.string.label_user_logout);
                logOutUser(CamerasActivity.this);
            }
        }).show();
    }

    public static int readScreenWidth(Activity activity)
    {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static int readScreenHeight(Activity activity)
    {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    private int recalculateCameraPerRow()
    {
        int screenWidth = readScreenWidth(this);
        int maxCamerasPerRow = 3;
        int minCamerasPerRow = 1;
        if(screenWidth != 0)
        {
            maxCamerasPerRow = screenWidth / 350;
        }

        int oldCamerasPerRow = PrefsManager.getCameraPerRow(this, 2);
        if(maxCamerasPerRow < oldCamerasPerRow && maxCamerasPerRow != 0)
        {
            PrefsManager.setCameraPerRow(this, maxCamerasPerRow);
            return maxCamerasPerRow;
        }
        else if(maxCamerasPerRow == 0)
        {
            return minCamerasPerRow;
        }
        return oldCamerasPerRow;
    }

    private Rect readLiveBoundsOfScrollView()
    {
        CustomScrollView scrollView = (CustomScrollView) CamerasActivity.this.findViewById(R.id
                .cameras_scroll_view);
        return scrollView.getLiveBoundsRect();
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
            if(hasNetwork)
            {
                if(type == InternetCheckType.START)
                {
                    startLoadingCameras();
                }
                else if(type == InternetCheckType.RESTART)
                {
                    if(reloadCameraList || !liveViewCameraId.isEmpty())
                    {
                        // If returned from account management, the
                        // default user could possibly changed,
                        // so remove all cameras and reload.

                        // addUsersToDropdownActionBar();
                        removeAllCameraViews();
                        startLoadingCameras();
                        reloadCameraList = false;
                    }
                    else
                    {
                        // Re-calculate camera per row because screen size
                        // could change because of screen rotation.
                        int camsOldValue = camerasPerRow;
                        camerasPerRow = recalculateCameraPerRow();
                        if(camsOldValue != camerasPerRow)
                        {
                            removeAllCameraViews();
                            addAllCameraViews(true, true);
                        }

                        // Refresh camera names in case it's changed from camera
                        // live view
                        updateCameraNames();
                    }
                }
            }
            else
            {
                CustomedDialog.showInternetNotConnectDialog(CamerasActivity.this);
            }
        }
    }
}
