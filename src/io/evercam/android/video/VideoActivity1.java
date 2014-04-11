package io.evercam.android.video;

//package io.evercam.android.video;
//
//import java.io.File;
//import java.lang.ref.WeakReference;
//import java.net.InetAddress;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.RejectedExecutionException;
//
//import org.videolan.libvlc.EventHandler;
//import org.videolan.libvlc.IVideoPlayer;
//import org.videolan.libvlc.LibVLC;
//import org.videolan.libvlc.Media;
//import org.videolan.libvlc.MediaList;
//
//import android.app.ActionBar;
//import android.app.ActionBar.OnNavigationListener;
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.res.Configuration;
//import android.graphics.Color;
//import android.graphics.PixelFormat;
//import android.graphics.drawable.Drawable;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.os.SystemClock;
//import android.preference.PreferenceManager;
//import android.util.Log;
//import android.view.Display;
//import android.view.Gravity;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup.LayoutParams;
//import android.view.WindowManager;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.webkit.URLUtil;
//import android.widget.ArrayAdapter;
//import android.widget.ImageView;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.bugsense.trace.BugSenseHandler;
//import io.evercam.android.ParentActivity;
//import io.evercam.android.R;
//import io.evercam.android.custom.ProgressView;
//import io.evercam.android.dto.Camera;
//import io.evercam.android.slidemenu.SlideMenu;
//import io.evercam.android.slidemenu.SlideMenuInterface;
//import io.evercam.android.utils.AppData;
//import io.evercam.android.utils.Commons;
//import io.evercam.android.utils.Constants;
//import io.evercam.android.utils.UIUtils;
//import com.google.analytics.tracking.android.EasyTracker;
////import com.camba.killruddery.VideoViewCustom;
//
//
//public class VideoActivity1 extends ParentActivity 
//implements SlideMenuInterface.OnSlideMenuItemClickListener
//, SurfaceHolder.Callback
//, IVideoPlayer 
//{
//
//	private final static String TAG = "VideoActivity";
//	
//	private static List<MRLCamba> mrls = null ;
//	private int mrlIndex = -1 ; 
//	private String mrlPlaying = null ;
//
//	// display surface
//	private SurfaceView mSurface;
//	private SurfaceHolder holder;
//	
//	
//	ProgressView myProgressView = null;
//
//	// media player
//	private LibVLC libvlc;
//	private int mVideoWidth;
//	private int mVideoHeight;
//	private final static int VideoSizeChanged = -1;
//
//	
//	private SlideMenu slidemenu;
//	
////	Screen view change vraibales
//	private int screen_width, screen_height;
//	private int media_width = 0, media_height = 0;
//	private boolean landscape;
//	
//	
////	video playing controls and variables
//	private RelativeLayout iView ;
//	private ImageView imgCam ;
//	private ImageView ivMediaPlayer;
//	
//	
//	private boolean isProgressShowing = true;
//	static boolean enableLogs = true;
//	
//	
//	
//	private Boolean isShowingFailureMessage = false; // whether error message for failure is showing or not
//	
//	private Boolean optionsActivityStarted = false;  // whether preference activity is showing or not
//
//	public AlertDialog adLocalNetwork;			// dialoge message for local network not connected 
//
//	
//	public static Camera camera = null; // if camera uses cookies authentication, then use these cookies to pass to camera
//	
// 
////	preferences options
//	private String localnetworkSettings = "0";
//	private boolean isLocalNetwork = false;
//	
//	boolean paused = false; // whether media player or playing of video(images) is paused or not
//	
//	Animation myFadeInAnimation = null;	// animation that shows the playing icon of media player fading and disappearing
//
//	boolean end = false; // whether to end this activity or not
//
//	
//	
//	
//	public void addCamsToDropdownActionBar(){
//		
//		new AsyncTask<String, String, String[]>(){
//			final ArrayList<Camera> ActiveCamers=new ArrayList<Camera>();
//			int defaultCamIndex = 0;
//			@Override
//			protected String[] doInBackground(String... params) {
//
//				ArrayList<String> Cams= new ArrayList<String>();
//			     
//			     
//			     for (int i=0;i<AppData.camesList.size();i++){
//			    	 if (!AppData.camesList.get(i).getStatus().equalsIgnoreCase("Offline"))
//			    	 {
//			    		 ActiveCamers.add(AppData.camesList.get(i));
//			    	 Cams.add(AppData.camesList.get(i).getName());
//			    	 if (AppData.camesList.get(i).getCameraID() == camera.getCameraID())
//				    		defaultCamIndex=Cams.size()-1;
//				    	 
//			    	 }
//			    		
//			     }
//			     
//			     
//			     
//			     String[] cameras = new String[Cams.size()];
//				Cams.toArray(cameras);
//			
//				return cameras;
//			}
//			protected void onPostExecute(final String[] CamsList){
//				try{
//					ArrayAdapter<String> adapter = new ArrayAdapter<String>(VideoActivity.this, android.R.layout.simple_spinner_dropdown_item, CamsList);
//					VideoActivity.this.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST); // dropdown list navigation for the action bar
//					OnNavigationListener navigationListener = new OnNavigationListener() {
//						@Override
//						public boolean onNavigationItemSelected(int itemPosition, long itemId) {
//							try{
//								
//								
//								
//								SetCameraForPlaying(VideoActivity.this,ActiveCamers.get(itemPosition));
//
//								SetImageAttributesAndLoadImage();
//
//								StartResumeDownloading();
//								
//
//							}catch(Exception e){
//								Log.e(TAG, e.getMessage(),e);
//								if(Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//							}
//							return false;
//						}
//					};
//
//					getActionBar().setListNavigationCallbacks(adapter, navigationListener);
//					getActionBar().setSelectedNavigationItem(defaultCamIndex);
//
//				}
//				catch (Exception e){
//					Log.e(TAG, e.getMessage(),e);
//					if(Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//				}
//				
//				
//			}
//			
//			
//			
//			
//			
//			
//			
//			
//		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"");
//		
//		
//		
//		
//		
//	}
//
//	
//	
//	
//	
//	
//	
//	
//	
//	public static boolean StartPlayingVIdeoForCamera(Context context, int camID)
//	{
//		if(AppData.camesList != null)
//		for(Camera cam : AppData.camesList)
//		{
//			if(cam.getCameraID() == camID)
//			{ 
//                
//				StartPlayingVIdeo(context, cam);
//				return true;
//			}
//		}
//		return false;
//	}
//	
//	public static void StartPlayingVIdeo(Context context, Camera cam)
//	{
//		try
//		{
//			
//			SetCameraForPlaying(context,cam);
//			
//			//			going to start the activity to show the full screen video
//			Intent i = new Intent(context, VideoActivity.class);
//			context.startActivity(i);
//
//		}
//		catch(Exception e)
//		{
//			if(enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
//			UIUtils.GetAlertDialog(context, "Exception", e.toString()).show();// + "::cam.getCameraImageUrl() [" +  cam.getCameraImageUrl() + "], cam.getLowResolutionSnapshotUrl() ["+cam.getLowResolutionSnapshotUrl()+"]").show();
//		}
//	}
//	
//	private static void SetCameraForPlaying(Context context,Camera cam)
//	{
//		try
//		{
//			
//			if(enableLogs) Log.i(TAG, "Playing camera:" + cam.toString());
//
//			String ImageUrl = ((cam.getLowResolutionSnapshotUrl() != null && URLUtil.isValidUrl(cam.getLowResolutionSnapshotUrl())) ? cam.getLowResolutionSnapshotUrl() :cam.getCameraImageUrl()); 
//
//			if(!URLUtil.isValidUrl(ImageUrl))
//			{
//				UIUtils.GetAlertDialog(context, "Camera Error", "Unable to play camera. Please try refreshing the cameras.").show();
//				return;
//			}
//
//			//VideoActivity.imageLiveCameraURL = ImageUrl;
//			VideoActivity.camera = cam;
//			
//			if(cam.getLocalIpPort() != null && cam.getLocalIpPort().length() > 10) 
//			{
//				String Prefix = (ImageUrl.startsWith("https://") ? "https://" : "https://" ) ; 
//				
//				if(ImageUrl.startsWith("https://"))//			Extracting information from the camera image url
//				ImageUrl = ImageUrl.replace("http://", "");
//				
//				//VideoActivity.imageLiveLocalURL = Prefix + cam.getLocalIpPort().trim() + ImageUrl.substring(ImageUrl.indexOf("/", Prefix.length() + 1));
//			}
//			else
//			{
//				//VideoActivity.imageLiveCameraURL = null;
//			}
//			
//			mrls = new ArrayList<VideoActivity.MRLCamba>();
//			
//			if(cam.getH264Url() != null && cam.getH264Url().length() > 10)
//			{
//				mrls.add(new MRLCamba(cam.getH264Url(),false));	
//			}
//			
//			if(cam.getRtspUrl() != null && cam.getRtspUrl().length() > 10)
//			{
//				mrls.add(new MRLCamba(cam.getRtspUrl(),false));	
//			}
//			
//			if(cam.getMobileUrl() != null && cam.getMobileUrl().length() > 10)
//			{
//				mrls.add(new MRLCamba(cam.getMobileUrl(),false));	
//			}
//			
//			if(cam.getMpeg4Url() != null && cam.getMpeg4Url().length() > 10)
//			{
//				mrls.add(new MRLCamba(cam.getMpeg4Url(),false));	
//			}
//			
//			if(cam.getMjpgUrl() != null && cam.getMjpgUrl().length() > 10)
//			{
//				mrls.add(new MRLCamba(cam.getMjpgUrl(),false));	
//			}
//			
//			
//		}
//		catch(Exception e)
//		{
//			if(enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
//			UIUtils.GetAlertDialog(context, "Exception", e.toString()).show();// + "::cam.getCameraImageUrl() [" +  cam.getCameraImageUrl() + "], cam.getLowResolutionSnapshotUrl() ["+cam.getLowResolutionSnapshotUrl()+"]").show();
//		}
//
//	}
//
//	
//	
//	
////	Loads image from cache. First image gets loaded correctly and hence we can start making requests concurrently as well
//	public boolean LoadImageFromCache()
//	{
//		try{
//			String path = this.getCacheDir() + "/" + camera.getCameraID() + ".jpg";
//			if(new File(path).exists())
//			{
//				Drawable result = Drawable.createFromPath(path);
//				if(result!= null)
//				{
//					//startDownloading = true;
//					imgCam.setImageDrawable(result);
//					
//					if(enableLogs) Log.i(TAG, "Loaded first image from Cache: " + media_width + ":" + media_height);
//					return true;
//				}
//				else
//				{
//					if(enableLogs) Log.e(TAG, "laodimagefromcache drawable d1 is null. Camera Object is [" + camera.toString() + "]");
//				}
//			}
//		}
//		catch(OutOfMemoryError e)
//		{
//			if(enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
//			return false;
//		}
//		catch(Exception e)
//		{
//			if(enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
//			if(Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//		}
//
//		return false;
//	}
//
//
////	preferences options for this screen
//	@Override 
//	public boolean onCreateOptionsMenu(Menu menu) {
//		try{
//			MenuInflater inflater = getMenuInflater();  
//			inflater.inflate(R.menu.ivideomenulayout, menu);
//			
//			return true;
//		}
//		catch(Exception ex) {if(enableLogs) Log.e(TAG, ex.toString());if(Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ex);}
//		return true;
//	}
//
//	@Override
//	public boolean onPrepareOptionsMenu (Menu menu){
//		super.onPrepareOptionsMenu(menu);
//	
//		menu.clear();
//		
//		MenuInflater inflater = getMenuInflater();  
//		inflater.inflate(R.menu.ivideomenulayout, menu);
//		
//		
//		return true;
//	}
//	
////	Tells that what item has been selected from options. We need to call the relevent code for that item.
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		try{
//			// Handle item selection
//			switch (item.getItemId()) {
//			case R.id.menusettings:
//				optionsActivityStarted = true;
//				paused = true;
//				//sajjad startActivity(new Intent(this, IVideoPrefsActivity.class));
//				ivMediaPlayer.setVisibility(View.GONE);
//				
//				showProgressView();
//				if(enableLogs) Log.i(TAG, "Options Activity Started in onPrepareOptionsMenu event");
//				return true;
//			case android.R.id.home:
//				this.finish();
//				return true;
//			default:
//				return super.onOptionsItemSelected(item);
//			}
//		}
//		catch(OutOfMemoryError e)
//		{
//			if(enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
//			return true;
//		}
//		catch(Exception e)
//		{
//			if(enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
//			if(Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//			return true;
//		}
//	}
//
//
////	Read preferences for playing options audio and Video(images)
//	public  void readSetPreferences()
//	{
//		try
//		{
//			
//			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
//
//			Boolean bchkenabfps = false;
//
//
//			isLocalNetwork = false;
//				localnetworkSettings = sharedPrefs.getString("pref_enablocalnetwork" + camera.getCameraID(), "0");//("chkenablocalnetwork", false);
//				if(localnetworkSettings.equalsIgnoreCase("1")) 
//					isLocalNetwork = true; 
//				else 
//					isLocalNetwork = false;
//				
//
//
//			
//			
//// sajjad			if(bchkenabfps)
////				txtframerate.setVisibility(View.VISIBLE);
////			else
////				txtframerate.setVisibility(View.GONE);
//
//		}
//		catch(OutOfMemoryError e)
//		{
//			if(enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
//		}
//		catch(Exception ex)
//		{if(enableLogs) Log.e(TAG,ex.toString());if(Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ex);}
//	}
//
//	
//	/** Called when the activity is first created. */
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		try
//		{
//			super.onCreate(savedInstanceState);
//			
//			if(Constants.isAppTrackingEnabled)
//				if(Constants.isAppTrackingEnabled) BugSenseHandler.initAndStartSession(this, Constants.bugsense_ApiKey);
//			
//			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//			
//			int orientation=this.getResources().getConfiguration().orientation;
//		    if (orientation==Configuration.ORIENTATION_PORTRAIT){
//		    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//		    }
//		    else{
//		    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		    }
//		    
//			if(this.getActionBar() != null) 
//			{
//
//				this.getActionBar().setHomeButtonEnabled(true);
//				this.getActionBar().setTitle("");
//				this.getActionBar().setIcon(R.drawable.ic_navigation_back);
//			}
//
//			
//			setContentView(R.layout.videolayoutwithslide);
//			
//			myProgressView = ((ProgressView)iView.findViewById(R.id.ivprogressspinner1));
//			
//			iView = (RelativeLayout) this.findViewById(R.id.camimage1);
//			imgCam = (ImageView) this.findViewById(R.id.img_camera1);
//			ivMediaPlayer =(ImageView) this.findViewById(R.id.ivmediaplayer1);
//			
//			mSurface = (SurfaceView) findViewById(R.id.surface1);
//			holder = mSurface.getHolder();
//			holder.addCallback(this);
//			
//			addCamsToDropdownActionBar();
//
//			
//			
//	
//			
//			
//			
//			
//			
//			if(!Commons.isOnline(this)) // check whether the network is available or not?
//			{
//				try{
//					UIUtils.GetAlertDialog(VideoActivity.this, "Network not available", "Please connect to internat and try again", new DialogInterface.OnClickListener() {
//
//						public void onClick(DialogInterface dialog, int which) {
//							try{
//							// TODO Auto-generated method stub
//								//IVideoActivity.this.onStop();
//								paused = true;
////								end = true; // do not finish activity but
//								dialog.dismiss();
//								hideProgressView();
//							}catch(Exception e){if(Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);}
//						}
//					}).show();
//					return ;
//				}
//				catch(Exception e){if(Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);}
//			}
//	
//			
//			
//			
//			readSetPreferences();
//			
//			myProgressView.CanvasColor = Color.TRANSPARENT; // transparent color because image loaded in cache should be displayed as well
//			
//			isProgressShowing = true;
//			myProgressView.setVisibility(View.VISIBLE);
//			
//			ivMediaPlayer.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
//					if(end) {Toast.makeText(VideoActivity.this, "Please close and try again.", Toast.LENGTH_SHORT).show(); return;}
//					if(isProgressShowing) return;
//					if(paused) //  video is currently paused. Now we need to resume it.
//					{
//						playPlayer();
//						
//						ivMediaPlayer.setImageBitmap(null);
//						ivMediaPlayer.setVisibility(View.VISIBLE);
//						ivMediaPlayer.setImageResource(android.R.drawable.ic_media_pause);
//
//						startMediaPlayerAnimation();
//						
//						paused = false;
//					}
//					else // video is currently playing. Now we need to pause video
//					{
//						pausePlayer();
//						ivMediaPlayer.clearAnimation();
//						if(myFadeInAnimation != null && myFadeInAnimation.hasStarted() && !myFadeInAnimation.hasEnded())
//						{
//							myFadeInAnimation.cancel();
//							myFadeInAnimation.reset();
//						}
//						ivMediaPlayer.setVisibility(View.VISIBLE);
//						ivMediaPlayer.setImageBitmap(null);
//						ivMediaPlayer.setImageResource(android.R.drawable.ic_media_play);
//
//						paused = true; // mark the images as paused. Do not stop threads, but do not show the images showing up
//					}
//				}
//			});
//
//			                  
//			iView.setOnClickListener(new OnClickListener() {
//				public void onClick(View v) {
//					
//					
//					
//					if(end) {Toast.makeText(VideoActivity.this, "Please close and try again.", Toast.LENGTH_SHORT).show(); return;}
//					if(isProgressShowing) return;
//					
//					if (!paused && !end) // video is currently playing. Now we need to pause video
//					{
//						
//						VideoActivity.this.getActionBar().show();
//						ivMediaPlayer.setImageResource(android.R.drawable.ic_media_pause);
//						
//						ivMediaPlayer.setVisibility(View.VISIBLE);	
//							
//						startMediaPlayerAnimation();
//						
//					
//					}
//
//				}
//			});
//			if(enableLogs) Log.i(TAG, "Got image view " + iView.toString());
//
//			// Get the size of the device, will be our maximum.
//			Display display = getWindowManager().getDefaultDisplay();
//			screen_width = display.getWidth();
//			screen_height = display.getHeight();
//			if(enableLogs) Log.i(TAG, "Got Display specs");
//			
//			//  Keep the screen on 
//			
//			 
//			if(enableLogs) Log.i(TAG, "acquired the power lock");
//
//			//Thread
//			   
////			if (imageThread==null){
////			imageThread=new browseImages();
////			
////			}
//			
//			
//			
//			if(!this.paused)
//			{
//				StartResumeDownloading();
//			}
//			
//			
//
//		}
//		catch(OutOfMemoryError e)
//		{
//			if(enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
//		}
//		catch(Exception ex)
//		{
//			if(enableLogs) Log.e(TAG, ex.toString(), ex);
//			if(Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ex);
//		}
//	}
//	
//	
//	private void startMediaPlayerAnimation()
//	{
//		if (myFadeInAnimation != null)
//		{
//			myFadeInAnimation.cancel();
//			myFadeInAnimation.reset();
//			
//			ivMediaPlayer.clearAnimation();
//		}
//			
//			myFadeInAnimation = AnimationUtils.loadAnimation(VideoActivity.this, R.layout.fadein); 
//		
//			myFadeInAnimation.setAnimationListener(new Animation.AnimationListener() { 
//				public void onAnimationStart(Animation animation) { 
//					// TODO Auto-generated method stub 
//				} 
//				public void onAnimationRepeat(Animation animation) { 
//					// TODO Auto-generated method stub 
//				} 
//				public void onAnimationEnd(Animation animation) { 
//
//					if(!paused)
//						ivMediaPlayer.setVisibility(View.GONE); 
//					else
//						ivMediaPlayer.setVisibility(View.VISIBLE);
//					
//					int orientation = VideoActivity.this.getResources().getConfiguration().orientation;
//				    if (!paused && orientation == Configuration.ORIENTATION_LANDSCAPE){
//				    	VideoActivity.this.getActionBar().hide();
//				    }
//				} 
//			}); 
//
//
//			ivMediaPlayer.startAnimation(myFadeInAnimation);
//	}
//	
//	
//	private boolean isCurrentMRLValid()
//	{
//		if(mrlIndex < 0 || mrlIndex > mrls.size() || mrls.size() == 0)
//			return false;
//		return true;
//	}
//	private boolean isNextMRLValid()
//	{
//		if(mrlIndex + 1 > mrls.size() || mrls.size() == 0)
//			return false;
//		return true;
//	}
//	
//	private String getCurrentMRL()
//	{
//		if(isCurrentMRLValid()) return mrls.get(mrlIndex).MRL ;
//		return null;
//	}
//	
//	private String getNextMRL()
//	{
//		if(isNextMRLValid()) return mrls.get(++mrlIndex).MRL ;
//		return null;
//	}
//
//	/*************
//	 * Surface
//	 *************/
//
//	public void surfaceCreated(SurfaceHolder holder) {
//	}
//
//	public void surfaceChanged(SurfaceHolder surfaceholder, int format,
//			int width, int height) {
//		if (libvlc != null)
//			libvlc.attachSurface(holder.getSurface(), this);
//	}
//
//	public void surfaceDestroyed(SurfaceHolder surfaceholder) {
//	}
//
//	private void setSize(int width, int height) {
//		mVideoWidth = width;
//		mVideoHeight = height;
//		if (mVideoWidth * mVideoHeight <= 1)
//			return;
//
//		// get screen size
//		int w = getWindow().getDecorView().getWidth();
//		int h = getWindow().getDecorView().getHeight();
//
//		// getWindow().getDecorView() doesn't always take orientation into
//		// account, we have to correct the values
//		boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
//		if (w > h && isPortrait || w < h && !isPortrait) {
//			int i = w;
//			w = h;
//			h = i;
//		}
//
//		float videoAR = (float) mVideoWidth / (float) mVideoHeight;
//		float screenAR = (float) w / (float) h;
//
//		if (screenAR < videoAR)
//			h = (int) (w / videoAR);
//		else
//			w = (int) (h * videoAR);
//
//		// force surface buffer size
//		holder.setFixedSize(mVideoWidth, mVideoHeight);
//
//		// set display size
//		LayoutParams lp = mSurface.getLayoutParams();
//		lp.width = w;
//		lp.height = h;
//		mSurface.setLayoutParams(lp);
//		mSurface.invalidate();
//	}
//
//	@Override
//	public void setSurfaceSize(int width, int height, int visible_width,
//			int visible_height, int sar_num, int sar_den) {
//		Message msg = Message.obtain(mHandler, VideoSizeChanged, width, height);
//		msg.sendToTarget();
//	}
//	
//	/*************
//	 * Player
//	 *************/
//
//	private void createPlayer(String media) {
//		releasePlayer();
//		try {
//			if (media.length() > 0) {
//				Toast toast = Toast.makeText(this, media, Toast.LENGTH_LONG);
//				toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
//						0);
//				toast.show();
//			}
//
//			// Create a new media player
//			libvlc = LibVLC.getInstance();
//			//sajjad libvlc.setIomx(false);
//			libvlc.setSubtitlesEncoding("");
//			libvlc.setAout(LibVLC.AOUT_OPENSLES);
//			libvlc.setTimeStretching(true);
//			libvlc.setChroma("RV32");
//			libvlc.setVerboseMode(true);
//			LibVLC.restart(this);
//			EventHandler.getInstance().addHandler(mHandler);
//			holder.setFormat(PixelFormat.RGBX_8888);
//			holder.setKeepScreenOn(true);
//			MediaList list = libvlc.getMediaList();
//			list.clear();
//			list.add(new Media(libvlc, LibVLC.PathToURI(media)), false);
//			libvlc.playIndex(0);
//		} catch (Exception e) {
//			Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
//		}
//	}
//
//	private void releasePlayer() {
//		try{
//
//
//
//			if (libvlc == null)
//				return;
//			EventHandler.getInstance().removeHandler(mHandler);
//			libvlc.stop();
//			libvlc.detachSurface();
//			holder = null;
//			libvlc.closeAout();
//			libvlc.destroy();
//			libvlc = null;
//
//			mVideoWidth = 0;
//			mVideoHeight = 0;
//		}
//		catch(Exception e)
//		{
//			Log.e("sajjad", e.getMessage());
//		}
//	}
//
//
//	private void RestartPlay(String media)
//	{
//
//
//		if (libvlc == null)
//			return;
//
//
//		try {
//
//			libvlc.stop();
//
//			if (media.length() > 0) {
//				Toast toast = Toast.makeText(this, media, Toast.LENGTH_LONG);
//				toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
//						0);
//				toast.show();
//			}
//
//			libvlc.getMediaList().clear();
//			libvlc.playMRL(media);
//
//		} catch (Exception e) {
//			Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
//		}
//	}
//
//	private void pausePlayer()
//	{
//		try{
//		if (libvlc == null)
//			return;
//		libvlc.pause();
//		}catch(Exception e){}
//	}
//
//	private void playPlayer()
//	{
//		if (libvlc == null)
//			return;
//		libvlc.play();
//	}
//
//	/*************
//	 * Events
//	 *************/
//
//	private Handler mHandler = new MyHandler(this);
//
//	private static class MyHandler extends Handler {
//		private WeakReference<VideoActivity> mOwner;
//
//		public MyHandler(VideoActivity owner) {
//			mOwner = new WeakReference<VideoActivity>(owner);
//		}
//
//		@Override
//		public void handleMessage(Message msg) {
//
//			try{
//
//
//				VideoActivity player = mOwner.get();
//
//				// SamplePlayer events
//				if (msg.what == VideoSizeChanged) {
//					player.setSize(msg.arg1, msg.arg2);
//					return;
//				}
//
//				// Libvlc events
//				Bundle b = msg.getData();
//				int event = b.getInt("event");
//				//            if(event == EventHandler.MediaPlayerPositionChanged || event == EventHandler.MediaPlayerVout)
//				//            	return;
//				switch (event) {
//				case EventHandler.MediaPlayerEndReached:
//					player.releasePlayer();
//					break;
//				case EventHandler.MediaPlayerPlaying:
//					player.mrlPlaying = player.getCurrentMRL();
//					Log.e("sajjad","EventHandler.MediaPlayerPlaying");
//					player.mSurface.setVisibility(View.VISIBLE);
//					player.imgCam.setVisibility(View.GONE);
//					break;
//				
//				case EventHandler.MediaPlayerPaused:
//					Log.e("sajjad","EventHandler.MediaPlayerPaused");
//					break;
//				
//				case EventHandler.MediaPlayerStopped:
//					Log.e("sajjad","EventHandler.MediaPlayerStopped");
//					break;
//				
//				case EventHandler.MediaPlayerEncounteredError:
//					Log.e("sajjad","EventHandler.MediaPlayerEncounteredError");
//					
//					if(player.mrlPlaying == null && player.isNextMRLValid())
//						player.RestartPlay(player.getNextMRL());	
//					
//					break;
//
//				case EventHandler.MediaPlayerVout:
//					Log.e("sajjad","EventHandler.MediaPlayerVout");
//					break;
//				default:
//					break;
//				}
//
//			}catch(Exception e)
//			{
//				Log.e("sajjad", e.getMessage());
//			}
//		}
//	}
//
//	
//	
//	public void SetImageAttributesAndLoadImage(){
//		try{
////s		isFirstImageLiveReceived = false;
////s		isFirstImageLocalReceived = false;
////s		isFirstImageLiveEnded = false;
////s		isFirstImageLocalEnded = false;
//		
//		ivMediaPlayer.setVisibility(View.GONE); // hide the media player play icon when animation ends
//		
//		//showProgressView();
//		
//		readSetPreferences();
//		
////s		startDownloading = false;
//		this.paused = false;
//		this.end = false;
//		this.isShowingFailureMessage = false;
//		}
//		catch(Exception e){
//			Log.e(TAG, e.getMessage(),e);
//			if(Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//		}
//	}
//	
//	
//	
//	private class browseImages extends AsyncTask<String, String, String>{
//	
//		@Override
//		protected String doInBackground(String... params) {
//			// TODO Auto-generated method stub
//			int LiveTaskID= 0;
//			while (!end) // keep on sending requests until the activity ends
//			{
//				Log.d("Umar", "MainLoop");
//				try{
//					// wait for starting
//					try{while(!startDownloading) // if downloading has not started, keep on waiting until it starts
//					{if(enableLogs) Log.i(TAG, "going to sleep for half second.");;
//					Thread.sleep(500);}}catch(Exception e){if(Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);}
//					
//					
//					Log.d("Umar", "Ivideo->browseimage->paused="+paused);
//					if(!paused) // if application is paused, do not send the requests. Rather wait for the play command
//					{
//						downloadStartCount++;
//						DownloadImage tasklive = new DownloadImage();	
//						
//						if(LiveTaskID >= 100) LiveTaskID = 1;
//						
//						tasklive.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[] {	getImageUrlToPost() });
//						
//
//						if(downloadStartCount - downloadEndCount > 9)
//						{
//							sleepInterval += intervalAdjustment;
//						}
//						else if(sleepInterval >= sleepIntervalMinTime)
//						{
//							sleepInterval -= intervalAdjustment;
//						}
//					}
////					else
////					{
////						return null;
////					}
////					
//					
//
//				}
//				catch(RejectedExecutionException ree)
//				{
//					 Log.e(TAG, ree.toString() + "-::REE::-" + Log.getStackTraceString(ree));
//					 
////					sleepInterval += 100;
//				}
//				catch(OutOfMemoryError e)
//				{
//					 Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
//				}
//				catch(Exception ex)
//				{
//					downloadStartCount--;
//					 Log.e(TAG,ex.toString() + "-::::-" + Log.getStackTraceString(ex));
//					 if(Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ex);
//				}
//				try{
//					Thread.currentThread().sleep(sleepInterval,50);
//					Log.d("Umar", "sleepInterval"+sleepInterval);
//					}
//				catch(Exception e){if(enableLogs) Log.e(TAG, e.toString() + "-::::-" + Log.getStackTraceString(e));}
//			}
//
//			
//			
//			
//			
//			
//			return null;
//			
//			
//
//			
//		}
//		@Override
//		protected void onPostExecute(String s) {
//			Log.d("Umar", "onPostExecute() called!");
//			Log.d("Umar","end="+end);
//			Log.d("Umar", "paused="+paused);
//			Log.d("Umar", "startDownloading="+startDownloading);
//			
//		}
//		
//		
//	}
//	
//	
//	
//	
////	sajjad
////	private String getImageUrlToPost()
////	{
////		if(localnetworkSettings.equals("1"))
////			return imageLiveLocalURL;
////		else if(localnetworkSettings.equals("2"))
////			return imageLiveCameraURL;
////		else if(isLocalNetwork)
////			return imageLiveLocalURL;
////		else 
////			return imageLiveCameraURL;
////	}
//
//	@Override
//	public void onResume()
//	{
//		try{
//			super.onResume();
//			createPlayer(getCurrentMRL());
//			if(enableLogs) Log.i(TAG, "onResume called");
//			if(optionsActivityStarted)
//			{
//				optionsActivityStarted = false;
//			
//				if(enableLogs) Log.i(TAG, "onResume in block executed");
//				
//				showProgressView();
//				
//				readSetPreferences();
//				
//				startDownloading = false;
//				this.paused = false;
//				this.end = false;
//				this.isShowingFailureMessage = false;
//				
//				latestStartImageTime = SystemClock.uptimeMillis();
//				
//				
//				
//
//				
//				if(imageThread.getStatus()!=AsyncTask.Status.RUNNING)
//				{
//					imageThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
//				}
//				else if (imageThread.getStatus()==AsyncTask.Status.FINISHED){
//					imageThread=new browseImages();
//					imageThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
//				}
//			}
//			if(enableLogs) Log.i(TAG, "onResume ended");
//		}
//		catch(OutOfMemoryError e)
//		{
//			if(enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
//		}
//		catch(Exception e){if(enableLogs) Log.e(TAG, e.toString());if(Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);}
//	}
//	
//	// Hide progress view
//	void hideProgressView()
//	{
//		iView.findViewById(R.id.ivprogressspinner1).setVisibility(View.GONE);isProgressShowing = false;
//		isProgressShowing = false;
//	}
//	void showProgressView()
//	{
//			myProgressView.CanvasColor = Color.TRANSPARENT;
//			myProgressView.setVisibility(View.VISIBLE);
//			isProgressShowing = true;
//	}
//
////	When activity gets focused again
//	@Override
//	public void onRestart()
//	{
//		try{
//			super.onRestart();
//			if(enableLogs) Log.i(TAG, "onRestart called");
//			if(optionsActivityStarted)
//			{
//				
//
//				optionsActivityStarted = false;
//				this.paused = false;
//				this.end = false;
//				
//				showProgressView();
//				
//				
//				this.isShowingFailureMessage = false;
//				if(enableLogs) Log.i(TAG, "onRestart in block executed");
//				readSetPreferences();
//
//				startDownloading = false;
//				latestStartImageTime = SystemClock.uptimeMillis();
//
//				StartResumeDownloading();				
//							}
//		}
//		catch(OutOfMemoryError e)
//		{
//			if(enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
//		}
//		catch(Exception e){if(enableLogs) Log.e(TAG, e.toString());
//				if(Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//		}
//	}
//	
//	//	take care of the steps that are required to start downloading
//	public void StartResumeDownloading()
//	{
//		try
//		{
//			startDownloading = false;
//			
//			
//			isLocalNetwork = false;
//			latestStartImageTime = SystemClock.uptimeMillis();
//			if(localnetworkSettings.equalsIgnoreCase("1"))
//			{
//				DownloadImage task = new DownloadImage();
//				isLocalNetwork = true;
//				task.isLocalNetworkRequest = true;
//				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new String[] { imageLiveLocalURL });
//				
//			}
//			else if(localnetworkSettings.equalsIgnoreCase("2"))
//			{
//				DownloadImage task = new DownloadImage();
//				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new String[] { imageLiveCameraURL });
//			}
//			else // autodetect
//			{
//				// Local Network Request
//				DownloadImage task1 = new DownloadImage();
//				task1.isLocalNetworkRequest = true;
//				task1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new String[] { imageLiveLocalURL });
//
//				// Live Camera Request
//				DownloadImage task = new DownloadImage();
//				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new String[] { imageLiveCameraURL });
//				
//			}
//
//			end = false;	
//
//			
//			if (imageThread.getStatus()==AsyncTask.Status.FINISHED){
//				imageThread=new browseImages();
//				imageThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
//			}
//			else if (imageThread.getStatus()!=AsyncTask.Status.RUNNING){
//				imageThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
//			}
//			
//	
//			LoadImageFromCache();
//		}
//		catch(OutOfMemoryError e)
//		{
//			if(enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
//		}
//		catch(Exception e){if(enableLogs) Log.e(TAG, e.toString());if(Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);}
//	}
//	
//
//	
////	when activity loses focus. we need to end this activity 	
//	@Override
//	public void onPause()
//	{
//		try{
//			super.onPause();
//			releasePlayer();
//			if(enableLogs) Log.i(TAG, "onPause called");
//			isFirstImageLiveReceived = false;
//			isFirstImageLocalReceived = false;
//			isFirstImageLiveEnded = false;
//			isFirstImageLocalEnded = false;
//			if(!optionsActivityStarted)
//			{
//				this.paused = true;
//				this.end =true;
//				this.onDestroy();
//				if(enableLogs) Log.i(TAG, "onPause in block executed");
//				System.runFinalizersOnExit(true); 
//				//super.finish();
//				//this.finish();
//				
//				if(enableLogs) Log.i(TAG, "going to end the activity....." );
//			}
//		}
//		catch(Exception ex)
//		{if(Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ex);}
//	}
//
////	When activity loses focus and some other activity gets activated. We need to end this activity
//	
//	@Override
//	  public void onStart() {
//	    super.onStart();
//	    if(Constants.isAppTrackingEnabled)
//	    {
//	    	EasyTracker.getInstance().activityStart(this); // Add this method.
//	    	if(Constants.isAppTrackingEnabled) BugSenseHandler.startSession(this);
//	    }
//	  }
//	  
//	@Override
//	public void onStop()
//	{
//		try{
//			super.onStop();
//			if(enableLogs) Log.i(TAG, "onStop called");
//			isFirstImageLiveReceived = false;
//			isFirstImageLocalReceived = false;
//			isFirstImageLiveEnded = false;
//			isFirstImageLocalEnded = false;
//			if(!optionsActivityStarted)
//			{
//				this.paused = true;
//				this.end =true; // this will end the thread as well
//				if(enableLogs) Log.i(TAG, "onStop in block executed");
//				this.onDestroy();
//			}
//		}
//		catch(Exception ex)
//		{}
//		
//		if(Constants.isAppTrackingEnabled)
//		{
//			EasyTracker.getInstance().activityStop(this);
//			if(Constants.isAppTrackingEnabled) BugSenseHandler.closeSession(this);
//		}
//	}
//
//
////	when screen gets rotated
//	@Override
//	public void onConfigurationChanged(Configuration newConfig)
//	{
//		try{
//			Log.i("sajjadpp", "onConfigurationChanged called");
//			
//			super.onConfigurationChanged(newConfig);
//			
//	    	getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//	    	getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//	    	int orientation = newConfig.orientation;
//		    if (orientation==Configuration.ORIENTATION_PORTRAIT){
//		    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//		    	landscape = false;
//		    	this.getActionBar().show();
//		    }
//		    else{
//		    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		    	landscape = true;
//		    	
//		    	if(!paused && !end && !isProgressShowing)
//		    		this.getActionBar().hide();
//		    	else
//		    		this.getActionBar().show();
//		    }
//
//			this.invalidateOptionsMenu();
//			
//			mVideoWidth = mSurface.getWidth();
//			mVideoHeight = mSurface.getHeight() - this.getActionBar().getHeight();
//			setSize(mVideoWidth, mVideoHeight);
//			
//		}catch(Exception e){if(Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);}
//	}
//
////	resize the activity if screen gets rotated
//	public void resize(int imageHieght, int imageWidth)
//	{
//		int w = landscape ? screen_height : screen_width;
//		int h = landscape ? screen_width : screen_height;
//		
//		//sajjad h -= actionBarHeight;
//
//		// If we have the media, calculate best scaling inside bounds.
//		if (imageWidth > 0 && imageHieght > 0) {
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
//			w = (int)temp_w;
//			h = (int)temp_h;
//		}
//		media_height =(int) h;
//		media_width = (int) w;
//		if(enableLogs) Log.i(TAG, "resize method called: " + w + ":" + h);
//	}
//
//	
//
//
//
////	download the image from the camera
//	private class DownloadImage extends AsyncTask<String, Void, Drawable> {
//		private long myStartImageTime ;
//		private boolean isLocalNetworkRequest = false;
//		@Override
//		protected Drawable doInBackground(String... urls) {
//			Drawable response = null;
//			for (String url1 : urls) {
//				try
//				{
//					myStartImageTime = SystemClock.uptimeMillis();
//					
//					if(camera.getUseCredentials())
//					{
//						response = Commons.getDrawablefromUrlAuthenticated1(url1, camera.getCameraUserName(), camera.getCameraPassword(),camera.cookies,15000);
//						
//					}
//					else
//					{
//						URL url = new URL(url1);
//						response = Commons.DownlaodDrawableSync(url,15000);
//					}
//					if(response != null)
//						successiveFailureCount = 0; 
//				}
//				catch(OutOfMemoryError e)
//				{
//					if(enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
//					successiveFailureCount++;
//					return null;
//				}
//				catch (Exception e) {
//					if(enableLogs) Log.e(TAG,"Exception: " + e.toString() + "\r\n" + "ImageURl=[" + url1 +"]");
//					if(Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//					successiveFailureCount++;
//				}
//			}
//			return response;
//		}
//
//
//		@Override
//		protected void onPostExecute(Drawable result) {// DownloadImage Live
//			try
//			{  
//				downloadEndCount++;
//				try{if(isLocalNetworkRequest) isFirstImageLocalEnded = true; else isFirstImageLiveEnded = true;}catch(Exception ex){}
//				if(result != null && result.getIntrinsicWidth() > 0 && result.getIntrinsicHeight() > 0 && myStartImageTime >= latestStartImageTime && !paused && !end)
//				{
//					if(isLocalNetworkRequest) isFirstImageLocalReceived = true; else isFirstImageLiveReceived = true;
//					if(isLocalNetworkRequest && localnetworkSettings.equalsIgnoreCase("0")) isLocalNetwork = true;
//					
//					latestStartImageTime = myStartImageTime;
//					
//					if(ivMediaPlayer.getVisibility() != View.VISIBLE && VideoActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
//						VideoActivity.this.getActionBar().hide();
//					
//					imgCam.setImageDrawable(result);
//					
//					if(enableLogs) Log.i(TAG, "image loaded in ivideo");
//					txtframerate.setText(String.format("[F/s:%.2f]", (1000/(float)sleepInterval)));
//					
//					hideProgressView();
//					
//				}
//				// do not show message on local network failure request. 
//				else if(
//								( 		
//									(!isFirstImageLocalEnded && !isFirstImageLiveEnded && !isFirstImageLocalReceived && !isFirstImageLiveReceived &&  localnetworkSettings.equalsIgnoreCase("0")) // loclal task ended. Now this is live image request
//									|| successiveFailureCount > 10
//									
////									( 		(	!isFirstImageLocalReceived && localnetworkSettings.equalsIgnoreCase("1") ) 
////											|| (!isFirstImageLiveReceived && localnetworkSettings.equalsIgnoreCase("2") ) 
////											|| (isFirstImageLocalEnded && !isLocalNetworkRequest ) // loclal task ended. Now this is live image request
////											|| (isFirstImageLiveEnded && isLocalNetworkRequest ) // Image Live task ended. Now this is local image request
////											|| successiveFailureCount > 10
////											
//								) 
//								&& !isShowingFailureMessage && myStartImageTime >= latestStartImageTime && !paused && !end
//					) // end endif condition
//				{
//					isShowingFailureMessage = true;
//					hideProgressView();
//					UIUtils.GetAlertDialog(VideoActivity.this
//							, "Unable to connect"
//							, "Check camera and try again."
//							, new DialogInterface.OnClickListener() {
//						
//											public void onClick(DialogInterface dialog, int which) {
//												try{
//												//IVideoActivity.this.onStop();
//												VideoActivity.this.getActionBar().show();
//												paused = true;
////												end = true; // do not finish activity but
//												isShowingFailureMessage = false;
//												dialog.dismiss();
//												hideProgressView();
//											}
//											catch(Exception e){if(Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);}
//											}
//								}).show();
//				}
//				else
//				{
//					if(enableLogs) Log.i(TAG, "downloaded image discarded. ");
//				}
//			}
//			catch(OutOfMemoryError e)
//			{
//				if(enableLogs) Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
//			}
//			catch(Exception e){if(enableLogs) Log.e(TAG,e.toString());if(Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);}
//			try{startDownloading = true;}catch(Exception ex){}
//		}
//	}
//	
//	static public class MRLCamba
//	{
//		public String MRL = "";
//		public boolean isLocalNetwork = false;
//		
//		public MRLCamba(String _MRL, boolean _isLocalNetwork)
//		{
//			MRL = _MRL;
//			isLocalNetwork = _isLocalNetwork;
//		}
//	}
//
//
//	
//public void onSlideMenuItemClick(int itemId) {
//		
//		for(Camera caml :  AppData.camesList)
//		{
//			if(caml.getCameraID() == itemId && caml.getCameraID() != camera.getCameraID())
//			{
//				VideoActivity.StartPlayingVIdeo(VideoActivity.this, caml);
//				return;
//			}
//		}
//	}
// }
