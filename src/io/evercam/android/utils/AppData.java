package io.evercam.android.utils;

import io.evercam.android.dto.AppUser;
import io.evercam.android.dto.Camera;

import java.util.ArrayList;
import java.util.List;

public final class AppData
{

	public static ArrayList<Camera> cameraList = new ArrayList<Camera>();
	public static List<AppUser> appUsers;

	public static AppUser defaultUser = null;
//	public static String default = null;
//	public static String cambaApiKey;

	public void reset()
	{
		cameraList.clear();
		appUsers = null;
		defaultUser = null;
	}
}
