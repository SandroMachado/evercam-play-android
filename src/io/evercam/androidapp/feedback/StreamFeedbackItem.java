package io.evercam.androidapp.feedback;

import android.content.Context;
import io.evercam.androidapp.utils.DataCollector;

public class StreamFeedbackItem 
{
	private String user = "";
	private Long timestamp;
	private Context context;
	
	//Camera stream details;
	private String url = "";
	private String camera_id = "";
	private Boolean is_success;
	private Float load_time; 
	
	//Device details
	private String network = "";
	private String app_version = "";
	private String device = "";
	private String android_version = "";
	
	public StreamFeedbackItem(Context context, String username, Boolean isSuccess)
	{
		this.context = context;
		this.user = username;
		this.is_success = isSuccess;
		this.timestamp = System.currentTimeMillis();
		setDeviceData() ;
	}
	
	public void setUrl(String url) 
	{
		this.url = url;
	}

	public void setCameraId(String cameraId) 
	{
		this.camera_id = cameraId;
	}
	
	public void setLoadTime(Float loadTime)
	{
		this.load_time = loadTime;
	}
	
	public Float getLoad_time()
	{
		return load_time;
	}

	public String getUser() 
	{
		return user;
	}
	
	public Long getTimestamp()
	{
		return timestamp;
	}
	
	public Boolean getIs_success()
	{
		return is_success;
	}

	public String getUrl() 
	{
		return url;
	}

	public String getNetwork() 
	{
		return network;
	}

	public String getCamera_id() 
	{
		return camera_id;
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

	private void setDeviceData() 
	{
		DataCollector dataCollector = new DataCollector(context);
		this.app_version = dataCollector.getAppVersion();
		this.network = dataCollector.getNetworkString();
		this.device = DataCollector.getDeviceName();
		this.android_version = DataCollector.getAndroidVersion();
	}
}
