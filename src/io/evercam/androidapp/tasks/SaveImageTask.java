package io.evercam.androidapp.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.evercam.androidapp.utils.EvercamFile;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.util.Log;

public class SaveImageTask extends AsyncTask<Void, Void, Void>
{
	private final String TAG = "evercamplay-SaveImageTask";
	private Context context;
	private Bitmap bitmap;
	private String cameraId;

	public SaveImageTask(Context context, Bitmap bitmap, String cameraId)
	{
		this.context = context;
		this.bitmap = bitmap;
		this.cameraId = cameraId;
	}

	@Override
	protected Void doInBackground(Void... params)
	{
		try
		{
			File externalFile = EvercamFile.getExternalFile(context, cameraId);
			createFile(externalFile, bitmap);

			// Check the file is saved or not
			checkFile(externalFile);
		}
		catch (IOException e)
		{
			Log.e(TAG, "Error saving external file: " + Log.getStackTraceString(e));
		}

		try
		{
			File cacheFile = EvercamFile.getCacheFileRelative(context, cameraId);
			createFile(cacheFile, bitmap);

			checkFile(cacheFile);
		}
		catch (IOException e)
		{
			Log.e(TAG, "Error saving cache file: " + Log.getStackTraceString(e));
		}

		return null;
	}

	private void createFile(File file, Bitmap bitmap) throws IOException
	{
		if (bitmap != null)
		{
			if (file.exists())
			{
				file.delete();
			}
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			bitmap.compress(CompressFormat.PNG, 0, fos);
			fos.close();
		}
	}

	private void checkFile(File file)
	{
		if (file.exists())
		{
			if (file.length() > 0)
			{
				//Valid file exists, do nothing for now.
			}
			else
			{
				file.delete();
				Log.e(TAG, cameraId + " File Deleted. File was empty.");
			}
		}
		else
		{
			Log.e(TAG, "Unable to save image: " + cameraId);
		}
	}
}
