package io.evercam.android.dto;

import java.util.ArrayList;
import org.apache.http.cookie.Cookie;
import android.util.Log;
import android.webkit.URLUtil;

//Camera object having information related to one camera received from the camba api

public class Camera
{
	private static final String TAG = "Camera";

	int ID;
	private int CameraID;

	private String UserEmail;

	public ImageLoadingStatus loadingStatus = ImageLoadingStatus.not_started;

	private String Name;
	private String Code;
	private String BaseUrl;
	private String Status;
	private String CameraUserName;
	private String CameraPassword;
	private String CameraImageUrl;
	private String LowResolutionSnapshotUrl;
	private String H264Url;
	private String MjpgUrl;
	private String Mpeg4Url;
	private String MobileUrl;
	private String AudioUrl;
	private String BrowserUrl;
	private boolean UseCredentials;
	private String OfflineTime;
	private String CameraTimeZone;
	private String CameraModel;
	private String CameraMake;
	private String HistoryDays;
	private String AccessMethod;
	private String LocalIpPort;
	private String RtspPort;
	private String RtspUrl;
	private boolean VideoRecording;

	private String AlarmLevel;
	private String CameraGroup;
	private int CameraStatusIntVal;
	private boolean IsMdEnabled;

	private String HourServerIp;

	private String UtcOffset;

	// Private URL variables

	public ArrayList<Cookie> cookies = null;

	public Camera()
	{
	}

	public Camera(int _ID, int _CameraID, String _UserEmail, String _AccessMethod,
			String _AudioUrl, String _BaseUrl, String _BrowserUrl, String _CameraImageUrl,
			String _CameraMake, String _CameraModel, String _CameraPassword,
			String _CameraTimeZone, String _CameraUserName, String _Code, String _H264Url,
			String _HistoryDays, String _LocalIpPort, String _LowResolutionSnapshotUrl,
			String _MjpgUrl, String _MobileUrl, String _Mpeg4Url, String _Name,
			String _OfflineTime, String _RtspPort, String _RtspUrl, String _Status,
			Boolean _UseCredentials, Boolean _VideoRecording, String _AlarmLevel,
			String _CameraGroup, int _CameraStatusIntVal, Boolean _isMdEnabled, String _UtcOffset)
	{
		ID = _ID;
		CameraID = _CameraID;
		UserEmail = _UserEmail;

		AccessMethod = _AccessMethod;
		AudioUrl = _AudioUrl;
		BaseUrl = _BaseUrl;
		BrowserUrl = _BrowserUrl;
		CameraImageUrl = _CameraImageUrl;
		CameraMake = _CameraMake;
		CameraModel = _CameraModel;
		CameraPassword = _CameraPassword;
		CameraTimeZone = _CameraTimeZone;
		CameraUserName = _CameraUserName;
		Code = _Code;
		H264Url = _H264Url;
		HistoryDays = _HistoryDays;
		LocalIpPort = _LocalIpPort;
		LowResolutionSnapshotUrl = _LowResolutionSnapshotUrl;
		MjpgUrl = _MjpgUrl;
		MobileUrl = _MobileUrl;
		Mpeg4Url = _Mpeg4Url;
		Name = _Name;
		OfflineTime = _OfflineTime;
		RtspPort = _RtspPort;
		RtspUrl = _RtspUrl;
		Status = _Status;
		UseCredentials = _UseCredentials;
		VideoRecording = _VideoRecording;

		AlarmLevel = _AlarmLevel;
		CameraGroup = _CameraGroup;
		CameraStatusIntVal = _CameraStatusIntVal;
		IsMdEnabled = _isMdEnabled;

		UtcOffset = _UtcOffset;

		// BasicClientCookie bcc = new BasicClientCookie("a", "b");

	}

