package io.evercam.androidapp.tasks;

import java.util.ArrayList;

import org.apache.http.cookie.Cookie;

import io.evercam.Camera;
import io.evercam.CameraDetail;
import io.evercam.EvercamException;
import io.evercam.androidapp.AddEditCameraActivity;
import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.dal.DbCamera;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.CameraStatus;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.video.VideoActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AddCameraTask extends AsyncTask<Void, Void, EvercamCamera>
{
	private final String TAG = "evercamplay-AddCameraTask";
	private CameraDetail cameraDetail;
	private Activity activity;
	private CustomProgressDialog customProgressDialog;
	private String errorMessage;
	private boolean isReachableExternally = false;
	private boolean isReachableInternally = false;

	public AddCameraTask(CameraDetail cameraDetail, Activity activity)
	{
		this.cameraDetail = cameraDetail;
		this.activity = activity;
	}

	@Override
	protected void onPreExecute()
	{
		errorMessage = activity.getString(R.string.unknown_error);
		customProgressDialog = new CustomProgressDialog(activity);
		customProgressDialog.show(activity.getString(R.string.testing_snapshot));
	}

	@Override
	protected void onPostExecute(EvercamCamera evercamCamera)
	{
		customProgressDialog.dismiss();
		if (evercamCamera != null)
		{
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
			CustomToast.showInCenterLong(activity, errorMessage);
		}
	}

	@Override
	protected EvercamCamera doInBackground(Void... params)
	{
		isReachableExternally = isSnapshotReachableExternally();
		isReachableInternally = isSnapshotReachableInternally();
		this.publishProgress();
		return createCamera(cameraDetail);
	}

	@Override
	protected void onProgressUpdate(Void... values)
	{
		customProgressDialog.setMessage(activity.getString(R.string.creating_camera));
	}

	private boolean isSnapshotReachableExternally()
	{
		String externalHost = cameraDetail.getExternalHost();
		final String username = cameraDetail.getCameraUsername();
		final String password = cameraDetail.getCameraPassword();
		String jpgUrlString = cameraDetail.getJpgUrl();

		final String jpgUrl = AddEditCameraActivity.buildJpgUrlWithSlash(jpgUrlString);

		if (externalHost != null && !externalHost.isEmpty())
		{
			String portString = String.valueOf(cameraDetail.getExternalHttpPort());
			String externalFullUrl = buildFullHttpUrl(externalHost, portString, jpgUrl);
			
			ArrayList<Cookie> cookies = new ArrayList<Cookie>();
			try
			{
				Drawable drawable = Commons.getDrawablefromUrlAuthenticated(externalFullUrl, username, password, cookies, 3000);
				if(drawable != null)
				{
					return true;
				}
			}
			catch (Exception e)
			{
				Log.e(TAG, e.toString());
			}
		}
		return false;
	}
	
	private boolean isSnapshotReachableInternally()
	{
		String internalHost = cameraDetail.getInternalHost();

		final String username = cameraDetail.getCameraUsername();
		final String password = cameraDetail.getCameraPassword();
		String jpgUrlString = cameraDetail.getJpgUrl();

		final String jpgUrl = AddEditCameraActivity.buildJpgUrlWithSlash(jpgUrlString);

		if (internalHost!= null && !internalHost.isEmpty())
		{
			String portString = String.valueOf(cameraDetail.getInternalHttpPort());
			String internalFullUrl = buildFullHttpUrl(internalHost, portString, jpgUrl);
			
			ArrayList<Cookie> cookies = new ArrayList<Cookie>();
			try
			{
				Drawable drawable = Commons.getDrawablefromUrlAuthenticated(internalFullUrl, username, password, cookies, 3000);
				if(drawable != null)
				{
					return true;
				}
			}
			catch (Exception e)
			{
				Log.e(TAG, e.toString());
			}
		}
		return false;
	}
	
	private String buildFullHttpUrl(String host, String portString, String jpgEnding)
	{
		if(portString == null || portString.isEmpty() || portString.equals("0"))
		{
			portString = "80";
		}
		return activity.getString(R.string.prefix_http) + host + ":" + portString + jpgEnding;
	}

	private EvercamCamera createCamera(CameraDetail detail)
	{
		try
		{
			Camera.create(detail);
			Camera camera = Camera.getById(detail.getId(), false);
			EvercamCamera evercamCamera = new EvercamCamera().convertFromEvercam(camera);
			// Mandatory check camera snapshot after create camera.
			//TODO: What if camera is offline?
			//TODO: What if camera is online locally?
			if(isReachableExternally || isReachableInternally)
			{
				evercamCamera.setStatus(CameraStatus.ACTIVE);
			}
			DbCamera dbCamera = new DbCamera(activity);
			dbCamera.addCamera(evercamCamera);
			AppData.evercamCameraList.add(evercamCamera);

			return evercamCamera;
		}
		catch (EvercamException e)
		{
			errorMessage = e.getMessage();
			Log.e(TAG, "add camera to evercam: " + e.getMessage());
			return null;
		}
	}
}
