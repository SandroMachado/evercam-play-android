package io.evercam.androidapp.utils;

public final class Constants
{

	public static final boolean isOfflineDebugging = false;

	public static final boolean isAppTrackingEnabled = true;

	public static final String bugsense_ApiKey = "560565cb";

	public static final String GCM_SENDER_ID = "614642678474";

	public static final String Success = "success";
	public static final String ErrorMessageGeneric = "Unexpected error. Please close and retry.";
	public static final String ErrorMessageNoConnectivity = "Connectivity lost. Please check network connectivity.";
	public static final String ErrorMessageInvalidCredentials = "Email or Password is incorrect. Please correct and try again.";
	public static final String ErrorMessageInvalidCredentialsAndLogout = "Email or Password is incorrect. Please give valid Email and Password to login.";
	public static final String ErrorMessageRefreshCamerasWhenNoCamerasExist = "Unable to load cameras from server. Please close and retry.";
	public static final String ErrorMessageRefreshCamerasWhenCamerasLoadedFromLocalDB = "Unable to refresh cameras from server.";
	public static final int httptimeout = 1000 * 30 * 1; // 0 for default

	// public static final int id_menu_settings = 500;
	// public static final int id_menu_logout = 501;
	// public static final int id_menu_about = 502;

	public static final String GCMNotificationIDString = "GCMNotificationID";

	public static int ALert_GCMRegistration = 1;
	public static int ALert_CameraMD = 2;
	public static int ALert_CameraOnline = 3;
	public static int ALert_CameraOffline = 4;

}
