package io.evercam.androidapp.tasks;

import io.evercam.Camera;
import io.evercam.EvercamException;
import io.evercam.Snapshot;
import io.evercam.androidapp.custom.CameraLayout;
import io.evercam.androidapp.dto.ImageLoadingStatus;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

public class DownloadLatestTask extends AsyncTask<Void, Void, Bitmap>
{
	private final String TAG = "evercamplay-DownloadLatestTask";
	String cameraId;
	Context context;
	CameraLayout cameraLayout;

	public DownloadLatestTask(String cameraId, CameraLayout cameraLayout)
	{
		this.cameraId = cameraId;
		this.cameraLayout = cameraLayout;
		this.context = cameraLayout.context;
	}

	@Override
	protected Bitmap doInBackground(Void... params)
	{
		try
		{
			Snapshot latestSnapshot = Camera.getLatestArchivedSnapshot(cameraId, true);
			byte[] snapshotByte = latestSnapshot.getData();

			Bitmap bitmap = BitmapFactory.decodeByteArray(snapshotByte, 0, snapshotByte.length);

			return bitmap;
		}
		catch (EvercamException e)
		{
			Log.e(TAG, e.toString());
		}
		catch (OutOfMemoryError e)
		{
			Log.e(TAG, e.toString());
		}
		return null;
	}

	@Override
	protected void onPostExecute(Bitmap bitmap)
	{
		if (bitmap != null)
		{
			new SaveImageTask(context, bitmap, cameraId)
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

			Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
			if (drawable != null && drawable.getIntrinsicWidth() > 0
					&& drawable.getIntrinsicHeight() > 0)
			{
				cameraLayout.cameraRelativeLayout.setVisibility(View.VISIBLE);
				cameraLayout.cameraRelativeLayout.setBackgroundDrawable(drawable);
				cameraLayout.evercamCamera.loadingStatus = ImageLoadingStatus.camba_image_received;
			}

			if (cameraLayout.evercamCamera.loadingStatus != ImageLoadingStatus.camba_image_received)
			{
				cameraLayout.evercamCamera.loadingStatus = ImageLoadingStatus.camba_not_received;
			}

			cameraLayout.handler.postDelayed(cameraLayout.LoadImageRunnable, 0);
		}
	}
}
