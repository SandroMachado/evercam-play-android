package io.evercam.androidapp.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.evercam.androidapp.utils.EvercamFile;

public class SaveImageRunnable implements Runnable
{
    private static final String TAG = "SaveImageRunnable";
    private Context context;
    private String thumbnailUrl = "";
    private String cameraId;
    private Bitmap bitmap;

    public SaveImageRunnable(Context context, String thumbnailUrl, String cameraId)
    {
        this.context = context;
        this.thumbnailUrl = thumbnailUrl;
        this.cameraId = cameraId;
    }

    public SaveImageRunnable(Context context, Bitmap bitmap, String cameraId)
    {
        this.context = context;
        this.bitmap = bitmap;
        this.cameraId = cameraId;
    }

    @Override
    public void run()
    {
        saveImage();
    }

    public void saveImage()
    {
        if(bitmap == null && !thumbnailUrl.isEmpty())
        {
            bitmap = requestForBitmapByUrl();
        }

        try
        {
            File externalFile = EvercamFile.getExternalFile(context, cameraId);
            createFile(externalFile, bitmap);

            // Check the file is saved or not
            checkFile(externalFile);
        }
        catch(IOException e)
        {
            Log.e(TAG, "Error saving external file: " + Log.getStackTraceString(e));
        }

        try
        {
            File cacheFile = EvercamFile.getCacheFileRelative(context, cameraId);
            createFile(cacheFile, bitmap);

            checkFile(cacheFile);
        }
        catch(IOException e)
        {
            Log.e(TAG, "Error saving cache file: " + Log.getStackTraceString(e));
        }
    }

    private Bitmap requestForBitmapByUrl()
    {
        try
        {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder().url(thumbnailUrl).build();

            Response response = client.newCall(request).execute();
            return BitmapFactory.decodeStream(response.body().byteStream());
        }
        catch(IOException e)
        {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    private static void createFile(File file, Bitmap bitmap) throws IOException
    {
        if(bitmap != null)
        {
            if(file.exists())
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
        if(file.exists())
        {
            if(file.length() > 0)
            {
                // Valid file exists, do nothing for now.
                //Log.d(TAG, "Cache file saved: " + cameraId);
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
