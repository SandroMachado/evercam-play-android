package io.evercam.androidapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

import io.evercam.androidapp.authentication.EvercamAccount;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dal.DbCamera;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.tasks.CheckInternetTask;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.PrefsManager;
import io.evercam.androidapp.video.HomeShortcut;

/*
 * Main starting activity. 
 * Checks whether user should login first or load the cameras straight away
 * */
public class MainActivity extends Activity
{
    private static final String TAG = "evercamplay-MainActivity";
    private String liveViewCameraId = "";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.d(TAG,"onCreate");
        if(Constants.isAppTrackingEnabled)
        {
            BugSenseHandler.initAndStartSession(this, Constants.bugsense_ApiKey);
        }

        readShortcutCameraId();

        setContentView(R.layout.mainactivitylayout);

        launch();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        launch();
    }

    private void launch()
    {
        int versionCode = Commons.getAppVersionCode(this);
        boolean isReleaseNotesShown = PrefsManager.isRleaseNotesShown(this, versionCode);

        if(versionCode > 0)
        {
            if(isReleaseNotesShown)
            {
                startApplication();
            }
            else
            {
                Intent act = new Intent(MainActivity.this, ReleaseNotesActivity.class);
                startActivity(act);
                this.finish();
            }
        }
    }

    private void startApplication()
    {
        new MainCheckInternetTask(MainActivity.this).executeOnExecutor(AsyncTask
                .THREAD_POOL_EXECUTOR);
    }

    private void startCamerasActivity()
    {
        int notificationID = 0;
        String strNotificationID = this.getIntent().getStringExtra(Constants
                .GCMNotificationIDString);

        if(strNotificationID != null && !strNotificationID.equals(""))
            notificationID = Integer.parseInt(strNotificationID);

        if(CamerasActivity.activity != null)
        {
            CamerasActivity.activity.finish();
        }

        Intent intent = new Intent(this, CamerasActivity.class);
        intent.putExtra(Constants.GCMNotificationIDString, notificationID);

        //Parse the camera id from shortcut to the next activity
        if(!liveViewCameraId.isEmpty())
        {
            intent.putExtra(HomeShortcut.KEY_CAMERA_ID, liveViewCameraId);
        }
        this.startActivity(intent);

        MainActivity.this.finish();
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if(Constants.isAppTrackingEnabled)
        {
            if(Constants.isAppTrackingEnabled)
            {
                BugSenseHandler.startSession(this);
            }
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();

        if(Constants.isAppTrackingEnabled)
        {
            if(Constants.isAppTrackingEnabled)
            {
                BugSenseHandler.closeSession(this);
            }
        }
    }

    private boolean isUserLogged()
    {
        try
        {
            String defaultEmail = PrefsManager.getUserEmail(this);
            if(defaultEmail != null)
            {
                AppUser defaultUser = new EvercamAccount(this).retrieveUserByEmail(defaultEmail);
                AppData.defaultUser = defaultUser;
                AppData.evercamCameraList = new DbCamera(this).getCamerasByOwner(defaultUser
                        .getUsername(), 500);
            }
        }
        catch(Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            BugSenseHandler.sendException(e);
            EvercamPlayApplication.sendCaughtException(this, e);
            CustomedDialog.showUnexpectedErrorDialog(MainActivity.this);
        }
        return (AppData.defaultUser != null);
    }

    private void readShortcutCameraId()
    {
        Intent liveViewIntent = this.getIntent();
        if(liveViewIntent != null && liveViewIntent.getExtras() != null)
        {
            liveViewCameraId = liveViewIntent.getExtras().getString(HomeShortcut.KEY_CAMERA_ID, "");
        }
    }

    class MainCheckInternetTask extends CheckInternetTask
    {

        public MainCheckInternetTask(Context context)
        {
            super(context);
        }

        @Override
        protected void onPostExecute(Boolean hasNetwork)
        {
            if(hasNetwork)
            {
                if(isUserLogged())
                {
                    finish();
                    startCamerasActivity();
                }
                else
                {
                    finish();
                    Intent slideIntent = new Intent(MainActivity.this, SlideActivity.class);
                    startActivity(slideIntent);
                }
            }
            else
            {
                CustomedDialog.showInternetNotConnectDialog(MainActivity.this);
            }
        }
    }
}