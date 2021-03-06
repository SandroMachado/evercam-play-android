package io.evercam.androidapp.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

import io.evercam.androidapp.ScanActivity;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.feedback.ScanFeedbackItem;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.NetInfo;
import io.evercam.network.EvercamDiscover;
import io.evercam.network.discovery.DiscoveredCamera;
import io.evercam.network.discovery.ScanRange;

public class ScanForCameraTask extends AsyncTask<Void, Void, ArrayList<DiscoveredCamera>>
{
    private final String TAG = "ScanForCameraTask";
    private ScanActivity scanActivity;
    private NetInfo netInfo;
    private Date startTime;

    public ScanForCameraTask(ScanActivity scanActivity)
    {
        this.scanActivity = scanActivity;
        netInfo = new NetInfo(scanActivity);
    }

    @Override
    protected ArrayList<DiscoveredCamera> doInBackground(Void... params)
    {
        startTime = new Date();
        ArrayList<DiscoveredCamera> cameraList = null;
        try
        {
            EvercamDiscover evercamDiscover = new EvercamDiscover();
            ScanRange scanRange = new ScanRange(netInfo.getGatewayIp(), netInfo.getNetmaskIp());
            cameraList = evercamDiscover.discoverAllAndroid(scanRange);
        }
        catch(Exception e)
        {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }

        return cameraList;
    }

    @Override
    protected void onPostExecute(ArrayList<DiscoveredCamera> cameraList)
    {
        Float scanningTime = Commons.calculateTimeDifferenceFrom(startTime);
        Log.d(TAG, "Scanning time: " + scanningTime);

        String username = "";
        if(AppData.defaultUser != null)
        {
            username = AppData.defaultUser.getUsername();
        }
        new ScanFeedbackItem(scanActivity, username, scanningTime, cameraList).sendToKeenIo();

        scanActivity.showProgress(false);

        scanActivity.showScanResults(cameraList);
    }
}
