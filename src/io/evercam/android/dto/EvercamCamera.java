package io.evercam.android.dto;

import java.util.ArrayList;

import org.apache.http.cookie.Cookie;

public class EvercamCamera
{
	public ImageLoadingStatus loadingStatus = ImageLoadingStatus.not_started;
	public ArrayList<Cookie> cookies = null;
	
	private String cameraId;
	private String snapshotUrl;
	private String name;
	private String status;
	private String username;
	private String password;
	
	public String getCameraId()
	{
		return "liutingdu93662";
	}
	
	public String getName()
	{
		return "RemembranceCam";
	}
	
	public String getSnapshotUrl()
	{
		return "http://89.101.225.158:8101/Streaming/channels/1/picture";
	}
	
	public String getStatus()
	{
		return "Active";
	}
	
	public String getUsername()
	{
		return "admin";
	}
	
	public String getPassword()
	{
		return "12345";
	}
}
