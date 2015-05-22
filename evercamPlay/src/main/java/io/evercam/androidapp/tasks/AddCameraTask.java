package io.evercam.androidapp.tasks;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import io.evercam.Camera;
import io.evercam.CameraDetail;
import io.evercam.EvercamException;
import io.evercam.Snapshot;
import io.evercam.androidapp.AddEditCameraActivity;
import io.evercam.androidapp.EvercamPlayApplication;
import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dal.DbCamera;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.CameraStatus;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.feedback.KeenHelper;
import io.evercam.androidapp.feedback.NewCameraFeedbackItem;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.video.VideoActivity;

public class AddCameraTask extends AsyncTask<Void, Boolean, EvercamCamera>
{
    private final String TAG = "AddCameraTask";
    private CameraDetail cameraDetail;
    private AddEditCameraActivity activity;
    private CustomProgressDialog customProgressDialog;
    private String errorMessage = null;
    private boolean isReachableExternally = false;
    private Boolean readyToCreateCamera = null;
    private boolean isFromScan;

    public AddCameraTask(CameraDetail cameraDetail, AddEditCameraActivity activity, boolean isFromScan)
    {
        this.cameraDetail = cameraDetail;
        this.activity = activity;
        this.isFromScan = isFromScan;
    }

    @Override
    protected void onPreExecute()
    {
        customProgressDialog = new CustomProgressDialog(activity);
        customProgressDialog.show(activity.getString(R.string.testing_snapshot));
    }

    @Override
    protected void onPostExecute(EvercamCamera evercamCamera)
    {
        customProgressDialog.dismiss();
        if(evercamCamera != null)
        {
            NewCameraFeedbackItem newCameraItem = new NewCameraFeedbackItem(activity,
                    AppData.defaultUser.getUsername(), cameraDetail.getId());
            if(isFromScan)
            {
                EvercamPlayApplication.sendEventAnalytics(activity, R.string.category_add_camera,
                        R.string.action_addcamera_success_scan,
                        R.string.label_addcamera_successful_scan);
                newCameraItem.setIsFromDiscovery(true);

            }
            else
            {
                EvercamPlayApplication.sendEventAnalytics(activity, R.string.category_add_camera,
                        R.string.action_addcamera_success_manual, R.string.label_addcamera_successful_manual);
            }

            activity.getMixpanel().sendEvent(R.string.mixpanel_event_create_camera, null);

            newCameraItem.sendToKeenIo(KeenHelper.getClient(activity));

            CustomToast.showInBottom(activity, R.string.create_success);

            /**
             * Successfully added a camera, so refresh camera list.
             */
            Intent returnIntent = new Intent();
            activity.setResult(Constants.RESULT_TRUE, returnIntent);
            // activity.finish();

            /**
             * Successfully added camera, show camera live view and finish add
             * camera activity
             */
            VideoActivity.startPlayingVideoForCamera(activity, evercamCamera.getCameraId());
            activity.finish();
        }
        else
        {
            if(errorMessage != null)
            {
                CustomToast.showInCenterLong(activity, errorMessage);
            }
        }
    }

    @Override
    protected EvercamCamera doInBackground(Void... params)
    {
        // Check camera is reachable or not by request for snapshot
        // If either internal or external url return a snapshot, create the
        // camera
        // If neither of the urls return a snapshot, warn the user.
        isReachableExternally = isSnapshotReachableExternally();

        if(isReachableExternally)
        {
            publishProgress(true);
        }
        else
        {
            publishProgress(false);
        }

        while(readyToCreateCamera == null)
        {
            try
            {
                Thread.sleep(500);
            }
            catch(InterruptedException e)
            {
                Log.e(TAG, e.toString());
            }
        }

        if(readyToCreateCamera)
        {
            return createCamera(cameraDetail);
        }
        else if(!readyToCreateCamera)
        {
            Log.d(TAG, "Not ready to create camera");
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Boolean... values)
    {
        boolean isSnapshotReceived = values[0];

        if(isSnapshotReceived)
        {
            customProgressDialog.setMessage(activity.getString(R.string.creating_camera));
            readyToCreateCamera = true;
        }
        else
        {
            if(!activity.isFinishing())
            {
                CustomedDialog.getConfirmCreateDialog(activity,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                customProgressDialog.setMessage(activity.getString(R.string.creating_camera));
                                readyToCreateCamera = true;
                                return;
                            }
                        }, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                readyToCreateCamera = false;
                                customProgressDialog.dismiss();
                                return;
                            }
                        }).show();
            }
        }
    }

    private boolean isSnapshotReachableExternally()
    {
        String externalHost = cameraDetail.getExternalHost();
        final String username = cameraDetail.getCameraUsername();
        final String password = cameraDetail.getCameraPassword();
        String jpgUrlString = cameraDetail.getJpgUrl();

        final String jpgUrl = AddEditCameraActivity.buildJpgUrlWithSlash(jpgUrlString);

        if(externalHost != null && !externalHost.isEmpty())
        {
            String portString = String.valueOf(cameraDetail.getExternalHttpPort());
            String externalUrl = buildHttpUrl(externalHost, portString);

            try
            {
                Snapshot snapshot = Camera.testSnapshot(externalUrl, jpgUrl, username, password);
                byte[] snapshotData = snapshot.getData();
                Bitmap bitmap = BitmapFactory.decodeByteArray(snapshotData, 0, snapshotData.length);


                if(bitmap != null)
                {
                    // Save this image.
                    new Thread(new SaveImageRunnable(activity, bitmap,
                            cameraDetail.getId())).start();
                    return true;
                }
            }
            catch(Exception e)
            {
                Log.e(TAG, e.toString());
            }
        }
        return false;
    }

    private String buildHttpUrl(String host, String portString)
    {
        if(portString == null || portString.isEmpty() || portString.equals("0"))
        {
            portString = "80";
        }
        return activity.getString(R.string.prefix_http) + host + ":" + portString;
    }

    private EvercamCamera createCamera(CameraDetail detail)
    {
        try
        {
            Camera camera = Camera.create(detail);
            // Camera camera = Camera.getById(detail.getId(), false);
            EvercamCamera evercamCamera = new EvercamCamera().convertFromEvercam(camera);
            if(isReachableExternally)
            {
                evercamCamera.setStatus(CameraStatus.ACTIVE);
            }
            DbCamera dbCamera = new DbCamera(activity);
            dbCamera.addCamera(evercamCamera);
            AppData.evercamCameraList.add(evercamCamera);

            return evercamCamera;
        }
        catch(EvercamException e)
        {
            errorMessage = e.getMessage();
            Log.e(TAG, "add camera to evercam: " + e.getMessage());
            return null;
        }
    }
}
