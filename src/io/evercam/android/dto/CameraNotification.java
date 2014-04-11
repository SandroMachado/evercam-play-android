package io.evercam.android.dto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class CameraNotification
{

	static String TAG = "CameraNotification";

	// private variables
	private int ID;
	private int CameraID;
	private String UserEmail;
	private int AlertTypeID;
	private String AlertTypeText;
	private String AlertMessage;
	private Date AlertTime;
	private String SnapUrls;
	private String RecordingViewURL;
	private boolean IsRead;

	// ID,
	// CameraID,
	// UserEmail,
	// AlertTypeID,
	// AlertTypeText,
	// AlertMessage,
	// AlertTime,
	// SnapUrls,
	// RecordingViewURL,
	// IsRead,
	//
	// KEY_ID, KEY_CameraID, KEY_UserEmail, KEY_AlertTypeID, KEY_AlertTypeText,
	// KEY_AlertMessage, KEY_AlertTime, KEY_SnapUrls, KEY_RecordingViewURL,
	// KEY_IsRead,

	// Empty constructor
	public CameraNotification()
	{

	}

	// constructor
	public CameraNotification(int _ID, int _CameraID, String _UserEmail, int _AlertTypeID,
			String _AlertTypeText, String _AlertMessage, Date _AlertTime, String _SnapUrls,
			String _RecordingViewURL, boolean _IsRead)
	{
		this.ID = _ID;
		this.CameraID = _CameraID;
		this.UserEmail = _UserEmail;
		this.AlertTypeID = _AlertTypeID;
		this.AlertTypeText = _AlertTypeText;
		this.AlertMessage = _AlertMessage;
		this.AlertTime = _AlertTime;
		this.SnapUrls = _SnapUrls.replace("\\/", "/").replace("\"", "").replace(" ", "");
		this.RecordingViewURL = _RecordingViewURL;
		this.IsRead = _IsRead;
	}

	public CameraNotification(int _ID, int _CameraID, String _UserEmail, int _AlertTypeID,
			String _AlertTypeText, String _AlertMessage, Long _AlertTime, String _SnapUrls,
			String _RecordingViewURL, int _IsRead)
	{
		try
		{
			this.ID = _ID;
			this.CameraID = _CameraID;
			this.UserEmail = _UserEmail;
			this.AlertTypeID = _AlertTypeID;
			this.AlertTypeText = _AlertTypeText;
			this.AlertMessage = _AlertMessage;
			this.AlertTime = new SimpleDateFormat("yyyyMMddHHmmss").parse(_AlertTime + "");
			this.SnapUrls = _SnapUrls.replace("\\/", "/").replace("\"", "").replace(" ", "");
			this.RecordingViewURL = _RecordingViewURL;
			this.IsRead = (_IsRead == 1);
		}
		catch (Exception e)
		{
		}
	}

	public int getID()
	{
		return ID;
	}

	public int getCameraID()
	{
		return CameraID;
	}

	public String getUserEmail()
	{
		return UserEmail;
	}

	public int getAlertTypeID()
	{
		return AlertTypeID;
	}

	public String getAlertTypeText()
	{
		return AlertTypeText;
	}

	public String getAlertMessage()
	{
		return AlertMessage;
	}

	public Date getAlertTime()
	{
		return AlertTime;
	}

	public Long getAlertTimeInteger()
	{
		Log.e(TAG, "");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		return Long.parseLong(sdf.format(AlertTime));
	}

	public String getSnapUrls()
	{
		return (SnapUrls != null && SnapUrls.contains("////") ? SnapUrls.replace("//", "/")
				: SnapUrls);
	}

	public String getRecordingViewURL()
	{
		return RecordingViewURL;
	}

	public boolean getIsRead()
	{
		return IsRead;
	}

	public int getIsReadInteger()
	{
		return (IsRead ? 1 : 0);
	}

	public void setID(int _ID)
	{
		ID = _ID;
	}

	public void setCameraID(int _CameraID)
	{
		CameraID = _CameraID;
	}

	public void setUserEmail(String _UserEmail)
	{
		UserEmail = _UserEmail;
	}

	public void setAlertTypeID(int _AlertTypeID)
	{
		AlertTypeID = _AlertTypeID;
	}

	public void setAlertTypeText(String _AlertTypeText)
	{
		AlertTypeText = _AlertTypeText;
	}

	public void setAlertMessage(String _AlertMessage)
	{
		AlertMessage = _AlertMessage;
	}

	public void setAlertTime(Date _AlertTime)
	{
		AlertTime = _AlertTime;
	}

	public void setAlertTimeInteger(Long _AlertTime)
	{
		try
		{
			AlertTime = new SimpleDateFormat("yyyyMMddHHmmss").parse(_AlertTime + "");
		}
		catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setSnapUrls(String _SnapUrls)
	{
		SnapUrls = _SnapUrls.replace("\\/", "/").replace("\"", "").replace(" ", "");
	}

	public void setRecordingViewURL(String _RecordingViewURL)
	{
		RecordingViewURL = _RecordingViewURL;
	}

	public void setIsRead(boolean _IsRead)
	{
		IsRead = _IsRead;
	}

	public void setIsReadInteger(int _IsReadInteger)
	{
		IsRead = (_IsReadInteger == 1);
	}

	@Override
	public String toString()
	{
		return "ID[" + ID + "], CameraID[" + CameraID + "], UserEmail [" + UserEmail
				+ "], AlertTypeID[" + AlertTypeID + "]" + ", AlertTypeText [" + AlertTypeText
				+ "], AlertMessage [" + AlertMessage + "], AlertTime [" + AlertTime
				+ "], SnapUrls[" + SnapUrls + "]" + ", RecordingViewURL [" + RecordingViewURL
				+ "], IsRead[" + IsRead + "]";
	}

}
