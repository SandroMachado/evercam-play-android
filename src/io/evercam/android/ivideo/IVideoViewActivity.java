package io.evercam.android.ivideo;

import io.evercam.android.ParentActivity;
import io.evercam.android.custom.ProgressView;
import io.evercam.android.dto.Camera;
import io.evercam.android.slidemenu.SlideMenu;
import io.evercam.android.slidemenu.SlideMenuInterface;
import io.evercam.android.utils.AppData;
import io.evercam.android.utils.Commons;
import io.evercam.android.utils.Constants;
import io.evercam.android.utils.UIUtils;

import java.io.File;

import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.RejectedExecutionException;

//import com.camba.killruddery.VideoViewCustom;

import com.bugsense.trace.BugSenseHandler;
import io.evercam.android.R;

import com.google.analytics.tracking.android.EasyTracker;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ActionBar.OnNavigationListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class IVideoViewActivity extends ParentActivity implements
		SlideMenuInterface.OnSlideMenuItemClickListener
{

	private final static String TAG = "IVideoViewActivity";

	private SlideMenu slidemenu;

	// Screen view change vraibales
	private int screen_width, screen_height;
	private int media_width = 0, media_height = 0;
	public int actionBarHeight = 0;
	private boolean landscape;

	// video playing controls and variables
	private RelativeLayout iView;
	private ImageView imgCam;
	private ImageView ivMediaPlayer;
	// private String imageURL =
	// null;//"http://killruddery1.dtdns.net:8001/snapshot1.jpg" ;
	private long downloadStartCount = 0;
	private long downloadEndCount = 0;
	private TextView txtframerate;
	private browseImages imageThread;
	private boolean isProgressShowing = true;
	static boolean enableLogs = true;

	// currently queued tasks that need to be cancelled when quiting
	ArrayList<DownloadImage> taskList = new ArrayList<IVideoViewActivity.DownloadImage>();

	// image tasks and thread variables
	private int sleepIntervalMinTime = 51; // interval between two requests of
											// iamges
	private int intervalAdjustment = 10; // how much milli seconds to increment
											// or decrement on image failure or
											// success
	private int sleepInterval = sleepIntervalMinTime + 450; // starting image
															// interval
	private boolean startDownloading = false; // start making requests soon
												// after the image is received
												// first time. Until first image
												// is not received, do not make
												// requests
	private static long latestStartImageTime = 0; // time of the latest request
													// that has been made
	private boolean isFirstImageLiveReceived = false; // Whether first image has
														// been received
	private boolean isFirstImageLocalReceived = false; // Whether first image
														// has been received
	private boolean isFirstImageLiveEnded = false; // Whether first image has
													// been received
	private boolean isFirstImageLocalEnded = false; // Whether first image has
													// been received
	private int successiveFailureCount = 0; // how much successive image
											// requests have failed
	private Boolean isShowingFailureMessage = false; // whether error message
														// for failure is
														// showing or not

	private Boolean optionsActivityStarted = false; // whether preference
													// activity is showing or
													// not

	public AlertDialog adLocalNetwork; // dialoge message for local network not
										// connected

	// Camera URL options
	public static String host = "killruddery1.dtdns.net", port = "8001",
			localip = "192.168.1.198"// "149.5.40.144";
			, localport = "8001", HighResImageURL = "snapshot1.jpg",
			LowResImageURL = "snapshot_3gp.jpg";
	public static Camera camera = null; // if camera uses cookies
										// authentication, then use these
										// cookies to pass to camera

	// preferences options
	private boolean playAudio = false;
	private boolean chkhighqualityvideo = true;
	private String localnetworkSettings = "0";
	private boolean isLocalNetwork = false;

	private String imageLiveCameraURL = null;
	private String imageLiveLocalURL = null;

	boolean paused = false; // whether media player or playing of video(images)
							// is paused or not

	Animation myFadeInAnimation = null; // animation that shows the playing icon
										// of media player fading and
										// disappearing

	boolean end = false; // whether to end this activity or not

	public void addCamsToDropdownActionBar()
	{

		new AsyncTask<String, String, String[]>(){
			final ArrayList<Camera> ActiveCamers = new ArrayList<Camera>();
			int defaultCamIndex = 0;

			@Override
			protected String[] doInBackground(String... params)
			{
				ArrayList<String> Cams = new ArrayList<String>();

				for (int i = 0; i < AppData.cameraList.size(); i++)
				{
					if (!AppData.cameraList.get(i).getStatus().equalsIgnoreCase("Offline"))
					{
						ActiveCamers.add(AppData.cameraList.get(i));
						Cams.add(AppData.cameraList.get(i).getName());
						if (AppData.cameraList.get(i).getCameraID() == camera.getCameraID()) defaultCamIndex = Cams
								.size() - 1;

					}

				}

				// return (String[]) Cams.toArray();
				String[] cameras = new String[Cams.size()];
				Cams.toArray(cameras);

				return cameras;
			}

			@Override
			protected void onPostExecute(final String[] CamsList)
			{
				try
				{
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(
							IVideoViewActivity.this, android.R.layout.simple_spinner_dropdown_item,
							CamsList);
					IVideoViewActivity.this.getActionBar().setNavigationMode(
							ActionBar.NAVIGATION_MODE_LIST); // dropdown list
																// navigation
																// for the
																// action bar
					OnNavigationListener navigationListener = new OnNavigationListener(){
						@Override
						public boolean onNavigationItemSelected(int itemPosition, long itemId)
						{
							try
							{

								SetCameraForPlaying(IVideoViewActivity.this,
										ActiveCamers.get(itemPosition));

								SetImageAttributesAndLoadImage();

								StartResumeDownloading();
								// //imageThread=new browseImages();
								// //imageThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new
								// String[] {});

							}
							catch (Exception e)
							{
								Log.e(TAG, e.getMessage(), e);
								if (Constants.isAppTrackingEnabled) BugSenseHandler
										.sendException(e);
							}
							return false;
						}
					};

					getActionBar().setListNavigationCallbacks(adapter, navigationListener);
					getActionBar().setSelectedNavigationItem(defaultCamIndex);

				}
				catch (Exception e)
				{
					Log.e(TAG, e.getMessage(), e);
					if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
				}

			}

		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");

	}

	public static boolean StartPlayingVIdeoForCamera(Context context, int camID)
	{
		if (AppData.cameraList != null) for (Camera cam : AppData.cameraList)
		{
			if (cam.getCameraID() == camID)
			{

				StartPlayingVIdeo(context, cam);
				return true;
			}
		}
		return false;
	}

	public static void StartPlayingVIdeo(Context context, Camera cam)
	{
		try
		{

			SetCameraForPlaying(context, cam);

			// going to start the activity to show the full screen video
			Intent i = new Intent(context, io.evercam.android.ivideo.IVideoViewActivity.class);
			context.startActivity(i);

		}
		catch (Exception e)
		{
			if (enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
			UIUtils.GetAlertDialog(context, "Exception", e.toString()).show();// +
																				// "::cam.getCameraImageUrl() ["
																				// +
																				// cam.getCameraImageUrl()
																				// +
																				// "], cam.getLowResolutionSnapshotUrl() ["+cam.getLowResolutionSnapshotUrl()+"]").show();
		}
	}

	public static void SetCameraForPlaying(Context context, Camera cam)
	{
		try
		{
			if (enableLogs) Log.i(TAG, "Playing camera:" + cam.toString());
			// passing the medium, mobile compatible range image url to the
			// camra
			String ImageUrl = ((cam.getLowResolutionSnapshotUrl() != null && URLUtil.isValidUrl(cam
					.getLowResolutionSnapshotUrl())) ? cam.getLowResolutionSnapshotUrl() : cam
					.getCameraImageUrl());

			if (!URLUtil.isValidUrl(ImageUrl))
			{
				UIUtils.GetAlertDialog(context, "Camera URL Error",
						"Invalid camera settings. Please contact camba.tv team.").show();
				return;
			}

			// Extracting information from the camera image url
			ImageUrl = ImageUrl.replace("http://", "");
			boolean hasPort = ImageUrl.contains(":");
			io.evercam.android.ivideo.IVideoViewActivity.host = (String) (hasPort ? ImageUrl
					.subSequence(0, ImageUrl.indexOf(":")) : ImageUrl.subSequence(0,
					ImageUrl.indexOf("/")));
			if (hasPort)
			{
				IVideoViewActivity.port = (String) ImageUrl.subSequence(ImageUrl.indexOf(":") + 1,
						ImageUrl.indexOf("/"));
			}
			else
			{
				IVideoViewActivity.port = "";
			}
			ImageUrl = ImageUrl.replace(IVideoViewActivity.host
					+ (hasPort ? ":" + IVideoViewActivity.port : "") + "/", "");
			io.evercam.android.ivideo.IVideoViewActivity.HighResImageURL = ImageUrl;
			io.evercam.android.ivideo.IVideoViewActivity.LowResImageURL = ImageUrl;
			IVideoViewActivity.camera = cam;
			String LocalIpPort = cam.getLocalIpPort() + "";
			if (LocalIpPort.contains(":"))
			{
				IVideoViewActivity.localip = LocalIpPort.substring(0, LocalIpPort.indexOf(":"));
				IVideoViewActivity.localport = LocalIpPort.substring(LocalIpPort.indexOf(":") + 1);
			}
			else if (LocalIpPort.length() > 0)
			{
				IVideoViewActivity.localip = LocalIpPort;
				IVideoViewActivity.localport = null;
			}
			else
			{
				IVideoViewActivity.localip = null;
				IVideoViewActivity.localport = null;
			}

			// going to start the activity to show the full screen video

		}
		catch (Exception e)
		{
			if (enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
			UIUtils.GetAlertDialog(context, "Exception", e.toString()).show();// +
																				// "::cam.getCameraImageUrl() ["
																				// +
																				// cam.getCameraImageUrl()
																				// +
																				// "], cam.getLowResolutionSnapshotUrl() ["+cam.getLowResolutionSnapshotUrl()+"]").show();
		}

	}

	// Loads image from cache. First image gets loaded correctly and hence we
	// can start making requests concurrently as well
	public boolean LoadImageFromCache()
	{
		try
		{
			String path = this.getCacheDir() + "/" + camera.getCameraID() + ".jpg";
			if (new File(path).exists())
			{
				Drawable result = Drawable.createFromPath(path);
				if (result != null)
				{
					startDownloading = true;
					imgCam.setImageDrawable(result);

					if (enableLogs) Log.i(TAG, "Loaded first image from Cache: " + media_width
							+ ":" + media_height);
					return true;
				}
				else
				{
					if (enableLogs) Log.e(
							TAG,
							"laodimagefromcache drawable d1 is null. Camera Object is ["
									+ camera.toString() + "]");
				}
			}
		}
		catch (OutOfMemoryError e)
		{
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

	// preferences options for this screen
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		try
		{
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.ivideomenulayout, menu);

			return true;
		}
		catch (Exception ex)
		{
			if (enableLogs) Log.e(TAG, ex.toString());
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ex);
		}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);

		menu.clear();

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.ivideomenulayout, menu);

		return true;
	}

	// Tells that what item has been selected from options. We need to call the
	// relevent code for that item.
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		try
		{
			// Handle item selection
			switch (item.getItemId())
			{
			case R.id.menusettings:
				optionsActivityStarted = true;
				paused = true;
				startActivity(new Intent(this, IVideoPrefsActivity.class));
				ivMediaPlayer.setVisibility(View.GONE);

				showProgressView();
				if (enableLogs) Log
						.i(TAG, "Options Activity Started in onPrepareOptionsMenu event");
				return true;
			case android.R.id.home:
				this.finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		}
		catch (OutOfMemoryError e)
		{
			if (enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
			return true;
		}
		catch (Exception e)
		{
			if (enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
			return true;
		}
	}

	// Read preferences for playing options audio and Video(images)
	public void readSetPreferences()
	{
		try
		{

			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

			Boolean bchkenabfps = false;

			chkhighqualityvideo = sharedPrefs.getBoolean("chkhighqualityvideo", true);
			bchkenabfps = sharedPrefs.getBoolean("chkenablefps", false);

			isLocalNetwork = false;
			localnetworkSettings = sharedPrefs.getString(
					"pref_enablocalnetwork" + camera.getCameraID(), "0");// ("chkenablocalnetwork",
																			// false);
			if (localnetworkSettings.equalsIgnoreCase("1")) isLocalNetwork = true;
			else isLocalNetwork = false;

			String portString = (localport == null || localport.equalsIgnoreCase("") ? "" : ":"
					+ localport);
			imageLiveLocalURL = "http://" + localip + portString + "/"
					+ (chkhighqualityvideo ? HighResImageURL : LowResImageURL);

			portString = (port == null || port.equalsIgnoreCase("") ? "" : ":" + port);
			imageLiveCameraURL = "http://" + host + portString + "/"
					+ (chkhighqualityvideo ? HighResImageURL : LowResImageURL);

			if (bchkenabfps) txtframerate.setVisibility(View.VISIBLE);
			else txtframerate.setVisibility(View.GONE);

		}
		catch (OutOfMemoryError e)
		{
			if (enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
		}
		catch (Exception ex)
		{
			if (enableLogs) Log.e(TAG, ex.toString());
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ex);
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		try
		{
			super.onCreate(savedInstanceState);

			if (Constants.isAppTrackingEnabled) if (Constants.isAppTrackingEnabled) BugSenseHandler
					.initAndStartSession(this, Constants.bugsense_ApiKey);

			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

			int orientation = this.getResources().getConfiguration().orientation;
			if (orientation == Configuration.ORIENTATION_PORTRAIT)
			{
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
						WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			}
			else
			{
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
						WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}

			if (this.getActionBar() != null)
			{

				this.getActionBar().setHomeButtonEnabled(true);
				this.getActionBar().setTitle("");
				this.getActionBar().setIcon(R.drawable.ic_navigation_back);
			}

			setContentView(R.layout.ivideoimagelayoutwithslide);

			addCamsToDropdownActionBar();

			if (!Commons.isOnline(this)) // check whether the network is
											// available or not?
			{
				try
				{
					UIUtils.GetAlertDialog(IVideoViewActivity.this, "Network not available",
							"Please connect to internat and try again",
							new DialogInterface.OnClickListener(){

								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									try
									{
										// TODO Auto-generated method stub
										// IVideoViewActivity.this.onStop();
										paused = true;
										// end = true; // do not finish activity
										// but
										dialog.dismiss();
										hideProgressView();
									}
									catch (Exception e)
									{
										if (Constants.isAppTrackingEnabled) BugSenseHandler
												.sendException(e);
									}
								}
							}).show();
					return;
				}
				catch (Exception e)
				{
					if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
				}
			}

			if (enableLogs) Log.i(TAG, "getting Image View");
			iView = (RelativeLayout) this.findViewById(R.id.camimage);
			imgCam = (ImageView) this.findViewById(R.id.img_camera);
			ivMediaPlayer = (ImageView) this.findViewById(R.id.ivmediaplayer);
			txtframerate = (TextView) IVideoViewActivity.this.findViewById(R.id.txtframerate);
			txtframerate.setText(String.format("[F/s:%.2f]", ((float) 0)));

			readSetPreferences();

			((ProgressView) iView.findViewById(R.id.ivprogressspinner)).CanvasColor = Color.TRANSPARENT; // transparent
																											// color
																											// because
																											// image
																											// loaded
																											// in
																											// cache
																											// should
																											// be
																											// displayed
																											// as
																											// well

			isProgressShowing = true;
			((ProgressView) iView.findViewById(R.id.ivprogressspinner)).setVisibility(View.VISIBLE);

			ivMediaPlayer.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v)
				{
					// TODO Auto-generated method stub
					if (end)
					{
						Toast.makeText(IVideoViewActivity.this, "Please close and try again.",
								Toast.LENGTH_SHORT).show();
						return;
					}
					if (isProgressShowing) return;
					if (paused) // video is currently paused. Now we need to
								// resume it.
					{
						ivMediaPlayer.setImageBitmap(null);
						ivMediaPlayer.setVisibility(View.VISIBLE);
						ivMediaPlayer.setImageResource(android.R.drawable.ic_media_pause);

						startMediaPlayerAnimation();

						paused = false;
					}
					else
					// video is currently playing. Now we need to pause video
					{
						ivMediaPlayer.clearAnimation();
						if (myFadeInAnimation != null && myFadeInAnimation.hasStarted()
								&& !myFadeInAnimation.hasEnded())
						{
							myFadeInAnimation.cancel();
							myFadeInAnimation.reset();
						}
						ivMediaPlayer.setVisibility(View.VISIBLE);
						ivMediaPlayer.setImageBitmap(null);
						ivMediaPlayer.setImageResource(android.R.drawable.ic_media_play);

						paused = true; // mark the images as paused. Do not stop
										// threads, but do not show the images
										// showing up
					}
				}
			});

			iView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v)
				{

					if (end)
					{
						Toast.makeText(IVideoViewActivity.this, "Please close and try again.",
								Toast.LENGTH_SHORT).show();
						return;
					}
					if (isProgressShowing) return;

					if (!paused && !end) // video is currently playing. Now we
											// need to pause video
					{

						IVideoViewActivity.this.getActionBar().show();
						ivMediaPlayer.setImageResource(android.R.drawable.ic_media_pause);

						ivMediaPlayer.setVisibility(View.VISIBLE);

						startMediaPlayerAnimation();

					}

				}
			});
			if (enableLogs) Log.i(TAG, "Got image view " + iView.toString());

			// Get the size of the device, will be our maximum.
			Display display = getWindowManager().getDefaultDisplay();
			screen_width = display.getWidth();
			screen_height = display.getHeight();
			if (enableLogs) Log.i(TAG, "Got Display specs");

			// Keep the screen on

			if (enableLogs) Log.i(TAG, "acquired the power lock");

			// Thread

			if (imageThread == null)
			{
				imageThread = new browseImages();

			}

			if (!this.paused)
			{
				StartResumeDownloading();
			}

		}
		catch (OutOfMemoryError e)
		{
			if (enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
		}
		catch (Exception ex)
		{
			if (enableLogs) Log.e(TAG, ex.toString(), ex);
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ex);
		}
	}

	private void startMediaPlayerAnimation()
	{
		if (myFadeInAnimation != null)
		{
			myFadeInAnimation.cancel();
			myFadeInAnimation.reset();

			ivMediaPlayer.clearAnimation();
		}

		myFadeInAnimation = AnimationUtils.loadAnimation(IVideoViewActivity.this, R.layout.fadein);

		myFadeInAnimation.setAnimationListener(new Animation.AnimationListener(){
			@Override
			public void onAnimationStart(Animation animation)
			{
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{

				if (!paused) ivMediaPlayer.setVisibility(View.GONE);
				else ivMediaPlayer.setVisibility(View.VISIBLE);

				int orientation = IVideoViewActivity.this.getResources().getConfiguration().orientation;
				if (!paused && orientation == Configuration.ORIENTATION_LANDSCAPE)
				{
					IVideoViewActivity.this.getActionBar().hide();
				}
			}
		});

		ivMediaPlayer.startAnimation(myFadeInAnimation);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putLong("downloadStartCount", downloadStartCount);
		savedInstanceState.putLong("downloadEndCount", downloadEndCount);
		savedInstanceState.putBoolean("isProgressShowing", isProgressShowing);
		savedInstanceState.putInt("sleepIntervalMinTime", sleepIntervalMinTime);
		savedInstanceState.putInt("intervalAdjustment", intervalAdjustment);
		savedInstanceState.putInt("sleepInterval", sleepInterval);
		savedInstanceState.putBoolean("startDownloading", startDownloading);

		savedInstanceState.putBoolean("isFirstImageLiveReceived", isFirstImageLiveReceived);
		savedInstanceState.putBoolean("isFirstImageLocalReceived", isFirstImageLocalReceived);
		savedInstanceState.putBoolean("isFirstImageLiveEnded", isFirstImageLiveEnded);
		savedInstanceState.putBoolean("isFirstImageLocalEnded", isFirstImageLocalEnded);
		savedInstanceState.putInt("successiveFailureCount", successiveFailureCount);
		savedInstanceState.putBoolean("isShowingFailureMessage", isShowingFailureMessage);
		savedInstanceState.putBoolean("optionsActivityStarted", optionsActivityStarted);
		savedInstanceState.putBoolean("playAudio", playAudio);
		savedInstanceState.putBoolean("chkhighqualityvideo", chkhighqualityvideo);
		savedInstanceState.putString("localnetworkSettings", localnetworkSettings);
		savedInstanceState.putBoolean("isLocalNetwork", isLocalNetwork);
		savedInstanceState.putString("imageLiveCameraURL", imageLiveCameraURL);
		savedInstanceState.putString("imageLiveLocalURL", imageLiveLocalURL);
		savedInstanceState.putBoolean("paused", paused);
		savedInstanceState.putBoolean("end", end);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		downloadStartCount = savedInstanceState.getLong("downloadStartCount");
		downloadEndCount = savedInstanceState.getLong("downloadEndCount");
		isProgressShowing = savedInstanceState.getBoolean("isProgressShowing");
		sleepIntervalMinTime = savedInstanceState.getInt("sleepIntervalMinTime");
		intervalAdjustment = savedInstanceState.getInt("intervalAdjustment");
		sleepInterval = savedInstanceState.getInt("sleepInterval");
		startDownloading = savedInstanceState.getBoolean("startDownloading");

		isFirstImageLiveReceived = savedInstanceState.getBoolean("isFirstImageLiveReceived");
		isFirstImageLocalReceived = savedInstanceState.getBoolean("isFirstImageLocalReceived");
		isFirstImageLiveEnded = savedInstanceState.getBoolean("isFirstImageLiveEnded");
		isFirstImageLocalEnded = savedInstanceState.getBoolean("isFirstImageLocalEnded");
		successiveFailureCount = savedInstanceState.getInt("successiveFailureCount");
		isShowingFailureMessage = savedInstanceState.getBoolean("isShowingFailureMessage");
		optionsActivityStarted = savedInstanceState.getBoolean("optionsActivityStarted");
		playAudio = savedInstanceState.getBoolean("playAudio");
		chkhighqualityvideo = savedInstanceState.getBoolean("chkhighqualityvideo");
		localnetworkSettings = savedInstanceState.getString("localnetworkSettings");
		isLocalNetwork = savedInstanceState.getBoolean("isLocalNetwork");
		imageLiveCameraURL = savedInstanceState.getString("imageLiveCameraURL");
		imageLiveLocalURL = savedInstanceState.getString("imageLiveLocalURL");
		paused = savedInstanceState.getBoolean("paused");
		end = savedInstanceState.getBoolean("end");

	}

	public void SetImageAttributesAndLoadImage()
	{
		try
		{
			isFirstImageLiveReceived = false;
			isFirstImageLocalReceived = false;
			isFirstImageLiveEnded = false;
			isFirstImageLocalEnded = false;

			ivMediaPlayer.setVisibility(View.GONE); // hide the media player
													// play icon when animation
													// ends

			// showProgressView();

			readSetPreferences();

			startDownloading = false;
			this.paused = false;
			this.end = false;
			this.isShowingFailureMessage = false;

			for (int i = 0; i < taskList.size(); i++)
			{
				try
				{
					if (enableLogs) Log.e(TAG, "canceling task # " + i);
					taskList.get(i).cancel(true);
				}
				catch (Exception e)
				{
					if (enableLogs) Log
							.e(TAG, e.toString() + "-::::-" + Log.getStackTraceString(e));
				}
			}
			// LoadImageFromCache();
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getMessage(), e);
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
		}
	}

	private class browseImages extends AsyncTask<String, String, String>
	{

		@Override
		protected String doInBackground(String... params)
		{
			// TODO Auto-generated method stub
			int LiveTaskID = 0;
			while (!end) // keep on sending requests until the activity ends
			{
				Log.d("Umar", "MainLoop");
				try
				{
					// wait for starting
					try
					{
						while (!startDownloading) // if downloading has not
													// started, keep on waiting
													// until it starts
						{
							if (enableLogs) Log.i(TAG, "going to sleep for half second.");
							;
							Thread.sleep(500);
						}
					}
					catch (Exception e)
					{
						if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
					}

					Log.d("Umar", "Ivideo->browseimage->paused=" + paused);
					if (!paused) // if application is paused, do not send the
									// requests. Rather wait for the play
									// command
					{
						downloadStartCount++;
						DownloadImage tasklive = new DownloadImage();

						if (LiveTaskID >= 100) LiveTaskID = 1;

						tasklive.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
								new String[] { getImageUrlToPost() });

						taskList.add(tasklive); // add the newly created task to
												// array so that we can remove
												// if that has not been executed

						if (downloadStartCount - downloadEndCount > 9)
						{
							sleepInterval += intervalAdjustment;
						}
						else if (sleepInterval >= sleepIntervalMinTime)
						{
							sleepInterval -= intervalAdjustment;
						}
					}
					// else
					// {
					// return null;
					// }
					//

				}
				catch (RejectedExecutionException ree)
				{
					Log.e(TAG, ree.toString() + "-::REE::-" + Log.getStackTraceString(ree));

					// sleepInterval += 100;
				}
				catch (OutOfMemoryError e)
				{
					Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
				}
				catch (Exception ex)
				{
					downloadStartCount--;
					Log.e(TAG, ex.toString() + "-::::-" + Log.getStackTraceString(ex));
					if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ex);
				}
				try
				{
					Thread.currentThread();
					Thread.sleep(sleepInterval, 50);
					Log.d("Umar", "sleepInterval" + sleepInterval);
				}
				catch (Exception e)
				{
					if (enableLogs) Log
							.e(TAG, e.toString() + "-::::-" + Log.getStackTraceString(e));
				}
			}

			return null;

		}

		@Override
		protected void onPostExecute(String s)
		{
			Log.d("Umar", "onPostExecute() called!");
			Log.d("Umar", "end=" + end);
			Log.d("Umar", "paused=" + paused);
			Log.d("Umar", "startDownloading=" + startDownloading);

		}

	}

	private String getImageUrlToPost()
	{
		if (localnetworkSettings.equals("1")) return imageLiveLocalURL;
		else if (localnetworkSettings.equals("2")) return imageLiveCameraURL;
		else if (isLocalNetwork) return imageLiveLocalURL;
		else return imageLiveCameraURL;
	}

	@Override
	public void onResume()
	{
		try
		{
			super.onResume();
			if (enableLogs) Log.i(TAG, "onResume called");
			if (optionsActivityStarted)
			{
				optionsActivityStarted = false;

				if (enableLogs) Log.i(TAG, "onResume in block executed");

				showProgressView();

				readSetPreferences();

				startDownloading = false;
				this.paused = false;
				this.end = false;
				this.isShowingFailureMessage = false;

				for (int i = 0; i < taskList.size(); i++)
				{
					try
					{
						if (enableLogs) Log.e(TAG, "canceling task # " + i);
						taskList.get(i).cancel(true);
					}
					catch (Exception e)
					{
						if (enableLogs) Log.e(TAG,
								e.toString() + "-::::-" + Log.getStackTraceString(e));
					}
				}
				latestStartImageTime = SystemClock.uptimeMillis();

				if (imageThread.getStatus() != AsyncTask.Status.RUNNING)
				{
					imageThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
				}
				else if (imageThread.getStatus() == AsyncTask.Status.FINISHED)
				{
					imageThread = new browseImages();
					imageThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
				}
			}
			if (enableLogs) Log.i(TAG, "onResume ended");
		}
		catch (OutOfMemoryError e)
		{
			if (enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
		}
		catch (Exception e)
		{
			if (enableLogs) Log.e(TAG, e.toString());
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
		}
	}

	// Hide progress view
	void hideProgressView()
	{
		iView.findViewById(R.id.ivprogressspinner).setVisibility(View.GONE);
		isProgressShowing = false;
		isProgressShowing = false;
	}

	void showProgressView()
	{
		((ProgressView) iView.findViewById(R.id.ivprogressspinner)).CanvasColor = Color.TRANSPARENT;
		iView.findViewById(R.id.ivprogressspinner).setVisibility(View.VISIBLE);
		isProgressShowing = true;
	}

	// When activity gets focused again
	@Override
	public void onRestart()
	{
		try
		{
			super.onRestart();
			if (enableLogs) Log.i(TAG, "onRestart called");
			if (optionsActivityStarted)
			{

				optionsActivityStarted = false;
				this.paused = false;
				this.end = false;

				showProgressView();

				this.isShowingFailureMessage = false;
				if (enableLogs) Log.i(TAG, "onRestart in block executed");
				readSetPreferences();

				startDownloading = false;
				for (int i = 0; i < taskList.size(); i++)
				{
					try
					{
						if (enableLogs) Log.e(TAG, "canceling task # " + i);
						taskList.get(i).cancel(true);
					}
					catch (Exception e)
					{
						if (enableLogs) Log.e(TAG,
								e.toString() + "-::::-" + Log.getStackTraceString(e));
					}
				}
				latestStartImageTime = SystemClock.uptimeMillis();

				StartResumeDownloading();
			}
		}
		catch (OutOfMemoryError e)
		{
			if (enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
		}
		catch (Exception e)
		{
			if (enableLogs) Log.e(TAG, e.toString());
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
		}
	}

	// take care of the steps that are required to start downloading
	public void StartResumeDownloading()
	{
		try
		{
			startDownloading = false;

			isLocalNetwork = false;
			latestStartImageTime = SystemClock.uptimeMillis();
			if (localnetworkSettings.equalsIgnoreCase("1"))
			{
				DownloadImage task = new DownloadImage();
				isLocalNetwork = true;
				task.isLocalNetworkRequest = true;
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
						new String[] { imageLiveLocalURL });

			}
			else if (localnetworkSettings.equalsIgnoreCase("2"))
			{
				DownloadImage task = new DownloadImage();
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
						new String[] { imageLiveCameraURL });
			}
			else
			// autodetect
			{
				// Local Network Request
				DownloadImage task1 = new DownloadImage();
				task1.isLocalNetworkRequest = true;
				task1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
						new String[] { imageLiveLocalURL });

				// Live Camera Request
				DownloadImage task = new DownloadImage();
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
						new String[] { imageLiveCameraURL });

			}

			end = false;

			if (imageThread.getStatus() == AsyncTask.Status.FINISHED)
			{
				imageThread = new browseImages();
				imageThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
			}
			else if (imageThread.getStatus() != AsyncTask.Status.RUNNING)
			{
				imageThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
			}

			LoadImageFromCache();
		}
		catch (OutOfMemoryError e)
		{
			if (enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
		}
		catch (Exception e)
		{
			if (enableLogs) Log.e(TAG, e.toString());
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
		}
	}

	// when activity loses focus. we need to end this activity
	@Override
	public void onPause()
	{
		try
		{
			super.onPause();
			if (enableLogs) Log.i(TAG, "onPause called");
			isFirstImageLiveReceived = false;
			isFirstImageLocalReceived = false;
			isFirstImageLiveEnded = false;
			isFirstImageLocalEnded = false;
			if (!optionsActivityStarted)
			{
				this.paused = true;
				this.end = true;
				this.onDestroy();
				if (enableLogs) Log.i(TAG, "onPause in block executed");
				System.runFinalizersOnExit(true);
				// super.finish();
				// this.finish();

				if (enableLogs) Log
						.i(TAG,
								"goint to end the activity......................................................................................");
			}
		}
		catch (Exception ex)
		{
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ex);
		}
	}

	// When activity loses focus and some other activity gets activated. We need
	// to end this activity

	@Override
	public void onStart()
	{
		super.onStart();
		if (Constants.isAppTrackingEnabled)
		{
			EasyTracker.getInstance().activityStart(this); // Add this method.
			if (Constants.isAppTrackingEnabled) BugSenseHandler.startSession(this);
		}
	}

	@Override
	public void onStop()
	{
		try
		{
			super.onStop();
			if (enableLogs) Log.i(TAG, "onStop called");
			isFirstImageLiveReceived = false;
			isFirstImageLocalReceived = false;
			isFirstImageLiveEnded = false;
			isFirstImageLocalEnded = false;
			if (!optionsActivityStarted)
			{
				this.paused = true;
				this.end = true; // this will end the thread as well
				if (enableLogs) Log.i(TAG, "onStop in block executed");
				this.onDestroy();
				// android.os.Process.killProcess(android.os.Process.myPid());
				System.runFinalizersOnExit(true);
				// System.exit(0);
				// super.finish();
				// this.finish();
			}
		}
		catch (Exception ex)
		{
		}

		if (Constants.isAppTrackingEnabled)
		{
			EasyTracker.getInstance().activityStop(this);
			if (Constants.isAppTrackingEnabled) BugSenseHandler.closeSession(this);
		}
	}

	// when screen gets rotated
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		try
		{
			Log.i("sajjadpp", "onConfigurationChanged called");

			super.onConfigurationChanged(newConfig);

			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			int orientation = newConfig.orientation;
			if (orientation == Configuration.ORIENTATION_PORTRAIT)
			{
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
						WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
				landscape = false;
				this.getActionBar().show();
			}
			else
			{
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
						WindowManager.LayoutParams.FLAG_FULLSCREEN);
				landscape = true;

				if (!paused && !end && !isProgressShowing) this.getActionBar().hide();
				else this.getActionBar().show();
			}

			this.invalidateOptionsMenu();

		}
		catch (Exception e)
		{
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
		}
	}

	// resize the activity if screen gets rotated
	public void resize(int imageHieght, int imageWidth)
	{
		int w = landscape ? screen_height : screen_width;
		int h = landscape ? screen_width : screen_height;

		h -= actionBarHeight;

		// If we have the media, calculate best scaling inside bounds.
		if (imageWidth > 0 && imageHieght > 0)
		{
			final float max_w = w;
			final float max_h = h;
			float temp_w = imageWidth;
			float temp_h = imageHieght;
			float factor = max_w / temp_w;
			temp_w *= factor;
			temp_h *= factor;

			// If we went above the height limit, scale down.
			if (temp_h > max_h)
			{
				factor = max_h / temp_h;
				temp_w *= factor;
				temp_h *= factor;
			}

			w = (int) temp_w;
			h = (int) temp_h;
		}
		media_height = h;
		media_width = w;
		if (enableLogs) Log.i(TAG, "resize method called: " + w + ":" + h);
	}

	// make sure that the ip is reachable or not
	// this class needs some more improvements and coding --
	private class taskCheckReachableIP extends AsyncTask<String, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(String... IPs)
		{
			for (String ip : IPs)
			{
				try
				{
					return InetAddress.getByName("GOOGLE.COM").isReachable(1000 * 5);
				}
				catch (OutOfMemoryError e)
				{
					if (enableLogs) Log.e(TAG,
							e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
				}
				catch (Exception e)
				{
					if (enableLogs) Log.e(TAG, "Exception: " + e.toString());
					if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
				}
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result)
		{ // taskCheckReachableIP
			try
			{
				// if(pdLoading.isShowing())pdLoading.dismiss();
				adLocalNetwork = UIUtils.GetAlertDialog(IVideoViewActivity.this,
						"Stream Not Found",
						"Camera stream not found. In settings, try turning on/off local network.",
						new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						});

				adLocalNetwork.show();
				paused = true;

			}
			catch (OutOfMemoryError e)
			{
				if (enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
			}
			catch (Exception e)
			{
				if (enableLogs) Log.e(TAG, e.toString());
				if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
			}
		}
	}

	// download the image from the camera
	private class DownloadImage extends AsyncTask<String, Void, Drawable>
	{
		private long myStartImageTime;
		private boolean isLocalNetworkRequest = false;

		@Override
		protected Drawable doInBackground(String... urls)
		{
			Drawable response = null;
			for (String url1 : urls)
			{
				try
				{
					myStartImageTime = SystemClock.uptimeMillis();

					if (camera.getUseCredentials())
					{
						response = Commons.getDrawablefromUrlAuthenticated1(url1,
								camera.getCameraUserName(), camera.getCameraPassword(),
								camera.cookies, 15000);

					}
					else
					{
						URL url = new URL(url1);
						response = Commons.DownlaodDrawableSync(url, 15000);
					}
					if (response != null) successiveFailureCount = 0;
				}
				catch (OutOfMemoryError e)
				{
					if (enableLogs) Log.e(TAG,
							e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
					successiveFailureCount++;
					return null;
				}
				catch (Exception e)
				{
					if (enableLogs) Log.e(TAG, "Exception: " + e.toString() + "\r\n" + "ImageURl=["
							+ url1 + "]");
					if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
					successiveFailureCount++;
				}
			}
			return response;
		}

		@Override
		protected void onPostExecute(Drawable result)
		{// DownloadImage Live
			try
			{
				downloadEndCount++;
				try
				{
					if (isLocalNetworkRequest) isFirstImageLocalEnded = true;
					else isFirstImageLiveEnded = true;
				}
				catch (Exception ex)
				{
				}
				if (result != null && result.getIntrinsicWidth() > 0
						&& result.getIntrinsicHeight() > 0
						&& myStartImageTime >= latestStartImageTime && !paused && !end)
				{
					if (isLocalNetworkRequest) isFirstImageLocalReceived = true;
					else isFirstImageLiveReceived = true;
					if (isLocalNetworkRequest && localnetworkSettings.equalsIgnoreCase("0")) isLocalNetwork = true;

					latestStartImageTime = myStartImageTime;

					if (ivMediaPlayer.getVisibility() != View.VISIBLE
							&& IVideoViewActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) IVideoViewActivity.this
							.getActionBar().hide();

					imgCam.setImageDrawable(result);

					if (enableLogs) Log.i(TAG, "image loaded in ivideo");
					txtframerate.setText(String
							.format("[F/s:%.2f]", (1000 / (float) sleepInterval)));

					hideProgressView();

				}
				// do not show message on local network failure request.
				else if (((!isFirstImageLocalEnded && !isFirstImageLiveEnded
						&& !isFirstImageLocalReceived && !isFirstImageLiveReceived && localnetworkSettings
							.equalsIgnoreCase("0")) // loclal task ended. Now
													// this is live image
													// request
				|| successiveFailureCount > 10

				// ( ( !isFirstImageLocalReceived &&
				// localnetworkSettings.equalsIgnoreCase("1") )
				// || (!isFirstImageLiveReceived &&
				// localnetworkSettings.equalsIgnoreCase("2") )
				// || (isFirstImageLocalEnded && !isLocalNetworkRequest ) //
				// loclal task ended. Now this is live image request
				// || (isFirstImageLiveEnded && isLocalNetworkRequest ) // Image
				// Live task ended. Now this is local image request
				// || successiveFailureCount > 10
				//
				)
						&& !isShowingFailureMessage
						&& myStartImageTime >= latestStartImageTime
						&& !paused && !end) // end endif condition
				{
					isShowingFailureMessage = true;
					hideProgressView();
					UIUtils.GetAlertDialog(IVideoViewActivity.this, "Unable to connect",
							"Check camera and try again.", new DialogInterface.OnClickListener(){

								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									try
									{
										// IVideoViewActivity.this.onStop();
										IVideoViewActivity.this.getActionBar().show();
										paused = true;
										// end = true; // do not finish activity
										// but
										isShowingFailureMessage = false;
										dialog.dismiss();
										hideProgressView();
									}
									catch (Exception e)
									{
										if (Constants.isAppTrackingEnabled) BugSenseHandler
												.sendException(e);
									}
								}
							}).show();
				}
				else
				{
					if (enableLogs) Log.i(TAG, "downloaded image discarded. ");
				}
			}
			catch (OutOfMemoryError e)
			{
				if (enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
			}
			catch (Exception e)
			{
				if (enableLogs) Log.e(TAG, e.toString());
				if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
			}
			try
			{
				startDownloading = true;
			}
			catch (Exception ex)
			{
			}
			try
			{
				taskList.remove(this);
			}
			catch (Exception e)
			{
				if (enableLogs) Log.e(TAG, e.toString() + "-::::-" + Log.getStackTraceString(e));
			}
		}
	}

	@Override
	public void onSlideMenuItemClick(int itemId)
	{

		for (Camera caml : AppData.cameraList)
		{
			if (caml.getCameraID() == itemId && caml.getCameraID() != camera.getCameraID())
			{
				IVideoViewActivity.StartPlayingVIdeo(IVideoViewActivity.this, caml);
				return;
			}
		}
	}
}