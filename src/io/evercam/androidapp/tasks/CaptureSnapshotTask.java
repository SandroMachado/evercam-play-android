package io.evercam.androidapp.tasks;

import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.video.SnapshotManager;
import io.evercam.androidapp.video.VideoActivity;
import io.evercam.androidapp.video.SnapshotManager.FileType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

public class CaptureSnapshotTask extends AsyncTask<Void, Void, Boolean> 
{
	private final String TAG = "evercamplay_CaptureSnapshotTask";
	
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
				SnapshotManager.updateGallery(savedPath, activity);
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
			CustomToast.showSnapshotSaved(activity);
		}
		else
		{
			//This should never happen
			//But considering unexpected situation, show a toast
			CustomToast.showInBottom(activity, R.string.msg_snapshot_saved_failed);
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
		if (snapshotBitmap != null)
		{
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			snapshotBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

			File f = new File(SnapshotManager.createFilePath(cameraId, FileType.JPG));

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
}
