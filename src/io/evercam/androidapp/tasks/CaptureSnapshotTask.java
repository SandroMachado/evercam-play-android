package io.evercam.androidapp.tasks;

import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomToast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

public class CaptureSnapshotTask extends AsyncTask<Void, Void, Boolean> 
{
	private final String TAG = "evercamplay_CaptureSnapshotTask";
	private final String SNAPSHOT_FOLDER_NAME_EVERCAM = "Evercam";
	private final String SNAPSHOT_FOLDER_NAME_PLAY = "Evercam Play";
	
	private Activity activity;
	private String cameraId;
	private Drawable drawable;
	
	public CaptureSnapshotTask(Activity activity, String cameraId, Drawable drawable)
	{
		this.activity = activity;
		this.cameraId = cameraId;
		this.drawable = drawable;
	}

	@Override
	protected Boolean doInBackground(Void... params) 
	{
		Bitmap bitmap = drawableToBitmap(drawable);
		if(bitmap != null)
		{
			String savedPath = capture(bitmap);
			if(!savedPath.isEmpty())
			{
				updateGallery(savedPath);
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void onPostExecute(Boolean success) 
	{
		if(success)
		{
			CustomToast.showInCenter(activity, R.string.msg_snapshot_saved);
		}
		else
		{
			//This should never happen
			//TODO: But considering unexpected situation. Handle this later.
		}
	}

	public static Bitmap drawableToBitmap (Drawable drawable) 
	{
	    if (drawable instanceof BitmapDrawable) {
	        return ((BitmapDrawable)drawable).getBitmap();
	    }

	    int width = drawable.getIntrinsicWidth();
	    width = width > 0 ? width : 1;
	    int height = drawable.getIntrinsicHeight();
	    height = height > 0 ? height : 1;

	    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap); 
	    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
	    drawable.draw(canvas);

	    return bitmap;
	}
	
	public String capture(Bitmap snapshotBitmap)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String timeString = dateFormat.format(Calendar.getInstance().getTime());
		String fileName = cameraId + "_" + timeString + ".jpg";
		
		File folder = new File(
				Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
						+ File.separator + SNAPSHOT_FOLDER_NAME_EVERCAM + File.separator + SNAPSHOT_FOLDER_NAME_PLAY);
		if (!folder.exists())
		{
			folder.mkdirs();
		}

		if (snapshotBitmap != null)
		{
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			snapshotBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

			File f = new File(folder.getPath() + File.separator + fileName);

			try
			{
				f.createNewFile();
				FileOutputStream fo = new FileOutputStream(f);
				fo.write(bytes.toByteArray());
				fo.close();
				return f.getPath();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return "";
	}

	public void updateGallery(String path)
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
		{
			activity
					.sendBroadcast(new Intent(
							Intent.ACTION_MEDIA_MOUNTED,
							Uri.parse("file://"
									+ Environment
											.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))));
		}
		else
		{
			new SingleMediaScanner(activity,path);
		}
	}
	
	class SingleMediaScanner implements MediaScannerConnectionClient
	{
		MediaScannerConnection connection;
		Context ctxt;
		private String imagepath;

		public SingleMediaScanner(Context ctxt, String url)
		{
			this.ctxt = ctxt;
			startScan(url);
		}

		public void startScan(String url)
		{
			imagepath = url;
			if (connection != null) connection.disconnect();
			connection = new MediaScannerConnection(ctxt, this);
			connection.connect();
		}

		@Override
		public void onMediaScannerConnected()
		{
			try
			{
				connection.scanFile(imagepath, null);
			}
			catch (java.lang.IllegalStateException e)
			{
			}
		}

		@Override
		public void onScanCompleted(String path, Uri uri)
		{
			connection.disconnect();
		}
	}
}
