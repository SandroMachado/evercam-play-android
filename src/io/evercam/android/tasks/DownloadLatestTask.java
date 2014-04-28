package io.evercam.android.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.evercam.Camera;
import io.evercam.EvercamException;
import io.evercam.Snapshot;
import io.evercam.android.custom.CameraLayout;
import io.evercam.android.dto.ImageLoadingStatus;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;

public class DownloadLatestTask extends AsyncTask<Void, Void, String>
{
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
	protected String doInBackground(Void... params)
	{
		try
		{
			Snapshot latestSnapshot = Camera.getLatestArchivedSnapshot(cameraId, true);
			byte[] snapshotByte = latestSnapshot.getData();

			String pathString = context.getCacheDir() + "/" + cameraId + ".jpg";
			File file = new File(pathString);
			if (file.exists())
			{
				file.delete();
			}
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);

			fos.write(snapshotByte);

			fos.flush();
			fos.close();

			if (file.exists() && file.length() > 0)
			{
				return pathString;
			}
			else if (file.exists())
			{
				file.delete();
			}
		}
		catch (EvercamException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(String result)
	{
		if (result != null)
		{
			Drawable drawable = Drawable.createFromPath(result);
			if (drawable != null && drawable.getIntrinsicWidth() > 0 && drawable.getIntrinsicHeight() > 0)
			{
				cameraLayout.cameraRelativeLayout.setVisibility(View.VISIBLE);
				cameraLayout.cameraRelativeLayout.setBackgroundDrawable(drawable);
				cameraLayout.evercamCamera.loadingStatus = ImageLoadingStatus.camba_image_received;
			}

			try
			{
				if (cameraLayout.evercamCamera.loadingStatus != ImageLoadingStatus.camba_image_received)
				{
					cameraLayout.evercamCamera.loadingStatus = ImageLoadingStatus.camba_not_received;
				}
			}
			catch (Exception e)
			{
			}
		}
	}

}
