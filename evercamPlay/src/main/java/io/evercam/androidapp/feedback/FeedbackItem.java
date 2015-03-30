package io.evercam.androidapp.feedback;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.evercam.androidapp.utils.DataCollector;
import io.keen.client.java.KeenClient;

public abstract class FeedbackItem
{
    private final String TAG = "FeedbackItem";
    protected final String FROM_ANDROID = "android";

    //Device details
    protected String network = "";
    protected String app_version = "";
    protected String device = "";
    protected String android_version = "";
    protected Context context;
    protected Long timestamp;
    protected String user = "";

    public FeedbackItem(Context context, String username)
    {
        this.context = context;
        this.user = username;
        this.timestamp = System.currentTimeMillis();
        setDeviceData();
    }

    private void setDeviceData()
    {
        DataCollector dataCollector = new DataCollector(context);
        this.app_version = dataCollector.getAppVersion();
        this.network = dataCollector.getNetworkString();
        this.device = DataCollector.getDeviceName();
        this.android_version = DataCollector.getAndroidVersion();
    }

    public String getUser()
    {
        return user;
    }

    public Long getTimestamp()
    {
        return timestamp;
    }

    public String getNetwork()
    {
        return network;
    }

    public String getApp_version()
    {
        return app_version;
    }

    public String getDevice()
    {
        return device;
    }

    public String getAndroid_version()
    {
        return android_version;
    }

    protected JSONObject getBaseJsonObject() throws JSONException
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user", user);
        jsonObject.put("timestamp", timestamp);
        jsonObject.put("network_type", network);
        jsonObject.put("app_version", app_version);
        jsonObject.put("device", device);
        jsonObject.put("android_version", android_version);
        return jsonObject;
    }

    private HashMap<String, Object> getBaseHashMap()
    {
        HashMap<String, Object> eventMap = new HashMap<>();
        eventMap.put("user", user);
        eventMap.put("network_type", network);
        eventMap.put("app_version", app_version);
        eventMap.put("device", device);
        eventMap.put("android_version", android_version);
        return eventMap;
    }

    public HashMap<String, Object> toHashMap()
    {
        return getBaseHashMap();
    }

    public abstract void sendToKeenIo(final KeenClient client);
}
