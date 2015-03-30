package io.evercam.androidapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.bugsense.trace.BugSenseHandler;

import io.evercam.API;
import io.evercam.androidapp.authentication.EvercamAccount;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dal.DbCamera;
import io.evercam.androidapp.dto.AppData;
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
    private static final String TAG = "evercam-MainActivity";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(Constants.isAppTrackingEnabled)
        {
            BugSenseHandler.initAndStartSession(this, Constants.bugsense_ApiKey);
        }

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
        if(CamerasActivity.activity != null)
        {
            CamerasActivity.activity.finish();
        }

        this.startActivity(new Intent(this, CamerasActivity.class));

        MainActivity.this.finish();
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

    public static boolean isUserLogged(Context context)
    {
        AppData.defaultUser = new EvercamAccount(context).getDefaultUser();
        if(AppData.defaultUser != null)
        {
            AppData.evercamCameraList = new DbCamera(context).getCamerasByOwner(AppData
                    .defaultUser.getUsername(), 500);
            API.setUserKeyPair(AppData.defaultUser.getApiKey(), AppData.defaultUser.getApiId());
            return true;
        }
        return false;
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
                if(isUserLogged(MainActivity.this))
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