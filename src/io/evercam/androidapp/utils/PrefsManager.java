package io.evercam.androidapp.utils;

import android.content.SharedPreferences;

public class PrefsManager
{
	public final static String KEY_USER_API_KEY = "userApiKey";
	public final static String KEY_USER_API_ID = "userApiId";
	public final static String KEY_USER_EMAIL = "userEmail";
	public final static String KEY_CAMERA_PER_ROW = "lstgridcamerasperrow";

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

	public static int getCameraPerRow(SharedPreferences sharedPrefs, int oldNumber)
	{
		return Integer.parseInt(sharedPrefs.getString(KEY_CAMERA_PER_ROW, "" + oldNumber));
	}
}
