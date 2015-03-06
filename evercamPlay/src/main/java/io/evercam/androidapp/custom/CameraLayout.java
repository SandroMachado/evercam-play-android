package io.evercam.androidapp.custom;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.NotificationCompatSideChannelService;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bugsense.trace.BugSenseHandler;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.http.cookie.Cookie;

import java.io.InputStream;
import java.util.ArrayList;

import io.evercam.androidapp.R;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.CameraStatus;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.dto.ImageLoadingStatus;
import io.evercam.androidapp.tasks.SaveImageRunnable;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.video.VideoActivity;

public class CameraLayout extends LinearLayout
{
    private static final String TAG = "CameraLayout";

    public RelativeLayout cameraRelativeLayout;

    public Context context;
    public EvercamCamera evercamCamera;
//    private DownloadLiveImageTask liveImageTask;

    private boolean end = false; // tells whether application has ended or not.
    // If it is
    // true, all tasks must end and no further
    // processing should be done in any thread.
    private ProgressView loadingAnimation = null;
    private ImageView snapshotImageView;
    private ImageView offlineImage = null;
    private GradientTitleLayout gradientLayout;

    // Handler for the handling the next request. It will call the image loading
    // thread so that it can proceed with next step.
    public final Handler handler = new Handler();

    public CameraLayout(final Activity activity, EvercamCamera camera, boolean showThumbnails)
    {
        super(activity.getApplicationContext());
        this.context = activity.getApplicationContext();

        try
        {
            evercamCamera = camera;

            this.setOrientation(LinearLayout.VERTICAL);
            this.setGravity(Gravity.START);
            this.setBackgroundColor(getResources().getColor(R.color.evercam_color_light_gray));

            cameraRelativeLayout = new RelativeLayout(context);
            RelativeLayout.LayoutParams ivParams = new RelativeLayout.LayoutParams(android.view
                    .ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams
                    .MATCH_PARENT);
            cameraRelativeLayout.setLayoutParams(ivParams);

            this.addView(cameraRelativeLayout);

            snapshotImageView = new ImageView(context);
            RelativeLayout.LayoutParams imageViewParams = new RelativeLayout.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            snapshotImageView.setLayoutParams(imageViewParams);
            snapshotImageView.setBackgroundColor(Color.TRANSPARENT);
            snapshotImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            cameraRelativeLayout.addView(snapshotImageView);

            // control to show progress spinner
            loadingAnimation = new ProgressView(context);
            RelativeLayout.LayoutParams ivProgressParams = new RelativeLayout.LayoutParams
                    (android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            ivProgressParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            ivProgressParams.addRule(RelativeLayout.CENTER_VERTICAL);
            loadingAnimation.setLayoutParams(ivProgressParams);

            cameraRelativeLayout.addView(loadingAnimation);

            offlineImage = new ImageView(context);
            RelativeLayout.LayoutParams offlineImageParams = new RelativeLayout.LayoutParams
                    (android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            offlineImageParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            offlineImageParams.addRule(RelativeLayout.CENTER_VERTICAL);
            offlineImage.setLayoutParams(offlineImageParams);
            cameraRelativeLayout.addView(offlineImage);
            offlineImage.setImageResource(R.drawable.cam_unavailable);
            offlineImage.setVisibility(View.INVISIBLE);

            gradientLayout = new GradientTitleLayout(activity);
            gradientLayout.setTitle(evercamCamera.getName());
            cameraRelativeLayout.addView(gradientLayout);

            cameraRelativeLayout.setClickable(true);

            // Show thumbnail returned from Evercam
            if(showThumbnails)
            {
                showThumbnail();
            }
            cameraRelativeLayout.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    VideoActivity.startPlayingVideoForCamera(activity, evercamCamera.getCameraId());
                }
            });
        }
        catch(OutOfMemoryError e)
        {
            Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
        }
        catch(Exception e)
        {
            Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
            if(Constants.isAppTrackingEnabled)
            {
                BugSenseHandler.sendException(e);
            }
        }
    }

    // This method will call the image
    // loading thread to further load from camera
    public void loadImage()
    {
        if(!end)
        {
            handler.postDelayed(LoadImageRunnable, 0);
        }
    }

    public void updateTitleIfDifferent()
    {
        for(EvercamCamera camera : AppData.evercamCameraList)
        {
            if(evercamCamera.getCameraId().equals(camera.getCameraId()))
            {
                gradientLayout.setTitle(camera.getName());
            }
        }
    }

    // Stop the image loading process. May be need to end current activity
    public boolean stopAllActivity()
    {
        end = true;
//        if(liveImageTask != null && liveImageTask.getStatus() != AsyncTask.Status.FINISHED)
//            liveImageTask.cancel(true);

        return true;
    }

    // Image loaded form camera and now set the controls appearance and text
    // accordingly
    private void setLayoutForLiveImageReceived()
    {
        evercamCamera.setStatus(CameraStatus.ACTIVE);
        offlineImage.setVisibility(View.INVISIBLE);

        if(cameraRelativeLayout.indexOfChild(loadingAnimation) >= 0)
        {
            loadingAnimation.setVisibility(View.GONE);
            cameraRelativeLayout.removeView(loadingAnimation);
        }

        handler.removeCallbacks(LoadImageRunnable);
    }

    private boolean showThumbnail()
    {
        String thumbnailUrl = evercamCamera.getThumbnailUrl();
        if(thumbnailUrl != null && !thumbnailUrl.isEmpty())
        {

            Picasso.with(context).load(thumbnailUrl).fit().into(snapshotImageView);

            loadingAnimation.setVisibility(View.GONE);
            cameraRelativeLayout.removeView(loadingAnimation);

            if(!evercamCamera.isActive())
            {
                showGreyImage();
                gradientLayout.showOfflineIcon(true);
            }
            return true;
        }
        else
        {
            offlineImage.setVisibility(View.VISIBLE);
            snapshotImageView.setBackgroundColor(getResources().getColor(R.color.evercam_color_dark_gray));
            gradientLayout.removeGradientShadow();
            CameraLayout.this.evercamCamera.loadingStatus = ImageLoadingStatus.live_not_received;
            handler.postDelayed(LoadImageRunnable, 0);
        }
        return false;
    }

    // Image not received form cache, Evercam nor camera side. Set the controls
    // appearance and text accordingly
    private void setLayoutForNoImageReceived()
    {
        if(cameraRelativeLayout.indexOfChild(loadingAnimation) >= 0)
        {
            loadingAnimation.setVisibility(View.GONE);
            cameraRelativeLayout.removeView(loadingAnimation);
        }

        if(!evercamCamera.isActive())
        {
            showGreyImage();

            gradientLayout.showOfflineIcon(true);
        }

        // animation must have been stopped when image loaded from cache
        handler.removeCallbacks(LoadImageRunnable);
    }

    public Runnable LoadImageRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                if(end) return;

                if(evercamCamera.loadingStatus == ImageLoadingStatus.not_started)
                {
//                    liveImageTask = new DownloadLiveImageTask();
//                    liveImageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    if(evercamCamera.isActive())
                    {
                        Target liveSnapshotTarget = new Target() {
                            @Override
                            public void onBitmapLoaded (final Bitmap bitmap, Picasso.LoadedFrom from)
                            {
                                Log.d(TAG, "onBitmapLoaded " + evercamCamera.getCameraId());
                                snapshotImageView.setImageBitmap(bitmap);
                                CameraLayout.this.evercamCamera.loadingStatus = ImageLoadingStatus.live_received;
                                handler.postDelayed(LoadImageRunnable, 0);
                                new Thread(new SaveImageRunnable(context, bitmap, evercamCamera.getCameraId()))
                                        .start();
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable)
                            {
                                Log.d(TAG, "onBitmapFailed " + evercamCamera.getCameraId());
                                if(errorDrawable == null)
                                {
                                    Log.d(TAG, "onBitmapFailed " + evercamCamera.getCameraId() + " drawable is null");
                                    CameraLayout.this.evercamCamera.loadingStatus = ImageLoadingStatus.live_not_received;
                                    handler.postDelayed(LoadImageRunnable, 0);
                                }
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable)
                            {

                            }
                        };
                        Picasso.with(context).load(evercamCamera.getSnapshotUrl()).into(liveSnapshotTarget);
                    }
                }
                else if(evercamCamera.loadingStatus == ImageLoadingStatus.live_received)
                {
                    setLayoutForLiveImageReceived();
                }
                else if(evercamCamera.loadingStatus == ImageLoadingStatus.live_not_received)
                {
                    setLayoutForNoImageReceived();
                }
            }
            catch(OutOfMemoryError e)
            {
                Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));

                handler.postDelayed(LoadImageRunnable, 5000);

                return;
            }
            catch(Exception e)
            {
                Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
                if(!end)
                {
                    handler.postDelayed(LoadImageRunnable, 5000);
                }
            }
        }
    };

    private void showGreyImage()
    {
        snapshotImageView.setAlpha(0.5f);
    }

