package io.evercam.androidapp.tasks;

import io.evercam.androidapp.utils.EvercamFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

public class SaveImageRunnable implements Runnable
{
	private static final String TAG = "evercamplay-SaveImageRunnable";
	private Context context;
	private Bitmap bitmap;
	private static String cameraId;

	public SaveImageRunnable(Context context, Bitmap bitmap, String cameraId)
	{
		this.context = context;
		this.bitmap = bitmap;
		SaveImageRunnable.cameraId = cameraId;
	}

	@Override
	public void run()
	{
		saveImage(context, bitmap, cameraId);
	}

	public static void saveImage(Context context, Bitmap bitmap, String cameraId)
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
	}

	private static void createFile(File file, Bitmap bitmap) throws IOException
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

	private static void checkFile(File file)
	{
		if (file.exists())
		{
			if (file.length() > 0)
			{
				// Valid file exists, do nothing for now.
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
