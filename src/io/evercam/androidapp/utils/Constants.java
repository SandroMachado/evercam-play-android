package io.evercam.androidapp.utils;

public final class Constants
{
	public static final boolean isAppTrackingEnabled = true;

	public static final String bugsense_ApiKey = "560565cb";
	public static final String LOGENTRIES_TOKEN = "65a08184-a03f-4f8a-ae05-a9ce8e66bdd6";

	public static final String GCM_SENDER_ID = "614642678474";

	public static final String ErrorMessageNoConnectivity = "Connectivity lost. Please check network connectivity.";
	public static final int httptimeout = 1000 * 30 * 1; // 0 for default

	public static final String GCMNotificationIDString = "GCMNotificationID";

	/**
	 * Values of start activity for result
	 */
	public static final int REQUEST_CODE_ADD_CAMERA = 1;
	public static final int REQUEST_CODE_DELETE_CAMERA = 2;
	public static final int REQUEST_CODE_PATCH_CAMERA = 3;
	public static final int REQUEST_CODE_VIEW_CAMERA = 4;
	public static final int REQUEST_CODE_MANAGE_ACCOUNT = 5;
	public static final int REQUEST_CODE_FEEDBACK = 6;
	public static final int REQUEST_CODE_SIGN_IN = 7;
	public static final int REQUEST_CODE_SIGN_UP = 8;
	public static final int RESULT_TRUE = 1;
	public static final int RESULT_FALSE = 0;
	public static final int RESULT_ACCOUNT_CHANGED = 1;

	// Values for intent bundle
	public static final String KEY_IS_EDIT = "isEdit";
	
	//Bundle key
	public static final String BUNDLE_KEY_CAMERA_ID = "cameraId";

}
