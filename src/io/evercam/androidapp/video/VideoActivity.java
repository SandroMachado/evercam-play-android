package io.evercam.androidapp.video;

import io.evercam.Camera;
import io.evercam.EvercamException;
import io.evercam.androidapp.AddEditCameraActivity;
import io.evercam.androidapp.EvercamPlayApplication;
import io.evercam.androidapp.FeedbackActivity;
import io.evercam.androidapp.LocalStorageActivity;
import io.evercam.androidapp.ParentActivity;
import io.evercam.androidapp.ViewCameraActivity;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.custom.ProgressView;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.CameraStatus;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.feedback.FirebaseHelper;
import io.evercam.androidapp.feedback.StreamFeedbackItem;
import io.evercam.androidapp.tasks.CaptureSnapshotTask;
import io.evercam.androidapp.tasks.DeleteCameraTask;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.EnumConstants.DeleteType;
import io.evercam.androidapp.utils.EvercamFile;
import io.evercam.androidapp.utils.PrefsManager;
import io.evercam.androidapp.video.SnapshotManager.FileType;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.apache.http.cookie.Cookie;
import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaList;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.OnClickWrapper;
import com.logentries.android.AndroidLogger;

import io.evercam.androidapp.R;

public class VideoActivity extends ParentActivity implements SurfaceHolder.Callback,IVideoPlayer
{
	public static EvercamCamera evercamCamera;

	private final static String TAG = "evercamplay-VideoActivity";

	private static List<MediaURL> mediaUrls = null;
	private static int mrlIndex = -1;
	private String mrlPlaying = null;
	private boolean showImagesVideo = false;

	// display surface
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private ProgressView progressView = null;
	private TextView offlineTextView;
	private TextView timeCountTextView;

	// media player
	private LibVLC libvlc;
	private int mVideoWidth;
	private int mVideoHeight;
	private final static int videoSizeChanged = -1;

	// Screen view change variables
	private int screen_width, screen_height;
	private int media_width = 0, media_height = 0;
	private boolean landscape;

	private RelativeLayout imageViewLayout;
	private ImageView imageView;
	private ImageView mediaPlayerView;
	private ImageView snapshotMenuView;

	private long downloadStartCount = 0;
	private long downloadEndCount = 0;
	private BrowseImages imageThread;
	private boolean isProgressShowing = true;
	static boolean enableLogs = true;

	// image tasks and thread variables
	private int sleepIntervalMinTime = 200; // interval between two requests of
											// images
	private int intervalAdjustment = 10; // how much milli seconds to increment
										// or decrement on image failure or
										// success
	private int sleepInterval = sleepIntervalMinTime + 290; // starting image
															// interval
	private boolean startDownloading = false; // start making requests soon
												// after the image is received
												// first time. Until first image
												// is not received, do not make
												// requests
	private static long latestStartImageTime = 0; // time of the latest request
													// that has been made
	private boolean isFirstImageLiveReceived = false;
	private boolean isFirstImageLocalReceived = false;
	private boolean isFirstImageLiveEnded = false;
	private boolean isFirstImageLocalEnded = false;
	private int successiveFailureCount = 0; // how much successive image
											// requests have failed
	private Boolean isShowingFailureMessage = false;
	private Boolean optionsActivityStarted = false; // whether preference
													// activity is showing or
													// not

	public static String startingCameraID;
	private int defaultCameraIndex;
	// preferences options
	private String localnetworkSettings = "0";
	private boolean isLocalNetwork = false;

	private static String imageLiveCameraURL = "";
	private static String imageLiveLocalURL = "";

	private boolean paused = false;
	private boolean isPlayingJpg = false;// If true, stop trying video
												// URL for reconnecting.
	private boolean isJpgSuccessful = false; //Whether or not the JPG view ever
													//got successfully played

	private Animation fadeInAnimation = null; // animation that shows the
												// playing icon
	// of media player fading and
	// disappearing

	private boolean end = false; // whether to end this activity or not

	private Handler handler = new MyHandler(this);

	private boolean editStarted = false;
	private boolean feedbackStarted = false;

	private Handler timerHandler = new Handler();
	private Thread timerThread;
	private Runnable timerRunnable;
	
	private TimeCounter timeCounter;
	
	private FirebaseHelper firebaseHelper;
	private Date startTime;
	private AndroidLogger logger;
	private String username = "";

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		try
		{
			super.onCreate(savedInstanceState);

			if (Constants.isAppTrackingEnabled)
			{
				BugSenseHandler.initAndStartSession(this, Constants.bugsense_ApiKey);
			}

			EvercamPlayApplication.sendScreenAnalytics(this, getString(R.string.screen_video));

			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			
			firebaseHelper = new FirebaseHelper(this);

			launchSleepTimer();

			setDisplayOriention();

			if (this.getActionBar() != null)
			{
				this.getActionBar().setDisplayHomeAsUpEnabled(true);
				this.getActionBar().setTitle("");
				this.getActionBar().setIcon(R.drawable.icon_50x50);
			}

			setContentView(R.layout.video_activity_layout);

			initialPageElements();

			if(AppData.defaultUser != null)
			{
				username = AppData.defaultUser.getUsername();
			}
			logger = AndroidLogger.getLogger(getApplicationContext(), Constants.LOGENTRIES_TOKEN, false);
			
			startPlay();
		}
		catch (OutOfMemoryError e)
		{
			Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// Here actually no matter what result is returned, all restart video
		// play, but keep the verbose code for future extension.
		if (requestCode == Constants.REQUEST_CODE_PATCH_CAMERA)
		{
			// Restart video playing no matter the patch is success or not.
			if (resultCode == Constants.RESULT_TRUE)
			{
				startPlay();
			}
			else
			{
				startPlay();
			}
		}
		else
		// If back from view camera or feedback
		{
			startPlay();
		}
	}

