package io.evercam.androidapp.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import io.evercam.API;
import io.evercam.Camera;
import io.evercam.androidapp.authentication.EvercamAccount;
import io.evercam.androidapp.dal.DbCamera;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.dto.EvercamCamera;

public class CameraListSyncAdapter extends AbstractThreadedSyncAdapter
{
    private static final String TAG = "CameraListSyncAdapter";
    private Context mContext;

    public CameraListSyncAdapter (Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult)
    {
        Log.d(TAG, "onPerformSync: " + account.name);

        boolean updateDB = false;

        String originalApiKey = API.getUserKeyPair()[0];
        String originalApiId = API.getUserKeyPair()[1];

        // Get the user details for the current account
        AppUser appUser = new EvercamAccount(mContext).retrieveUserDetailFromAccount(account);
        String apiKey = appUser.getApiKey();
        String apiId = appUser.getApiId();
        String username = appUser.getUsername();
        boolean isDefault = appUser.getIsDefault();

        if(isDefault)
        {
            //Only sync the default user for now because multiple users are causing problems
            try
            {
                API.setUserKeyPair(apiKey, apiId);

                ArrayList<EvercamCamera> databaseCameralist = new DbCamera(mContext).getCamerasByOwner(username, 500);

                Log.d(TAG, "Total cameras for user - " + username + " from DB: " + databaseCameralist.size());

                ArrayList<Camera> cameras = Camera.getAll(username, true, false);

                //Reset API key pair for default user because here it could be syncing other
                // users' cameras
                API.setUserKeyPair(originalApiKey, originalApiId);

                ArrayList<EvercamCamera> evercamCameras = new ArrayList<>();
                for(io.evercam.Camera camera : cameras)
                {
                    EvercamCamera evercamCamera = new EvercamCamera().convertFromEvercam(camera);

                    evercamCameras.add(evercamCamera);
                }

                if(databaseCameralist.size() != cameras.size())
                {
                    updateDB = true;
                }

                for(EvercamCamera camera : evercamCameras)
                {
                    if(!databaseCameralist.contains(camera))
                    {
                        Log.d(TAG, "new camera detected: " + camera.getCameraId());
                        updateDB = true;
                        break;
                    }
                }

                // Step 3: Check if any local camera no longer exists in Evercam
                if(!updateDB)
                {
                    for(EvercamCamera camera : databaseCameralist)
                    {
                        if(!evercamCameras.contains(camera))
                        {
                            Log.d(TAG, "camera deleted");
                            updateDB = true;
                            break;
                        }
                    }
                }

                // Step 4: If any different camera, replace all local camera data.
                if(updateDB)
                {
                    Log.d(TAG, "Updating DB in sync!!!!!");
                    DbCamera dbCamera = new DbCamera(mContext);
                    dbCamera.deleteCameraByOwner(username);

                    for(EvercamCamera camera : evercamCameras)
                    {
                        dbCamera.addCamera(camera);
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
