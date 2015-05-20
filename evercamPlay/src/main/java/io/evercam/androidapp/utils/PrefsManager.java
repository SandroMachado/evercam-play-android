package io.evercam.androidapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefsManager
{
    public final static String KEY_CAMERA_PER_ROW = "lstgridcamerasperrow";
    public final static String KEY_RELEASE_NOTES_SHOWN = "isReleaseNotesShown";
    public static final String KEY_AWAKE_TIME = "prefsAwakeTime";
    public static final String KEY_FORCE_LANDSCAPE = "prefsForceLandscape";
    public static final String KEY_SHOW_OFFLINE_CAMERA = "prefsShowOfflineCameras";
    public static final String KEY_VERSION = "version_preference";
    public static final String KEY_ABOUT = "about_preference";

    public static int getCameraPerRow(Context context, int oldNumber)
    {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(sharedPrefs.getString(KEY_CAMERA_PER_ROW, "" + oldNumber));
    }

    public static void setCameraPerRow(Context context, int cameraPerRow)
    {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(KEY_CAMERA_PER_ROW, "" + cameraPerRow);
        editor.apply();
    }

    public static String getSleepTimeValue(Context context)
    {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(KEY_AWAKE_TIME, "" + 0);
    }

    public static boolean isForceLandscape(Context context)
    {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean(KEY_FORCE_LANDSCAPE, false);
    }

    public static boolean showOfflineCameras(Context context)
    {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean(KEY_SHOW_OFFLINE_CAMERA, false);
    }

    public static boolean isReleaseNotesShown(Context context, int versionCode)
    {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPrefs.getBoolean(KEY_RELEASE_NOTES_SHOWN + versionCode, false);
    }

    public static void setReleaseNotesShown(Context context, int versionCode)
    {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(KEY_RELEASE_NOTES_SHOWN + versionCode, true);
        editor.apply();
    }
}
