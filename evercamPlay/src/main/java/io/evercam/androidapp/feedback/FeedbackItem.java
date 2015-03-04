package io.evercam.androidapp.feedback;

import android.content.Context;

import io.evercam.androidapp.utils.DataCollector;

public class FeedbackItem
{
    //Device details
    protected String network = "";
    protected String app_version = "";
    protected String device = "";
    protected String android_version = "";
    protected Context context;
    protected Long timestamp;
    protected String user = "";

    public FeedbackItem()
    {

    }

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
}
