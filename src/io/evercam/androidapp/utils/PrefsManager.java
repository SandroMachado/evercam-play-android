package io.evercam.androidapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefsManager
{
	public final static String KEY_USER_API_KEY = "userApiKey";
	public final static String KEY_USER_API_ID = "userApiId";
	public final static String KEY_USER_EMAIL = "userEmail";
	public final static String KEY_CAMERA_PER_ROW = "lstgridcamerasperrow";
	public final static String KEY_RELEASE_NOTES_SHOWN = "isReleaseNotesShown";
	public static final String KEY_AWAKE_TIME = "prefsAwakeTime";
	public static final String KEY_SCREEN_ROTATE = "prefsScreenRotate";

	public static void saveEvercamUserKeyPair(SharedPreferences sharedPrefs, String apiKey,
			String apiId)
	{
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString(KEY_USER_API_KEY, apiKey);
		editor.putString(KEY_USER_API_ID, apiId);
		editor.commit();
	}

	public static String getUserApiKey(SharedPreferences sharedPrefs)
	{
		return sharedPrefs.getString(KEY_USER_API_KEY, null);
	}

	public static String getUserApiId(SharedPreferences sharedPrefs)
	{
		return sharedPrefs.getString(KEY_USER_API_ID, null);
	}

	public static void saveUserEmail(SharedPreferences sharedPrefs, String email)
	{
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString(KEY_USER_EMAIL, email);
		editor.commit();
	}

	public static void saveUserEmail(Context context, String email)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString(KEY_USER_EMAIL, email);
		editor.commit();
	}

	public static String getUserEmail(Context context)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPrefs.getString(KEY_USER_EMAIL, null);
	}

	public static String getUserEmail(SharedPreferences sharedPrefs)
	{
		return sharedPrefs.getString(KEY_USER_EMAIL, null);
	}

	public static void removeUserEmail(SharedPreferences sharedPrefs)
	{
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString(KEY_USER_EMAIL, null);
		editor.commit();
	}

	public static int getCameraPerRow(Context context, int oldNumber)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(sharedPrefs.getString(KEY_CAMERA_PER_ROW, "" + oldNumber));
	}

	public static void setCameraPerRow(Context context, int cameraPerRow)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString(KEY_CAMERA_PER_ROW, "" + 2);
		editor.commit();
	}

	public static String getSleepTimeValue(Context context)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPrefs.getString(KEY_AWAKE_TIME, "" + 0);
	}
	
	public static boolean isRotateEnabled(Context context)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPrefs.getBoolean(KEY_SCREEN_ROTATE, true);
	}
	
	public static boolean isRleaseNotesShown(Context context, int versionCode)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

		return sharedPrefs.getBoolean(KEY_RELEASE_NOTES_SHOWN + versionCode, false);
	}

	public static void setReleaseNotesShown(Context context, int versionCode)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putBoolean(KEY_RELEASE_NOTES_SHOWN + versionCode, true);
		editor.commit();
	}
}
