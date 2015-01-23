package io.evercam.androidapp.video;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import io.evercam.androidapp.R;
import io.evercam.androidapp.dto.EvercamCamera;

public class HomeShortcut
{
    private static final String KEY_CAMERA_ID = "cameraId";

    /**
     * Create a shortcut that link to specific camera live view on home screen
     */
    public static void create(Context context, EvercamCamera evercamCamera)
    {
        //The intent that launches the live view for specific camera
        Intent shortcutIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getLiveViewUri(context)));
        shortcutIntent.putExtra(KEY_CAMERA_ID, evercamCamera.getId());

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, evercamCamera.getName());
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context, R.drawable.icon_192x192));
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
        return context.getString(R.string.data_scheme) + "://" + context.getString(R.string.data_host)
                + context.getString(R.string.data_path);
    }
}
