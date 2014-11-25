//package io.evercam.androidapp.rvideo;
//
//import io.evercam.androidapp.ParentActivity;
//import io.evercam.androidapp.custom.CustomedDialog;
//import io.evercam.androidapp.custom.ProgressView;
//import io.evercam.androidapp.dto.AppData;
//import io.evercam.androidapp.dto.EvercamCamera;
//import io.evercam.androidapp.dto.ImageRecord;
//import io.evercam.androidapp.utils.CLog;
//import io.evercam.androidapp.utils.CambaRecordingApiManager;
//import io.evercam.androidapp.utils.Commons;
//import io.evercam.androidapp.utils.Constants;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.lang.reflect.Method;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.concurrent.RejectedExecutionException;
//import com.bugsense.trace.BugSenseHandler;
//import io.evercam.androidapp.R;
//
//import android.annotation.SuppressLint;
//import android.app.ActionBar;
//import android.app.AlertDialog;
//import android.app.ActionBar.OnNavigationListener;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.res.Configuration;
//import android.graphics.Bitmap;
//import android.graphics.Bitmap.CompressFormat;
//import android.graphics.Color;
//import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Drawable;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.SystemClock;
//import android.util.Log;
//import android.view.Display;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.WindowManager;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.ArrayAdapter;
//import android.widget.ImageView;
//import android.widget.RelativeLayout;
//import android.widget.Toast;
//
//public class RVideoViewActivity extends ParentActivity
//{
//
//	private final static String TAG = "RVideoViewActivity";
//
//	// Screen view change vraibales
//	private int screen_width, screen_height;
//	private int media_width = 0, media_height = 0;
//	public int actionBarHeight = 0;
//	private boolean landscape;
//
//	private Menu menu = null;
//
//	String CacheUrl;
//	// video playing controls and variables
//	private RelativeLayout iView;
//	private ImageView imgCam;
//	private ImageView rvmediaplayer;
//	// private TextView txtframerate;
//	private browseImages imageThread;
//	private boolean isProgressShowing = true;
//	private boolean isProgressShowingLastImage = true;
//	static boolean enableLogs = true;
//
//	boolean isActionBarAlwaysVisible = true;
//
//	// currently queued tasks that need to be cancelled when quiting
//	ArrayList<DownloadImage> taskList = new ArrayList<RVideoViewActivity.DownloadImage>();
//
//	// image tasks and thread variables
//	private int sleepIntervalMinTime = 51; // interval between two requests of
//											// iamges
//	private int intervalAdjustment = 10; // how much milli seconds to increment
//											// or decrement on image failure or
//											// success
//	private int sleepInterval = sleepIntervalMinTime + 450; // starting image
//															// interval
//	private boolean startDownloading = false; // start making requests soon
//												// after the image is received
//												// first time. Until first image
//												// is not received, do not make
//												// requests
//	private boolean isImageUrlsDownloadStarted = false; // start making requests
//														// soon after the image
//														// is received first
//														// time. Until first
//														// image is not
//														// received, do not make
//														// requests
//	private static long latestStartImageTime = 0; // time of the latest request
//													// that has been made
//	private boolean isFirstImageLiveReceived = false; // Whether first image has
//														// been received
//	private boolean isFirstImageLocalReceived = false; // Whether first image
//														// has been received
//	private boolean isFirstImageLiveEnded = false; // Whether first image has
//													// been received
//	private boolean isFirstImageLocalEnded = false; // Whether first image has
//													// been received
//	private int successiveFailureCount = 0; // how much successive image
//											// requests have failed
//	private Boolean isShowingFailureMessage = false; // whether error message
//														// for failure is
//														// showing or not
//
//	private Boolean optionsActivityStarted = false; // whether preference
//													// activity is showing or
//													// not
//
//	public AlertDialog adLocalNetwork; // dialoge message for local network not
//										// connected
//
//	public static String recordingStartTime = null;
//	public static EvercamCamera camera = null; // if camera uses cookies
//	// authentication, then use these
//	// cookies to pass to camera
//	private String CameraID = "";
//	private MenuItem dateTimeMenuItem = null;
//
//	private static volatile ArrayList<ImageRecord> imageList = new ArrayList<ImageRecord>();
//	private int imageIndex = 0;
//	private int lastImageIndex = 0;
//	boolean isOrientationChanged = false;
//	boolean paused = false; // whether media player or playing of video(images)
//							// is paused or not
//	boolean isLastImagePaused = false;
//	Animation myFadeInAnimation = null; // animation that shows the playing icon
//										// of media player fading and
//										// disappearing
//
//	boolean end = false; // whether to end this activity or not
//
//	// check whether Internet is connected or not
//	public void addCamsToDropdownActionBar()
//	{
//
//		new AsyncTask<String, String, String[]>(){
//
//			int defaultCamIndex = 0;
//
//			@Override
//			protected String[] doInBackground(String... params)
//			{
//				// TODO Auto-generated method stub
//				ArrayList<String> Cams = new ArrayList<String>();
//
//				for (int i = 0; i < AppData.evercamCameraList.size(); i++)
//				{
//
//					Cams.add(AppData.evercamCameraList.get(i).getName());
//
//					if (AppData.evercamCameraList.get(i).getCameraId() == camera.getCameraId()) defaultCamIndex = Cams
//							.size() - 1;
//
//				}
//
//				String[] cameras = new String[Cams.size()];
//				Cams.toArray(cameras);
//
//				return cameras;
//			}
//
//			@Override
//			protected void onPostExecute(final String[] CamsList)
//			{
//				try
//				{
//					ArrayAdapter<String> adapter = new ArrayAdapter<String>(
//							RVideoViewActivity.this, android.R.layout.simple_spinner_dropdown_item,
//							CamsList);
//					RVideoViewActivity.this.getActionBar().setNavigationMode(
//							ActionBar.NAVIGATION_MODE_LIST); // dropdown list
//																// navigation
//																// for the
//																// action bar
//
//					OnNavigationListener navigationListener = new OnNavigationListener(){
//						@Override
//						public boolean onNavigationItemSelected(int itemPosition, long itemId)
//						{
//							try
//							{
//
//								RVideoViewActivity.camera = AppData.evercamCameraList
//										.get(itemPosition);
//								//
//								//
//								setImageAttributesAndLoadImage();
//								//
//								//
//
//								// if (!isOrientationChanged){
//								// imageList = new ArrayList<ImageRecord>();
//								// }
//
//								if (CameraID != camera.getCameraId() || imageList == null
//										|| imageList.size() == 0)
//								{
//									imageIndex = 0;
//									imageList = new ArrayList<ImageRecord>();
//
//									new DownloadImageUrlsData().executeOnExecutor(
//											AsyncTask.THREAD_POOL_EXECUTOR, "");
//								}
//
//								StartResumeDownloading();
//
//								//
//
//							}
//							catch (Exception e)
//							{
//								Log.e(TAG, e.getMessage(), e);
//								if (Constants.isAppTrackingEnabled) BugSenseHandler
//										.sendException(e);
//							}
//							return false;
//						}
//					};
//
//					getActionBar().setListNavigationCallbacks(adapter, navigationListener);
//					getActionBar().setSelectedNavigationItem(defaultCamIndex);
//
//				}
//				catch (Exception e)
//				{
//					Log.e(TAG, e.getMessage(), e);
//					if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//				}
//
//			}
//
//		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
//
//	}
//
//	public static boolean startPlayingVIdeoForCamera(Context context, String camID, String time)
//	{
//		if (AppData.evercamCameraList != null) for (EvercamCamera cam : AppData.evercamCameraList)
//		{
//			if (cam.getCameraId() == camID)
//			{
//				startPlayingVIdeo(context, cam, time);
//				return true;
//			}
//		}
//		return false;
//	}
//
//	public static void startPlayingVIdeo(Context context, EvercamCamera cam, String time)
//	{
//		try
//		{
//
//			imageList = new ArrayList<ImageRecord>();
//
//			RVideoViewActivity.camera = cam;
//
//			if (time != null && time.length() == 17)
//			{
//				recordingStartTime = time;
//			}
//
//			// going to start the activity to show the full screen video
//			Intent i = new Intent(context, io.evercam.androidapp.rvideo.RVideoViewActivity.class);
//			context.startActivity(i);
//
//		}
//		catch (Exception e)
//		{
//			if (enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
//			CustomedDialog.getAlertDialog(context, "Exception", e.toString()).show();// +
//			// "::cam.getCameraImageUrl() ["
//			// +
//			// cam.getCameraImageUrl()
//			// +
//			// "], cam.getLowResolutionSnapshotUrl() ["+cam.getLowResolutionSnapshotUrl()+"]").show();
//
//		}
//	}
//
//	// Loads image from cache. First image gets loaded correctly and hence we
//	// can start making requests concurrently as well
//	@SuppressLint("NewApi")
//	public boolean loadImageFromCache()
//	{
//		try
//		{
//
//			boolean isPathExists = false;
//
//			String path = this.getCacheDir() + "/" + camera.getCameraId() + "_"
//					+ RVideoDateTimeDialog.GetDateTimeStringFullNoSpaces() + ".jpg";
//			if (new File(path).exists())
//			{
//				isPathExists = true;
//			}
//			else if (new File(this.getCacheDir() + "/" + camera.getCameraId() + ".jpg").exists())
//			{
//				path = this.getCacheDir() + "/" + camera.getCameraId() + ".jpg";
//				isPathExists = true;
//			}
//
//			if (isPathExists)
//			{
//				Drawable result = Drawable.createFromPath(path);
//				if (result != null)
//				{
//					startDownloading = true;
//
//					imgCam.setImageDrawable(result);
//
//					if (enableLogs) Log.i(TAG, "Loaded first image from Cache: " + media_width
//							+ ":" + media_height);
//					return true;
//				}
//				else
//				{
//					if (enableLogs) Log.e(
//							TAG,
//							"laodimagefromcache drawable d1 is null. Camera ID is "
//									+ camera.getCameraId());
//				}
//			}
//		}
//		catch (OutOfMemoryError e)
//		{
//			if (enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
//			return false;
//		}
//		catch (Exception e)
//		{
//			if (enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
//			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//		}
//
//		return false;
//	}
//
//	// preferences options for this screen
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu)
//	{
//		try
//		{
//			super.onCreateOptionsMenu(menu);
//			MenuInflater inflater = getMenuInflater();
//			inflater.inflate(R.menu.rivideomenulayout, menu);
//
//			this.menu = menu;
//
//			if (menu.findItem(R.id.menue_recording_datetime) != null)
//			{
//				dateTimeMenuItem = menu.findItem(R.id.menue_recording_datetime);
//				dateTimeMenuItem.setTitle(RVideoDateTimeDialog.GetDateTimeString());
//
//			}
//			return true;
//		}
//		catch (Exception ex)
//		{
//			if (enableLogs) Log.e(TAG, ex.toString());
//			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ex);
//		}
//		return true;
//	}
//
//	@Override
//	public boolean onPrepareOptionsMenu(Menu menu)
//	{
//		super.onPrepareOptionsMenu(menu);
//
//		// menu.clear();
//		//
//		// MenuInflater inflater = getMenuInflater();
//		// inflater.inflate(R.menu.rivideomenulayout, menu);
//		//
//		// for(int i = 0; i < menu.size(); i++)
//		// menu.getItem(i).setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT |
//		// MenuItem.SHOW_AS_ACTION_IF_ROOM);
//		//
//		// dateTimeMenuItem = (MenuItem)
//		// menu.findItem(R.id.menue_recording_datetime);
//		// dateTimeMenuItem.setTitle(RVideoDateTimeDialog.GetDateTimeString());
//
//		return true;
//	}
//
//	// Tells that what item has been selected from options. We need to call the
//	// relevent code for that item.
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item)
//	{
//		try
//		{
//			// Handle item selection
//			switch (item.getItemId())
//			{
//			case R.id.menue_recording_datetime:
//				optionsActivityStarted = true;
//				paused = true;
//				startActivity(new Intent(this, RVideoDateTimeDialog.class));
//				rvmediaplayer.setVisibility(View.GONE);
//
//				showProgressView();
//				if (enableLogs) Log
//						.i(TAG, "Options Activity Started in onPrepareOptionsMenu event");
//				return true;
//
//			case android.R.id.home:
//				this.finish();
//				return true;
//			default:
//				return super.onOptionsItemSelected(item);
//			}
//		}
//		catch (OutOfMemoryError e)
//		{
//			if (enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
//			return true;
//		}
//		catch (Exception e)
//		{
//			if (enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
//			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//			return true;
//		}
//	}
//
//	/** Called when the activity is first created. */
//	@Override
//	public void onCreate(Bundle savedInstanceState)
//	{
//		try
//		{
//			super.onCreate(savedInstanceState);
//
//			if (Constants.isAppTrackingEnabled) if (Constants.isAppTrackingEnabled) BugSenseHandler
//					.initAndStartSession(this, Constants.bugsense_ApiKey);
//
//			int orientation = this.getResources().getConfiguration().orientation;
//			if (orientation == Configuration.ORIENTATION_PORTRAIT)
//			{
//				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
//						WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//			}
//			else
//			{
//				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//						WindowManager.LayoutParams.FLAG_FULLSCREEN);
//			}
//
//			if (this.getActionBar() != null)
//			{
//
//				this.getActionBar().setHomeButtonEnabled(true);
//				this.getActionBar().setTitle("");
//				this.getActionBar().setIcon(R.drawable.ic_navigation_back);
//			}
//
//			setContentView(R.layout.rvideoimagelayoutwithslidenew);
//
//			if (savedInstanceState != null)
//			{
//
//				if (savedInstanceState.getBoolean("isProgressShowingLastImage"))
//				{
//					showProgressView();
//				}
//				else
//				{
//					hideProgressView();
//				}
//			}
//
//			addCamsToDropdownActionBar();
//
//			if (!Commons.isOnline(this)) // check whether the network is
//											// available or not?
//			{
//				try
//				{
//					CustomedDialog.getAlertDialog(RVideoViewActivity.this, "Network not available",
//							"Please connect to internat and try again",
//							new DialogInterface.OnClickListener(){
//
//								@Override
//								public void onClick(DialogInterface dialog, int which)
//								{
//									try
//									{
//										// TODO Auto-generated method stub
//										paused = true;
//										// end = true; // do not finish activity
//										// but
//										dialog.dismiss();
//										hideProgressView();
//									}
//									catch (Exception e)
//									{
//										if (Constants.isAppTrackingEnabled) BugSenseHandler
//												.sendException(e);
//									}
//								}
//							}).show();
//					return;
//				}
//				catch (Exception e)
//				{
//					if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//				}
//			}
//
//			if (recordingStartTime != null)
//			{
//				RVideoDateTimeDialog.setTime(recordingStartTime);
//				recordingStartTime = null;
//			}
//
//			if (enableLogs) Log.i(TAG, "getting Image View");
//			iView = (RelativeLayout) this.findViewById(R.id.camimage);
//			imgCam = (ImageView) this.findViewById(R.id.img_camera);
//			rvmediaplayer = (ImageView) this.findViewById(R.id.rvmediaplayer);
//			rvmediaplayer.setOnClickListener(new OnClickListener(){
//
//				@Override
//				public void onClick(View v)
//				{
//					// TODO Auto-generated method stub
//					if (end)
//					{
//						Toast.makeText(RVideoViewActivity.this, "Please close and try again.",
//								Toast.LENGTH_SHORT).show();
//						return;
//					}
//					if (isProgressShowing) return;
//					if (paused) // video is currently paused. Now we need to
//								// resume it.
//					{
//						rvmediaplayer.setImageBitmap(null);
//						rvmediaplayer.setVisibility(View.VISIBLE);
//						rvmediaplayer.setImageResource(android.R.drawable.ic_media_pause);
//
//						startMediaPlayerAnimation();
//
//						paused = false;
//					}
//					else
//					// video is currently playing. Now we need to pause video
//					{
//						rvmediaplayer.clearAnimation();
//						if (myFadeInAnimation != null && myFadeInAnimation.hasStarted()
//								&& !myFadeInAnimation.hasEnded())
//						{
//							myFadeInAnimation.cancel();
//							myFadeInAnimation.reset();
//						}
//						rvmediaplayer.setVisibility(View.VISIBLE);
//						rvmediaplayer.setImageBitmap(null);
//						rvmediaplayer.setImageResource(android.R.drawable.ic_media_play);
//
//						paused = true; // mark the images as paused. Do not stop
//										// threads, but do not show the images
//										// showing up
//					}
//				}
//			});
//
//			iView.setOnClickListener(new OnClickListener(){
//				@Override
//				public void onClick(View v)
//				{
//
//					if (end)
//					{
//						Toast.makeText(RVideoViewActivity.this, "Please close and try again.",
//								Toast.LENGTH_SHORT).show();
//						return;
//					}
//					if (isProgressShowing) return;
//
//					if (!paused && !end) // video is currently playing. Now we
//											// need to pause video
//					{
//
//						RVideoViewActivity.this.getActionBar().show();
//						rvmediaplayer.setImageResource(android.R.drawable.ic_media_pause);
//
//						rvmediaplayer.setVisibility(View.VISIBLE);
//
//						startMediaPlayerAnimation();
//
//					}
//
//				}
//			});
//			if (enableLogs) Log.i(TAG, "Got image view " + iView.toString());
//
//			// Get the size of the device, will be our maximum.
//			Display display = getWindowManager().getDefaultDisplay();
//			screen_width = display.getWidth();
//			screen_height = display.getHeight();
//			if (enableLogs) Log.i(TAG, "Got Display specs");
//			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//			if (enableLogs) Log.i(TAG, "acquired the power lock");
//
//			// AsyncTask
//			if (imageThread == null)
//			{
//				imageThread = new browseImages();
//
//			}
//
//			taskList = new ArrayList<DownloadImage>(); // currently tasks that
//														// are requesting images
//
//			if (!this.paused)
//			{
//				StartResumeDownloading();
//			}
//
//		}
//		catch (OutOfMemoryError e)
//		{
//			if (enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
//		}
//		catch (Exception ex)
//		{
//			if (enableLogs) Log.e(TAG, ex.toString(), ex);
//			CLog.email(RVideoViewActivity.this, ex.getMessage(), ex);
//		}
//	}
//
//	private void startMediaPlayerAnimation()
//	{
//		if (myFadeInAnimation != null)
//		{
//			myFadeInAnimation.cancel();
//			myFadeInAnimation.reset();
//
//			rvmediaplayer.clearAnimation();
//		}
//
//		myFadeInAnimation = AnimationUtils.loadAnimation(RVideoViewActivity.this, R.layout.fadein);
//
//		myFadeInAnimation.setAnimationListener(new Animation.AnimationListener(){
//			@Override
//			public void onAnimationStart(Animation animation)
//			{
//				// TODO Auto-generated method stub
//			}
//
//			@Override
//			public void onAnimationRepeat(Animation animation)
//			{
//				// TODO Auto-generated method stub
//			}
//
//			@Override
//			public void onAnimationEnd(Animation animation)
//			{
//
//				if (!paused) rvmediaplayer.setVisibility(View.GONE);
//				else rvmediaplayer.setVisibility(View.VISIBLE);
//
//				int orientation = RVideoViewActivity.this.getResources().getConfiguration().orientation;
//				if (!paused && orientation == Configuration.ORIENTATION_LANDSCAPE)
//				{
//					RVideoViewActivity.this.getActionBar().hide();
//				}
//			}
//		});
//
//		rvmediaplayer.startAnimation(myFadeInAnimation);
//	}
//
//	public void setImageAttributesAndLoadImage()
//	{
//		try
//		{
//			isFirstImageLiveReceived = false;
//			isFirstImageLocalReceived = false;
//			isFirstImageLiveEnded = false;
//			isFirstImageLocalEnded = false;
//
//			// showProgressView();//
//
//			startDownloading = false;
//			this.paused = false;
//			this.end = false;
//			this.isShowingFailureMessage = false;
//
//			for (int i = 0; i < taskList.size(); i++)
//			{
//				try
//				{
//					if (enableLogs) Log.e(TAG, "canceling task # " + i);
//					taskList.get(i).cancel(true);
//				}
//				catch (Exception e)
//				{
//					if (enableLogs) Log
//							.e(TAG, e.toString() + "-::::-" + Log.getStackTraceString(e));
//				}
//			}
//		}
//		catch (Exception e)
//		{
//			Log.e(TAG, e.getMessage(), e);
//			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//		}
//	}
//
//	private class browseImages extends AsyncTask<String, Void, Void>
//	{
//		@Override
//		protected Void doInBackground(String... params)
//		{
//			// TODO Auto-generated method stub
//
//			int LiveTaskID = 0;
//
//			Log.d("Umar", "browseImages->end=" + end);
//			while (!end) // keep on sending requests until the activity ends
//			{
//				try
//				{
//
//					if (isCancelled()) break;
//
//					// // wait for starting i.e. first image
//					try
//					{
//						while (!startDownloading) // if downloading has not
//													// started, keep on waiting
//													// until it starts
//						{
//							Thread.sleep(500);
//						}
//					}
//					catch (Exception e)
//					{
//						if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//					}
//					Log.d("Umar", "browseImages->paused=" + paused);
//					Log.d("Umar", "browseImages->isImageUrlsDownloadStarted="
//							+ isImageUrlsDownloadStarted);
//					if (!paused && isImageUrlsDownloadStarted) // if application
//																// is paused, do
//																// not send the
//																// requests.
//																// Rather wait
//																// for the play
//																// command
//					{
//
//						DownloadImage tasklive = new DownloadImage();
//
//						// tasklive.imageID = ++LiveTaskID;
//						if (LiveTaskID >= 100) LiveTaskID = 1;
//						if (imageIndex >= imageList.size())
//						{
//							return null;
//						}
//						tasklive.MyImageIndex = imageIndex;
//						Log.d("Umar", "browseImages=>" + "ImageIndex=" + imageIndex + "::"
//								+ "ImageListSize=" + imageList.size());
//
//						if (imageList.size() != 0)
//						{
//							tasklive.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
//									imageList.get(imageIndex++));
//						}
//
//						Log.e("Umar", this.toString());
//						taskList.add(tasklive); // add the newly created task to
//												// array so that we can remove
//												// if that has not been executed
//
//					}
//
//				}
//				catch (RejectedExecutionException ree)
//				{
//					Log.e(TAG, ree.toString() + "-::REE::-" + Log.getStackTraceString(ree));
//
//				}
//				catch (OutOfMemoryError e)
//				{
//					Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
//				}
//				catch (Exception ex)
//				{
//					Log.e(TAG, ex.toString() + "-::::-" + Log.getStackTraceString(ex));
//					if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ex);
//				}
//				try
//				{
//					Thread.currentThread();
//					Thread.sleep(sleepInterval, 0);
//				}
//				catch (Exception e)
//				{
//					if (enableLogs) Log
//							.e(TAG, e.toString() + "-::::-" + Log.getStackTraceString(e));
//				}
//			}
//
//			return null;
//		}
//
//	}
//
//	// Hide progress view
//	void hideProgressView()
//	{
//
//		if (iView != null)
//		{
//			iView.findViewById(R.id.rvprogressspinner).setVisibility(View.GONE);
//			isProgressShowing = false;
//		}
//
//	}
//
//	void showProgressView()
//	{
//		if (iView != null)
//		{
//			((ProgressView) iView.findViewById(R.id.rvprogressspinner)).canvasColor = Color.TRANSPARENT;
//			iView.findViewById(R.id.rvprogressspinner).setVisibility(View.VISIBLE);
//			isProgressShowing = true;
//		}
//	}
//
//	// When activity gets focused again
//	@Override
//	public void onRestart()
//	{
//		try
//		{
//			super.onRestart();
//
//			if (optionsActivityStarted)
//			{
//
//				optionsActivityStarted = false;
//				this.paused = false;
//				this.end = false;
//
//				showProgressView();
//
//				this.isShowingFailureMessage = false;
//				if (enableLogs) Log.i(TAG, "onRestart in block executed");
//
//				startDownloading = false;
//				for (int i = 0; i < taskList.size(); i++)
//				{
//					try
//					{
//						if (enableLogs) Log.e(TAG, "canceling task # " + i);
//						taskList.get(i).cancel(true);
//					}
//					catch (Exception e)
//					{
//						if (enableLogs) Log.e(TAG,
//								e.toString() + "-::::-" + Log.getStackTraceString(e));
//					}
//				}
//				latestStartImageTime = SystemClock.uptimeMillis();
//
//				dateTimeMenuItem.setTitle(RVideoDateTimeDialog.GetDateTimeString());
//
//				imageList = new ArrayList<ImageRecord>();
//				StartResumeDownloading();
//			}
//		}
//		catch (OutOfMemoryError e)
//		{
//			if (enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
//		}
//		catch (Exception e)
//		{
//			if (enableLogs) Log.e(TAG, e.toString());
//			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//		}
//	}
//
//	// take care of the steps that are required to start downloading
//	public void StartResumeDownloading()
//	{
//		try
//		{
//			startDownloading = true;
//
//			latestStartImageTime = SystemClock.uptimeMillis();
//
//			// isImageUrlsDownloadStarted = false;
//
//			end = false;
//			String status = imageThread.getStatus().toString();
//			if (imageThread.getStatus() == AsyncTask.Status.FINISHED)
//			{
//				imageThread = new browseImages();
//				imageThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
//			}
//			else if (imageThread.getStatus() != AsyncTask.Status.RUNNING)
//			{
//				imageThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
//			}
//
//			loadImageFromCache();
//		}
//		catch (OutOfMemoryError e)
//		{
//			if (enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
//		}
//		catch (Exception e)
//		{
//			if (enableLogs) Log.e(TAG, e.toString());
//			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//		}
//	}
//
//	// when activity loses focus. we need to end this activity
//	@Override
//	public void onPause()
//	{
//		try
//		{
//			super.onPause();
//
//			isFirstImageLiveReceived = false;
//			isFirstImageLocalReceived = false;
//			isFirstImageLiveEnded = false;
//			isFirstImageLocalEnded = false;
//			isLastImagePaused = paused;
//			if (!optionsActivityStarted)
//			{
//
//				this.paused = true;
//				this.end = true;
//				this.onDestroy();
//				if (enableLogs) Log.i(TAG, "onPause in block executed");
//				System.runFinalizersOnExit(true);
//				// super.finish();
//				// this.finish();
//
//				if (enableLogs) Log
//						.i(TAG,
//								"goint to end the activity......................................................................................");
//			}
//		}
//		catch (Exception ex)
//		{
//			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ex);
//		}
//	}
//
//	@Override
//	public void onStart()
//	{
//		super.onStart();
//		Log.i("sajjadpp", "onStart called");
//		if (Constants.isAppTrackingEnabled)
//		{
//			if (Constants.isAppTrackingEnabled) BugSenseHandler.startSession(this);
//		}
//	}
//
//	// When activity loses focus and some other activity gets activated. We need
//	// to end this activity
//	@Override
//	public void onStop()
//	{
//		try
//		{
//			super.onStop();
//
//			isFirstImageLiveReceived = false;
//			isFirstImageLocalReceived = false;
//			isFirstImageLiveEnded = false;
//			isFirstImageLocalEnded = false;
//
//			if (!optionsActivityStarted)
//			{
//
//				this.paused = true;
//				this.end = true; // this will end the thread as well
//				if (enableLogs) Log.i(TAG, "onStop in block executed");
//				this.onDestroy();
//				// android.os.Process.killProcess(android.os.Process.myPid());
//				System.runFinalizersOnExit(true);
//				// System.exit(0);
//				// super.finish();
//				// this.finish();
//			}
//		}
//		catch (Exception ex)
//		{
//		}
//		if (Constants.isAppTrackingEnabled)
//		{
//			if (Constants.isAppTrackingEnabled) BugSenseHandler.closeSession(this);
//		}
//	}
//
//	// when screen gets rotated
//	@Override
//	public void onConfigurationChanged(Configuration newConfig)
//	{
//		try
//		{
//			Log.i("sajjadpp", "onConfigurationChanged called");
//
//			super.onConfigurationChanged(newConfig);
//
//			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//			int orientation = newConfig.orientation;
//			if (orientation == Configuration.ORIENTATION_PORTRAIT)
//			{
//				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
//						WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//				landscape = false;
//				this.getActionBar().show();
//			}
//			else
//			{
//				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//						WindowManager.LayoutParams.FLAG_FULLSCREEN);
//				landscape = true;
//
//				if (!paused && !end && !isProgressShowing) this.getActionBar().hide();
//				else this.getActionBar().show();
//			}
//
//			Method setShowAsAction = MenuItem.class.getMethod("setShowAsAction", Integer.TYPE);
//
//			MenuItem item = menu.findItem(R.id.menue_recording_datetime);
//			menu.removeItem(R.id.menue_recording_datetime);
//			menu.add(item.getGroupId(), item.getItemId(), item.getOrder(), item.getTitle())
//					.setIcon(item.getIcon());
//			dateTimeMenuItem = menu.findItem(R.id.menue_recording_datetime);
//
//			int state = MenuItem.class.getField("SHOW_AS_ACTION_ALWAYS").getInt(null)
//					| MenuItem.class.getField("SHOW_AS_ACTION_WITH_TEXT").getInt(null);
//
//			setShowAsAction.invoke(dateTimeMenuItem, state);
//
//		}
//		catch (Exception e)
//		{
//			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//		}
//	}
//
//	public void resize(int imageHieght, int imageWidth)
//	{
//		int abHeight = this.getActionBar().getHeight();
//		int w = screen_width;
//		int h = screen_height;
//
//		h -= actionBarHeight;
//
//		// If we have the media, calculate best scaling inside bounds.
//		if (imageWidth > 0 && imageHieght > 0)
//		{
//			final float max_w = w;
//			final float max_h = h;
//			float temp_w = imageWidth;
//			float temp_h = imageHieght;
//			float factor = max_w / temp_w;
//			temp_w *= factor;
//			temp_h *= factor;
//
//			// If we went above the height limit, scale down.
//			if (temp_h > max_h)
//			{
//				factor = max_h / temp_h;
//				temp_w *= factor;
//				temp_h *= factor;
//			}
//
//			w = (int) temp_w;
//			h = (int) temp_h;
//		}
//		media_height = h;
//		media_width = w;
//		if (enableLogs) Log.i(TAG, "resize method called: " + w + ":" + h);
//	}
//
//	private class DownloadImage extends AsyncTask<ImageRecord, Void, Drawable>
//	{
//		private long myStartImageTime;
//		public int MyImageIndex = 0;
//
//		@Override
//		protected Drawable doInBackground(ImageRecord... imageRec)
//		{
//			Drawable response = null;
//			for (ImageRecord imager : imageRec)
//			{
//				try
//				{
//					startDownloading = false;
//					myStartImageTime = SystemClock.uptimeMillis();
//
//					URL url = new URL(imager.getUrl());
//					response = Commons.DownlaodDrawableSync(url, 15000);
//
//					Bitmap bmp = ((BitmapDrawable) response).getBitmap();
//					CacheUrl = RVideoViewActivity.this.getCacheDir() + "/" + camera.getCameraId()
//							+ "_" + imager.getFDT() + ".jpg";
//
//					FileOutputStream fos = new FileOutputStream(CacheUrl);
//					bmp.compress(CompressFormat.JPEG, 100, fos);
//
//					fos.close();
//
//					if (response != null) successiveFailureCount = 0;
//
//				}
//				catch (OutOfMemoryError e)
//				{
//					if (enableLogs) Log.e(TAG,
//							e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
//					successiveFailureCount++;
//					return null;
//				}
//				catch (Exception e)
//				{
//					if (enableLogs) Log.e(TAG, "Exception: " + e.toString() + "\r\n" + "ImageURl=["
//							+ imager.getUrl() + "]");
//					if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//					successiveFailureCount++;
//				}
//			}
//			return response;
//		}
//
//		@SuppressLint("NewApi")
//		@Override
//		protected void onPostExecute(Drawable result)
//		{// DownloadImage Live
//			try
//			{
//
//				if (result != null && result.getIntrinsicWidth() > 0
//						&& result.getIntrinsicHeight() > 0
//						&& myStartImageTime >= latestStartImageTime && !paused && !end)
//				{
//
//					if (rvmediaplayer.getVisibility() != View.VISIBLE
//							&& RVideoViewActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) RVideoViewActivity.this
//							.getActionBar().hide();
//
//					latestStartImageTime = myStartImageTime;
//
//					imgCam.setImageDrawable(result);
//
//					hideProgressView();
//
//					String FDT = imageList.get(imageIndex).getFDT();
//					if (FDT != null && FDT.length() > 14)
//					{
//						RVideoDateTimeDialog.setTime(FDT);
//
//						dateTimeMenuItem.setTitle(RVideoDateTimeDialog.GetDateTimeString());
//					}
//
//					lastImageIndex = MyImageIndex;
//
//				}
//				// do not show message on local network failure request.
//				else if (
//
//				successiveFailureCount > 5 && !isShowingFailureMessage
//						&& myStartImageTime >= latestStartImageTime && !paused && !end)
//				{
//					isShowingFailureMessage = true;
//					hideProgressView();
//					CustomedDialog.getAlertDialog(RVideoViewActivity.this, "Unable to connect",
//							"Internet connectivity is lost.",
//							new DialogInterface.OnClickListener(){
//
//								@Override
//								public void onClick(DialogInterface dialog, int which)
//								{
//									try
//									{
//										// RVideoViewActivity.this.onStop();
//										RVideoViewActivity.this.getActionBar().show();
//										isActionBarAlwaysVisible = true;
//										paused = true;
//										// end = true; // do not finish activity
//										// but
//										isShowingFailureMessage = false;
//										dialog.dismiss();
//										hideProgressView();
//									}
//									catch (Exception e)
//									{
//										if (Constants.isAppTrackingEnabled) BugSenseHandler
//												.sendException(e);
//									}
//								}
//							}).show();
//				}
//				else
//				{
//					if (enableLogs) Log.i(TAG, "downloaded image discarded. ");
//				}
//			}
//			catch (OutOfMemoryError e)
//			{
//				if (enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
//			}
//			catch (Exception e)
//			{
//				if (enableLogs) Log.e(TAG, e.toString());
//				if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//			}
//			try
//			{
//				startDownloading = true;
//			}
//			catch (Exception ex)
//			{
//			}
//			try
//			{
//				taskList.remove(this);
//			}
//			catch (Exception e)
//			{
//				if (enableLogs) Log.e(TAG, e.toString() + "-::::-" + Log.getStackTraceString(e));
//			}
//			startDownloading = true;
//		}
//	}
//
//	private class DownloadImageUrlsData extends AsyncTask<String, Void, Drawable>
//	{
//		private long myStartImageTime;
//
//		@Override
//		protected Drawable doInBackground(String... urls)
//		{
//			Drawable response = null;
//			for (String url1 : urls)
//			{
//				try
//				{
//					isImageUrlsDownloadStarted = false;
//
//					// Calendar c = Calendar.getInstance();
//					// c.setTimeZone(TimeZone.getTimeZone(id))
//					//
//					// c.set(Calendar.YEAR, dpRecordingDate.getYear());
//					// c.set(Calendar.MONTH, dpRecordingDate.getMonth() );
//					// c.set(Calendar.DAY_OF_MONTH,
//					// dpRecordingDate.getDayOfMonth());
//					// c.set(Calendar.HOUR_OF_DAY,
//					// tpRecordingTime.getCurrentHour());\
//					// c.set(Calendar.MINUTE,
//					// tpRecordingTime.getCurrentMinute());
//					// c.set(Calendar.SECOND, 0);
//					// c.set(Calendar.MILLISECOND, 0);
//					//
//
//					int iteration = 0;
//					// camera.setHourServerIp(CambaRecordingApiManager.GetCameraInfoOptimized(camera.getCameraID(),
//					// camera.getCameraStatusIntVal(),
//					// camera.getIsMdEnabled(),camera.getAlarmLevelInteger(),
//					// camera.getCameraTimeZone(), camera.getCameraGroup(),
//					// RVideoDateTimeDialog.GetDateTimeStringFullNoSpaces(),
//					// RVideoDateTimeDialog.GetDateTimeStringFullNoSpacesAddMinutes(20),
//					// RVideoDateTimeDialog.GetDateTimeStringFullNoSpaces()));
//					// if(camera.getHourServerIp() == null)
//					// {
//					// // CustomedDialog.GetAlertDialog(RVideoViewActivity.this,
//					// "Recording not found",
//					// "Unable to find recordings for the given time. Please try later.").show();
//					// return null;
//					// }
//
//					while (CambaRecordingApiManager.GetSnapshotsOptimized(imageList,
//							camera.getCode(), RVideoDateTimeDialog.GetDateTimeStringFullNoSpaces(),
//							RVideoDateTimeDialog.GetDateTimeStringFullNoSpacesAddMinutes(20),
//							iteration, 100))
//					{
//						iteration++;
//						isImageUrlsDownloadStarted = true;
//
//					}
//
//				}
//				catch (OutOfMemoryError e)
//				{
//					if (enableLogs) Log.e(TAG,
//							e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
//					successiveFailureCount++;
//					return null;
//				}
//				catch (Exception e)
//				{
//					if (enableLogs) Log.e(TAG, "Exception: " + e.toString() + "\r\n" + "ImageURl=["
//							+ url1 + "]");
//					if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//				}
//			}
//			return response;
//		}
//
//		@Override
//		protected void onPostExecute(Drawable result)
//		{// DownloadImage Live
//			if (!isImageUrlsDownloadStarted)
//
//			CustomedDialog.getAlertDialog(RVideoViewActivity.this, "Recordings Unavailable",
//					"No recordings available for this time.").show();
//		}
//	}
//
//}