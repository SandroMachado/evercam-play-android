package io.evercam.androidapp.video;

import io.evercam.Camera;
import io.evercam.EvercamException;
import io.evercam.androidapp.EvercamPlayApplication;
import io.evercam.androidapp.ParentActivity;
import io.evercam.androidapp.custom.ProgressView;
import io.evercam.androidapp.dto.CameraStatus;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.utils.AppData;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.EvercamFile;
import io.evercam.androidapp.utils.CustomedDialog;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaList;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
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

	private long downloadStartCount = 0;
	private long downloadEndCount = 0;
	private BrowseImages imageThread;
	private boolean isProgressShowing = true;
	static boolean enableLogs = true;

	// image tasks and thread variables
	private int sleepIntervalMinTime = 201; // interval between two requests of
											// images
	private int intervalAdjustment = 1; // how much milli seconds to increment
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

	private static String startingCameraID;
	private int defaultCameraIndex;
	// preferences options
	private String localnetworkSettings = "0";
	private boolean isLocalNetwork = false;

	private static String imageLiveCameraURL = "";
	private static String imageLiveLocalURL = "";

	private boolean paused = false;
	private static boolean isPlayingJpg = false;// If true, stop trying video
												// URL for reconnecting.

	private Animation fadeInAnimation = null; // animation that shows the
												// playing icon
	// of media player fading and
	// disappearing

	private boolean end = false; // whether to end this activity or not

	private Handler handler = new MyHandler(this);

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

			setDisplayOriention();

			if (this.getActionBar() != null)
			{
				this.getActionBar().setHomeButtonEnabled(true);
				this.getActionBar().setTitle("");
				this.getActionBar().setIcon(R.drawable.ic_navigation_back);
			}

			setContentView(R.layout.videolayoutwithslide);

			initialPageElements();

			loadImageFromCache(startingCameraID);

			checkNetworkStatus();

			addCamerasToDropdownActionBar();

			readSetPreferences();
		}
		catch (OutOfMemoryError e)
		{
			Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
		}
		catch (Exception ex)
		{
			Log.e(TAG, ex.toString(), ex);
			if (Constants.isAppTrackingEnabled)
			{
				BugSenseHandler.sendException(ex);
			}
		}
	}

	@Override
	public void onResume()
	{
		try
		{
			super.onResume();
			this.paused = false;

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

				if (imageThread == null) // ignore if image thread is null
				{

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
		catch (Exception e)
		{
			Log.e(TAG, e.toString());
			if (Constants.isAppTrackingEnabled)
			{
				BugSenseHandler.sendException(e);
			}
		}
	}

	@Override
	public void onPause()
	{
		try
		{
			super.onPause();

			if (!optionsActivityStarted)
			{
				this.paused = true;
			}
		}
		catch (Exception ex)
		{
			if (Constants.isAppTrackingEnabled)
			{
				BugSenseHandler.sendException(ex);
			}
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
		try
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
				this.finish();
			}
		}
		catch (Exception ex)
		{
		}

		if (Constants.isAppTrackingEnabled)
		{
			BugSenseHandler.closeSession(this);
		}
	}

	//
	// @Override
	// public boolean onCreateOptionsMenu(Menu menu)
	// {
	// try
	// {
	// MenuInflater inflater = getMenuInflater();
	// inflater.inflate(R.menu.videomenulayout, menu);
	//
	// return true;
	// }
	// catch (Exception ex)
	// {
	// Log.e(TAG, ex.toString());
	// if (Constants.isAppTrackingEnabled)
	// {
	// BugSenseHandler.sendException(ex);
	// }
	// }
	// return true;
	// }
	//

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		try
		{
			switch (item.getItemId())
			{
			case R.id.menusettings_video:
				optionsActivityStarted = true;
				paused = true;
				startActivity(new Intent(this, VideoPrefsActivity.class));
				mediaPlayerView.setVisibility(View.GONE);

				showProgressView();

				return true;
			case android.R.id.home:
				this.finish();
				return true;
			default:

				optionsActivityStarted = true;
				paused = true;
				startActivity(new Intent(this, VideoPrefsActivity.class));
				mediaPlayerView.setVisibility(View.GONE);

				showProgressView();

				return true;

			}
		}
		catch (OutOfMemoryError e)
		{
			Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
			return true;
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
			if (Constants.isAppTrackingEnabled)
			{
				BugSenseHandler.sendException(e);
			}
			return true;
		}
	}

	public void addCamerasToDropdownActionBar()
	{
		Log.d(TAG, "Prepare to add");
		new LoadActiveCamerasTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public static boolean startPlayingVideoForCamera(Context context, String cameraId)
	{
		startingCameraID = cameraId;
		Intent intent = new Intent(context, VideoActivity.class);
		context.startActivity(intent);

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

			paused = false;
			end = false;

			surfaceView.setVisibility(View.GONE);
			imageView.setVisibility(View.VISIBLE);
			showProgressView();

			if (evercamCamera != null)
			{
				loadImageFromCache(evercamCamera.getCameraId());
			}

			showProgressView();

			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			mrlPlaying = sharedPrefs.getString("pref_mrlplaying" + evercamCamera.getCameraId(),
					null);

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
			CustomedDialog.showUnexpectedErrorDialog(VideoActivity.this);
		}
	}

	private void addUrlIfValid(String url, EvercamCamera cam)
	{
		try
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
		catch (Exception e)
		{
		}
	}

	// Loads image from cache. First image gets loaded correctly and hence we
	// can start making requests concurrently as well
	public boolean loadImageFromCache(String cameraId)
	{
		try
		{
			imageView.setImageDrawable(null);
			if (evercamCamera == null && cameraId.isEmpty()) return false;
			File cacheFile = EvercamFile.getCacheFileRelative(this, cameraId);
			if (cacheFile.exists())
			{
				Drawable result = Drawable.createFromPath(cacheFile.getPath());
				if (result != null)
				{
					startDownloading = true;
					imageView.setImageDrawable(result);

					Log.d(TAG, "Loaded first image from Cache: " + media_width + ":" + media_height);
					return true;
				}
			}
		}
		catch (OutOfMemoryError e)
		{
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

				if (!paused) mediaPlayerView.setVisibility(View.GONE);
				else mediaPlayerView.setVisibility(View.VISIBLE);

				int orientation = VideoActivity.this.getResources().getConfiguration().orientation;
				if (!paused && orientation == Configuration.ORIENTATION_LANDSCAPE)
				{
					VideoActivity.this.getActionBar().hide();
				}
			}
		});

		mediaPlayerView.startAnimation(fadeInAnimation);
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
		releasePlayer();
		try
		{
			if (media != null && media.length() > 0)
			{
				showToast(getString(R.string.connecting) + media);
			}
			else
			{
				media = "http://127.0.0.1:554/NoVideo-CambaTv";
			}

			// Create a new media player
			libvlc = LibVLC.getInstance();
			libvlc.setSubtitlesEncoding("");
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
		catch (Exception e)
		{
			Toast.makeText(this, "Error connecting! " + media + " ::::: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
		catch (Error e)
		{
			Log.e(TAG, e.getMessage());
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
			libvlc.destroy();
			libvlc = null;

			mVideoWidth = 0;
			mVideoHeight = 0;
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getMessage());
		}
	}

	private void restartPlay(String media)
	{
		if (libvlc == null) return;

		try
		{
			libvlc.stop();

			if (media.length() > 0)
			{
				showToast(getString(R.string.reconnecting) + media);
			}

			libvlc.getMediaList().clear();
			libvlc.playMRL(media);
		}
		catch (Exception e)
		{
			if (!isPlayingJpg)
			{
				Toast.makeText(this, "Error reconnecting! " + media + " ::::: " + e.getMessage(),
						Toast.LENGTH_LONG).show();
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

			readSetPreferences();

			startDownloading = false;
			this.paused = false;
			this.end = false;
			this.isShowingFailureMessage = false;
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getMessage(), e);
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
			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
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

	void showMediaFailureDialog()
	{
		CustomedDialog.getAlertDialog(VideoActivity.this, getString(R.string.msg_unable_to_play),
				getString(R.string.msg_please_check_camera), new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						try
						{
							VideoActivity.this.getActionBar().show();
							paused = true;
							isShowingFailureMessage = false;
							dialog.dismiss();
							hideProgressView();
						}
						catch (Exception e)
						{
							if (Constants.isAppTrackingEnabled)
							{
								BugSenseHandler.sendException(e);
							}
						}
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
			try
			{
				CustomedDialog.getNoInternetDialog(this, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						try
						{
							paused = true;
							dialog.dismiss();
							hideProgressView();
						}
						catch (Exception e)
						{
							if (Constants.isAppTrackingEnabled)
							{
								BugSenseHandler.sendException(e);
							}
						}
					}
				}).show();
				return;
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

	private void initialPageElements()
	{
		imageViewLayout = (RelativeLayout) this.findViewById(R.id.camimage1);
		imageView = (ImageView) this.findViewById(R.id.img_camera1);
		mediaPlayerView = (ImageView) this.findViewById(R.id.ivmediaplayer1);

		surfaceView = (SurfaceView) findViewById(R.id.surface1);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);

		progressView = ((ProgressView) imageViewLayout.findViewById(R.id.ivprogressspinner1));

		progressView.canvasColor = Color.TRANSPARENT;

		isProgressShowing = true;
		progressView.setVisibility(View.VISIBLE);

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
					showProgressView();

					mediaPlayerView.setImageBitmap(null);
					mediaPlayerView.setVisibility(View.VISIBLE);
					mediaPlayerView.setImageResource(android.R.drawable.ic_media_pause);

					startMediaPlayerAnimation();

					restartPlay(mrlPlaying);
					paused = false;
				}
				else
				// video is currently playing. Now we need to pause video
				{
					mediaPlayerView.clearAnimation();
					if (fadeInAnimation != null && fadeInAnimation.hasStarted()
							&& !fadeInAnimation.hasEnded())
					{
						fadeInAnimation.cancel();
						fadeInAnimation.reset();
					}
					mediaPlayerView.setVisibility(View.VISIBLE);
					mediaPlayerView.setImageBitmap(null);
					mediaPlayerView.setImageResource(android.R.drawable.ic_media_play);

					stopPlayer();

					paused = true; // mark the images as paused. Do not stop
									// threads, but do not show the images
									// showing up
				}
			}
		});

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

				if (!paused && !end) // video is currently playing. Now we
										// need to pause video
				{
					VideoActivity.this.getActionBar().show();
					mediaPlayerView.setImageResource(android.R.drawable.ic_media_pause);

					mediaPlayerView.setVisibility(View.VISIBLE);

					startMediaPlayerAnimation();
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
					try
					{
						while (!startDownloading)
						{
							Thread.sleep(500);
						}
					}
					catch (Exception e)
					{
						if (Constants.isAppTrackingEnabled)
						{
							BugSenseHandler.sendException(e);
						}
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
						DownloadImage tasklive = new DownloadImage();

						if (downloadStartCount - downloadEndCount < 9)
						{
							tasklive.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
									new String[] { imageLiveCameraURL, imageLiveLocalURL });
						}

						if (downloadStartCount - downloadEndCount > 9 && sleepInterval < 2000)
						{
							sleepInterval += intervalAdjustment;
						}
						else if (sleepInterval >= sleepIntervalMinTime)
						{
							sleepInterval -= intervalAdjustment;
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
					Log.e(TAG, e.toString() + "-::::-" + Log.getStackTraceString(e));
				}
			}
			return null;
		}
	}

	/*************
	 * Events
	 *************/

	private static class MyHandler extends Handler
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
					player.surfaceView.setVisibility(View.VISIBLE);
					player.imageView.setVisibility(View.GONE);
					player.mrlPlaying = player.getCurrentMRL();

					break;

				case EventHandler.MediaPlayerPaused:
					break;

				case EventHandler.MediaPlayerStopped:
					break;

				case EventHandler.MediaPlayerEncounteredError:

					Log.v(TAG, "EventHandler.MediaPlayerEncounteredError");
					if (evercamCamera != null)
					{
						player.loadImageFromCache(evercamCamera.getCameraId());
					}

					if (player.mrlPlaying == null && player.isNextMRLValid())
					{
						player.restartPlay(player.getNextMRL());
					}
					else if (player.mrlPlaying != null)
					{
						player.restartPlay(player.mrlPlaying);
					}
					else
					{
						isPlayingJpg = true;
						player.showToast(videoActivity.get().getString(R.string.msg_switch_to_jpg));
						player.showImagesVideo = true;
						player.createNewImageThread();
					}

					break;

				case EventHandler.MediaPlayerVout:
					Log.v(TAG, "EventHandler.MediaPlayerVout");
					player.hideProgressView();
					try
					{
						if (VideoActivity.mediaUrls.get(mrlIndex).isLocalNetwork == false)
						{
							SharedPreferences sharedPrefs = PreferenceManager
									.getDefaultSharedPreferences(player);
							SharedPreferences.Editor editor = sharedPrefs.edit();
							editor.putString("pref_mrlplaying" + evercamCamera.getCameraId(),
									player.mrlPlaying);
							editor.commit();
						}
					}
					catch (Exception ex)
					{

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

		@Override
		protected Drawable doInBackground(String... urls)
		{
			if (!showImagesVideo) return null;
			Drawable response = null;
			if (evercamCamera.hasCredentials())
			{
				for (String url : urls)
				{
					if (!url.isEmpty())
					{
						Log.d(TAG, "Running: " + url);
						try
						{
							downloadStartCount++;
							myStartImageTime = SystemClock.uptimeMillis();

							response = Commons.getDrawablefromUrlAuthenticated(url,
									evercamCamera.getUsername(), evercamCamera.getPassword(),
									evercamCamera.cookies, 3000);
							if (response != null) successiveFailureCount = 0;
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
						} finally
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
					Camera camera = Camera.getById(evercamCamera.getCameraId());
					InputStream stream = camera.getSnapshotFromEvercam();
					response = Drawable.createFromStream(stream, "src");
					if (response != null) successiveFailureCount = 0;
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
			}
			return response;
		}

		@Override
		protected void onPostExecute(Drawable result)
		{
			try
			{
				if (!showImagesVideo) return;

				if (isLocalNetworkRequest) isFirstImageLocalEnded = true;
				else isFirstImageLiveEnded = true;

				if (result != null && result.getIntrinsicWidth() > 0
						&& result.getIntrinsicHeight() > 0
						&& myStartImageTime >= latestStartImageTime && !paused && !end)
				{
					if (isLocalNetworkRequest) isFirstImageLocalReceived = true;
					else isFirstImageLiveReceived = true;
					if (isLocalNetworkRequest && localnetworkSettings.equalsIgnoreCase("0")) isLocalNetwork = true;

					latestStartImageTime = myStartImageTime;

					if (mediaPlayerView.getVisibility() != View.VISIBLE
							&& VideoActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) VideoActivity.this
							.getActionBar().hide();

					if (showImagesVideo) imageView.setImageDrawable(result);

					hideProgressView();

				}
				// do not show message on local network failure request.
				else if (((!isFirstImageLocalEnded && !isFirstImageLiveEnded
						&& !isFirstImageLocalReceived && !isFirstImageLiveReceived && localnetworkSettings
							.equalsIgnoreCase("0")) // local task ended. Now
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
					showMediaFailureDialog();
					imageThread.cancel(true);
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

			startDownloading = true;

		}
	}

	private class LoadActiveCamerasTask extends AsyncTask<String, String, String[]>
	{
		final ArrayList<EvercamCamera> activeCameras = new ArrayList<EvercamCamera>();
		int defaultCameraIndex = 0;

		public LoadActiveCamerasTask()
		{
			Log.d(TAG, "Constructor called");
		}

		@Override
		protected String[] doInBackground(String... params)
		{
			Log.d(TAG, "LoadActiveCamerasTask started");
			ArrayList<String> cameraNames = new ArrayList<String>();

			for (int count = 0; count < AppData.evercamCameraList.size(); count++)
			{
				//Disabled online status check in drop down camera list because we want
				//offline camera show in live view page as well.
//				if (!AppData.evercamCameraList.get(count).getStatus()
//						.equalsIgnoreCase(CameraStatus.OFFLINE))
//				{
					activeCameras.add(AppData.evercamCameraList.get(count));
					cameraNames.add(AppData.evercamCameraList.get(count).getName());
					if (AppData.evercamCameraList.get(count).getCameraId() == startingCameraID)
					{
						defaultCameraIndex = cameraNames.size() - 1;
					}
	//			}
			}

			String[] cameraArray = new String[cameraNames.size()];
			cameraNames.toArray(cameraArray);

			return cameraArray;
		}

		@Override
		protected void onPostExecute(final String[] cameraNames)
		{
			Log.d(TAG, "LoadActiveCamerasTask finished");
			try
			{
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(VideoActivity.this,
						android.R.layout.simple_spinner_dropdown_item, cameraNames);
				VideoActivity.this.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
				OnNavigationListener navigationListener = new OnNavigationListener(){
					@Override
					public boolean onNavigationItemSelected(int itemPosition, long itemId)
					{
						try
						{
							showImagesVideo = false;
							if (imageThread != null
									&& imageThread.getStatus() != AsyncTask.Status.RUNNING)
							{
								imageThread.cancel(true);
							}
							imageThread = null;

							mrlPlaying = null;
							setCameraForPlaying(VideoActivity.this, activeCameras.get(itemPosition));

							createPlayer(getCurrentMRL());

						}
						catch (Exception e)
						{
							Log.e(TAG, e.getMessage(), e);
							if (Constants.isAppTrackingEnabled)
							{
								BugSenseHandler.sendException(e);
							}
						}
						return false;
					}
				};

				getActionBar().setListNavigationCallbacks(adapter, navigationListener);
				getActionBar().setSelectedNavigationItem(defaultCameraIndex);
				Log.d(TAG, "LoadActiveCamerasTask listed");

			}
			catch (Exception e)
			{
				Log.e(TAG, "Error when load dropdown list: " + e.toString());
				if (Constants.isAppTrackingEnabled)
				{
					BugSenseHandler.sendException(e);
				}
			}
		}
	}
}