//    private class DownloadLiveImageTask extends AsyncTask<Void, Drawable, Drawable>
//    {
//
//        public boolean isTaskended = false;
//
//        // Save image to external cache folder and return file path.
//        @Override
//        protected Drawable doInBackground(Void... params)
//        {
//            try
//            {
//                ArrayList<Cookie> cookies = new ArrayList<Cookie>();
//                Drawable drawable = null;
//                String externalJpgUrl = evercamCamera.getExternalSnapshotUrl();
//                String internalJpgUrl = evercamCamera.getInternalSnapshotUrl();
//                if(evercamCamera.hasCredentials())
//                {
//                    if(!evercamCamera.getExternalHost().isEmpty())
//                    {
//                        if(evercamCamera.isActive())
//                        {
//                            drawable = Commons.getDrawablefromUrlAuthenticated(externalJpgUrl,
//                                    evercamCamera.getUsername(), evercamCamera.getPassword(),
//                                    cookies, 5000);
//                        }
//
//                        if(drawable == null)
//                        {
//                            if(!evercamCamera.getInternalHost().isEmpty())
//                            {
//                                internalJpgUrl = evercamCamera.getInternalSnapshotUrl();
//                                drawable = Commons.getDrawablefromUrlAuthenticated
//                                        (internalJpgUrl, evercamCamera.getUsername(),
//                                                evercamCamera.getPassword(), cookies, 5000);
//                            }
//                        }
//                    }
//                    else
//                    {
//                        if(!internalJpgUrl.isEmpty())
//                        {
//                            drawable = Commons.getDrawablefromUrlAuthenticated(internalJpgUrl,
//                                    evercamCamera.getUsername(), evercamCamera.getPassword(),
//                                    cookies, 5000);
//                        }
//                    }
//                }
//                else
//                {
//                    if(evercamCamera.camera != null)
//                    {
//                        if(evercamCamera.camera.isOnline())
//                        {
//                            InputStream stream = evercamCamera.camera.getSnapshotFromEvercam();
//                            drawable = Drawable.createFromStream(stream, "src");
//                        }
//                    }
//                    else
//                    {
//                        Log.e(TAG, "EvercamCamera.camera is null");
//                    }
//                }
//                if(cookies.size() > 0)
//                {
//                    evercamCamera.cookies = cookies;
//                }
//
//                return drawable;
//            }
//            catch(OutOfMemoryError e)
//            {
//                Log.e(TAG, e.toString() + "-::OOM::-" + Log.getStackTraceString(e));
//                return null;
//            }
//            catch(Exception e)
//            {
//                if(e.getMessage() != null)
//                {
//                    Log.e(TAG, "Error request snapshot: " + e.getMessage());
//                }
//                else
//                {
//                    Log.e(TAG, "Error request snapshot: " + Log.getStackTraceString(e));
//                }
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Drawable drawable)
//        {
//            if(drawable != null && !end && drawable.getIntrinsicWidth() > 0 && drawable
//                    .getIntrinsicHeight() > 0)
//            {
//                snapshotImageView.setImageDrawable(drawable);
//                CameraLayout.this.evercamCamera.loadingStatus = ImageLoadingStatus.live_received;
//
//                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
//
//                new Thread(new SaveImageRunnable(context, bitmap, evercamCamera.getCameraId()))
//                        .start();
//            }
//
//            synchronized(this)
//            {
//                isTaskended = true;
//
//                if(liveImageTask.isTaskended && CameraLayout.this.evercamCamera.loadingStatus !=
//                        ImageLoadingStatus.live_received)
//                {
//                    CameraLayout.this.evercamCamera.loadingStatus = ImageLoadingStatus
//                            .live_not_received;
//                }
//                if(liveImageTask.isTaskended)
//                {
//                    handler.postDelayed(LoadImageRunnable, 0);
//                }
//            }
//        }
//    }
}