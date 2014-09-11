package io.evercam.androidapp.custom;

import io.evercam.EvercamException;
import io.evercam.androidapp.dto.*;
import io.evercam.androidapp.tasks.DownloadLatestTask;
import io.evercam.androidapp.tasks.SaveImageRunnable;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.EvercamFile;
import io.evercam.androidapp.video.VideoActivity;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import org.apache.http.cookie.Cookie;
import com.bugsense.trace.BugSenseHandler;

import io.evercam.androidapp.CamerasActivity;
import io.evercam.androidapp.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.widget.*;

public class CameraLayout extends LinearLayout
{
	private static final String TAG = "evercamplay-CameraLayout";

	public RelativeLayout cameraRelativeLayout;
	private TextView titleText;

	public Context context;
	public EvercamCamera evercamCamera;
	private DownloadLiveImageTask liveImageTask;
	private DownloadLatestTask latestTask;

	private boolean end = false; // tells whether application has ended or not.
									// If it is
	// true, all tasks must end and no further
	// processing should be done in any thread.
	private ProgressView loadingAnimation = null;
	private TextView imageMessage = null;
	private boolean isImageLoadedFromCache = false;

	// Handler for the handling the next request. It will call the image loading
	// thread so that it can proceed with next step.
	public final Handler handler = new Handler();

	public CameraLayout(final Activity activity, EvercamCamera camera)
	{
		super(activity.getApplicationContext());
		this.context = activity.getApplicationContext();

		try
		{
			evercamCamera = camera;

			this.setOrientation(LinearLayout.VERTICAL);
			this.setGravity(Gravity.LEFT);

			this.setBackgroundColor(Color.WHITE);

			LinearLayout titleLayout = new LinearLayout(context);
			titleLayout.setLayoutParams(new LinearLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
			titleLayout.setBackgroundColor(Color.parseColor("#414042"));
			titleLayout.setOrientation(LinearLayout.HORIZONTAL);
			titleLayout.setGravity(Gravity.CENTER_HORIZONTAL);

			// text view to show the camera name on the top grey band of camera
			titleText = new TextView(context);
			titleText.setText(evercamCamera.getName());
			titleText.setLayoutParams(new LinearLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
			titleText.setTextColor(Color.parseColor("#f1f1f1"));

			titleLayout.addView(titleText);

			this.addView(titleLayout);

			cameraRelativeLayout = new RelativeLayout(context);
			RelativeLayout.LayoutParams ivParams = new RelativeLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					android.view.ViewGroup.LayoutParams.MATCH_PARENT);
			cameraRelativeLayout.setLayoutParams(ivParams);

			this.addView(cameraRelativeLayout);

			// control to show progress spinner
			loadingAnimation = new ProgressView(context);
			RelativeLayout.LayoutParams ivProgressParams = new RelativeLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			ivProgressParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			ivProgressParams.addRule(RelativeLayout.CENTER_VERTICAL);
			loadingAnimation.setLayoutParams(ivProgressParams);

			cameraRelativeLayout.addView(loadingAnimation);

			// Message to show the status of the camera
			imageMessage = new TextView(context);
			RelativeLayout.LayoutParams ivMessageParams = new RelativeLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			ivMessageParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			ivMessageParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			imageMessage.setLayoutParams(ivMessageParams);
			imageMessage.setText(R.string.connecting);
			imageMessage.setGravity(Gravity.CENTER);
			cameraRelativeLayout.addView(imageMessage);

			cameraRelativeLayout.setClickable(true);

			// Show thumbnail returned from Evercam
			showThumbnail();
			cameraRelativeLayout.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v)
				{
					VideoActivity.startPlayingVideoForCamera(activity, evercamCamera.getCameraId());
				}
			});

