package io.evercam.androidapp.video;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.util.Log;

import io.evercam.EvercamException;
import io.evercam.androidapp.R;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.utils.EvercamFile;

public class HomeShortcut
{
    public static final String KEY_CAMERA_ID = "cameraId";

    private static final String TAG = "evercamplay-HomeShortcut";

    /**
     * Create a shortcut that link to specific camera live view on home screen
     */
    public static void create(Context context, EvercamCamera evercamCamera)
    {
        //The intent that launches the live view for specific camera
        Intent shortcutIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getLiveViewUri(context)));
        shortcutIntent.putExtra(KEY_CAMERA_ID, evercamCamera.getCameraId());

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, evercamCamera.getName());
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, getIconForShortcut(evercamCamera, context));
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        addIntent.putExtra("duplicate", false);

        //If the 'duplicate' not working, uninstall and then install it
        addIntent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
        context.sendBroadcast(addIntent);

        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        context.sendBroadcast(addIntent);
    }

    private static String getLiveViewUri(Context context)
    {
        return context.getString(R.string.data_scheme) + "://" + context.getString(R.string
                .data_host) + context.getString(R.string.data_path);
    }

    private static Bitmap getIconForShortcut(EvercamCamera evercamCamera, Context context)
    {
        Bitmap bitmap = null;
        try
        {
            bitmap = getThumbnailFor(context, evercamCamera);

            if(bitmap == null)
            {
                bitmap = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.icon_192x192);
                return Bitmap.createScaledBitmap(bitmap, 192, 192, false);
            }

            //Resize the thumbnail for desktop icon size
            bitmap = Bitmap.createScaledBitmap(bitmap, 192, 192, false);

            //Rounded image corner
            bitmap = getRoundedCornerBitmap(bitmap);

            //Rounded gray corner
            bitmap = addBorder(bitmap, 3, 3, 3, 3, Color.GRAY);
            bitmap = getRoundedCornerBitmap(bitmap);

            //Transparent border that makes the icon smaller to enlarge Evercam logo
            bitmap = addBorder(bitmap, 0, 30, 20, 30, Color.TRANSPARENT);

            //Append Evercam logo as overlay
            Bitmap logoBitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.icon_40x40);
            appendOverlay(bitmap, logoBitmap);
        }
        catch(EvercamException e)
        {
            Log.e(TAG, e.getMessage());
        }

        return bitmap;
    }

    private static Bitmap getThumbnailFor(Context context, EvercamCamera evercamCamera) throws
            EvercamException
    {
        if(evercamCamera.camera != null)
        {
            //Load thumbnail from Evercam camera object if not null
            byte[] snapshotByte = evercamCamera.camera.getThumbnailData();
            return BitmapFactory.decodeByteArray(snapshotByte, 0, snapshotByte.length);
        }
        else
        {
            //Otherwise load from cache
            return EvercamFile.loadBitmapForCamera(context, evercamCamera.getCameraId());
        }
    }

    /**
     * Add border to existing bitmap
     */
    private static Bitmap addBorder(Bitmap bmp, int topBorderSize, int bottomBorderSize,
                                    int leftBorderSize, int rightBorderSize, int color)
    {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + leftBorderSize +
                rightBorderSize, bmp.getHeight() + topBorderSize + bottomBorderSize,
                bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(color);
        canvas.drawBitmap(bmp, leftBorderSize, topBorderSize, null);
        return bmpWithBorder;
    }

    /**
     * Transform existing bitmap to rounded corner
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap)
    {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 12;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static void appendOverlay(Bitmap bitmap, Bitmap overlay)
    {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(overlay, bitmap.getWidth() - 80, bitmap.getHeight() - 80, paint);
    }
}