	@Override
	public boolean equals(Object o)
	{
		Camera cam = (Camera) o;

		return
		// ID == cam.ID &&
		CameraID == cam.CameraID
				&& (UserEmail + "").equalsIgnoreCase(cam.UserEmail + "")
				&& (AccessMethod + "").equalsIgnoreCase(cam.AccessMethod + "")
				&& (AudioUrl + "").equalsIgnoreCase(cam.AudioUrl + "")
				&& (BaseUrl + "").equalsIgnoreCase(cam.BaseUrl + "")
				&& (BrowserUrl + "").equalsIgnoreCase(cam.BrowserUrl + "")
				&& (CameraImageUrl + "").equalsIgnoreCase(cam.CameraImageUrl + "")
				&& (CameraMake + "").equalsIgnoreCase(cam.CameraMake + "")
				&& (CameraModel + "").equalsIgnoreCase(cam.CameraModel + "")
				&& (CameraPassword + "").equalsIgnoreCase(cam.CameraPassword + "")
				&& (CameraTimeZone + "").equalsIgnoreCase(cam.CameraTimeZone + "")
				&& (CameraUserName + "").equalsIgnoreCase(cam.CameraUserName + "")
				&& (Code + "").equalsIgnoreCase(cam.Code + "")
				&& (H264Url + "").equalsIgnoreCase(cam.H264Url + "")
				&& (HistoryDays + "").equalsIgnoreCase(cam.HistoryDays + "")
				&& (LocalIpPort + "").equalsIgnoreCase(cam.LocalIpPort + "")
				&& (LowResolutionSnapshotUrl + "").equalsIgnoreCase(cam.LowResolutionSnapshotUrl
						+ "")
				&& (MjpgUrl + "").equalsIgnoreCase(cam.MjpgUrl + "")
				&& (MobileUrl + "").equalsIgnoreCase(cam.MobileUrl + "")
				&& (Mpeg4Url + "").equalsIgnoreCase(cam.Mpeg4Url + "")
				&& (Name + "").equalsIgnoreCase(cam.Name + "")
				&&
				// (OfflineTime + "").equalsIgnoreCase(cam.OfflineTime + "") &&
				(RtspPort + "").equalsIgnoreCase(cam.RtspPort + "")
				&& (RtspUrl + "").equalsIgnoreCase(cam.RtspUrl + "")
				&& (Status + "").equalsIgnoreCase(cam.Status + "")
				&& UseCredentials == cam.UseCredentials && VideoRecording == cam.VideoRecording
				&& (AlarmLevel + "").equalsIgnoreCase(cam.AlarmLevel + "")
				&& (CameraGroup + "").equalsIgnoreCase(cam.CameraGroup + "")
				&& CameraStatusIntVal == cam.CameraStatusIntVal && IsMdEnabled == cam.IsMdEnabled
				&& (UtcOffset + "").equalsIgnoreCase(cam.UtcOffset + "")

		;

	}

	@Override
	public String toString()
	{

		// return
		// ", CameraID  ["+ CameraID + "]" +
		// ", CameraUserName ["+ CameraUserName + "]" +
		// ", Name ["+ Name + "]" +
		// ", H264Url ["+ H264Url + "]" +
		// ", RtspUrl ["+ RtspUrl + "]" +
		// ", MobileUrl ["+ MobileUrl + "]" +
		// ", Mpeg4Url ["+ Mpeg4Url + "]" +
		// ", MjpgUrl ["+ MjpgUrl + "]" +
		// ", RtspPort ["+ RtspPort + "]"
		//
		//
		// ;
		return "ID [" + ID + "]" + ", CameraID  [" + CameraID + "]" + ", UserEmail [" + UserEmail
				+ "]" + ", AccessMethod [" + AccessMethod + "]" + ", AudioUrl [" + AudioUrl + "]"
				+ ", BaseUrl [" + BaseUrl + "]" + ", BrowserUrl [" + BrowserUrl + "]"
				+ ", CameraImageUrl [" + CameraImageUrl + "]" + ", CameraMake [" + CameraMake + "]"
				+ ", CameraModel [" + CameraModel + "]" + ", CameraPassword [" + CameraPassword
				+ "]" + ", CameraTimeZone [" + CameraTimeZone + "]" + ", CameraUserName ["
				+ CameraUserName + "]" + ", Code [" + Code + "]" + ", H264Url [" + H264Url + "]"
				+ ", HistoryDays [" + HistoryDays + "]" + ", LocalIpPort [" + LocalIpPort + "]"
				+ ", LowResolutionSnapshotUrl  [" + LowResolutionSnapshotUrl + "]" + ", MjpgUrl ["
				+ MjpgUrl + "]" + ", MobileUrl [" + MobileUrl + "]" + ", Mpeg4Url [" + Mpeg4Url
				+ "]" + ", Name [" + Name + "]" + ", OfflineTime [" + OfflineTime + "]"
				+ ", RtspPort [" + RtspPort + "]" + ", RtspUrl [" + RtspUrl + "]" + ", Status ["
				+ Status + "]" + ", UseCredentials  [" + UseCredentials + "]"
				+ ", VideoRecording  [" + VideoRecording + "]" + ", loadingStatus:["
				+ loadingStatus + "]" + ", AlarmLevel:[" + AlarmLevel + "]" + ", CameraGroup:["
				+ CameraGroup + "]" + ", CameraStatusIntVal:[" + CameraStatusIntVal + "]"
				+ ", IsMdEnabled:[" + IsMdEnabled + "]" + ", UtcOffset:[" + UtcOffset + "]"

		;

	}

