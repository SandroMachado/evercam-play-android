package io.evercam.androidapp.utils;

public final class Constants
{
	public static final boolean isAppTrackingEnabled = true;

	public static final String bugsense_ApiKey = "560565cb";

	public static final String GCM_SENDER_ID = "614642678474";

	public static final String ErrorMessageNoConnectivity = "Connectivity lost. Please check network connectivity.";
	public static final int httptimeout = 1000 * 30 * 1; // 0 for default

	public static final String GCMNotificationIDString = "GCMNotificationID";

	// Preference keys
	public static final String KEY_CAMERA_PER_ROW = "lstgridcamerasperrow";
	
	/**
	 * Values of start activity for result
	 */
	public static final int REQUEST_CODE_ADD_CAMERA = 1;
	public static final int REQUEST_CODE_DELETE_CAMERA = 2;
	public static final int RESULT_TRUE = 1;
	public static final int RESULT_FALSE = 0;
}
