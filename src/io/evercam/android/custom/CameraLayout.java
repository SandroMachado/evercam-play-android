package io.evercam.android.custom;

import io.evercam.android.CamerasActivity;
import io.evercam.android.dto.*;
import io.evercam.android.rvideo.RVideoViewActivity;
import io.evercam.android.utils.CambaApiManager;
import io.evercam.android.utils.Commons;
import io.evercam.android.utils.Constants;
import io.evercam.android.utils.UIUtils;
import io.evercam.android.video.VideoActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import org.apache.http.cookie.Cookie;
import com.bugsense.trace.BugSenseHandler;
import io.evercam.android.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
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

	private static final String TAG = "CameraLayout";

	RelativeLayout camview; // layout for the image and controls showing on
							// screen
	Context context; // context for the activity
	Camera cam; // camera object for this camera. This will have the api values
				// for this specific camera
	DownloadImageLiveNew imageLiveTask; // task to download image from camera
	DownloadImageLiveNew imageLiveLocalNetworkTask; // task to download image
													// from camera
	DownloadImageLatestCamba imageCambaTask; // tsk to download image from camba
												// website
	static boolean enableLogs = false; // tells whether logging is enabled or
										// not for this activity

	boolean end = false; // tells whether application has ended or not. If it is
							// true, all tasks must end and no further
							// processing should be done in any thread.
	ProgressView loadingAnimation = null; // View that shows the animated gif
											// for progress spinner
	TextView imageMessage = null; // message being displayed on the camera. It
									// is status message. What doing now and
									// what camera stataus

	boolean isImageLodedFromCache = false;

	// Status for current request being sent for image

	// Handler for the handling the next request. It will call the image loading
	// thread so that it can proceed with next step.
	final Handler handler = new Handler();

	// This method will load image from cache and call the image loading thread
	// for further processing
	public boolean LoadImageFromCache()
	{
		try
		{
			String cachePath = context.getCacheDir().getAbsolutePath() + "/" + cam.getCameraID()
					+ ".jpg";
			File cachefile = new File(cachePath);

			String extCachePath = null;
			File extfile = null;
			try
			{
				extCachePath = context.getExternalFilesDir(null) + "/" + cam.getCameraID() + ".jpg";
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
					camview.setVisibility(View.VISIBLE);
					camview.setBackgroundDrawable(d1);

					isImageLodedFromCache = true;
					if (enableLogs) Log.i(TAG,
							"laodimagefromcache image loaded for camera [" + cam.getCameraID()
									+ ":" + cam.getName() + "].");
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
									+ cam.getCameraID() + ":" + cam.getName() + "]." + ds);
				}
			}

			// if imange status was loaded on previously and now image is not in
			// cache, then we need to restart this
			if (!isImageLodedFromCache
					&& (this.cam.loadingStatus == ImageLoadingStatus.live_received || this.cam.loadingStatus == ImageLoadingStatus.camba_image_received)) // status
																																							// will
																																							// only
																																							// be
																																							// live
																																							// if
																																							// it
																																							// was
																																							// previously
																																							// found
																																							// from
																																							// live
																																							// or
																																							// camba
			this.cam.loadingStatus = ImageLoadingStatus.not_started;
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

	// This method will load the iamge from cache and later on call the image
	// loading thread to further load from camera or camba website
	public void LoadImage()
	{
		try
		{
			boolean isLoaded = LoadImageFromCache();
			isImageLodedFromCache = isLoaded;

			if (!end)
			{
				handler.postDelayed(LoadImageRunnable, 0);
				if (enableLogs) Log.i(TAG,
						"handler posted after cache for camera [" + cam.toString() + "]");
			}
		}
		catch (Exception e)
		{
			if (enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
		}

	}

	// Stop the image laoding process. My be need to end current activity
	public boolean StopAllActivity()
	{
		try
		{
			if (enableLogs) Log.i(TAG, "StopAllActivity called. end = true. [" + cam.toString()
					+ "]");
			end = true;
			if (imageLiveTask != null && imageLiveTask.getStatus() != AsyncTask.Status.FINISHED) imageLiveTask
					.cancel(true);
			if (imageCambaTask != null && imageCambaTask.getStatus() != AsyncTask.Status.FINISHED) imageCambaTask
					.cancel(true);
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
		if (camview.indexOfChild(loadingAnimation) >= 0)
		{
			loadingAnimation.setVisibility(View.GONE);
			camview.removeView(loadingAnimation);
		}

	}

	// Image loaded form camera and now set the controls appearence and text
	// accordingly
	void setlayoutForLiveImageReceived()
	{
		imageMessage.setVisibility(View.GONE);

		if (camview.indexOfChild(loadingAnimation) >= 0)
		{
			loadingAnimation.setVisibility(View.GONE);
			camview.removeView(loadingAnimation);
			if (camview.indexOfChild(imageMessage) >= 0) camview.removeView(imageMessage);
		}

		handler.removeCallbacks(LoadImageRunnable);
	}

	// Image loaded form camba website and now set the controls appearence and
	// text accordingly
	void setlayoutForCambaImageReceived()
	{
		if (camview.indexOfChild(loadingAnimation) >= 0)
		{
			loadingAnimation.setVisibility(View.GONE);
			camview.removeView(loadingAnimation);
		}

		imageMessage.setVisibility(View.VISIBLE);
		if ((cam.getStatus() + "").contains("Active"))
		{
			imageMessage.setText("");
		}
		else
		{
			imageMessage.setText(cam.getStatus() + "");
			greyImageShown();
		}

		imageMessage.setTextColor(Color.RED);

		handler.removeCallbacks(LoadImageRunnable);
	}

	// Image not received form cahce, camba nor camera side. Set the controls
	// appearence and text accordingly
	void setlayoutForNoImageReceived()
	{
		if (camview.indexOfChild(loadingAnimation) >= 0)
		{
			loadingAnimation.setVisibility(View.GONE);
			camview.removeView(loadingAnimation);
		}

		if (isImageLodedFromCache)
		{

			imageMessage.setVisibility(View.VISIBLE);
			imageMessage.setTextColor(Color.RED);
		}
		else
		{

			for (int i = 0; i < camview.getChildCount(); i++)
			{
				camview.getChildAt(i).setVisibility(View.GONE);
			}
			camview.setBackgroundResource(R.drawable.cam_unavailable);
			imageMessage.setVisibility(View.VISIBLE);
			greyImageShown();

		}

		if ((cam.getStatus() + "").contains("Active")) imageMessage.setText("Unable to Connect.");
		else imageMessage.setText(cam.getStatus() + "");

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

				if (enableLogs) Log.i(TAG, "Timer event called for camera + " + cam.toString()
						+ ":: Loading Status:" + cam.loadingStatus);

				if (cam.loadingStatus == ImageLoadingStatus.not_started)
				{
					imageLiveLocalNetworkTask = new DownloadImageLiveNew();
					imageLiveLocalNetworkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
							new String[] { cam.getLowResSnapshotLocalURLforCameraPlaying() });

					imageLiveTask = new DownloadImageLiveNew();
					String ImageUrl = ((cam.getLowResolutionSnapshotUrl() != null && URLUtil
							.isValidUrl(cam.getLowResolutionSnapshotUrl())) ? cam
							.getLowResolutionSnapshotUrl() : cam.getCameraImageUrl());
					imageLiveTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
							new String[] { ImageUrl });
					if (enableLogs) Log.i(TAG, "live task started:" + ImageUrl);
				}
				else if (cam.loadingStatus == ImageLoadingStatus.live_received)
				{
					setlayoutForLiveImageReceived();
					return;
				}
				else if (cam.loadingStatus == ImageLoadingStatus.live_not_received)
				{
					imageCambaTask = new DownloadImageLatestCamba();
					imageCambaTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
							new Integer[] { cam.getCameraID() });
				}
				else if (cam.loadingStatus == ImageLoadingStatus.camba_image_received)
				{
					setlayoutForCambaImageReceived();
					return;
				}
				else if (cam.loadingStatus == ImageLoadingStatus.camba_not_received)
				{
					setlayoutForNoImageReceived();
				}

				if (enableLogs) Log.i(TAG,
						"Timer event executed successfully for camera + " + cam.getCameraID());

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

	public CameraLayout(Context cont, Camera camera)
	{// ,String imageReceivedFromValue){
		super(cont);
		context = cont;

		try
		{

			cam = camera;
			if (enableLogs) Log.i(TAG, "create activity called for camera [" + cam.getCameraID()
					+ ":" + cam.getName() + "]");

			this.setOrientation(LinearLayout.VERTICAL);
			this.setGravity(Gravity.LEFT);

			Resources res = getResources();

			this.setBackgroundColor(Color.WHITE);

			LinearLayout title = new LinearLayout(context);
			title.setLayoutParams(new LinearLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.FILL_PARENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
			title.setBackgroundColor(Color.parseColor("#E0E0E0"));
			title.setOrientation(LinearLayout.HORIZONTAL);
			title.setGravity(Gravity.CENTER_HORIZONTAL);

			// text view to show the camera name on the top grey band of cmaera
			TextView tv = new TextView(context);
			tv.setText(cam.getName());
			// tv.setTextSize(5);
			tv.setLayoutParams(new LinearLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT));// ,(float)
																		// 0.75));
			tv.setTextColor(Color.parseColor("#372c24"));
			title.addView(tv);

			this.addView(title);

			camview = new RelativeLayout(context);
			RelativeLayout.LayoutParams ivParams = new RelativeLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.FILL_PARENT,
					android.view.ViewGroup.LayoutParams.FILL_PARENT);
			camview.setLayoutParams(ivParams);

			this.addView(camview);

			// control to show progress spinner
			loadingAnimation = new ProgressView(context);
			RelativeLayout.LayoutParams ivProgressParams = new RelativeLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			ivProgressParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			ivProgressParams.addRule(RelativeLayout.CENTER_VERTICAL);
			loadingAnimation.setLayoutParams(ivProgressParams);
			// loadingAnimation.setLayoutParams(new
			// ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
			camview.addView(loadingAnimation);
			// loadingAnimation.setBackgroundColor(color.transparent);

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
			camview.addView(imageMessage);

			// On click of the camera to show the full screen video of camerra
			camview.setClickable(true);
			camview.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v)
				{

					AlertDialog.Builder builder = UIUtils.GetAlertDialogBuilderNoTitle(context);
					Log.e("sajjad12345", context.toString());
					final View layout = ((CamerasActivity) context).getLayoutInflater().inflate(
							R.layout.cameralayout_dialog_liverecordingview, null);

					// layout.setPadding(5, 5, 5, 5);
					builder.setView(layout);

					builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							// TODO Auto-generated method stub
							dialog.cancel();
						}
					});

					final AlertDialog dialog = builder.create();

					Button btnLive = (Button) layout
							.findViewById(R.id.cameralayout_dialog_btn_live);
					btnLive.setEnabled(true);
					btnLive.getCompoundDrawables()[0].setAlpha(255);

					if (cam.getStatus().equalsIgnoreCase("Offline"))
					{
						btnLive.setText("Offline");

						btnLive.setEnabled(false);
						btnLive.getCompoundDrawables()[0].setAlpha(100);

					}

					btnLive.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v)
						{
							// IVideoViewActivity.StartPlayingVIdeo(context,
							// cam); // sajjad
							VideoActivity.StartPlayingVIdeoForCamera(context, cam.getCameraID());
							dialog.cancel();
						}
					});

					Button btnRecorded = (Button) layout
							.findViewById(R.id.cameralayout_dialog_btn_recored);
					btnRecorded.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v)
						{
							// TODO Auto-generated method stub
							RVideoViewActivity.StartPlayingVIdeo(context, cam, null);
							dialog.cancel();
						}
					});

					dialog.show();
				}
			});

			LoadImage();
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

	private void greyImageShown()
	{
		this.setBackgroundColor(Color.GRAY);
		camview.getBackground().setAlpha(50);

		// LinearLayout grey = new LinearLayout(context);
		// grey.setLayoutParams(new
		// LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		// grey.setBackgroundColor(Color.parseColor("#808080"));
		// grey.setAlpha((float) 0.8);
		// camview.addView(grey);
	}

	private class DownloadImageLiveNew extends AsyncTask<String, Void, String>
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
							cam.getCameraUserName(), cam.getCameraPassword(), cookies, 15000);
					if (cookies.size() > 0) cam.cookies = cookies;

					try
					// save image to external cache folder for cache
					{
						String extCachePath = context.getExternalFilesDir(null) + "/"
								+ cam.getCameraID() + ".jpg";
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

					String pathString = context.getCacheDir() + "/" + cam.getCameraID() + ".jpg";
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
										+ cam.getCameraID() + ":" + cam.getName()
										+ "]. File Deleted. File was empty.");
						return null;
					}
					else
					{
						if (enableLogs) Log.e(
								TAG,
								"File Error" + "::"
										+ "Unable to get the full file for the camera ["
										+ cam.getCameraID() + ":" + cam.getName() + "]");
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
					if (enableLogs) Log.e(TAG, e.toString() + "\n" + "[" + cam.getCameraID() + ":"
							+ cam.getName() + ":" + url1 + ":" + cam.getCameraUserName() + ":"
							+ cam.getCameraPassword() + "]" + "::" + Log.getStackTraceString(e));
					if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result)
		{// DownloadImageLive
			try
			{
				if (result != null && !end)
				{
					Drawable d = Drawable.createFromPath(result);
					if (d != null && d.getIntrinsicWidth() > 0 && d.getIntrinsicHeight() > 0)
					{
						camview.setBackgroundDrawable(d);

						// if(android.os.Build.VERSION.SDK_INT <
						// android.os.Build.VERSION_CODES.JELLY_BEAN)
						// {
						// camview.setBackgroundDrawable(d);
						// }
						// else {
						// camview.setBackground(d);
						// }
						CameraLayout.this.cam.loadingStatus = ImageLoadingStatus.live_received;
					}
					else
					{
						if (enableLogs) Log.e(TAG, "File Load Error" + "::"
								+ "Unable to load image for the camera [" + cam.getCameraID() + ":"
								+ cam.getName() + "] from path [" + result + "]");
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
					if (imageLiveTask.isTaskended
							&& imageLiveLocalNetworkTask.isTaskended
							&& CameraLayout.this.cam.loadingStatus != ImageLoadingStatus.live_received) CameraLayout.this.cam.loadingStatus = ImageLoadingStatus.live_not_received;
					if (imageLiveTask.isTaskended && imageLiveLocalNetworkTask.isTaskended) handler
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
	private class DownloadImageLatestCamba extends AsyncTask<Integer, Void, String>
	{
		@Override
		protected String doInBackground(Integer... cameraIDs)
		{
			for (int camid : cameraIDs)
			{
				try
				{
					String pathString = context.getCacheDir() + "/" + cam.getCameraID() + ".jpg";
					CambaApiManager.getCameraLatestImageAndSave(camid, pathString, 15000);

					File file = new File(pathString);
					if (file.exists() && file.length() > 0)
					{
						try
						// save image to external cache folder for cache
						{
							String extCachePath = context.getExternalFilesDir(null) + "/"
									+ cam.getCameraID() + ".jpg";
							File extfile = new File(extCachePath);
							if (!extfile.exists()) // write only when file does
													// not exist
							{
								Commons.copyFile(file, extfile);
							}
						}
						catch (Exception e)
						{
						}

						return pathString;
					}
					else
					{
						if (enableLogs) Log.e(
								TAG,
								"File Error" + "::"
										+ "Unable to get the full file for the camera ["
										+ cam.getCameraID() + ":" + cam.getName() + "]");
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
					if (enableLogs) Log.e(TAG, e.toString() + "\n" + "[" + cam.toString() + "]"
							+ "::" + Log.getStackTraceString(e));
					if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result)
		{ // DownloadImageLatestCamba
			try
			{
				if (result != null && !end)
				{
					Drawable d = Drawable.createFromPath(result);
					if (d != null && d.getIntrinsicWidth() > 0 && d.getIntrinsicHeight() > 0)
					{
						camview.setVisibility(View.VISIBLE);
						camview.setBackgroundDrawable(d);
						CameraLayout.this.cam.loadingStatus = ImageLoadingStatus.camba_image_received;
					}
					else
					{
						if (enableLogs) Log.e(TAG, "File Load Error" + "::"
								+ "Unable to load image for the camera [" + cam.getCameraID() + ":"
								+ cam.getName() + "] from path [" + result + "]");
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
				if (CameraLayout.this.cam.loadingStatus != ImageLoadingStatus.camba_image_received) CameraLayout.this.cam.loadingStatus = ImageLoadingStatus.camba_not_received;
			}
			catch (Exception e)
			{
			}
			try
			{
				if (!end)
				{
					handler.postDelayed(LoadImageRunnable, 0);
				}
			}
			catch (Exception e)
			{
			}
			try
			{
				imageCambaTask = null;
			}
			catch (Exception e)
			{
			}
		}
	}

}