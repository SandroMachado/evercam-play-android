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

	private int id = -1;
	private String cameraId = "";
	private String name = "";
	private String owner = "";
	private String username = "";
	private String password = "";
	private String timezone = "";
	private String vendor = "";
	private String model = "";
	private String mac = "";
	private String externalSnapshotUrl = "";
	private String internalSnapshotUrl = "";
	private String externalRtspUrl = "";
	private String status = "";

	public EvercamCamera()
	{

	}

	public EvercamCamera convertFromEvercam(io.evercam.Camera camera)
	{
		try
		{
			cameraId = camera.getId();
			name = camera.getName();
			owner = camera.getOwner();
			username = camera.getCameraUsername();
			password = camera.getCameraPassword();
			timezone = camera.getTimezone();
			vendor = camera.getVendor();
			model = camera.getModel();
			mac = camera.getMacAddress();
			externalSnapshotUrl = camera.getExternalJpgUrl();
			internalSnapshotUrl = camera.getInternalJpgUrl();
			if(camera.isOnline())
			{
				status = CameraStatus.ACTIVE;
			}
			else
			{
				status = CameraStatus.OFFLINE;
			}
		}
		catch (EvercamException e)
		{
			Log.e(TAG, e.getMessage());
		}
		return this;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getStatus()
	{
		return status;
	}

	public String getCameraId()
	{
		return cameraId;
	}

	public String getExternalSnapshotUrl()
	{
		return externalSnapshotUrl;
	}

	public String getInternalSnapshotUrl()
	{
		return internalSnapshotUrl;
	}

	public String getName()
	{
		return name;
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}

	public String getTimezone()
	{
		return timezone;
	}

	public String getModel()
	{
		return model;
	}

	public String getVendor()
	{
		return vendor;
	}

	public String getOwner()
	{
		return owner;
	}

	public void setCameraId(String cameraId)
	{
		this.cameraId = cameraId;
	}

	public void setExternalSnapshotUrl(String externalSnapshotUrl)
	{
		this.externalSnapshotUrl = externalSnapshotUrl;
	}

	public void setInternalSnapshotUrl(String internalSnapshotUrl)
	{
		this.internalSnapshotUrl = internalSnapshotUrl;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public void setTimezone(String timezone)
	{
		this.timezone = timezone;
	}

	public void setModel(String model)
	{
		this.model = model;
	}

	public void setVendor(String vendor)
	{
		this.vendor = vendor;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public String getMac()
	{
		return mac;
	}

	public void setMac(String mac)
	{
		this.mac = mac;
	}

	public String getExternalRtspUrl()
	{
		return externalRtspUrl;
	}

	public void setExternalRtspUrl(String externalRtspUrl)
	{
		this.externalRtspUrl = externalRtspUrl;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}
	
	public String getCode()
	{
		return "";
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		EvercamCamera other = (EvercamCamera) obj;
		if (cameraId.equals(other.cameraId) && externalRtspUrl.equals(other.externalRtspUrl)
				&& externalSnapshotUrl.equals(other.externalSnapshotUrl)
				&& internalSnapshotUrl.equals(other.internalSnapshotUrl) && mac.equals(other.mac)
				&& model.equals(other.model) && name.equals(other.name)
				&& owner.equals(other.owner) && password.equals(other.password)
				&& status.equals(other.status) && timezone.equals(other.timezone)
				&& username.equals(other.username) && vendor.equals(other.vendor))
		{
			return true;
		}
		return false;
	}
}