	public String getVideoStreamURL()
	{
		String videoURL = H264Url + "";
		if (videoURL == "") videoURL = MjpgUrl + "";
		else if (videoURL == "") videoURL = Mpeg4Url + "";
		else if (videoURL == "") videoURL = MjpgUrl + "";
		// if(useCredentials == "true" && !videoURL.isEmpty())
		if (videoURL != "")
		{
			int slashindex = videoURL.indexOf("//");
			videoURL = videoURL.substring(0, slashindex) + "//" + CameraUserName + ":"
					+ CameraPassword + "@" + videoURL.substring(slashindex + 2, videoURL.length());
			videoURL = videoURL.replaceFirst("http", "rtsp");
		}
		return videoURL;

	}

	public String getLowResSnapshotLocalURLforCameraPlaying()
	{

		String URL = null;
		if (URLUtil.isValidUrl(LowResolutionSnapshotUrl))
		{
			URL = LowResolutionSnapshotUrl;
			int start1 = URL.indexOf("//");
			start1 = URL.indexOf("/", start1 + 2);
			URL = URL.substring(start1);
			URL = "http://" + LocalIpPort + "/" + URL;
		}
		else if (URLUtil.isValidUrl(CameraImageUrl))
		{
			URL = CameraImageUrl;
			int start2 = URL.indexOf("//");
			start2 = URL.indexOf("/", start2 + 2);
			URL = URL.substring(start2);
			URL = "http://" + LocalIpPort + "/" + URL;
		}

		return URL;
	}

	public int getID()
	{
		return ID;
	}

	public String getUserEmail()
	{
		return UserEmail;
	}

	public int getCameraID()
	{
		return CameraID;
	}

	public String getAccessMethod()
	{
		return AccessMethod;
	}

	public String getAudioUrl()
	{
		return AudioUrl;
	}

	public String getBaseUrl()
	{
		return BaseUrl;
	}

	public String getBrowserUrl()
	{
		return BrowserUrl;
	}

	public String getCameraImageUrl()
	{
		return CameraImageUrl;
	}

	public String getCameraMake()
	{
		return CameraMake;
	}

	public String getCameraModel()
	{
		return CameraModel;
	}

	public String getCameraPassword()
	{
		return CameraPassword;
	}

	public String getCameraTimeZone()
	{
		return CameraTimeZone;
	}

	public String getCameraUserName()
	{
		return CameraUserName;
	}

	public String getCode()
	{
		return Code;
	}

	public String getH264Url()
	{
		return H264Url;
	}

	public String getHistoryDays()
	{
		return HistoryDays;
	}

	public String getLocalIpPort()
	{
		return LocalIpPort;
	}

	public String getLowResolutionSnapshotUrl()
	{
		return LowResolutionSnapshotUrl;
	}

