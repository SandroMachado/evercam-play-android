package io.evercam.android.utils;

import io.evercam.android.dto.AppUser;
import io.evercam.android.dto.EvercamCamera;

import java.util.ArrayList;
import java.util.List;

public final class AppData
{
	public static ArrayList<EvercamCamera> evercamCameraList = new ArrayList<EvercamCamera>();
	public static List<AppUser> appUsers;

	public static AppUser defaultUser = null;

	public void reset()
	{
		evercamCameraList.clear();
		appUsers = null;
		defaultUser = null;
	}
}
