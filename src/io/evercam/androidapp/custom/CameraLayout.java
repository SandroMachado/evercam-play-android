package io.evercam.androidapp.custom;

import io.evercam.Camera;
import io.evercam.androidapp.dto.*;
import io.evercam.androidapp.tasks.DownloadLatestTask;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.UIUtils;
import io.evercam.androidapp.video.VideoActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import org.apache.http.cookie.Cookie;
import com.bugsense.trace.BugSenseHandler;
import io.evercam.androidapp.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
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
	private static final String TAG = "evercamapp-CameraLayout";

	public RelativeLayout cameraRelativeLayout;
	public Context context;
	public EvercamCamera evercamCamera;
	private DownloadLiveImageTask liveImageTask;
	private DownloadLiveImageTask liveImageTaskLocal;
	// private DownloadLatestTask latestTask;

	private boolean end = false; // tells whether application has ended or not.
									// If it is
	// true, all tasks must end and no further
	// processing should be done in any thread.
	private ProgressView loadingAnimation = null;
	private TextView imageMessage = null;
	private boolean isImageLodedFromCache = false;

	// Handler for the handling the next request. It will call the image loading
	// thread so that it can proceed with next step.
	private final Handler handler = new Handler();

	public CameraLayout(Context context, EvercamCamera camera)
	{
		super(context);
		this.context = context;

		try
		{
			evercamCamera = camera;

			this.setOrientation(LinearLayout.VERTICAL);
			this.setGravity(Gravity.LEFT);

			this.setBackgroundColor(Color.WHITE);

			LinearLayout titleLayout = new LinearLayout(context);
			titleLayout.setLayoutParams(new LinearLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.FILL_PARENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
			titleLayout.setBackgroundColor(Color.parseColor("#E0E0E0"));
			titleLayout.setOrientation(LinearLayout.HORIZONTAL);
			titleLayout.setGravity(Gravity.CENTER_HORIZONTAL);

			// text view to show the camera name on the top grey band of cmaera
			TextView titleText = new TextView(context);
			titleText.setText(evercamCamera.getName());
			titleText.setLayoutParams(new LinearLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
			titleText.setTextColor(Color.parseColor("#372c24"));
			titleLayout.addView(titleText);

			this.addView(titleLayout);

			cameraRelativeLayout = new RelativeLayout(context);
			RelativeLayout.LayoutParams ivParams = new RelativeLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.FILL_PARENT,
					android.view.ViewGroup.LayoutParams.FILL_PARENT);
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
			cameraRelativeLayout.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v)
				{
					if (evercamCamera.getStatus().equalsIgnoreCase(CameraStatus.OFFLINE))
					{
						Toast toast = Toast.makeText(CameraLayout.this.context,
								R.string.msg_camera_offline, Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
					else
					{
						VideoActivity.startPlayingVideoForCamera(CameraLayout.this.context,
								evercamCamera.getCameraId());
					}
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
			UIUtils.getAlertDialog(context, "Exception", e.toString()).show();
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
			String cachePath = context.getCacheDir().getAbsolutePath() + "/"
					+ evercamCamera.getCameraId() + ".jpg";
			File cachefile = new File(cachePath);

			String extCachePath = null;
			File extfile = null;
			try
			{
				extCachePath = context.getExternalFilesDir(null) + "/"
						+ evercamCamera.getCameraId() + ".jpg";
				extfile = new File(extCachePath);
			}
			catch (Exception e)
			{
			}

			if (cachefile.exists() || (extfile != null && extfile.exists()))
			{
				Drawable drawable = null;
				if (cachefile.exists())
				{
					drawable = Drawable.createFromPath(cachePath);
				}
				else drawable = Drawable.createFromPath(extCachePath);

				if (drawable != null) // whether image was downloaded properly
										// or not
				{
					cameraRelativeLayout.setVisibility(View.VISIBLE);
					cameraRelativeLayout.setBackgroundDrawable(drawable);

					isImageLodedFromCache = true;
					setlayoutForImageLoadedFromCache();
					return true;
				}
				else
				{
					String ds = "path = [" + cachePath + "]";
					File file = new File(cachePath);
					if (file.exists())
					{
						ds += ". File Physically Exists with size [" + file.length() + "]";
						file.delete();
					}
				}
			}

			// if imange status was loaded on previously and now image is not in
			// cache, then we need to restart this
			if (!isImageLodedFromCache
					&& (this.evercamCamera.loadingStatus == ImageLoadingStatus.live_received || this.evercamCamera.loadingStatus == ImageLoadingStatus.camba_image_received)) this.evercamCamera.loadingStatus = ImageLoadingStatus.not_started;
		}
		catch (OutOfMemoryError e)
		{
			isImageLodedFromCache = false;
			Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
			return false;
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
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
		try
		{
			boolean isLoaded = loadImageFromCache();
			isImageLodedFromCache = isLoaded;

			if (!end)
			{
				handler.postDelayed(LoadImageRunnable, 0);
			}
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

	// Stop the image laoding process. My be need to end current activity
	public boolean stopAllActivity()
	{
		try
		{
			end = true;
			if (liveImageTask != null && liveImageTask.getStatus() != AsyncTask.Status.FINISHED) liveImageTask
					.cancel(true);
			// if (latestTask != null && latestTask.getStatus() !=
			// AsyncTask.Status.FINISHED) latestTask
			// .cancel(true);
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
			if (Constants.isAppTrackingEnabled)
			{
				BugSenseHandler.sendException(e);

			}
		}
		return true;
	}

	// Image loaded form cache and now set the controls appearence and text
	// accordingly
	private void setlayoutForImageLoadedFromCache()
	{
		if (cameraRelativeLayout.indexOfChild(loadingAnimation) >= 0)
		{
			loadingAnimation.setVisibility(View.GONE);
			cameraRelativeLayout.removeView(loadingAnimation);
		}
	}

	// Image loaded form camera and now set the controls appearence and text
	// accordingly
	private void setlayoutForLiveImageReceived()
	{
		Log.d(TAG, "live image received: " + evercamCamera.getCameraId() + "--camera status: " + evercamCamera.getStatus());
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

	// Image loaded form camba website and now set the controls appearence and
	// text accordingly
	private void setlayoutForCambaImageReceived()
	{
		Log.d(TAG, "camba image received: " + evercamCamera.getCameraId() + "--camera status: " + evercamCamera.getStatus());
		if (cameraRelativeLayout.indexOfChild(loadingAnimation) >= 0)
		{
			loadingAnimation.setVisibility(View.GONE);
			cameraRelativeLayout.removeView(loadingAnimation);
		}

		imageMessage.setVisibility(View.VISIBLE);
		if ((evercamCamera.getStatus() + "").contains("Active"))
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

	// Image not received form cahce, camba nor camera side. Set the controls
	// appearence and text accordingly
	private void setlayoutForNoImageReceived()
	{
		Log.d(TAG, "no image received: " + evercamCamera.getCameraId() + "--camera status: " + evercamCamera.getStatus());
		if (cameraRelativeLayout.indexOfChild(loadingAnimation) >= 0)
		{
			loadingAnimation.setVisibility(View.GONE);
			cameraRelativeLayout.removeView(loadingAnimation);
		}

		if (isImageLodedFromCache)
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

		if ((evercamCamera.getStatus() + "").contains("Active")) imageMessage
				.setText("Unable to Connect.");
		else imageMessage.setText(evercamCamera.getStatus() + "");

		imageMessage.setTextColor(Color.RED);

		// animation must have been stopped when image loaded from cache
		handler.removeCallbacks(LoadImageRunnable);
	}

	private Runnable LoadImageRunnable = new Runnable(){
		@Override
		public void run()
		{
			try
			{
				if (end) return;

				if (evercamCamera.loadingStatus == ImageLoadingStatus.not_started)
				{
					String internalJpgUrl = evercamCamera.getInternalSnapshotUrl();
					liveImageTaskLocal = new DownloadLiveImageTask();
					liveImageTaskLocal.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
							new String[] { internalJpgUrl });

					liveImageTask = new DownloadLiveImageTask();

					String externalJpgUrl = evercamCamera.getExternalSnapshotUrl();

					liveImageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
							new String[] { externalJpgUrl });
				}
				else if (evercamCamera.loadingStatus == ImageLoadingStatus.live_received)
				{
					setlayoutForLiveImageReceived();
					return;
				}
				else if (evercamCamera.loadingStatus == ImageLoadingStatus.live_not_received)
				{
					new DownloadLatestTask(evercamCamera.getCameraId(), CameraLayout.this)
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					setlayoutForNoImageReceived();
				}
				else if (evercamCamera.loadingStatus == ImageLoadingStatus.camba_image_received)
				{
					setlayoutForCambaImageReceived();
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
				try
				{
					handler.postDelayed(LoadImageRunnable, 5000);
				}
				catch (Exception e1)
				{
				}
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
		this.setBackgroundColor(Color.GRAY);
		cameraRelativeLayout.getBackground().setAlpha(50);
	}

	private class DownloadLiveImageTask extends AsyncTask<String, Void, String>
	{
		public boolean isTaskended = false;

		// Save image to external cache folder and return file path.
		@Override
		protected String doInBackground(String... urls)
		{
			for (String url : urls)
			{
				try
				{
					ArrayList<Cookie> cookies = new ArrayList<Cookie>();
					Drawable drawable = null;
					if (evercamCamera.hasCredential())
					{
						Log.d(TAG, "camera has credentials" + evercamCamera.getId() +"try url:" + url);
						drawable = Commons.getDrawablefromUrlAuthenticated1(url,
								evercamCamera.getUsername(), evercamCamera.getPassword(), cookies,
								15000);
					}
					else
					{
						Log.d(TAG, "camera has no credentials" + evercamCamera.getCameraId());
						Camera camera = evercamCamera.camera;
						if (camera != null)
						{
							InputStream stream = camera.getSnapshotFromEvercam();
							drawable = Drawable.createFromStream(stream, "src");
						}
					}
					if (cookies.size() > 0)
					{
						evercamCamera.cookies = cookies;
					}

					try
					{
						String extCachePath = context.getExternalFilesDir(null) + "/"
								+ evercamCamera.getCameraId() + ".jpg";
						File extfile = new File(extCachePath);
						if (drawable != null)
						{
							Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
							if (extfile.exists())
							{
								extfile.delete();
							}
							extfile.createNewFile();
							FileOutputStream fos = new FileOutputStream(extfile);
							bitmap.compress(CompressFormat.PNG, 0, fos);
							fos.close();
						}
					}
					catch (Exception e)
					{
						Log.e(TAG, Log.getStackTraceString(e));
					}

					String pathString = context.getCacheDir() + "/" + evercamCamera.getCameraId()
							+ ".jpg";
					File file = new File(pathString);

					if (drawable != null)
					{
						Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

						if (file.exists())
						{
							file.delete();
						}
						file.createNewFile();
						FileOutputStream fos = new FileOutputStream(file);

						bitmap.compress(CompressFormat.PNG, 0, fos);

						fos.close();
					}

					if (file.exists() && file.length() > 0)
					{
						return pathString;
					}
					else if (file.exists())
					{
						file.delete();
						Log.e(TAG,
								"File Error" + "::"
										+ "Unable to get the full file for the camera ["
										+ evercamCamera.getCameraId() + ":"
										+ evercamCamera.getName()
										+ "]. File Deleted. File was empty.");
						return null;
					}
					else
					{
						Log.e(TAG,
								"File Error" + "::"
										+ "Unable to get the full file for the camera ["
										+ evercamCamera.getCameraId() + ":"
										+ evercamCamera.getName() + "]");
						return null;
					}
				}
				catch (OutOfMemoryError e)
				{
					Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
					return null;
				}
				catch (Exception e)
				{
					Log.e(TAG, e.toString());
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result)
		{
			try
			{
				if (result != null && !end)
				{
					Drawable drawable = Drawable.createFromPath(result);
					if (drawable != null && drawable.getIntrinsicWidth() > 0
							&& drawable.getIntrinsicHeight() > 0)
					{
						cameraRelativeLayout.setBackgroundDrawable(drawable);
						CameraLayout.this.evercamCamera.loadingStatus = ImageLoadingStatus.live_received;
					}
				}
			}
			catch (OutOfMemoryError e)
			{
				Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
			}
			catch (Exception e)
			{
				Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
				if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
			}
			try
			{
				synchronized (this)
				{
					isTaskended = true;

					if (liveImageTask.isTaskended
							&& liveImageTaskLocal.isTaskended
							&& CameraLayout.this.evercamCamera.loadingStatus != ImageLoadingStatus.live_received)
					{
						CameraLayout.this.evercamCamera.loadingStatus = ImageLoadingStatus.live_not_received;
					}
					if (liveImageTask.isTaskended && liveImageTaskLocal.isTaskended)
					{
						handler.postDelayed(LoadImageRunnable, 0);
					}
				}
			}
			catch (Exception e)
			{
				if (Constants.isAppTrackingEnabled)
				{
					BugSenseHandler.sendException(e);
				}
			}
		}
	}
}