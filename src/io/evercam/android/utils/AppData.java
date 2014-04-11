package io.evercam.android.utils;

import io.evercam.android.dto.AppUser;
import io.evercam.android.dto.Camera;

import java.util.ArrayList;
import java.util.List;

public final class AppData
{

	public static ArrayList<Camera> camesList = new ArrayList<Camera>();
	public static List<AppUser> appUsers;

	public static String AppUserEmail = null;
	public static String AppUserPassword = null;
	public static String cambaApiKey;

}
