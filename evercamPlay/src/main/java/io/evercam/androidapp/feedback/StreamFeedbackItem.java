package io.evercam.androidapp.feedback;

import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.util.Log;
import io.evercam.androidapp.utils.DataCollector;

public class StreamFeedbackItem 
{
	private final static String TAG = "evercamplay-StreamFeedbackItem";
	public final static String TYPE_JPG = "jpg";
	public final static String TYPE_RTSP = "rtsp";
	private String user = "";
	private Long timestamp;
	private Context context;
	
	//Camera stream details;
	private String url = "";
	private String type = "";
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
	
	public void setType(String type)
	{
		this.type = type;
	}
	
	public String getType()
	{
		return type;
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
	
	public String toJson()
	{
		JSONObject jsonObject = new JSONObject();
		try 
		{
			jsonObject.put("camera_id", camera_id);
			jsonObject.put("user", user);
			jsonObject.put("timestamp", timestamp);
			jsonObject.put("url", url);
			jsonObject.put("is_success", is_success);
			jsonObject.put("load_time", load_time);
			jsonObject.put("network", network);
			jsonObject.put("app_version", app_version);
			jsonObject.put("device", device);
			jsonObject.put("android_version", android_version);
			jsonObject.put("type", type);
		} 
		catch (JSONException e) 
		{
			Log.e(TAG, e.toString());
		}
		return jsonObject.toString();
	}
}