	public String getMjpgUrl()
	{
		return MjpgUrl;
	}

	public String getMobileUrl()
	{
		return MobileUrl;
	}

	public String getMpeg4Url()
	{
		return Mpeg4Url;
	}

	public String getName()
	{
		return Name;
	}

	public String getOfflineTime()
	{
		return OfflineTime;
	}

	public String getRtspPort()
	{
		return RtspPort;
	}

	public String getRtspUrl()
	{
		return RtspUrl;
	}

	public String getStatus()
	{
		return Status;
	}

	public boolean getUseCredentials()
	{
		return UseCredentials;
	}

	public boolean getVideoRecording()
	{
		return VideoRecording;
	}

	public String getAlarmLevel()
	{
		return AlarmLevel;
	}

	public int getAlarmLevelInteger()
	{
		int value = 0;
		try
		{
			value = Integer.parseInt(AlarmLevel);
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getLocalizedMessage(), e);
		}
		return value;
	}

	public String getCameraGroup()
	{
		return CameraGroup;
	}

	public int getCameraStatusIntVal()
	{
		return CameraStatusIntVal;
	}

	public boolean getIsMdEnabled()
	{
		return IsMdEnabled;
	}

	public int getIsMdEnabledInteger()
	{
		return (IsMdEnabled ? 1 : 0);
	}

	public String getHourServerIp()
	{
		return HourServerIp;
	}

	public String getUtcOffset()
	{
		return UtcOffset;
	}

	public void setID(int _ID)
	{
		ID = _ID;
	}

	public void setUserEmail(String _UserEmail)
	{
		UserEmail = _UserEmail;
	}

	public void setCameraID(int _CameraID)
	{
		CameraID = _CameraID;
	}

	public void setAccessMethod(String _AccessMethod)
	{
		AccessMethod = _AccessMethod;
	}

	public void setAudioUrl(String _AudioUrl)
	{
		AudioUrl = _AudioUrl;
	}

	public void setBaseUrl(String _BaseUrl)
	{
		BaseUrl = _BaseUrl;
	}

	public void setBrowserUrl(String _BrowserUrl)
	{
		BrowserUrl = _BrowserUrl;
	}

	public void setCameraImageUrl(String _CameraImageUrl)
	{
		CameraImageUrl = _CameraImageUrl;
	}

	public void setCameraMake(String _CameraMake)
	{
		CameraMake = _CameraMake;
	}

	public void setCameraModel(String _CameraModel)
	{
		CameraModel = _CameraModel;
	}

	public void setCameraPassword(String _CameraPassword)
	{
		CameraPassword = _CameraPassword;
	}

	public void setCameraTimeZone(String _CameraTimeZone)
	{
		CameraTimeZone = _CameraTimeZone;
	}

	public void setCameraUserName(String _CameraUserName)
	{
		CameraUserName = _CameraUserName;
	}

	public void setCode(String _Code)
	{
		Code = _Code;
	}

	public void setH264Url(String _H264Url)
	{
		H264Url = _H264Url;
	}

	public void setHistoryDays(String _HistoryDays)
	{
		HistoryDays = _HistoryDays;
	}

	public void setLocalIpPort(String _LocalIpPort)
	{
		LocalIpPort = _LocalIpPort;
	}

	public void setLowResolutionSnapshotUrl(String _LowResolutionSnapshotUrl)
	{
		LowResolutionSnapshotUrl = _LowResolutionSnapshotUrl;
	}

	public void setMjpgUrl(String _MjpgUrl)
	{
		MjpgUrl = _MjpgUrl;
	}

	public void setMobileUrl(String _MobileUrl)
	{
		MobileUrl = _MobileUrl;
	}

	public void setMpeg4Url(String _Mpeg4Url)
	{
		Mpeg4Url = _Mpeg4Url;
	}

	public void setName(String _Name)
	{
		Name = _Name;
	}

	public void setOfflineTime(String _OfflineTime)
	{
		OfflineTime = _OfflineTime;
	}

	public void setRtspPort(String _RtspPort)
	{
		RtspPort = _RtspPort;
	}

	public void setRtspUrl(String _RtspUrl)
	{
		RtspUrl = _RtspUrl;
	}

	public void setStatus(String _Status)
	{
		Status = _Status;
	}

	public void setUseCredentials(Boolean _UseCredentials)
	{
		UseCredentials = _UseCredentials;
	}

	public void setVideoRecording(Boolean _VideoRecording)
	{
		VideoRecording = _VideoRecording;
	}

	public void setAlarmLevel(String _AlarmLevel)
	{
		AlarmLevel = _AlarmLevel;
	}

	public void setCameraGroup(String _CameraGroup)
	{
		CameraGroup = CameraGroup;
	}

	public void setCameraStatusIntVal(int _CameraStatusIntVal)
	{
		CameraStatusIntVal = _CameraStatusIntVal;
	}

	public void setIsMdEnabled(boolean _IsMdEnabled)
	{
		IsMdEnabled = _IsMdEnabled;
	}

	public void setIsMdEnabledInteger(int _IsMdEnabledInteger)
	{
		IsMdEnabled = (_IsMdEnabledInteger == 1);
	}

	public void setHourServerIp(String hourserverip)
	{
		this.HourServerIp = hourserverip;
	}

	public void setUtcOffset(String _UtcOffset)
	{
		UtcOffset = _UtcOffset;
	}

	public void SetCameraProperty(Camera obj, String propertyName, String propertyValue)
	{
		if (propertyName.equalsIgnoreCase("ID"))
		{
			obj.CameraID = Integer.parseInt(propertyValue);
		}
		else if (propertyName.equalsIgnoreCase("NAME"))
		{
			obj.Name = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("CODE"))
		{
			obj.Code = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("BASE_URL"))
		{
			obj.BaseUrl = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("STATUS"))
		{
			obj.Status = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("user_name"))
		{
			obj.CameraUserName = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("user_password"))
		{
			obj.CameraPassword = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("image_url"))
		{
			obj.CameraImageUrl = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("low_res_image_url"))
		{
			obj.LowResolutionSnapshotUrl = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("h264_url"))
		{
			obj.H264Url = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("mjpg_url"))
		{
			obj.MjpgUrl = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("mpeg4_url"))
		{
			obj.Mpeg4Url = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("use_credentials"))
		{
			obj.UseCredentials = (propertyValue + "").equalsIgnoreCase("true");
		}
		else if (propertyName.equalsIgnoreCase("offline_time"))
		{
			obj.OfflineTime = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("time_zone"))
		{
			obj.CameraTimeZone = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("model"))
		{
			obj.CameraModel = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("history_days"))
		{
			obj.HistoryDays = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("access_method"))
		{
			obj.AccessMethod = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("local_ip_port"))
		{
			obj.LocalIpPort = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("video_recording"))
		{
			obj.VideoRecording = (propertyValue + "").equalsIgnoreCase("true");
		}
		else if (propertyName.equalsIgnoreCase("browser_url"))
		{
			obj.BrowserUrl = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("make"))
		{
			obj.CameraMake = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("mobile_url"))
		{
			obj.MobileUrl = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("local_rtsp_port"))
		{
			obj.RtspPort = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("rtsp_url"))
		{
			obj.RtspUrl = propertyValue;
		}

		else if (propertyName.equalsIgnoreCase("AlarmLevel")) // sajjad
		{
			obj.AlarmLevel = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("server_group"))
		{
			obj.CameraGroup = propertyValue;
		}
		else if (propertyName.equalsIgnoreCase("status_code"))
		{
			obj.CameraStatusIntVal = Integer.getInteger(propertyValue, 0);
		}
		else if (propertyName.equalsIgnoreCase("AlertsEnabled"))
		{
			obj.IsMdEnabled = (propertyValue + "").equalsIgnoreCase("true");
		}

		else if (propertyName.equalsIgnoreCase("utc_offset"))
		{
			obj.UtcOffset = propertyValue;
		}

	}

}
