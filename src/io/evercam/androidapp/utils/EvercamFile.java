package io.evercam.androidapp.utils;

import java.io.File;

import android.content.Context;

public class EvercamFile
{
	public static final String SUFFIX_JPG = ".jpg";

	public static File getCacheFile(Context context, String cameraId)
	{
		String cachePath = context.getCacheDir().getAbsolutePath() + File.separator
				+ cameraId + SUFFIX_JPG;
		return new File(cachePath);
	}
	
	public static File getCacheFileRelative(Context context, String cameraId)
	{
		String cachePath = context.getCacheDir() + File.separator
				+ cameraId + SUFFIX_JPG;
		return new File(cachePath);
	}

	public static File getExternalFile(Context context, String cameraId)
	{
		File externalFile = null;
		String extCachePath = context.getExternalFilesDir(null) + File.separator
				+ cameraId + SUFFIX_JPG;
		externalFile = new File(extCachePath);
		return externalFile;
	}
}
