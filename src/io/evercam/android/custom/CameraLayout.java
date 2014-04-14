package io.evercam.android.custom;

import io.evercam.android.dto.*;
import io.evercam.android.utils.Commons;
import io.evercam.android.utils.Constants;
import io.evercam.android.utils.UIUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import org.apache.http.cookie.Cookie;
import com.bugsense.trace.BugSenseHandler;
import io.evercam.android.R;

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
import android.webkit.URLUtil;
import android.widget.*;

public class CameraLayout extends LinearLayout
{
	private static final String TAG = "evercamapp";

	private RelativeLayout cameraRelativeLayout; // layout for the image and controls showing on
							// screen
	private Context context;
	private EvercamCamera evercamCamera; 
	private DownloadLiveImageTask  liveImageTask; // task to download image from camera
	private DownloadLiveImageTask  liveImageTaskLocal; 
//	private DownloadLatestTask latestTask; // tsk to download image from camba
												// website
	private static boolean enableLogs = false; 

	private boolean end = false; // tells whether application has ended or not. If it is
							// true, all tasks must end and no further
							// processing should be done in any thread.
	private ProgressView loadingAnimation = null;
	private TextView imageMessage = null; // message being displayed on the camera. It
									// is status message.
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
			imageMessage.setText("Connecting...");
			imageMessage.setGravity(Gravity.CENTER);
			cameraRelativeLayout.addView(imageMessage);

			cameraRelativeLayout.setClickable(true);
//			cameraRelativeLayout.setOnClickListener(new View.OnClickListener(){
//				@Override
//				public void onClick(View v)
//				{
//					AlertDialog.Builder builder = UIUtils.GetAlertDialogBuilderNoTitle(CameraLayout.this.context);
//					final View layout = ((CamerasActivity) CameraLayout.this.context).getLayoutInflater().inflate(
//							R.layout.cameralayout_dialog_liverecordingview, null);
//
//					builder.setView(layout);
//
//					builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
//						@Override
//						public void onClick(DialogInterface dialog, int which)
//						{
//							dialog.cancel();
//						}
//					});
//
//					final AlertDialog dialog = builder.create();
//
//					Button btnLive = (Button) layout
//							.findViewById(R.id.cameralayout_dialog_btn_live);
//					btnLive.setEnabled(true);
//					btnLive.getCompoundDrawables()[0].setAlpha(255);
//
//					if (evercamCamera.getStatus().equalsIgnoreCase("Offline"))
//					{
//						btnLive.setText("Offline");
//
//						btnLive.setEnabled(false);
//						btnLive.getCompoundDrawables()[0].setAlpha(100);
//
//					}
//
//					btnLive.setOnClickListener(new OnClickListener(){
//						@Override
//						public void onClick(View v)
//						{
//							VideoActivity.startPlayingVIdeoForCamera(CameraLayout.this.context, evercamCamera.getCameraID());
//							dialog.cancel();
//						}
//					});
//
//					Button btnRecorded = (Button) layout
//							.findViewById(R.id.cameralayout_dialog_btn_recored);
//					btnRecorded.setOnClickListener(new OnClickListener(){
//						@Override
//						public void onClick(View v)
//						{
//							RVideoViewActivity.StartPlayingVIdeo(CameraLayout.this.context, evercamCamera, null);
//							dialog.cancel();
//						}
//					});
//
//					dialog.show();
//				}
//			});

