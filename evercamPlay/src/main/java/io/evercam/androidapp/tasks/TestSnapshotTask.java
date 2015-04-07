package io.evercam.androidapp.tasks;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.cookie.Cookie;

import java.net.URL;
import java.util.ArrayList;

import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.feedback.TestSnapshotFeedbackItem;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;
import io.evercam.network.discovery.PortScan;
import io.keen.client.android.AndroidKeenClientBuilder;
import io.keen.client.java.KeenClient;
import io.keen.client.java.KeenProject;

public class TestSnapshotTask extends AsyncTask<Void, Void, Drawable>
{
    private final String TAG = "TestSnapshotTask";
    private String url;
    private String username;
    private String password;
    private Activity activity;
    private CustomProgressDialog customProgressDialog;
    private String errorMessage = null;

    public TestSnapshotTask(String url, String username, String password, Activity activity)
    {
        this.url = url;
        this.username = username;
        this.password = password;
        this.activity = activity;
    }

    @Override
    protected void onPreExecute()
    {
        customProgressDialog = new CustomProgressDialog(activity);
        customProgressDialog.show(activity.getString(R.string.retrieving_snapshot));
    }

    @Override
    protected Drawable doInBackground(Void... params)
    {
        try
        {
            URL urlObject = new URL(url);
            boolean isReachable = PortScan.isPortReachable(urlObject.getHost(),
                    urlObject.getPort());
            if(!isReachable)
            {
                errorMessage = activity.getString(R.string.snapshot_test_port_closed);
                return null;
            }
        }
        catch(Exception e)
        {
            Log.e(TAG, e.toString());
            return null;
        }

        ArrayList<Cookie> cookies = new ArrayList<Cookie>();
        try
        {
            return Commons.getDrawablefromUrlAuthenticated(url, username, password, cookies, 5000);
        }
        catch(Exception e)
        {
            Log.e(TAG, e.toString());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Drawable drawable)
    {
        customProgressDialog.dismiss();

        KeenClient client = new AndroidKeenClientBuilder(activity).build();
        KeenProject keenProject = new KeenProject(Constants.KEEN_PROJECT_ID, Constants.KEEN_WRITE_KEY,
                Constants.KEEN_READ_KEY);
        client.setDefaultProject(keenProject);

        if(drawable != null)
        {
            CustomToast.showSnapshotTestResult(activity, R.string.snapshot_test_success);
            CustomedDialog.getSnapshotDialog(activity, drawable).show();

            new TestSnapshotFeedbackItem(activity, AppData.defaultUser.getUsername(), true, true)
            .setSnapshot_url(url).setCam_username(username).setCam_password(password).sendToKeenIo(client);
        }
        else
        {
            String username = "";
            if(AppData.defaultUser != null)
            {
                username = AppData.defaultUser.getUsername();
            }

            if(errorMessage == null)
            {
                CustomToast.showInCenterLong(activity, R.string.snapshot_test_failed);
                new TestSnapshotFeedbackItem(activity, username, false, true)
                        .setSnapshot_url(url).setCam_username(username).setCam_password(password).sendToKeenIo(client);
            }
            else
            {
                CustomToast.showInCenterLong(activity, errorMessage);
                new TestSnapshotFeedbackItem(activity, username, false, false)
                        .setSnapshot_url(url).setCam_username(username).setCam_password(password).sendToKeenIo(client);
            }
        }
    }
}