	@Override
	public void onResume()
	{
		try
		{
			super.onResume();
			this.paused = false;
			editStarted = false;
			feedbackStarted = false;

			if (optionsActivityStarted)
			{
				optionsActivityStarted = false;

				showProgressView();

				readSetPreferences();

				startDownloading = false;
				this.paused = false;
				this.end = false;
				this.isShowingFailureMessage = false;

				latestStartImageTime = SystemClock.uptimeMillis();

				if (imageThread == null) 
				{
					// ignore if image thread is null
				}
				else if (imageThread.getStatus() != AsyncTask.Status.RUNNING)
				{
					imageThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
				}
				else if (imageThread.getStatus() == AsyncTask.Status.FINISHED)
				{
					imageThread = new BrowseImages();
					imageThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
				}
			}
		}
		catch (OutOfMemoryError e)
		{
			Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString());
			if (Constants.isAppTrackingEnabled)
			{
				BugSenseHandler.sendException(e);
			}
		}
	}

	// When activity gets focused again
	@Override
	public void onRestart()
	{
		try
		{
			super.onRestart();
			paused = false;
			end = false;
			editStarted = false;
			feedbackStarted = false;

			if (optionsActivityStarted)
			{
				mrlPlaying = null;
				setCameraForPlaying(this, evercamCamera);

				createPlayer(getCurrentMRL());
			}
		}
		catch (OutOfMemoryError e)
		{
			Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();

		if (!optionsActivityStarted)
		{
			this.paused = true;
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.startSession(this);
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();
		releasePlayer();
		end = true;
		if (!optionsActivityStarted)
		{
			if (imageThread != null)
			{
				this.paused = true;
			}
			// Do not finish if user get into edit camera screen.
			if (!editStarted && !feedbackStarted)
			{
				this.finish();
			}
		}
		
		if(timeCounter != null)
		{
			timeCounter.stop();
			timeCounter = null;
		}

		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.closeSession(this);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event)
	{
		// TODO: Reset the timer to keep screen awake
		launchSleepTimer();
		return super.dispatchTouchEvent(event);
	}

	private void launchSleepTimer()
	{
		try
		{
			if (timerThread != null)
			{
				timerThread = null;
				timerHandler.removeCallbacks(timerRunnable);
			}

			final int sleepTime = getSleepTime();
			if (sleepTime != 0)
			{
				timerRunnable = new Runnable(){
					@Override
					public void run()
					{
						VideoActivity.this.getWindow().clearFlags(
								WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
					}
				};
				timerThread = new Thread(){
					@Override
					public void run()
					{
						timerHandler.postDelayed(timerRunnable, sleepTime);
					}
				};
				timerThread.start();
			}
		}
		catch (Exception e)
		{
			// Catch this exception and send by Google Analytics
			// This should not influence user using the app
			EvercamPlayApplication.sendCaughtException(this, e);
		}
	}

	private int getSleepTime()
	{
		final String VALUE_NEVER = "0";

		String valueString = PrefsManager.getSleepTimeValue(this);
		if (!valueString.equals(VALUE_NEVER))
		{
			return Integer.valueOf(valueString) * 1000;
		}
		else
		{
			return 0;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.video_menu, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		MenuItem editItem = menu.findItem(R.id.video_menu_edit_camera);
		MenuItem viewItem = menu.findItem(R.id.video_menu_view_camera);
		MenuItem localStorageItem = menu.findItem(R.id.video_menu_local_storage);

		if (evercamCamera != null)
		{
			if (evercamCamera.canEdit())
			{
				editItem.setVisible(true);
				viewItem.setVisible(false);
			}
			else
			{
				editItem.setVisible(false);
				viewItem.setVisible(true);
			}

			if (evercamCamera.isHikvision() && evercamCamera.hasCredentials())
			{
				localStorageItem.setVisible(true);
			}
			else
			{
				localStorageItem.setVisible(false);
			}
		}
		else
		{
			Log.e(TAG, "EvercamCamera is null");
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		try
		{
			if (itemId == R.id.video_menu_delete_camera)
			{
				CustomedDialog.getConfirmRemoveDialog(VideoActivity.this,
						new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface warningDialog, int which)
							{
								if (evercamCamera.canDelete())
								{
									new DeleteCameraTask(evercamCamera.getCameraId(),
											VideoActivity.this, DeleteType.DELETE_OWNED)
											.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
								}
								else
								{
									new DeleteCameraTask(evercamCamera.getCameraId(),
											VideoActivity.this, DeleteType.DELETE_SHARE)
											.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
								}
							}
						}, R.string.msg_confirm_remove_camera).show();
			}
			else if (itemId == R.id.video_menu_view_camera)
			{
				editStarted = true;
				Intent viewIntent = new Intent(VideoActivity.this, ViewCameraActivity.class);
				startActivityForResult(viewIntent, Constants.REQUEST_CODE_VIEW_CAMERA);
			}
			else if (itemId == R.id.video_menu_edit_camera)
			{
				editStarted = true;

				Intent editIntent = new Intent(VideoActivity.this, AddEditCameraActivity.class);
				editIntent.putExtra(Constants.KEY_IS_EDIT, true);
				startActivityForResult(editIntent, Constants.REQUEST_CODE_PATCH_CAMERA);
			}
			else if (itemId == R.id.video_menu_local_storage)
			{
				startActivity(new Intent(VideoActivity.this, LocalStorageActivity.class));
			}
			else if (itemId == android.R.id.home)
			{
				finish();
			}
			else if (itemId == R.id.video_menu_feedback)
			{
				feedbackStarted = true;
				startActivityForResult(new Intent(VideoActivity.this, FeedbackActivity.class), Constants.REQUEST_CODE_FEEDBACK);
			}
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
		return true;
	}

	private void startPlay()
	{
		logger.info("User: " + username + " is viewing camera: " + startingCameraID);
		
		paused = false;
		end = false;
		loadImageFromCache(startingCameraID);

		checkNetworkStatus();

		loadCamerasToActionBar();

		readSetPreferences();
	}

	public static boolean startPlayingVideoForCamera(Activity activity, String cameraId)
	{
		startingCameraID = cameraId;
		Intent intent = new Intent(activity, VideoActivity.class);

		activity.startActivityForResult(intent, Constants.REQUEST_CODE_DELETE_CAMERA);

		return false;
	}

	private void setCameraForPlaying(Context context, EvercamCamera evercamCamera)
	{
		try
		{
			VideoActivity.evercamCamera = evercamCamera;

			// ***Setting Defaults
			readSetPreferences();
			showImagesVideo = false;

			downloadStartCount = 0;
			downloadEndCount = 0;
			isProgressShowing = false;

			startDownloading = false;
			latestStartImageTime = 0;
			isFirstImageLiveReceived = false;
			isFirstImageLocalReceived = false;
			isFirstImageLiveEnded = false;
			isFirstImageLocalEnded = false;
			successiveFailureCount = 0;
			isShowingFailureMessage = false;

			optionsActivityStarted = false;

			isLocalNetwork = false;

			mediaPlayerView.setVisibility(View.GONE);
			snapshotMenuView.setVisibility(View.GONE);

			paused = false;
			end = false;

			surfaceView.setVisibility(View.GONE);
			imageView.setVisibility(View.VISIBLE);
			showProgressView();

			loadImageFromCache(evercamCamera.getCameraId());

			if (!evercamCamera.getStatus().equals(CameraStatus.OFFLINE))
			{
				startDownloading = true;
			}

			showProgressView();

			//Disabled by Liuting
//			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
//			mrlPlaying = sharedPrefs.getString("pref_mrlplaying" + evercamCamera.getCameraId(),
//					null);

			mrlPlaying = evercamCamera.getExternalRtspUrl();
			
			mediaUrls = new ArrayList<MediaURL>();
			mrlIndex = -1;

			if (mrlPlaying != null)
			{
				addUrlIfValid(mrlPlaying, evercamCamera);
				mrlIndex = 0;
				mrlPlaying = null;
			}

		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
			BugSenseHandler.sendException(e);
			EvercamPlayApplication.sendEventAnalytics(VideoActivity.this, R.string.category_error,
					R.string.action_error_video, R.string.label_error_play_video);
			EvercamPlayApplication.sendCaughtException(this, e);
			CustomedDialog.showUnexpectedErrorDialog(VideoActivity.this);
		}
	}

	private void addUrlIfValid(String url, EvercamCamera cam)
	{
		if (url == null
				|| url.trim().length() < 10
				|| !(url.startsWith("http://") || url.startsWith("https://")
						|| url.startsWith("rtsp://") || url.startsWith("tcp://"))) return;

		String liveURLString = evercamCamera.getExternalRtspUrl();
		String localURLString = evercamCamera.getInternalRtspUrl();

		if (!localURLString.isEmpty() && !localnetworkSettings.equalsIgnoreCase("2"))
		{
			MediaURL localMRL = new MediaURL(localURLString, true);

			if (!mediaUrls.contains(localMRL))
			{
				mediaUrls.add(localMRL);
			}
		}

		if (!localnetworkSettings.equalsIgnoreCase("1"))
		{
			MediaURL liveMRL = new MediaURL(liveURLString, false);
			if (!mediaUrls.contains(liveMRL))
			{
				mediaUrls.add(liveMRL);
			}
		}
		mrlIndex = 0;
	}

	// Loads image from cache. First image gets loaded correctly and hence we
	// can start making requests concurrently as well
	public boolean loadImageFromCache(String cameraId)
	{
		try
		{
			imageView.setImageDrawable(null);

			File cacheFile = EvercamFile.getCacheFileRelative(this, cameraId);
			if (cacheFile.exists())
			{
				Drawable result = Drawable.createFromPath(cacheFile.getPath());
				if (result != null)
				{
					imageView.setImageDrawable(result);

					Log.d(TAG, "Loaded first image from Cache: " + media_width + ":" + media_height);
					return true;
				}
				else
				{
					Log.e(TAG, "No image saved with camera: " + cameraId);
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
			if (Constants.isAppTrackingEnabled)
			{
				BugSenseHandler.sendException(e);
			}
		}

		return false;
	}

	// Read preferences for playing options audio and Video(images)
	public void readSetPreferences()
	{
		try
		{
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			isLocalNetwork = false;
			String cameraId;
			if (evercamCamera != null)
			{
				cameraId = evercamCamera.getCameraId();
			}
			else
			{
				cameraId = "nocamera";
			}
			localnetworkSettings = sharedPrefs.getString("pref_enablocalnetwork" + cameraId, "0");
			if (localnetworkSettings.equalsIgnoreCase("1")) isLocalNetwork = true;
			else isLocalNetwork = false;
		}
		catch (OutOfMemoryError e)
		{
			Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
		}
		catch (Exception ex)
		{
			Log.e(TAG, ex.toString());
			if (Constants.isAppTrackingEnabled)
			{
				BugSenseHandler.sendException(ex);
			}
		}
	}

	private void startMediaPlayerAnimation()
	{
		if (fadeInAnimation != null)
		{
			fadeInAnimation.cancel();
			fadeInAnimation.reset();

			snapshotMenuView.clearAnimation();
			mediaPlayerView.clearAnimation();
		}

		fadeInAnimation = AnimationUtils.loadAnimation(VideoActivity.this, R.layout.fadein);

		fadeInAnimation.setAnimationListener(new Animation.AnimationListener(){
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

				if (!paused) 
				{
					mediaPlayerView.setVisibility(View.GONE);
					snapshotMenuView.setVisibility(View.GONE);
				}
				else 
				{
					mediaPlayerView.setVisibility(View.VISIBLE);
					snapshotMenuView.setVisibility(View.VISIBLE);
				}

				int orientation = VideoActivity.this.getResources().getConfiguration().orientation;
				if (!paused && orientation == Configuration.ORIENTATION_LANDSCAPE)
				{
					VideoActivity.this.getActionBar().hide();
				}
			}
		});

		mediaPlayerView.startAnimation(fadeInAnimation);
		snapshotMenuView.startAnimation(fadeInAnimation);
	}

	private boolean isCurrentMRLValid()
	{
		if (mrlIndex < 0 || mrlIndex >= mediaUrls.size() || mediaUrls.size() == 0)
		{
			return false;
		}
		return true;
	}

	private boolean isNextMRLValid()
	{
		if (mrlIndex + 1 >= mediaUrls.size() || mediaUrls.size() == 0) return false;
		return true;
	}

	private String getCurrentMRL()
	{
		if (isCurrentMRLValid()) return mediaUrls.get(mrlIndex).url;
		return null;
	}

	private String getNextMRL()
	{
		if (isNextMRLValid()) return mediaUrls.get(++mrlIndex).url;
		return null;
	}

	/*************
	 * Surface
	 *************/

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder)
	{
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceholder, int format, int width, int height)
	{
		if (libvlc != null) libvlc.attachSurface(surfaceHolder.getSurface(), this);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceholder)
	{
	}

	private void setSize(int width, int height)
	{
		mVideoWidth = width;
		mVideoHeight = height;
		if (mVideoWidth * mVideoHeight <= 1) return;

		// get screen size
		int w = getWindow().getDecorView().getWidth();
		int h = getWindow().getDecorView().getHeight();

		// getWindow().getDecorView() doesn't always take orientation into
		// account, we have to correct the values
		boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
		if (w > h && isPortrait || w < h && !isPortrait)
		{
			int i = w;
			w = h;
			h = i;
		}

		float videoAR = (float) mVideoWidth / (float) mVideoHeight;
		float screenAR = (float) w / (float) h;

		if (screenAR < videoAR) h = (int) (w / videoAR);
		else w = (int) (h * videoAR);

		// force surface buffer size
		surfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);

		// set display size
		LayoutParams lp = surfaceView.getLayoutParams();
		lp.width = w;
		lp.height = h;
		surfaceView.setLayoutParams(lp);
		surfaceView.invalidate();
	}

	@Override
	public void setSurfaceSize(int width, int height, int visible_width, int visible_height,
			int sar_num, int sar_den)
	{
		Message msg = Message.obtain(handler, videoSizeChanged, width, height);
		msg.sendToTarget();
	}

	/*************
	 * Player
	 *************/

	private void createPlayer(String media)
	{
		startTime = new Date();
		releasePlayer();
		try
		{
			if (media != null && media.length() > 0)
			{
				//Stop showing RTSP URL, show log for debugging instead
		//		showToast(getString(R.string.connecting) + media);
				Log.d(TAG, getString(R.string.connecting) + media);
				
				// Create a new media player
				libvlc = LibVLC.getInstance();
				libvlc.setSubtitlesEncoding("");
				
				//Liuting: Disabled hardware acceleration because it causes crash on the Church camera
			    libvlc.setHardwareAcceleration(LibVLC.HW_ACCELERATION_DISABLED);
				libvlc.setAout(LibVLC.AOUT_OPENSLES);
				libvlc.setTimeStretching(false);
				libvlc.setChroma("RV32");
				libvlc.setVerboseMode(true);
				LibVLC.restart(this);
				EventHandler.getInstance().addHandler(handler);
				surfaceHolder.setFormat(PixelFormat.RGBX_8888);
				surfaceHolder.setKeepScreenOn(true);
				MediaList list = libvlc.getMediaList();
				list.clear();
				list.add(new Media(libvlc, LibVLC.PathToURI(media)), false);
				libvlc.playIndex(0);
			}
			else
			{
				//If no RTSP URL exists, start JPG view straight away
				showImagesVideo = true;
				createNewImageThread();
			}
		}
		catch (Exception e)
		{
//			Toast.makeText(this, "Error connecting! " + media + " ::::: " + e.getMessage(),
//					Toast.LENGTH_LONG).show();
			Log.e(TAG, "Error connecting! " + media + " ::::: " + e.getMessage());
			EvercamPlayApplication.sendCaughtException(this, e);
		}
	}

	private void releasePlayer()
	{
		try
		{
			if (libvlc == null) return;
			EventHandler.getInstance().removeHandler(handler);
			libvlc.stop();
			libvlc.detachSurface();
			libvlc.closeAout();
			// libvlc.destroy(); //Disabled by Liuting
			// libvlc = null;//Disabled by Liuting

			mVideoWidth = 0;
			mVideoHeight = 0;
		}
		catch (Exception e)
		{
			EvercamPlayApplication.sendCaughtException(this, e);
			Log.e(TAG, e.getMessage());
		}
	}

	private void restartPlay(String media)
	{
		if (libvlc == null) return;

		try
		{
			libvlc.stop();

			if (media != null && media.length() > 0)
			{
				Log.d(TAG, getString(R.string.reconnecting) + media);
			}

			libvlc.getMediaList().clear();
			libvlc.playMRL(media);
		}
		catch (Exception e)
		{
			EvercamPlayApplication.sendCaughtException(this, e);
			if (!isPlayingJpg)
			{
				Log.e(TAG, "Error reconnecting! " + media + " ::::: " + e.getMessage());
			}
		}
	}

	private void pausePlayer()
	{
		if (libvlc == null) return;
		libvlc.pause();
	}

	private void stopPlayer()
	{
		if (libvlc == null) return;
		libvlc.stop();
	}

	private void playPlayer()
	{
		if (libvlc == null) return;
		libvlc.play();
	}

	public void setImageAttributesAndLoadImage()
	{
		try
		{
			isFirstImageLiveReceived = false;
			isFirstImageLocalReceived = false;
			isFirstImageLiveEnded = false;
			isFirstImageLocalEnded = false;

			mediaPlayerView.setVisibility(View.GONE);
			snapshotMenuView.setVisibility(View.GONE);

			readSetPreferences();

			startDownloading = false;
			this.paused = false;
			this.end = false;
			this.isShowingFailureMessage = false;
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getMessage(), e);
			EvercamPlayApplication.sendCaughtException(this, e);
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
		}
	}

	// when screen gets rotated
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		try
		{
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

			mVideoWidth = surfaceView.getWidth();
			mVideoHeight = surfaceView.getHeight() - this.getActionBar().getHeight();
			setSize(mVideoWidth, mVideoHeight);
		}
		catch (Exception e)
		{
			EvercamPlayApplication.sendCaughtException(this, e);
			if (Constants.isAppTrackingEnabled)
			{
				BugSenseHandler.sendException(e);
			}
		}
	}

	// resize the activity if screen gets rotated
	public void resize(int imageHieght, int imageWidth)
	{
		int w = landscape ? screen_height : screen_width;
		int h = landscape ? screen_width : screen_height;

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
	}

	private void showToast(String text)
	{
		Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.show();
	}

	private void showMediaFailureDialog()
	{
		CustomedDialog.getCanNotPlayDialog(this, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which)
			{

				VideoActivity.this.getActionBar().show();
				paused = true;
				isShowingFailureMessage = false;
				dialog.dismiss();
				hideProgressView();
			//	timeCounter.stop();
			}
		}).show();
		isShowingFailureMessage = true;
		showImagesVideo = false;
	}

	private void setDisplayOriention()
	{
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
	}

	private void checkNetworkStatus()
	{
		if (!Commons.isOnline(this))
		{
			CustomedDialog.getNoInternetDialog(this, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					paused = true;
					dialog.dismiss();
					hideProgressView();
				}
			}).show();
			return;
		}
	}

	private void initialPageElements()
	{
		imageViewLayout = (RelativeLayout) this.findViewById(R.id.camera_view_layout);
		imageView = (ImageView) this.findViewById(R.id.img_camera1);
		mediaPlayerView = (ImageView) this.findViewById(R.id.ivmediaplayer1);
		snapshotMenuView = (ImageView) this.findViewById(R.id.player_savesnapshot);

		surfaceView = (SurfaceView) findViewById(R.id.surface1);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);

		progressView = ((ProgressView) imageViewLayout.findViewById(R.id.ivprogressspinner1));

		progressView.setMinimumWidth(mediaPlayerView.getWidth());
		progressView.setMinimumHeight(mediaPlayerView.getHeight());
		progressView.canvasColor = Color.TRANSPARENT;

		isProgressShowing = true;
		progressView.setVisibility(View.VISIBLE);

		offlineTextView = (TextView) findViewById(R.id.offline_text_view);
		timeCountTextView = (TextView) findViewById(R.id.time_text_view);

		/** The click listener for pause/play button */
		mediaPlayerView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				if (end)
				{
					Toast.makeText(VideoActivity.this, R.string.msg_try_again, Toast.LENGTH_SHORT)
							.show();
					return;
				}
				if (isProgressShowing) return;
				if (paused) // video is currently paused. Now we need to
							// resume it.
				{
					timeCountTextView.setVisibility(View.VISIBLE);
					showProgressView();

					mediaPlayerView.setImageBitmap(null);
					mediaPlayerView.setVisibility(View.VISIBLE);
					snapshotMenuView.setVisibility(View.VISIBLE);
					mediaPlayerView.setImageResource(android.R.drawable.ic_media_pause);

					startMediaPlayerAnimation();

					//If playing url is not null, resume rtsp stream
					if(mrlPlaying != null)
					{
						restartPlay(mrlPlaying);
					}
					//Otherwise restart jpg view
					else
					{
						//Don't need to do anything because image thread is listening
					}
					paused = false;
				}
				else
				// video is currently playing. Now we need to pause video
				{
					timeCountTextView.setVisibility(View.GONE);
					mediaPlayerView.clearAnimation();
					snapshotMenuView.clearAnimation();
					if (fadeInAnimation != null && fadeInAnimation.hasStarted()
							&& !fadeInAnimation.hasEnded())
					{
						fadeInAnimation.cancel();
						fadeInAnimation.reset();
					}
					mediaPlayerView.setVisibility(View.VISIBLE);
					snapshotMenuView.setVisibility(View.VISIBLE);
					mediaPlayerView.setImageBitmap(null);
					mediaPlayerView.setImageResource(android.R.drawable.ic_media_play);

					stopPlayer();

					paused = true; // mark the images as paused. Do not stop
									// threads, but do not show the images
									// showing up
				}
			}
		});

		/**
		 * The click listener of camera live view layout, including both RTSP and JPG view
		*  Once clicked, if camera view is playing, show the pause menu, otherwise do nothing.
		 */
		imageViewLayout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v)
			{
				if (end)
				{
					Toast.makeText(VideoActivity.this, R.string.msg_try_again, Toast.LENGTH_SHORT)
							.show();
					return;
				}
				if (isProgressShowing) return;

				if (!paused && !end) // video is currently playing. Show pause button
				{
					if(mediaPlayerView.getVisibility() == View.VISIBLE)
					{
						mediaPlayerView.setVisibility(View.GONE);
						snapshotMenuView.setVisibility(View.GONE);
						mediaPlayerView.clearAnimation();
						snapshotMenuView.clearAnimation();
						fadeInAnimation.reset();
					}
					else
					{
						VideoActivity.this.getActionBar().show();
						mediaPlayerView.setImageResource(android.R.drawable.ic_media_pause);

						mediaPlayerView.setVisibility(View.VISIBLE);
						snapshotMenuView.setVisibility(View.VISIBLE);

						startMediaPlayerAnimation();
					}
				}
			}
		});
		
		snapshotMenuView.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				//Hide pause/snapshot menu if the live view is not paused
				if(!paused)
				{
					mediaPlayerView.setVisibility(View.GONE);
					snapshotMenuView.setVisibility(View.GONE);
					mediaPlayerView.clearAnimation();
					snapshotMenuView.clearAnimation();
					fadeInAnimation.reset();
				}
				
				if(imageView.getVisibility() == View.VISIBLE)
				{
					final Drawable drawable = imageView.getDrawable();
					if (drawable != null)
					{
						CustomedDialog.getConfirmSnapshotDialog(VideoActivity.this, drawable,
								new DialogInterface.OnClickListener() 
						{
							@Override
							public void onClick(DialogInterface dialog, int which) 
							{
								new CaptureSnapshotTask(VideoActivity.this, evercamCamera.getCameraId(), drawable).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
							}
						}).show();
					}
				}
				else if (surfaceView.getVisibility() == View.VISIBLE)
				{
					CustomToast.showInBottom(VideoActivity.this, R.string.msg_taking_snapshot);
					new Handler().postDelayed(new Runnable()
					{
						@Override
						public void run() 
						{
							try 
							{
								String path = SnapshotManager.createFilePath(evercamCamera.getCameraId(), FileType.PNG);
								if (libvlc.takeSnapShot(path, mVideoWidth, mVideoHeight)) 
								{
									SnapshotManager.updateGallery(path, VideoActivity.this);

									CustomToast.showSnapshotSaved(VideoActivity.this);	
								} 
								else 
								{
									CustomToast.showInBottom(VideoActivity.this, R.string.msg_snapshot_saved_failed);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}, 200);
				}
			}
		});

		// Get the size of the device, will be our maximum.
		Display display = getWindowManager().getDefaultDisplay();
		screen_width = display.getWidth();
		screen_height = display.getHeight();
	}

	// Hide progress view
	void hideProgressView()
	{
		imageViewLayout.findViewById(R.id.ivprogressspinner1).setVisibility(View.GONE);
		isProgressShowing = false;
	}

	void showProgressView()
	{
		progressView.canvasColor = Color.TRANSPARENT;
		progressView.setVisibility(View.VISIBLE);
		isProgressShowing = true;
	}

	private void createNewImageThread()
	{
		imageThread = new BrowseImages();
		imageThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	private void startTimeCounter()
	{
		if(timeCounter == null)
		{
			String timezone = "Etc/UTC";
			if(evercamCamera!= null)
			{
				timezone = evercamCamera.getTimezone();
			}
			timeCounter = new TimeCounter(this, timezone);
		}
		if(!timeCounter.isStarted())
		{
			timeCounter.start();
		}
	}

	public class BrowseImages extends AsyncTask<String, String, String>
	{
		@Override
		protected String doInBackground(String... params)
		{
			while (!end && !isCancelled() && showImagesVideo)
			{
				try
				{
					// wait for starting
					while (!startDownloading)
					{
						Thread.sleep(500);
					}

					if (!paused) // if application is paused, do not send the
									// requests. Rather wait for the play
									// command
					{
						imageLiveCameraURL = evercamCamera.getExternalSnapshotUrl();
						imageLiveLocalURL = evercamCamera.getInternalSnapshotUrl();

						if (AbandonedJpgUrl.contains(imageLiveCameraURL))
						{
							imageLiveCameraURL = "";
						}

						if (AbandonedJpgUrl.contains(imageLiveLocalURL))
						{
							imageLiveLocalURL = "";
						}
						DownloadImage taskExternal = new DownloadImage();
						DownloadImage taskLocal = new DownloadImage();

						if (downloadStartCount - downloadEndCount < 9)
						{
							if(!imageLiveCameraURL.isEmpty())
							{
								taskExternal.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
										new String[] { imageLiveCameraURL});
							}
							if(!imageLiveLocalURL.isEmpty())
							{
								taskLocal.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
										new String[] { imageLiveLocalURL});
							}
							if(imageLiveCameraURL.isEmpty() && imageLiveLocalURL.isEmpty())
							{
								taskLocal.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
										new String[] {});
							}
						}

						if (downloadStartCount - downloadEndCount > 9 && sleepInterval < 2000)
						{
							sleepInterval += intervalAdjustment;
							Log.d(TAG, "Sleep interval adjusted to: " + sleepInterval);
						}
						else if (sleepInterval >= sleepIntervalMinTime)
						{
							sleepInterval -= intervalAdjustment;
							Log.d(TAG, "Sleep interval adjusted to: " + sleepInterval);
						}
					}
				}
				catch (RejectedExecutionException ree)
				{
					Log.e(TAG, ree.toString() + "-::REE::-" + Log.getStackTraceString(ree));

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
				}
				catch (Exception e)
				{
					EvercamPlayApplication.sendCaughtException(VideoActivity.this, e);
					Log.e(TAG, e.toString() + "-::::-" + Log.getStackTraceString(e));
				}
			}
			return null;
		}
	}

	/*************
	 * Events
	 *************/

	private class MyHandler extends Handler
	{
		private WeakReference<VideoActivity> videoActivity;

		public MyHandler(VideoActivity owner)
		{
			videoActivity = new WeakReference<VideoActivity>(owner);
		}

		@Override
		public void handleMessage(Message msg)
		{
			try
			{
				VideoActivity player = videoActivity.get();

				// SamplePlayer events
				if (msg.what == videoSizeChanged)
				{
					player.setSize(msg.arg1, msg.arg2);
					return;
				}

				// Libvlc events
				Bundle bundle = msg.getData();
				int event = bundle.getInt("event");

				switch (event)
				{
				case EventHandler.MediaPlayerEndReached:

					player.restartPlay(player.mrlPlaying);
					break;
				case EventHandler.MediaPlayerPlaying:

					isPlayingJpg = false;
					
					player.mrlPlaying = player.getCurrentMRL();
					
					//View gets played, show time count, and start buffering
					startTimeCounter();
					
					break;

				case EventHandler.MediaPlayerPaused:
					break;

				case EventHandler.MediaPlayerStopped:
					break;

				case EventHandler.MediaPlayerEncounteredError:

					if (evercamCamera != null)
					{
						player.loadImageFromCache(evercamCamera.getCameraId());
					}

					if (player.mrlPlaying == null && player.isNextMRLValid())
					{
						player.restartPlay(player.getNextMRL());
					}
					else if (player.mrlPlaying != null && !player.mrlPlaying.isEmpty())
					{
						player.restartPlay(player.mrlPlaying);
					}
					else
					{
						//Failed to play RTSP stream, send an event to Google Analytics
						Log.d(TAG, "Failed to play video stream");
						if(!player.isNextMRLValid())
						{
							EvercamPlayApplication.sendEventAnalytics(VideoActivity.this, R.string.category_streaming_rtsp,
									R.string.action_streaming_rtsp_failed, R.string.label_streaming_rtsp_failed);
							StreamFeedbackItem failedItem = new StreamFeedbackItem(VideoActivity.this, AppData.defaultUser.getUsername(), false);
							failedItem.setCameraId(evercamCamera.getCameraId());
							failedItem.setUrl(player.getCurrentMRL());
							failedItem.setType(StreamFeedbackItem.TYPE_RTSP);
							firebaseHelper.pushRtspItem(failedItem);
							logger.info(failedItem.toJson());
						}
						isPlayingJpg = true;
						player.showToast(videoActivity.get().getString(R.string.msg_switch_to_jpg));
						player.showImagesVideo = true;
						player.createNewImageThread();
					}

					break;

				case EventHandler.MediaPlayerVout:
					Log.v(TAG, "EventHandler.MediaPlayerVout");
					
					//Buffering finished and start to show the video
					player.surfaceView.setVisibility(View.VISIBLE);
					player.imageView.setVisibility(View.GONE);
					player.hideProgressView();
					
					//And send to Google Analytics
					//And send to Firebase
					EvercamPlayApplication.sendEventAnalytics(player, R.string.category_streaming_rtsp,
							R.string.action_streaming_rtsp_success, R.string.label_streaming_rtsp_success);
					
					StreamFeedbackItem successItem = new StreamFeedbackItem(VideoActivity.this, AppData.defaultUser.getUsername(), true);
					successItem.setCameraId(evercamCamera.getCameraId());
					successItem.setUrl(player.mrlPlaying);
					successItem.setType(StreamFeedbackItem.TYPE_RTSP);
					if(startTime != null)
					{
						long timeDifferenceLong = (new Date()).getTime() - startTime.getTime();
						float timeDifferenceFloat = (float)timeDifferenceLong/1000;
						Log.d(TAG, "Time difference: " + timeDifferenceFloat + " seconds");
						successItem.setLoadTime(timeDifferenceFloat);
						startTime = null;
					}
					firebaseHelper.pushRtspItem(successItem);
					
					logger.info(successItem.toJson());

					if (VideoActivity.mediaUrls.get(mrlIndex).isLocalNetwork == false)
					{
						SharedPreferences sharedPrefs = PreferenceManager
								.getDefaultSharedPreferences(player);
						SharedPreferences.Editor editor = sharedPrefs.edit();
						editor.putString("pref_mrlplaying" + evercamCamera.getCameraId(),
								player.mrlPlaying);
						editor.commit();
					}

					break;
				default:
					break;
				}
			}
			catch (Exception e)
			{
				Log.e(TAG, e.getMessage());
			}
		}
	}

	public static class MediaURL
	{
		public String url = "";
		public boolean isLocalNetwork = false;

		public MediaURL(String url, boolean isLocalNetwork)
		{
			this.url = url;
			this.isLocalNetwork = isLocalNetwork;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == null) return false;
			if (obj == this) return true;
			if (!(obj instanceof MediaURL)) return false;

			MediaURL mediaURL = (MediaURL) obj;
			return this.url.equalsIgnoreCase(mediaURL.url);
		}
	}

	private static class AbandonedJpgUrl
	{
		public static ArrayList<String> abandonedArray = new ArrayList<String>();

		public static void add(String url)
		{
			if (!abandonedArray.contains(url))
			{
				abandonedArray.add(url);
			}
		}

		public static boolean contains(String url)
		{
			if (!abandonedArray.isEmpty())
			{
				for (int index = 0; index < abandonedArray.size(); index++)
				{
					if (abandonedArray.get(index).equals(url))
					{
						return true;
					}
				}
			}
			return false;
		}
	}

	private class DownloadImage extends AsyncTask<String, Void, Drawable>
	{
		private long myStartImageTime;
		private boolean isLocalNetworkRequest = false;
		private String successUrl = "";//Only used for Firebase report

		@Override
		protected Drawable doInBackground(String... urls)
		{
			ArrayList<Cookie> cookies = new ArrayList<Cookie>();
			if (!showImagesVideo) 
			{
				return null;
			}
			Drawable response = null;
			if(!paused && !end)
			{
			if (evercamCamera.hasCredentials() && urls.length > 0)
			{
				for (String url : urls)
				{
					if (!url.isEmpty())
					{
						try
						{
							downloadStartCount++;
							myStartImageTime = SystemClock.uptimeMillis();

							response = Commons.getDrawablefromUrlAuthenticated(url,
									evercamCamera.getUsername(), evercamCamera.getPassword(),
								    cookies, 5000);
							if (response != null) 
							{
								successiveFailureCount = 0;
								successUrl = url;
							}
						}
						catch (OutOfMemoryError e)
						{
							if (enableLogs) Log.e(TAG,
									e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
							successiveFailureCount++;
							continue;
							
						}
						catch (Exception e)
						{
							Log.e(TAG, "Exception get camera with auth: " + e.toString() + "\r\n"
									+ "ImageURl=[" + url + "]" + "\r\n");

							AbandonedJpgUrl.add(url);
							successiveFailureCount++;
						}
						finally
						{
							downloadEndCount++;
						}
					}
				}
			}
			else
			{
				try
				{
					downloadStartCount++;
					Camera camera = Camera.getById(evercamCamera.getCameraId(), false);
					InputStream stream = camera.getSnapshotFromEvercam();
					response = Drawable.createFromStream(stream, "src");
					if (response != null) 
					{
						successiveFailureCount = 0;
					}
				}
				catch (EvercamException e)
				{
					Log.e(TAG, "Request snapshot from Evercam error: " + e.toString());
					successiveFailureCount++;
				}
				catch (Exception e)
				{
					Log.e(TAG, "Request snapshot from Evercam error: " + e.toString());
					successiveFailureCount++;
				}
				catch (OutOfMemoryError e)
				{
					Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
					successiveFailureCount++;
				}
				finally
				{
					downloadEndCount++;
				}
			}
			}
			else
			{
				Log.d(TAG, "Paused or ended");
			}
			return response;
		}

		@Override
		protected void onPostExecute(Drawable result)
		{
			try
			{
				if (!showImagesVideo) return;

				if (isLocalNetworkRequest) 
				{
					isFirstImageLocalEnded = true;
				}
				else 
				{
					isFirstImageLiveEnded = true;
				}

				Log.d(TAG, "Failure count:" + successiveFailureCount);

				if(!paused && !end)
				{
				if (result != null)
				{
					Log.d(TAG, "result not null");
					if(result.getIntrinsicWidth() > 0
							&& result.getIntrinsicHeight() > 0)
					{
						if(myStartImageTime >= latestStartImageTime)
						{
							//Log.d(TAG, "myStartImageTime >= latestStartImageTime");
							if (isLocalNetworkRequest) isFirstImageLocalReceived = true;
							else isFirstImageLiveReceived = true;
							if (isLocalNetworkRequest && localnetworkSettings.equalsIgnoreCase("0")) isLocalNetwork = true;

							latestStartImageTime = myStartImageTime;

							if (mediaPlayerView.getVisibility() != View.VISIBLE
									&& VideoActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) VideoActivity.this
									.getActionBar().hide();

							if (showImagesVideo) imageView.setImageDrawable(result);

							hideProgressView();
							
							//Image received, start time counter, need more tests
							startTimeCounter();
							
							if(!isJpgSuccessful)
							{					
								//Successfully played JPG view, send Google Analytics event
								//Log.d(TAG, "Jpg success!");
								isJpgSuccessful = true;
								EvercamPlayApplication.sendEventAnalytics(VideoActivity.this, R.string.category_streaming_jpg,
										R.string.action_streaming_jpg_success, R.string.label_streaming_jpg_success) ;
								StreamFeedbackItem successItem = new StreamFeedbackItem(VideoActivity.this, AppData.defaultUser.getUsername(), true);
								successItem.setCameraId(evercamCamera.getCameraId());
								successItem.setUrl(successUrl);
								successItem.setType(StreamFeedbackItem.TYPE_JPG);
								firebaseHelper.pushJpgItem(successItem);
								logger.info(successItem.toJson());
							}	
							else
							{
								//Log.d(TAG, "Jpg success but already reported");
							}
						}
						else
						{
							if (enableLogs) Log.i(TAG, "downloaded image discarded. ");
						}
				}
				}
				else if(result == null)
				{
					Log.d(TAG, "result is null");
					if(
				successiveFailureCount > 10
						&& !isShowingFailureMessage)
					{
						Log.d(TAG, "successiveFailureCount > 5 && !isShowingFailureMessage");
						if(myStartImageTime >= latestStartImageTime)
						{
							Log.d(TAG, "myStartImageTime >= latestStartImageTime");
							showMediaFailureDialog();
							imageThread.cancel(true);
							
							//Failed to play JPG view, send Google Analytics event
							EvercamPlayApplication.sendEventAnalytics(VideoActivity.this, R.string.category_streaming_jpg,
									R.string.action_streaming_jpg_failed, R.string.label_streaming_jpg_failed);
							
							//Send Firebase
							StreamFeedbackItem failedItem = new StreamFeedbackItem(VideoActivity.this, AppData.defaultUser.getUsername(), false);
							failedItem.setCameraId(evercamCamera.getCameraId());
							failedItem.setUrl(evercamCamera.getExternalSnapshotUrl());
							failedItem.setType(StreamFeedbackItem.TYPE_JPG);
							firebaseHelper.pushJpgItem(failedItem);
							logger.info(failedItem.toJson());
						}
					}
				}
				}
				else
				{
					Log.d(TAG, "paused or ended");
				}
			}
			catch (OutOfMemoryError e)
			{
				if (enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
			}
			catch (Exception e)
			{
				if (enableLogs) Log.e(TAG, e.toString());
				if (Constants.isAppTrackingEnabled)
				{
					BugSenseHandler.sendException(e);
				}
			}

			startDownloading = true;
		}
	}

	private String[] getCameraNameArray()
	{
		ArrayList<String> cameraNames = new ArrayList<String>();

		for (int count = 0; count < AppData.evercamCameraList.size(); count++)
		{
			cameraNames.add(AppData.evercamCameraList.get(count).getName());
			if (AppData.evercamCameraList.get(count).getCameraId().equals(startingCameraID))
			{
				defaultCameraIndex = cameraNames.size() - 1;
			}
		}

		String[] cameraNameArray = new String[cameraNames.size()];
		cameraNames.toArray(cameraNameArray);

		return cameraNameArray;
	}

	private void loadCamerasToActionBar()
	{
		String[] cameraNames = getCameraNameArray();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(VideoActivity.this,
				android.R.layout.simple_spinner_dropdown_item, cameraNames);
		VideoActivity.this.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		OnNavigationListener navigationListener = new OnNavigationListener(){
			@Override
			public boolean onNavigationItemSelected(int itemPosition, long itemId)
			{
				if (imageThread != null && imageThread.getStatus() != AsyncTask.Status.RUNNING)
				{
					imageThread.cancel(true);
				}
				imageThread = null;
				mrlPlaying = null;
				showImagesVideo = false;

				evercamCamera = AppData.evercamCameraList.get(itemPosition);

				if (AppData.evercamCameraList.get(itemPosition).getStatus()
						.equalsIgnoreCase(CameraStatus.OFFLINE))
				{
					// If camera is offline, show offline msg and stop video
					// playing.
					offlineTextView.setVisibility(View.VISIBLE);
					progressView.setVisibility(View.GONE);

					// Hide video elements if switch to an offline camera.
					surfaceView.setVisibility(View.GONE);
					imageView.setVisibility(View.GONE);
				}
				else
				{
					offlineTextView.setVisibility(View.GONE);
					setCameraForPlaying(VideoActivity.this,
							AppData.evercamCameraList.get(itemPosition));
					createPlayer(getCurrentMRL());
				}
				return false;
			}
		};

		getActionBar().setListNavigationCallbacks(adapter, navigationListener);
		getActionBar().setSelectedNavigationItem(defaultCameraIndex);
	}
}
