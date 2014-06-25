package io.evercam.androidapp.utils;

import io.evercam.androidapp.dto.EvercamCamera;

import java.io.File;

import android.content.Context;

public class EvercamFile
{
	public static final String SUFFIX_JPG = ".jpg";

	public static File getCacheFile(Context context, EvercamCamera evercamCamera)
	{
		String cachePath = context.getCacheDir().getAbsolutePath() + File.separator
				+ evercamCamera.getCameraId() + SUFFIX_JPG;
		return new File(cachePath);
	}

	public static File getExternalFile(Context context, EvercamCamera evercamCamera)
	{
		File externalFile = null;
		String extCachePath = context.getExternalFilesDir(null) + File.separator
				+ evercamCamera.getCameraId() + SUFFIX_JPG;
		externalFile = new File(extCachePath);
		return externalFile;
	}
}
