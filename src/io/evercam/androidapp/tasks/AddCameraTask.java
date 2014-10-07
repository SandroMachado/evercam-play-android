package io.evercam.androidapp.tasks;

import java.util.ArrayList;

import org.apache.http.cookie.Cookie;

import io.evercam.Camera;
import io.evercam.CameraDetail;
import io.evercam.EvercamException;
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
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.video.VideoActivity;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

public class AddCameraTask extends AsyncTask<Void, Boolean, EvercamCamera>
{
	private final String TAG = "evercamplay-AddCameraTask";
	private CameraDetail cameraDetail;
	private Activity activity;
	private CustomProgressDialog customProgressDialog;
	private String errorMessage = null;
	private boolean isReachableExternally = false;
	private boolean isReachableInternally = false;
	private Boolean readyToCreateCamera = null;
	private boolean isFromScan;

	public AddCameraTask(CameraDetail cameraDetail, Activity activity, boolean isFromScan)
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
		if (evercamCamera != null)
		{
			if (isFromScan)
			{
				EvercamPlayApplication.sendEventAnalytics(activity, R.string.category_add_camera,
						R.string.action_addcamera_success_scan,
						R.string.label_addcamera_successful_scan);
			}
			else
			{
				EvercamPlayApplication.sendEventAnalytics(activity, R.string.category_add_camera,
						R.string.action_addcamera_success, R.string.label_addcamera_successful);
			}

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
			if (errorMessage != null)
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
		if (!isReachableExternally)
		{
			isReachableInternally = isSnapshotReachableInternally();
		}

		if (isReachableExternally || isReachableInternally)
		{
			publishProgress(true);
		}
		else
		{
			publishProgress(false);
		}

		while (readyToCreateCamera == null)
		{
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				Log.e(TAG, e.toString());
			}
		}

		if (readyToCreateCamera)
		{
			return createCamera(cameraDetail);
		}
		else if (!readyToCreateCamera)
		{
			Log.d(TAG, "Not ready to create camera");
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Boolean... values)
	{
		boolean isSnapshotReceived = values[0];

		if (isSnapshotReceived)
		{
			customProgressDialog.setMessage(activity.getString(R.string.creating_camera));
			readyToCreateCamera = true;
		}
		else
		{
			CustomedDialog.getConfirmCreateDialog(activity, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					readyToCreateCamera = true;
					return;
				}
			}, new DialogInterface.OnClickListener(){
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
				Drawable drawable = Commons.getDrawablefromUrlAuthenticated(externalFullUrl,
						username, password, cookies, 3000);
				if (drawable != null)
				{
					// Save this image.
					Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
					new Thread(new SaveImageRunnable(activity, bitmap, cameraDetail.getId()))
							.start();
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

		if (internalHost != null && !internalHost.isEmpty())
		{
			String portString = String.valueOf(cameraDetail.getInternalHttpPort());
			String internalFullUrl = buildFullHttpUrl(internalHost, portString, jpgUrl);

			ArrayList<Cookie> cookies = new ArrayList<Cookie>();
			try
			{
				Drawable drawable = Commons.getDrawablefromUrlAuthenticated(internalFullUrl,
						username, password, cookies, 3000);
				if (drawable != null)
				{
					// Save this image.
					Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
					new Thread(new SaveImageRunnable(activity, bitmap, cameraDetail.getId()))
							.start();
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
		if (portString == null || portString.isEmpty() || portString.equals("0"))
		{
			portString = "80";
		}
		return activity.getString(R.string.prefix_http) + host + ":" + portString + jpgEnding;
	}

	private EvercamCamera createCamera(CameraDetail detail)
	{
		try
		{
			Camera camera = Camera.create(detail);
			// Camera camera = Camera.getById(detail.getId(), false);
			EvercamCamera evercamCamera = new EvercamCamera().convertFromEvercam(camera);
			if (isReachableExternally || isReachableInternally)
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
