package io.evercam.android.dto;

import io.evercam.EvercamException;

import java.util.ArrayList;

import org.apache.http.cookie.Cookie;

import android.util.Log;

public class EvercamCamera
{
	public ImageLoadingStatus loadingStatus = ImageLoadingStatus.not_started;
	public ArrayList<Cookie> cookies = null;
	
	private final String TAG = "evercamapp-EvercamCamera";
	private io.evercam.Camera camera;
	private String cameraId;
	private String snapshotUrl;
	private String name;
	private String status;
	private String username;
	private String password;
	
	public EvercamCamera(io.evercam.Camera camera)
	{
		this.camera = camera;
	}
	
	public String getCameraId()
	{
		try
		{
			return camera.getId();
		}
		catch (EvercamException e)
		{
			Log.e(TAG,e.getMessage());
		}
		return "";
	}
	
	public String getName()
	{
		try
		{
			return camera.getName();
		}
		catch (EvercamException e)
		{
			Log.e(TAG,e.getMessage());
		}
		return "";
	}
	
	public String getInternalSnapshotUrl()
	{
		try
		{
			camera.getJpgExternalFullUrl();
		}
		catch (EvercamException e)
		{
			Log.e(TAG,e.getMessage());
		}
		return "";
	}
	
	public String getExternalSnapshotUrl()
	{
		try
		{
			camera.getJpgExternalFullUrl();
		}
		catch (EvercamException e)
		{
			Log.e(TAG,e.getMessage());
		}
		return "";
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