			loadImage();
		}
		catch (OutOfMemoryError e)
		{
			Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
			if (Constants.isAppTrackingEnabled)
			{
				BugSenseHandler.sendException(e);
			}
		}
	}

	// This method will load image from cache and call the image loading thread
	// for further processing
	public boolean loadImageFromCache()
	{
		try
		{
			File cacheFile = EvercamFile.getCacheFile(context, evercamCamera.getCameraId());
			File externalFile = EvercamFile.getExternalFile(context, evercamCamera.getCameraId());

			if (cacheFile.exists() || (externalFile != null && externalFile.exists()))
			{
				Drawable drawable = null;
				if (cacheFile.exists())
				{
					drawable = Drawable.createFromPath(cacheFile.getPath());
				}
				else
				{
					drawable = Drawable.createFromPath(externalFile.getPath());
				}

				if (drawable != null)
				{
					cameraRelativeLayout.setBackgroundDrawable(drawable);

					isImageLoadedFromCache = true;
					setlayoutForImageLoadedFromCache();
					return true;
				}
				else
				{
					File file = new File(cacheFile.getPath());
					if (file.exists())
					{
						file.delete();
					}
				}
			}

			// if image status was loaded on previously and now image is not in
			// cache, then we need to restart this
			// Disabled to see how it works without this.
			// if (!isImageLoadedFromCache
			// && (this.evercamCamera.loadingStatus ==
			// ImageLoadingStatus.live_received ||
			// this.evercamCamera.loadingStatus ==
			// ImageLoadingStatus.camba_image_received))
			// this.evercamCamera.loadingStatus =
			// ImageLoadingStatus.not_started;
		}
		catch (OutOfMemoryError e)
		{
			isImageLoadedFromCache = false;
			Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
			return false;
		}
		catch (Exception e)
		{
			isImageLoadedFromCache = false;
			Log.e(TAG, "Load file from path: " + e.toString());
			if (Constants.isAppTrackingEnabled)
			{
				BugSenseHandler.sendException(e);
			}
		}
		return false;
	}

	// This method will call the image
	// loading thread to further load from camera
	public void loadImage()
	{
		isImageLoadedFromCache = loadImageFromCache();

		if (!end)
		{
			handler.postDelayed(LoadImageRunnable, 0);
		}
	}

	public void updateTitleIfdifferent()
	{
		for (EvercamCamera camera : AppData.evercamCameraList)
		{
			if (evercamCamera.getCameraId().equals(camera.getCameraId()))
			{
				titleText.setText(camera.getName());
			}
		}
	}

	private Drawable getThumbnailFromCamera(EvercamCamera evercamCamera)
	{
		try
		{
			if (evercamCamera.camera != null && evercamCamera.camera.isOnline())
			{
				byte[] snapshotByte = evercamCamera.camera.getThumbnailData();

				Bitmap bitmap = BitmapFactory.decodeByteArray(snapshotByte, 0, snapshotByte.length);

				Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);

				return drawable;
			}
		}
		catch (EvercamException e)
		{
			Log.e(TAG, e.toString());
		}
		return null;
	}

	// Stop the image loading process. May be need to end current activity
	public boolean stopAllActivity()
	{
		end = true;
		if (liveImageTask != null && liveImageTask.getStatus() != AsyncTask.Status.FINISHED) liveImageTask
				.cancel(true);

		return true;
	}

	// Image loaded form cache and now set the controls appearance and text
	// accordingly
	private void setlayoutForImageLoadedFromCache()
	{
		if (cameraRelativeLayout.indexOfChild(loadingAnimation) >= 0)
		{
			loadingAnimation.setVisibility(View.GONE);
			cameraRelativeLayout.removeView(loadingAnimation);
		}
	}

	// Image loaded form camera and now set the controls appearance and text
	// accordingly
	private void setlayoutForLiveImageReceived()
	{
		evercamCamera.setStatus(CameraStatus.ACTIVE);
		imageMessage.setVisibility(View.GONE);

		if (cameraRelativeLayout.indexOfChild(loadingAnimation) >= 0)
		{
			loadingAnimation.setVisibility(View.GONE);
			cameraRelativeLayout.removeView(loadingAnimation);
			if (cameraRelativeLayout.indexOfChild(imageMessage) >= 0) cameraRelativeLayout
					.removeView(imageMessage);
		}

		handler.removeCallbacks(LoadImageRunnable);
	}

	private void showThumbnail()
	{
		Drawable thumbnail = getThumbnailFromCamera(evercamCamera);
		if (thumbnail != null)
		{
			loadingAnimation.setVisibility(View.GONE);
			cameraRelativeLayout.removeView(loadingAnimation);
			cameraRelativeLayout.setBackgroundDrawable(thumbnail);
		}
	}

	// Image loaded from Evercam and now set the controls appearance and
	// text accordingly
	private void setlayoutForLatestImageReceived()
	{
		if (cameraRelativeLayout.indexOfChild(loadingAnimation) >= 0)
		{
			loadingAnimation.setVisibility(View.GONE);
			cameraRelativeLayout.removeView(loadingAnimation);
		}

		imageMessage.setVisibility(View.VISIBLE);
		if ((evercamCamera.getStatus() + "").contains(CameraStatus.ACTIVE))
		{
			imageMessage.setText("");
		}
		else
		{
			imageMessage.setText(evercamCamera.getStatus() + "");
			greyImageShown();
		}

		imageMessage.setTextColor(Color.RED);

		handler.removeCallbacks(LoadImageRunnable);
	}

	// Image not received form cache, Evercam nor camera side. Set the controls
	// appearance and text accordingly
	private void setlayoutForNoImageReceived()
	{
		if (cameraRelativeLayout.indexOfChild(loadingAnimation) >= 0)
		{
			loadingAnimation.setVisibility(View.GONE);
			cameraRelativeLayout.removeView(loadingAnimation);
		}

		if (isImageLoadedFromCache)
		{

			imageMessage.setVisibility(View.VISIBLE);
			imageMessage.setTextColor(Color.RED);
		}
		else
		{
			for (int i = 0; i < cameraRelativeLayout.getChildCount(); i++)
			{
				cameraRelativeLayout.getChildAt(i).setVisibility(View.GONE);
			}
			cameraRelativeLayout.setBackgroundResource(R.drawable.cam_unavailable);
			imageMessage.setVisibility(View.VISIBLE);
			greyImageShown();
		}

		if ((evercamCamera.getStatus() + "").contains(CameraStatus.ACTIVE))
		{
			imageMessage.setText(R.string.msg_unable_to_connect);
		}
		else
		{
			imageMessage.setText(evercamCamera.getStatus() + "");
			greyImageShown();
		}

		imageMessage.setTextColor(Color.RED);

		// animation must have been stopped when image loaded from cache
		handler.removeCallbacks(LoadImageRunnable);
	}

	public Runnable LoadImageRunnable = new Runnable(){
		@Override
		public void run()
		{
			try
			{
				if (end) return;

				if (CamerasActivity.stopImageLoading)
				{
					return;
				}

				if (evercamCamera.loadingStatus == ImageLoadingStatus.not_started)
				{
					liveImageTask = new DownloadLiveImageTask();
					liveImageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
				else if (evercamCamera.loadingStatus == ImageLoadingStatus.live_received)
				{
					setlayoutForLiveImageReceived();
					return;
				}
				else if (evercamCamera.loadingStatus == ImageLoadingStatus.live_not_received)
				{
					latestTask = new DownloadLatestTask(evercamCamera.getCameraId(),
							CameraLayout.this);
					latestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					setlayoutForNoImageReceived();
				}
				else if (evercamCamera.loadingStatus == ImageLoadingStatus.camba_image_received)
				{
					setlayoutForLatestImageReceived();
					return;
				}
				else if (evercamCamera.loadingStatus == ImageLoadingStatus.camba_not_received)
				{
					setlayoutForNoImageReceived();
				}
			}
			catch (OutOfMemoryError e)
			{
				Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));

				handler.postDelayed(LoadImageRunnable, 5000);

				return;
			}
			catch (Exception e)
			{
				Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
				if (!end)
				{
					handler.postDelayed(LoadImageRunnable, 5000);
				}
				if (Constants.isAppTrackingEnabled)
				{
					BugSenseHandler.sendException(e);
				}
			}
		}
	};

	private void greyImageShown()
	{
		try
		{
			this.setBackgroundColor(Color.GRAY);
			cameraRelativeLayout.getBackground().setAlpha(70);
		}
		catch (Exception e)
		{
			Log.e(TAG, "Show grey image:" + e.toString());
		}
	}

	private class DownloadLiveImageTask extends AsyncTask<Void, Drawable, Drawable>
	{

		public boolean isTaskended = false;

		// Save image to external cache folder and return file path.
		@Override
		protected Drawable doInBackground(Void... params)
		{
			if (CamerasActivity.stopImageLoading)
			{
				this.cancel(true);
			}
			try
			{
				ArrayList<Cookie> cookies = new ArrayList<Cookie>();
				Drawable drawable = null;
				String externalJpgUrl = evercamCamera.getExternalSnapshotUrl();
				String internalJpgUrl = null;
				if (evercamCamera.hasCredentials())
				{
					if (!evercamCamera.getExternalHost().isEmpty())
					{

						drawable = Commons.getDrawablefromUrlAuthenticated(externalJpgUrl,
								evercamCamera.getUsername(), evercamCamera.getPassword(), cookies,
								3000);

						if (drawable == null)
						{
							if (!evercamCamera.getInternalHost().isEmpty())
							{
								internalJpgUrl = evercamCamera.getInternalSnapshotUrl();
								drawable = Commons.getDrawablefromUrlAuthenticated(internalJpgUrl,
										evercamCamera.getUsername(), evercamCamera.getPassword(),
										cookies, 3000);
							}
						}
					}
					else
					{
						drawable = Commons.getDrawablefromUrlAuthenticated(internalJpgUrl,
								evercamCamera.getUsername(), evercamCamera.getPassword(), cookies,
								3000);
					}
				}
				else
				{
					if (evercamCamera.camera != null)
					{
						InputStream stream = evercamCamera.camera.getSnapshotFromEvercam();
						drawable = Drawable.createFromStream(stream, "src");
					}
					else
					{
						Log.e(TAG, "EvercamCamera.camera is null");
					}
				}
				if (cookies.size() > 0)
				{
					evercamCamera.cookies = cookies;
				}

				return drawable;
			}
			catch (OutOfMemoryError e)
			{
				Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
				return null;
			}
			catch (Exception e)
			{
				Log.e(TAG, "Error request snapshot: " + e.getMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Drawable drawable)
		{
			if (drawable != null && !end && drawable.getIntrinsicWidth() > 0
					&& drawable.getIntrinsicHeight() > 0)
			{
				cameraRelativeLayout.setBackgroundDrawable(drawable);
				CameraLayout.this.evercamCamera.loadingStatus = ImageLoadingStatus.live_received;

				Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
				// Tests showing that use either Runnable and AsyncTask is not
				// making
				// any difference on speed of camera list loading
				// new SaveImageTask(context, bitmap,
				// evercamCamera.getCameraId())
				// .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				new Thread(new SaveImageRunnable(context, bitmap, evercamCamera.getCameraId()))
						.start();
			}

			synchronized (this)
			{
				isTaskended = true;

				if (liveImageTask.isTaskended
						&& CameraLayout.this.evercamCamera.loadingStatus != ImageLoadingStatus.live_received)
				{
					CameraLayout.this.evercamCamera.loadingStatus = ImageLoadingStatus.live_not_received;
				}
				if (liveImageTask.isTaskended)
				{
					handler.postDelayed(LoadImageRunnable, 0);
				}
			}
		}
	}
}