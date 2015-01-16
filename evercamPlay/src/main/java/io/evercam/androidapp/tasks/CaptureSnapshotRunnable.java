package io.evercam.androidapp.tasks;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.evercam.androidapp.video.SnapshotManager;

public class CaptureSnapshotRunnable implements Runnable
{
    private final String TAG = "evercamplay_CaptureSnapshotTask";

    private Activity activity;
    private String path;
    private Bitmap bitmap;

    public CaptureSnapshotRunnable(Activity activity, String path, Bitmap bitmap)
    {
        this.activity = activity;
        this.path = path;
        this.bitmap = bitmap;
    }

    public static Bitmap drawableToBitmap(Drawable drawable)
    {
        if(drawable instanceof BitmapDrawable)
        {
            return ((BitmapDrawable) drawable).getBitmap();
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
        if(snapshotBitmap != null)
        {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            snapshotBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

            File f = new File(path);

            try
            {
                f.createNewFile();
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());
                fo.close();
                return f.getPath();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        return "";
    }

    @Override
    public void run()
    {
        if(bitmap != null)
        {
            String savedPath = capture(bitmap);
            if(!savedPath.isEmpty())
            {
                SnapshotManager.updateGallery(savedPath, activity);
            }
        }
    }
}