			loadImage();
		}
		catch (OutOfMemoryError e)
		{
			if (enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
		}
		catch (Exception e)
		{
			if (enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
			UIUtils.GetAlertDialog(context, "Exception", e.toString()).show();
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);

		}
	}

	// This method will load image from cache and call the image loading thread
	// for further processing
	public boolean loadImageFromCache()
	{
		try
		{
			String cachePath = context.getCacheDir().getAbsolutePath() + "/" + evercamCamera.getCameraId()
					+ ".jpg";
			File cachefile = new File(cachePath);

			String extCachePath = null;
			File extfile = null;
			try
			{
				extCachePath = context.getExternalFilesDir(null) + "/" + evercamCamera.getCameraId() + ".jpg";
				extfile = new File(extCachePath);
			}
			catch (Exception e)
			{
			}

			if (cachefile.exists() || (extfile != null && extfile.exists()))
			{
				Drawable d1 = null;
				if (cachefile.exists()) d1 = Drawable.createFromPath(cachePath);
				else d1 = Drawable.createFromPath(extCachePath);

				if (d1 != null) // whether image was downloaded properly or not
				{
					cameraRelativeLayout.setVisibility(View.VISIBLE);
					cameraRelativeLayout.setBackgroundDrawable(d1);

					isImageLodedFromCache = true;
					if (enableLogs) Log.i(TAG,
							"laodimagefromcache image loaded for camera [" + evercamCamera.getCameraId()
									+ ":" + evercamCamera.getName() + "].");
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
					if (enableLogs) Log.e(
							TAG,
							"laodimagefromcache drawable d1 is null for camera ["
									+ evercamCamera.getCameraId() + ":" + evercamCamera.getName() + "]." + ds);
				}
			}

			// if imange status was loaded on previously and now image is not in
			// cache, then we need to restart this
			if (!isImageLodedFromCache
					&& (this.evercamCamera.loadingStatus == ImageLoadingStatus.live_received || this.evercamCamera.loadingStatus == ImageLoadingStatus.camba_image_received))
			this.evercamCamera.loadingStatus = ImageLoadingStatus.not_started;
		}
		catch (OutOfMemoryError e)
		{
			isImageLodedFromCache = false;
			if (enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
			return false;
		}
		catch (Exception e)
		{
			if (enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
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
				if (enableLogs) Log.i(TAG,
						"handler posted after cache for camera [" + evercamCamera.toString() + "]");
			}
		}
		catch (Exception e)
		{
			if (enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
		}

	}

	// Stop the image laoding process. My be need to end current activity
	public boolean stopAllActivity()
	{
		try
		{
			if (enableLogs) Log.i(TAG, "StopAllActivity called. end = true. [" + evercamCamera.toString()
					+ "]");
			end = true;
			if (liveImageTask != null && liveImageTask.getStatus() != AsyncTask.Status.FINISHED) liveImageTask
					.cancel(true);
//			if (latestTask != null && latestTask.getStatus() != AsyncTask.Status.FINISHED) latestTask
//					.cancel(true);
		}
		catch (Exception e)
		{
			if (enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
		}
		return true;
	}

	// Image loaded form cache and now set the controls appearence and text
	// accordingly
	void setlayoutForImageLoadedFromCache()
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
		imageMessage.setVisibility(View.GONE);

		if (cameraRelativeLayout.indexOfChild(loadingAnimation) >= 0)
		{
			loadingAnimation.setVisibility(View.GONE);
			cameraRelativeLayout.removeView(loadingAnimation);
			if (cameraRelativeLayout.indexOfChild(imageMessage) >= 0) cameraRelativeLayout.removeView(imageMessage);
		}

		handler.removeCallbacks(LoadImageRunnable);
	}

	// Image loaded form camba website and now set the controls appearence and
	// text accordingly
	private void setlayoutForCambaImageReceived()
	{
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

		if ((evercamCamera.getStatus() + "").contains("Active")) imageMessage.setText("Unable to Connect.");
		else imageMessage.setText(evercamCamera.getStatus() + "");

		imageMessage.setTextColor(Color.RED);

		// anination must have been stopped when image loaded from cache
		handler.removeCallbacks(LoadImageRunnable);
	}

	private Runnable LoadImageRunnable = new Runnable(){
		@Override
		public void run()
		{
			try
			{
				if (end) return;

				if (enableLogs) Log.i(TAG, "Timer event called for camera + " + evercamCamera.toString()
						+ ":: Loading Status:" + evercamCamera.loadingStatus);

				if (evercamCamera.loadingStatus == ImageLoadingStatus.not_started)
				{
//					liveImageTaskLocal = new DownloadLiveImageTask ();
//					liveImageTaskLocal.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
//							new String[] { evercamCamera.getLowResSnapshotLocalURLforCameraPlaying() });

					liveImageTask = new DownloadLiveImageTask ();
//					String ImageUrl = ((evercamCamera.getLowResolutionSnapshotUrl() != null && URLUtil
//							.isValidUrl(evercamCamera.getLowResolutionSnapshotUrl())) ? evercamCamera
//							.getLowResolutionSnapshotUrl() : evercamCamera.getCameraImageUrl());
					String imageUrl = evercamCamera.getExternalSnapshotUrl();
					liveImageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
							new String[] { imageUrl });
				}
				else if (evercamCamera.loadingStatus == ImageLoadingStatus.live_received)
				{
					setlayoutForLiveImageReceived();
					return;
				}
				else if (evercamCamera.loadingStatus == ImageLoadingStatus.live_not_received)
				{
//					latestTask = new DownloadLatestTask();
//					latestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
//							new Integer[] { evercamCamera.getCameraId() });
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
				if (enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
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
				if (enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
				if (!end) handler.postDelayed(LoadImageRunnable, 5000);
				if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
			}
		}
	};

	private void greyImageShown()
	{
		this.setBackgroundColor(Color.GRAY);
		cameraRelativeLayout.getBackground().setAlpha(50);

		// LinearLayout grey = new LinearLayout(context);
		// grey.setLayoutParams(new
		// LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		// grey.setBackgroundColor(Color.parseColor("#808080"));
		// grey.setAlpha((float) 0.8);
		// cameraRelativeLayout.addView(grey);
	}

	private class DownloadLiveImageTask extends AsyncTask<String, Void, String>
	{
		public boolean isTaskended = false;

		@Override
		protected String doInBackground(String... urls)
		{
			for (String url1 : urls)
			{
				try
				{
					ArrayList<Cookie> cookies = new ArrayList<Cookie>();
					Drawable d = Commons.getDrawablefromUrlAuthenticated1(url1,
							evercamCamera.getUsername(), evercamCamera.getPassword(), cookies, 15000);
					if (cookies.size() > 0) evercamCamera.cookies = cookies;

					try
					// save image to external cache folder for cache
					{
						String extCachePath = context.getExternalFilesDir(null) + "/"
								+ evercamCamera.getCameraId() + ".jpg";
						File extfile = new File(extCachePath);
						if (d != null)
						{
							Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
							if (extfile.exists()) extfile.delete();
							extfile.createNewFile();
							FileOutputStream fos = new FileOutputStream(extfile);
							bitmap.compress(CompressFormat.PNG, 0, fos);
							fos.close();
						}
					}
					catch (Exception e)
					{
					}

					String pathString = context.getCacheDir() + "/" + evercamCamera.getCameraId() + ".jpg";
					File file = new File(pathString);

					if (d != null)
					{
						Bitmap bitmap = ((BitmapDrawable) d).getBitmap();

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
						if (enableLogs) Log.e(
								TAG,
								"File Error" + "::"
										+ "Unable to get the full file for the camera ["
										+ evercamCamera.getCameraId() + ":" + evercamCamera.getName()
										+ "]. File Deleted. File was empty.");
						return null;
					}
					else
					{
						if (enableLogs) Log.e(
								TAG,
								"File Error" + "::"
										+ "Unable to get the full file for the camera ["
										+ evercamCamera.getCameraId() + ":" + evercamCamera.getName() + "]");
						return null;
					}
				}
				catch (OutOfMemoryError e)
				{
					if (enableLogs) Log.e(TAG,
							e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
					return null;
				}
				catch (Exception e)
				{
					if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
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
					Drawable d = Drawable.createFromPath(result);
					if (d != null && d.getIntrinsicWidth() > 0 && d.getIntrinsicHeight() > 0)
					{
						cameraRelativeLayout.setBackgroundDrawable(d);
						CameraLayout.this.evercamCamera.loadingStatus = ImageLoadingStatus.live_received;
					}
					else
					{
						if (enableLogs) Log.e(TAG, "File Load Error" + "::"
								+ "Unable to load image for the camera [" + evercamCamera.getCameraId() + ":"
								+ evercamCamera.getName() + "] from path [" + result + "]");
					}
				}
			}
			catch (OutOfMemoryError e)
			{
				if (enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
			}
			catch (Exception e)
			{
				if (enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
				if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
			}
			try
			{
				synchronized (this)
				{
					isTaskended = true;
					if (liveImageTask.isTaskended
							&& liveImageTaskLocal.isTaskended
							&& CameraLayout.this.evercamCamera.loadingStatus != ImageLoadingStatus.live_received) CameraLayout.this.evercamCamera.loadingStatus = ImageLoadingStatus.live_not_received;
					if (liveImageTask.isTaskended && liveImageTaskLocal.isTaskended) handler
							.postDelayed(LoadImageRunnable, 0);
				}
			}
			catch (Exception e)
			{
				if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
			}
		}
	}

	// Connect to the camba site and get the last recorded image. If found, put
	// in the cache as well for future ease
//	private class DownloadLatestTask extends AsyncTask<Integer, Void, String>
//	{
//		@Override
//		protected String doInBackground(Integer... cameraIDs)
//		{
//			for (int camid : cameraIDs)
//			{
//				try
//				{
//					String pathString = context.getCacheDir() + "/" + evercamCamera.getCameraId() + ".jpg";
//					CambaApiManager.getCameraLatestImageAndSave(camid, pathString, 15000);
//
//					File file = new File(pathString);
//					if (file.exists() && file.length() > 0)
//					{
//						try
//						// save image to external cache folder for cache
//						{
//							String extCachePath = context.getExternalFilesDir(null) + "/"
//									+ evercamCamera.getCameraId() + ".jpg";
//							File extfile = new File(extCachePath);
//							if (!extfile.exists()) // write only when file does
//													// not exist
//							{
//								Commons.copyFile(file, extfile);
//							}
//						}
//						catch (Exception e)
//						{
//						}
//
//						return pathString;
//					}
//					else
//					{
//						if (enableLogs) Log.e(
//								TAG,
//								"File Error" + "::"
//										+ "Unable to get the full file for the camera ["
//										+ evercamCamera.getCameraId() + ":" + evercamCamera.getName() + "]");
//						return null;
//					}
//
//				}
//				catch (OutOfMemoryError e)
//				{
//					if (enableLogs) Log.e(TAG,
//							e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
//					return null;
//				}
//				catch (Exception e)
//				{
//					if (enableLogs) Log.e(TAG, e.toString() + "\n" + "[" + evercamCamera.toString() + "]"
//							+ "::" + Log.getStackTraceString(e));
//					if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//				}
//			}
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(String result)
//		{ // DownloadImageLatestCamba
//			try
//			{
//				if (result != null && !end)
//				{
//					Drawable d = Drawable.createFromPath(result);
//					if (d != null && d.getIntrinsicWidth() > 0 && d.getIntrinsicHeight() > 0)
//					{
//						cameraRelativeLayout.setVisibility(View.VISIBLE);
//						cameraRelativeLayout.setBackgroundDrawable(d);
//						CameraLayout.this.evercamCamera.loadingStatus = ImageLoadingStatus.camba_image_received;
//					}
//					else
//					{
//						if (enableLogs) Log.e(TAG, "File Load Error" + "::"
//								+ "Unable to load image for the camera [" + evercamCamera.getCameraId() + ":"
//								+ evercamCamera.getName() + "] from path [" + result + "]");
//					}
//				}
//			}
//			catch (OutOfMemoryError e)
//			{
//				if (enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
//			}
//			catch (Exception e)
//			{
//				if (enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
//				if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//
//			}
//			try
//			{
//				if (CameraLayout.this.evercamCamera.loadingStatus != ImageLoadingStatus.camba_image_received) CameraLayout.this.evercamCamera.loadingStatus = ImageLoadingStatus.camba_not_received;
//			}
//			catch (Exception e)
//			{
//			}
//			try
//			{
//				if (!end)
//				{
//					handler.postDelayed(LoadImageRunnable, 0);
//				}
//			}
//			catch (Exception e)
//			{
//			}
//			try
//			{
//				latestTask = null;
//			}
//			catch (Exception e)
//			{
//			}
//		}
//	}

}