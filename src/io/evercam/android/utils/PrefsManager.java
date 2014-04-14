//package io.evercam.android.utils;
//
//import android.content.SharedPreferences;
//
//public class PrefsManager
//{
//	public final static String KEY_USER_API_KEY = "userApiKey";
//	public final static String KEY_USER_API_ID = "userApiId";
//
//	public static void saveEvercamUserKeyPair(SharedPreferences sharedPrefs, String apiKey,
//			String apiId)
//	{
//		SharedPreferences.Editor editor = sharedPrefs.edit();
//		editor.putString(KEY_USER_API_KEY, apiKey);
//		editor.putString(KEY_USER_API_ID, apiId);
//		editor.commit();
//	}
//
//	public static String getUserApiKey(SharedPreferences sharedPrefs)
//	{
//		return sharedPrefs.getString(KEY_USER_API_KEY, null);
//	}
//
//	public static String getUserApiId(SharedPreferences sharedPrefs)
//	{
//		return sharedPrefs.getString(KEY_USER_API_ID, null);
//	}
//}
