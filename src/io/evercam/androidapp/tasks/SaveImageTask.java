package io.evercam.androidapp.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.utils.EvercamFile;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

public class SaveImageTask extends AsyncTask<Void, Void, Void>
{
	private final String TAG = "evercamplay-SaveImageTask";
	private Context context;
	private Drawable drawable;
	private EvercamCamera evercamCamera;

	public SaveImageTask(Context context, Drawable drawable, EvercamCamera evercamCamera)
	{
		this.context = context;
		this.drawable = drawable;
		this.evercamCamera = evercamCamera;
	}

	@Override
	protected Void doInBackground(Void... params)
	{
		try
		{
			File externalFile = EvercamFile.getExternalFile(context, evercamCamera);
			createFile(externalFile, drawable);
			
			//Check the file is saved or not
			checkFile(externalFile);
		}
		catch (IOException e)
		{
			Log.e(TAG, "Error saving external file: " + Log.getStackTraceString(e));
		}
		
		try
		{
			File cacheFile = EvercamFile.getCacheFile(context, evercamCamera);
			createFile(cacheFile, drawable);
			
			checkFile(cacheFile);
		}
		catch (IOException e)
		{
			Log.e(TAG, "Error saving cache file: " + Log.getStackTraceString(e));
		}

		return null;
	}

	private void createFile(File file, Drawable drawable) throws IOException
	{
		if (drawable != null)
		{
			Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

			if (file.exists())
			{
				// file.delete();
			}
			else
			{
				file.createNewFile();
				FileOutputStream fos = new FileOutputStream(file);
				bitmap.compress(CompressFormat.PNG, 0, fos);
				fos.close();
			}
		}
	}

	private void checkFile(File file)
	{
		if (file.exists())
		{
			if (file.length() > 0)
			{

			}
			else
			{
				file.delete();
				Log.e(TAG, evercamCamera.getCameraId() + " File Deleted. File was empty.");
			}
		}
		else
		{
			Log.e(TAG, "Unable to save image: " + evercamCamera.getCameraId());
		}
	}
}
