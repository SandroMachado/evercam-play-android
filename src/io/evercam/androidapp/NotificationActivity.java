package io.evercam.androidapp;
//package io.evercam.androidapp;
//
//import io.evercam.androidapp.dal.DbNotifcation;
//import io.evercam.androidapp.dto.CameraNotification;
//import io.evercam.androidapp.ivideo.IVideoViewActivity;
//import io.evercam.androidapp.rvideo.RVideoViewActivity;
//import io.evercam.androidapp.utils.Commons;
//import io.evercam.androidapp.utils.Constants;
//import io.evercam.androidapp.utils.UIUtils;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.net.URL;
//
//import android.app.ActionBar;
//import android.app.Activity;
//import android.content.Intent;
//import android.graphics.Bitmap.CompressFormat;
//import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Drawable;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.util.Log;
//import android.util.TypedValue;
//import android.view.Display;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import com.bugsense.trace.BugSenseHandler;
//import io.evercam.androidapp.R;
//import com.google.analytics.tracking.android.EasyTracker;
//
//public class NotificationActivity extends Activity
//{
//	private static final String TAG = "NotificationActivity";
//
//	public static int NotificationID = 0;
//
//	TextView title = null;
//	io.evercam.androidapp.custom.ProgressView progressspinner = null;
//	RelativeLayout eventimage = null;
//	RelativeLayout buttonslayout = null;
//	Button btnLiveView = null;
//	Button btnEvent = null;
//	CameraNotification notif = null;
//	TextView noimagetext;
//	ImageView image = null;
//
//	/**
//	 * Standard Android on create method that gets called when the activity
//	 * initialized.
//	 */
//	@Override
//	public void onCreate(Bundle savedInstanceState)
//	{
//		super.onCreate(savedInstanceState);
//
//		if (Constants.isAppTrackingEnabled) if (Constants.isAppTrackingEnabled) BugSenseHandler
//				.initAndStartSession(this, Constants.bugsense_ApiKey);
//
//		try
//		{
//
//			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//					WindowManager.LayoutParams.FLAG_FULLSCREEN);
//			ActionBar actionb = this.getActionBar();
//			if (this.getActionBar() != null)
//			{
//
//				if (actionb != null)
//				{
//					actionb.setHomeButtonEnabled(true);
//					actionb.setTitle("Event Occured");
//					actionb.setIcon(R.drawable.ic_navigation_back);
//					// actionb.setBackgroundDrawable(null);
//				}
//			}
//
//			setContentView(R.layout.notificationactivitylayout);
//
//			// db
//			io.evercam.androidapp.dal.DbNotifcation handler = new DbNotifcation(
//					NotificationActivity.this);
//			notif = handler.getCameraNotification(NotificationID);
//			Log.i(TAG, notif.toString());
//			// notif.setSnapUrls("http://sphotos-e.ak.fbcdn.net/hphotos-ak-snc7/418668_10151061619497406_1824023559_n.jpg,http://sphotos-e.ak.fbcdn.net/hphotos-ak-snc7/418668_10151061619497406_1824023559_n.jpg");
//
//			// intializing guis pointers
//			title = (TextView) this.findViewById(R.id.notificationactivity_title);
//			progressspinner = (io.evercam.androidapp.custom.ProgressView) this
//					.findViewById(R.id.notificationactivity_progressspinner);
//			eventimage = (RelativeLayout) this.findViewById(R.id.notificationactivity_eventimage);
//			buttonslayout = (RelativeLayout) this.findViewById(R.id.notificationactivity_buttons);
//			btnLiveView = (Button) this.findViewById(R.id.notificationactivity_btngotoliveview);
//			;
//			btnEvent = (Button) this.findViewById(R.id.notificationactivity_btngotoevent);
//			;
//			noimagetext = (TextView) this.findViewById(R.id.notificationactivity_noimagetext);
//			image = (ImageView) this.findViewById(R.id.notificationactivity_image);
//
//			// setting the event buttons
//			Display disp = getWindowManager().getDefaultDisplay();
//			if (notif.getAlertTypeID() == Constants.ALert_CameraOffline)
//			{
//				btnLiveView.setVisibility(View.GONE);
//				int width = disp.getWidth() - 10;
//				btnEvent.setWidth(width);
//			}
//			else
//			// online & MD Alert
//			{
//				int width = disp.getWidth() / 2 - 20;
//				btnEvent.setWidth(width);
//				btnLiveView.setWidth(width);
//				btnLiveView.setOnClickListener(new View.OnClickListener(){
//
//					@Override
//					public void onClick(View v)
//					{
//						try
//						{
//							// TODO Auto-generated method stub
//							if (!IVideoViewActivity.startPlayingVIdeoForCamera(
//									NotificationActivity.this, notif.getCameraID()))
//							{
//								UIUtils.GetAlertDialog(NotificationActivity.this,
//										"Camera not found",
//										"Camera not found. Please refresh the cameras and try again.")
//										.show();
//							}
//							else
//							{
//								NotificationActivity.this.finish();
//							}
//
//						}
//						catch (Exception ex)
//						{
//							Log.e(TAG, ex.toString(), ex);
//							if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ex);
//						}
//					}
//				});
//			}
//			btnEvent.setOnClickListener(new View.OnClickListener(){
//
//				@Override
//				public void onClick(View v)
//				{
//					try
//					{
//						if (notif.getAlertTypeID() != Constants.ALert_CameraMD)
//						{
//							Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(notif
//									.getRecordingViewURL())); // .getSnapUrls().split(",")[0]
//							browserIntent.addCategory("android.intent.category.BROWSABLE");
//							startActivity(browserIntent);
//							NotificationActivity.this.finish();
//						}
//						else
//						{
//							// String URLString=notif.getSnapUrls();
//							// int end=URLString.indexOf(".jpg");
//							// String time=URLString.substring(end-17, end);
//
//							String URLString = notif.getRecordingViewURL();
//							String time = URLString.substring(URLString.length() - 17,
//									URLString.length());
//
//							if (!RVideoViewActivity.StartPlayingVIdeoForCamera(
//									NotificationActivity.this, notif.getCameraID(), time))
//							{
//								UIUtils.GetAlertDialog(NotificationActivity.this,
//										"Camera not found",
//										"Camera not found. Please refresh the cameras and try again.")
//										.show();
//							}
//							else
//							{
//								NotificationActivity.this.finish();
//							}
//						}
//
//					}
//					catch (Exception ex)
//					{
//						Log.e(TAG, ex.toString(), ex);
//						if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ex);
//					}
//				}
//			});
//
//			if (actionb != null)
//			{
//				// title.setText(notif.getAlertMessage());
//				actionb.setTitle("Camera " + notif.getAlertTypeText());
//				title.setText(notif.getAlertMessage());
//
//			}
//
//			// setting image if exists otherwise call the image loading thread
//			String firstURL = notif.getSnapUrls().split(",")[0].replace("[", "").replace("]", "");
//			Log.i(TAG, "firstURL [" + firstURL + "]");
//			String pathName = NotificationActivity.this.getCacheDir() + "/notif"
//					+ NotificationActivity.this.notif.getID() + ".jpg";
//
//			File file = new File(pathName);
//			Drawable d = null;
//			if (file.exists())
//			{
//				Log.i(TAG, "file exists");
//				d = Drawable.createFromPath(pathName);
//			}
//			if (d != null && d.getIntrinsicHeight() > 0 && d.getIntrinsicWidth() > 0)
//			{
//				Log.i(TAG, "d.getIntrinsicHeight() [" + d.getIntrinsicHeight()
//						+ "] d.getIntrinsicWidth() [" + d.getIntrinsicWidth() + "]");
//
//				Log.i(TAG, "d not null. ");
//
//				setEventImageDrawable(d);
//			}
//			else
//			{
//				new DownloadImageFromUrl().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
//						firstURL);
//			}
//		}
//		catch (Exception ex)
//		{
//			Log.e(TAG, ex.toString(), ex);
//			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(ex);
//		}
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item)
//	{
//		try
//		{
//			switch (item.getItemId())
//			{
//
//			case android.R.id.home:
//				this.finish();
//				return true;
//			default:
//				return super.onOptionsItemSelected(item);
//			}
//		}
//
//		catch (Exception e)
//		{
//			Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
//			if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//			return true;
//		}
//	}
//
//	public void resizeImageContainer(int imageHieght, int imageWidth)
//	{
//		int w = eventimage.getWidth();// ? screen_height : screen_width;
//		int h = eventimage.getHeight() - buttonslayout.getHeight();// landscape
//																	// ?
//																	// screen_width
//																	// :
//																	// screen_height;
//
//		if (w == 0 || h == 0)
//		{
//			TypedValue tv = new TypedValue();
//			this.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
//			int actionBarHeight = getResources().getDimensionPixelSize(tv.resourceId);
//
//			Display d = this.getWindowManager().getDefaultDisplay();
//			w = d.getWidth();
//			h = d.getHeight() - 2 * actionBarHeight;
//		}
//
//		Log.i(TAG, "1 - h [" + h + "] w [" + w + "] ");
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
//			Log.i(TAG, "h [" + h + "] w [" + w + "] th [" + temp_h + "] tw [" + temp_w + "]");
//
//			w = (int) temp_w;
//			h = (int) temp_h;
//		}
//		image.getLayoutParams().height = h;
//		image.getLayoutParams().width = w;
//	}
//
//	private class DownloadImageFromUrl extends AsyncTask<String, Void, Drawable>
//	{
//		@Override
//		protected Drawable doInBackground(String... URLs)
//		{
//
//			try
//			{
//				URL ImageURL = new URL(URLs[0]);
//				String pathString = NotificationActivity.this.getCacheDir() + "/notif"
//						+ NotificationActivity.this.notif.getID() + ".jpg";
//				File file = new File(pathString);
//				if (file.exists()) return Drawable.createFromPath(pathString);
//
//				Log.v(TAG, pathString);
//
//				Log.i(TAG, "going to download iamge from [" + ImageURL + "]");
//				Drawable d = Commons.DownlaodDrawableSync(ImageURL, 30000);
//
//				if (d != null)
//				{
//					if (file.exists()) file.delete();
//					file.createNewFile();
//					FileOutputStream fos = new FileOutputStream(file);
//					((BitmapDrawable) d).getBitmap().compress(CompressFormat.JPEG, 100, fos);
//					fos.close();
//					return d;
//				}
//				else return null;
//
//			}
//			catch (OutOfMemoryError e)
//			{
//				return null;
//			}
//			catch (Exception e)
//			{
//				Log.e(TAG, e.getMessage(), e);
//				if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//			}
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(Drawable result)
//		{
//			try
//			{
//				NotificationActivity.this.setEventImageDrawable(result);
//			}
//			catch (Exception e)
//			{
//				UIUtils.GetAlertDialog(NotificationActivity.this, "No Image Found",
//						"Event Image not found.");
//				if (Constants.isAppTrackingEnabled) BugSenseHandler.sendException(e);
//			}
//		}
//	}
//
//	public void setEventImageDrawable(Drawable result)
//	{
//		// TODO Auto-generated method stub
//
//		progressspinner.setVisibility(View.GONE);
//		if (result != null)
//		{
//			Log.i(TAG, "result.getIntrinsicHeight() [" + result.getIntrinsicHeight()
//					+ "] result.getIntrinsicWidth() [" + result.getIntrinsicWidth() + "]");
//			resizeImageContainer(result.getIntrinsicHeight(), result.getIntrinsicWidth());
//			image.setVisibility(View.VISIBLE);
//			image.setImageDrawable(result);
//		}
//		else
//		{
//			noimagetext.setVisibility(View.VISIBLE);
//		}
//
//	}
//
//	@Override
//	public void onStart()
//	{
//		super.onStart();
//
//		if (Constants.isAppTrackingEnabled)
//		{
//			EasyTracker.getInstance().activityStart(this);
//			if (Constants.isAppTrackingEnabled) BugSenseHandler.startSession(this);
//		}
//	}
//
//	@Override
//	public void onStop()
//	{
//		super.onStop();
//
//		if (Constants.isAppTrackingEnabled)
//		{
//			EasyTracker.getInstance().activityStop(this);
//			if (Constants.isAppTrackingEnabled) BugSenseHandler.closeSession(this);
//		}
//	}
//}
