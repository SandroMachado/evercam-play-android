package io.evercam.androidapp.utils;

public final class Constants
{
    public static final boolean isAppTrackingEnabled = true;

    public static final String KEEN_COLLECTION_LIST_LOADING_TIME = "Camera List Loading Time";
    public static final String KEEN_COLLECTION_STREAM_LOADING_TIME = "Camera Live view Loading "
            + "Time";
    public static final String KEEN_COLLECTION_NEW_CAMERA = "New Camera Added";
    public static final String KEEN_COLLECTION_NEW_USER = "New User Created";
    public static final String KEEN_COLLECTION_HOME_SHORTCUT= "Home Shortcut";
    public static final String KEEN_COLLECTION_TEST_SNAPSHOT = "Test Snapshot";
    public static final String KEEN_COLLECTION_SCANNING_METRIC = "Scanning Metrics";
    public static final String KEEN_COLLECTION_DISCOVERED_CAMERAS = "Discovered Cameras";

    public static final String ErrorMessageNoConnectivity = "Connectivity lost. Please check " +
            "network connectivity.";

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

    /**
     * Bundle keys
     */
    public static final String BUNDLE_KEY_CAMERA_ID = "cameraId";
    public static final String BUNDLE_KEY_URL= "webUrl";
    public static final String KEY_IS_EDIT = "isEdit";

    /**
     * Regular Expressions
     */
    //Username should only contains lower case letters, numbers, dashes & underscores
    public static final String REGULAR_EXPRESSION_USERNAME = "^[A-Za-z0-9_-]*$";
    //Private IP address
    public static final String REGULAR_EXPRESSION_LOCAL_IP = "(127.0.0.1)|(192.168.*$)|(172.1[6-9].*$)|(172.2[0-9].*$)|(172.3[0-1].*$)|(10.*$)";

    public static final String API_MESSAGE_UNAUTHORIZED = "Unauthenticated";
    public static final String API_MESSAGE_INVALID_API_KEY = "Invalid user api key/id";
}
