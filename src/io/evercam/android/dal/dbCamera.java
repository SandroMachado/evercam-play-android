package io.evercam.android.dal;

import io.evercam.android.dto.Camera;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class dbCamera extends DatabaseMaster
{

	// Cameras table name
	private static final String TABLE_Camera = "ApiCamera";

	// Cameras Table Columns names
	public static final String KEY_ID = "_id";
	public static final String KEY_CameraID = "CameraID";
	public static final String KEY_UserEmail = "UserEmail";

	public static final String KEY_AccessMethod = "AccessMethod";
	public static final String KEY_AudioUrl = "AudioUrl";
	public static final String KEY_BaseUrl = "BaseUrl";
	public static final String KEY_BrowserUrl = "BrowserUrl";
	public static final String KEY_CameraImageUrl = "CameraImageUrl";
	public static final String KEY_CameraMake = "CameraMake";
	public static final String KEY_CameraModel = "CameraModel";
	public static final String KEY_CameraPassword = "CameraPassword";
	public static final String KEY_CameraTimeZone = "CameraTimeZone";
	public static final String KEY_CameraUserName = "CameraUserName";
	public static final String KEY_Code = "Code";
	public static final String KEY_H264Url = "H264Url";
	public static final String KEY_HistoryDays = "HistoryDays";
	public static final String KEY_LocalIpPort = "LocalIpPort";
	public static final String KEY_LowResolutionSnapshotUrl = "LowResolutionSnapshotUrl ";
	public static final String KEY_MjpgUrl = "MjpgUrl";
	public static final String KEY_MobileUrl = "MobileUrl";
	public static final String KEY_Mpeg4Url = "Mpeg4Url";
	public static final String KEY_Name = "Name";
	public static final String KEY_OfflineTime = "OfflineTime";
	public static final String KEY_RtspPort = "RtspPort";
	public static final String KEY_RtspUrl = "RtspUrl";
	public static final String KEY_Status = "Status";
	public static final String KEY_UseCredentials = "UseCredentials";
	public static final String KEY_VideoRecording = "VideoRecording";

	public static final String KEY_AlarmLevel = "AlarmLevel";
	public static final String Key_CameraGroup = "CameraGroup";
	public static final String Key_CameraStatusIntVal = "CameraStatusIntVal";
	public static final String Key_IsMdEnabled = "IsMdEnabled";

	public static final String Key_UtcOffset = "UtcOffset";

	// <ArrayOfApiCamera xmlns:i="http://www.w3.org/2001/XMLSchema-instance"
	// xmlns="http://schemas.datacontract.org/2004/07/StopMotion.Business.Entities"><ApiCamera><AccessMethod>Manual</AccessMethod><AlarmLevel>5</AlarmLevel><AudioUrl></AudioUrl><BaseUrl>http://93.107.43.164:8082/</BaseUrl><BrowserUrl>http://camba.tv/cameraview.aspx?c=esewekxn</BrowserUrl><CameraGroup>DCF</CameraGroup><CameraImageUrl>http://93.107.43.164:8082/snapshot.jpg</CameraImageUrl><CameraMake>Y-Cam</CameraMake><CameraModel>Black
	// SD</CameraModel><CameraPassword>mehcam4mehcam</CameraPassword><CameraStatusIntVal>3</CameraStatusIntVal><CameraTimeZone>GMT
	// Standard
	// Time</CameraTimeZone><CameraUserName>admin</CameraUserName><Code>esewekxn</Code><H264Url></H264Url><HistoryDays>90</HistoryDays><Id>105</Id><IsMdEnabled>true</IsMdEnabled><LocalIpPort>192.168.3.115:8082</LocalIpPort><LowResolutionSnapshotUrl>http://93.107.43.164:8082/snapshot1.jpg</LowResolutionSnapshotUrl><MjpgUrl>http://93.107.43.164:8082/stream.jpg</MjpgUrl><MobileUrl>rtsp://93.107.43.164:8082/live_3gpp.sdp</MobileUrl><Mpeg4Url>rtsp://93.107.43.164:8082/live_mpeg4.sdp</Mpeg4Url><Name>Aughrim
	// Office</Name><OfflineTime>2013-01-31T14:49:44.717</OfflineTime><RtspPort>8082</RtspPort><RtspUrl
	// i:nil="true"
	// /><Status>Offline</Status><UseCredentials>true</UseCredentials><VideoRecording>false</VideoRecording></ApiCamera><ApiCamera><AccessMethod>Manual</AccessMethod><AlarmLevel>7</AlarmLevel><AudioUrl></AudioUrl><BaseUrl>http://149.5.42.145:11214/</BaseUrl><BrowserUrl>http://camba.tv/cameraview.aspx?c=qasodset</BrowserUrl><CameraGroup>DCC</CameraGroup><CameraImageUrl>http://149.5.42.145:11214/axis-cgi/jpg/image.cgi?resolution=640x480</CameraImageUrl><CameraMake>Axis</CameraMake><CameraModel>Q
	// Series</CameraModel><CameraPassword>mehcam</CameraPassword><CameraStatusIntVal>1</CameraStatusIntVal><CameraTimeZone>GMT
	// Standard
	// Time</CameraTimeZone><CameraUserName>camba</CameraUserName><Code>qasodset</Code><H264Url>rtsp://149.5.42.145:11214/axis-media/media.amp?resolution=640x360</H264Url><HistoryDays>1</HistoryDays><Id>758</Id><IsMdEnabled>false</IsMdEnabled><LocalIpPort>192.168.0.214:80</LocalIpPort><LowResolutionSnapshotUrl>http://149.5.42.145:11214/axis-cgi/jpg/image.cgi?resolution=cif</LowResolutionSnapshotUrl><MjpgUrl>http://149.5.42.145:11214/axis-cgi/mjpg/video.cgi</MjpgUrl><MobileUrl>rtsp://149.5.42.145:10214/axis-media/media.amp?resolution=QCIF</MobileUrl><Mpeg4Url
	// i:nil="true" /><Name>Herbst Mast PTZ MJ</Name><OfflineTime i:nil="true"
	// /><RtspPort>10214</RtspPort><RtspUrl i:nil="true"
	// /><Status>Active</Status><UseCredentials>true</UseCredentials><VideoRecording>false</VideoRecording></ApiCamera><ApiCamera><AccessMethod>Manual</AccessMethod><AlarmLevel>5</AlarmLevel><AudioUrl></AudioUrl><BaseUrl>http://87.213.54.234:6010/</BaseUrl><BrowserUrl>http://camba.tv/cameraview.aspx?c=bhlkubut</BrowserUrl><CameraGroup>DCF</CameraGroup><CameraImageUrl>http://87.213.54.234:6010/Streaming/Channels/1/picture</CameraImageUrl><CameraMake>Hikvision</CameraMake><CameraModel>Other</CameraModel><CameraPassword>12345</CameraPassword><CameraStatusIntVal>3</CameraStatusIntVal><CameraTimeZone>GMT
	// Standard
	// Time</CameraTimeZone><CameraUserName>admin</CameraUserName><Code>bhlkubut</Code><H264Url>rtsp://87.213.54.234:6010/h264/ch1/main/av_stream</H264Url><HistoryDays>1</HistoryDays><Id>2048</Id><IsMdEnabled>false</IsMdEnabled><LocalIpPort
	// i:nil="true" /><LowResolutionSnapshotUrl i:nil="true" /><MjpgUrl
	// i:nil="true"
	// /><MobileUrl>rtsp://87.213.54.234:6010/h264/ch1/main/av_stream</MobileUrl><Mpeg4Url>rtsp://87.213.54.234:6010/mpeg4/ch1/main/av_stream</Mpeg4Url><Name>Hikvision
	// 1.3</Name><OfflineTime>2012-11-20T10:46:52.317</OfflineTime><RtspPort>554</RtspPort><RtspUrl
	// i:nil="true"
	// /><Status>Offline</Status><UseCredentials>true</UseCredentials><VideoRecording>false</VideoRecording></ApiCamera><ApiCamera><AccessMethod>Manual</AccessMethod><AlarmLevel>5</AlarmLevel><AudioUrl></AudioUrl><BaseUrl>http://87.213.54.234:6100/</BaseUrl><BrowserUrl>http://camba.tv/cameraview.aspx?c=bwcqmqyr</BrowserUrl><

	public dbCamera(Context context)
	{
		super(context);
	}

	// Creating Tables
	public void onCreateCustom(SQLiteDatabase db)
	{
		String CREATE_TABLE_Cameras = "CREATE TABLE " + TABLE_Camera + "(" + KEY_ID
				+ " INTEGER PRIMARY KEY autoincrement" + "," + KEY_CameraID + " INTEGER NOT NULL"
				+ "," + KEY_UserEmail + " TEXT  NOT NULL"

				+ "," + KEY_AccessMethod + " TEXT NULL" + "," + KEY_AudioUrl + " TEXT NULL" + ","
				+ KEY_BaseUrl + " TEXT NULL" + "," + KEY_BrowserUrl + " TEXT NULL" + ","
				+ KEY_CameraImageUrl + " TEXT NULL" + "," + KEY_CameraMake + " TEXT NULL" + ","
				+ KEY_CameraModel + " TEXT NULL" + "," + KEY_CameraPassword + " TEXT NULL" + ","
				+ KEY_CameraTimeZone + " TEXT NULL" + "," + KEY_CameraUserName + " TEXT NULL" + ","
				+ KEY_Code + " TEXT NULL" + "," + KEY_H264Url + " TEXT NULL" + ","
				+ KEY_HistoryDays + " TEXT NULL" + "," + KEY_LocalIpPort + " TEXT NULL" + ","
				+ KEY_LowResolutionSnapshotUrl + " TEXT NULL" + "," + KEY_MjpgUrl + " TEXT NULL"
				+ "," + KEY_MobileUrl + " TEXT NULL" + "," + KEY_Mpeg4Url + " TEXT NULL" + ","
				+ KEY_Name + " TEXT NULL" + "," + KEY_OfflineTime + " TEXT NULL" + ","
				+ KEY_RtspPort + " TEXT NULL" + "," + KEY_RtspUrl + " TEXT NULL" + "," + KEY_Status
				+ " TEXT NULL" + "," + KEY_UseCredentials + " INTEGER" + "," + KEY_VideoRecording
				+ " INTEGER"

				+ "," + KEY_AlarmLevel + " TEXT NULL " + "," + Key_CameraGroup + " TEXT NULL "
				+ "," + Key_CameraStatusIntVal + " INTEGER " + "," + Key_IsMdEnabled + " INTEGER "
				+ "," + Key_UtcOffset + " TEXT NULL "

				+ ",  CONSTRAINT uniqueCamAndUser UNIQUE (" + KEY_CameraID + ", " + KEY_UserEmail
				+ ")"

				+ ")";
		db.execSQL(CREATE_TABLE_Cameras);
	}

	// Upgrading database
	public void onUpgradeCustom(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_Camera);

		// Create tables again
		onCreateCustom(db);
	}

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 */

	// Adding new Camera
	public void addCamera(Camera cam)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(KEY_CameraID, cam.getCameraID());
		values.put(KEY_UserEmail, cam.getUserEmail());

		values.put(KEY_AccessMethod, cam.getAccessMethod());
		values.put(KEY_AudioUrl, cam.getAudioUrl());
		values.put(KEY_BaseUrl, cam.getBaseUrl());
		values.put(KEY_BrowserUrl, cam.getBrowserUrl());
		values.put(KEY_CameraImageUrl, cam.getCameraImageUrl());
		values.put(KEY_CameraMake, cam.getCameraMake());
		values.put(KEY_CameraModel, cam.getCameraModel());
		values.put(KEY_CameraPassword, cam.getCameraPassword());
		values.put(KEY_CameraTimeZone, cam.getCameraTimeZone());
		values.put(KEY_CameraUserName, cam.getCameraUserName());
		values.put(KEY_Code, cam.getCode());
		values.put(KEY_H264Url, cam.getH264Url());
		values.put(KEY_HistoryDays, cam.getHistoryDays());
		values.put(KEY_LocalIpPort, cam.getLocalIpPort());
		values.put(KEY_LowResolutionSnapshotUrl, cam.getLowResolutionSnapshotUrl());
		values.put(KEY_MjpgUrl, cam.getMjpgUrl());
		values.put(KEY_MobileUrl, cam.getMobileUrl());
		values.put(KEY_Mpeg4Url, cam.getMpeg4Url());
		values.put(KEY_Name, cam.getName());
		values.put(KEY_OfflineTime, cam.getOfflineTime());
		values.put(KEY_RtspPort, cam.getRtspPort());
		values.put(KEY_RtspUrl, cam.getRtspUrl());
		values.put(KEY_Status, cam.getStatus());
		values.put(KEY_UseCredentials, cam.getUseCredentials());
		values.put(KEY_VideoRecording, cam.getVideoRecording());

		values.put(KEY_AlarmLevel, cam.getAlarmLevel());
		values.put(Key_CameraGroup, cam.getCameraGroup());
		values.put(Key_CameraStatusIntVal, cam.getCameraStatusIntVal());
		values.put(Key_IsMdEnabled, cam.getIsMdEnabledInteger());
		values.put(Key_UtcOffset, cam.getUtcOffset());

		// Inserting Row
		db.insert(TABLE_Camera, null, values);
		db.close(); // Closing database connection

	}

	// Getting single Camera
	public Camera getCamera(int id)
	{
		Camera cam = null;

		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_Camera, new String[] { KEY_ID, KEY_CameraID, KEY_UserEmail,
				KEY_AccessMethod, KEY_AudioUrl, KEY_BaseUrl, KEY_BrowserUrl, KEY_CameraImageUrl,
				KEY_CameraMake, KEY_CameraModel, KEY_CameraPassword, KEY_CameraTimeZone,
				KEY_CameraUserName, KEY_Code, KEY_H264Url, KEY_HistoryDays, KEY_LocalIpPort,
				KEY_LowResolutionSnapshotUrl, KEY_MjpgUrl, KEY_MobileUrl, KEY_Mpeg4Url, KEY_Name,
				KEY_OfflineTime, KEY_RtspPort, KEY_RtspUrl, KEY_Status, KEY_UseCredentials,
				KEY_VideoRecording, KEY_AlarmLevel, Key_CameraGroup, Key_CameraStatusIntVal,
				Key_IsMdEnabled, Key_UtcOffset }, KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
		{
			if (cursor.moveToFirst())
			{
				cam = new Camera(Integer.parseInt(cursor.getString(0)), Integer.parseInt(cursor
						.getString(1)), cursor.getString(2), cursor.getString(3),
						cursor.getString(4), cursor.getString(5), cursor.getString(6),
						cursor.getString(7), cursor.getString(8), cursor.getString(9),
						cursor.getString(10), cursor.getString(11), cursor.getString(12),
						cursor.getString(13), cursor.getString(14), cursor.getString(15),
						cursor.getString(16), cursor.getString(17), cursor.getString(18),
						cursor.getString(19), cursor.getString(20), cursor.getString(21),
						cursor.getString(22), cursor.getString(23), cursor.getString(24),
						cursor.getString(25), (Integer.parseInt(cursor.getString(26)) == 1),
						(Integer.parseInt(cursor.getString(27)) == 1), cursor.getString(28),
						cursor.getString(29), Integer.getInteger(cursor.getString(30), 0),
						(Integer.parseInt(cursor.getString(31)) == 1), cursor.getString(32));
			}
			cursor.close();
		}
		db.close();

		return cam;
	}

	public int getMaxID()
	{
		int latestID = 0;
		// Select All Query
		String selectQuery = "SELECT  max(" + KEY_ID + ") FROM " + TABLE_Camera;// +
																				// " order by "
																				// +
																				// KEY_ID
																				// +
																				// " desc";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst())
		{

			latestID = Integer.parseInt(cursor.getString(0));

		}

		cursor.close();
		db.close();

		return latestID;
	}

	// Getting All Cameras
	public List<Camera> getAllCameras(int maxRecords)
	{
		List<Camera> CameraList = new ArrayList<Camera>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_Camera + " order by " + KEY_Name + " asc";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		int i = 0;
		if (cursor.moveToFirst())
		{

			do
			{
				Camera cam = new Camera(Integer.parseInt(cursor.getString(0)),
						Integer.parseInt(cursor.getString(1)), cursor.getString(2),
						cursor.getString(3), cursor.getString(4), cursor.getString(5),
						cursor.getString(6), cursor.getString(7), cursor.getString(8),
						cursor.getString(9), cursor.getString(10), cursor.getString(11),
						cursor.getString(12), cursor.getString(13), cursor.getString(14),
						cursor.getString(15), cursor.getString(16), cursor.getString(17),
						cursor.getString(18), cursor.getString(19), cursor.getString(20),
						cursor.getString(21), cursor.getString(22), cursor.getString(23),
						cursor.getString(24), cursor.getString(25), (Integer.parseInt(cursor
								.getString(26)) == 1),
						(Integer.parseInt(cursor.getString(27)) == 1), cursor.getString(28),
						cursor.getString(29), Integer.getInteger(cursor.getString(30), 0),
						(Integer.parseInt(cursor.getString(31)) == 1), cursor.getString(32));

				// Adding Camera to list
				CameraList.add(cam);
				i++;
			} while (cursor.moveToNext() && (maxRecords == 0 || i < maxRecords));

		}

		cursor.close();
		db.close();
		return CameraList;
	}

	public ArrayList<Camera> getAllCamerasForEmailID(String Email, int maxRecords)
	{
		ArrayList<Camera> CameraList = new ArrayList<Camera>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_Camera + " where upper(" + KEY_UserEmail
				+ ") = upper('" + Email + "') order by " + KEY_Name + " asc";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		int i = 0;
		if (cursor.moveToFirst())
		{

			do
			{
				Camera cam = new Camera(Integer.parseInt(cursor.getString(0)),
						Integer.parseInt(cursor.getString(1)), cursor.getString(2),
						cursor.getString(3), cursor.getString(4), cursor.getString(5),
						cursor.getString(6), cursor.getString(7), cursor.getString(8),
						cursor.getString(9), cursor.getString(10), cursor.getString(11),
						cursor.getString(12), cursor.getString(13), cursor.getString(14),
						cursor.getString(15), cursor.getString(16), cursor.getString(17),
						cursor.getString(18), cursor.getString(19), cursor.getString(20),
						cursor.getString(21), cursor.getString(22), cursor.getString(23),
						cursor.getString(24), cursor.getString(25), (Integer.parseInt(cursor
								.getString(26)) == 1),
						(Integer.parseInt(cursor.getString(27)) == 1), cursor.getString(28),
						cursor.getString(29), Integer.getInteger(cursor.getString(30), 0),
						(Integer.parseInt(cursor.getString(31)) == 1), cursor.getString(32));

				CameraList.add(cam);
				i++;
			} while (cursor.moveToNext() && (maxRecords == 0 || i < maxRecords));

		}

		cursor.close();
		db.close();

		return CameraList;
	}

	// Updating single Camera
	public int updateCamera(Camera cam)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_CameraID, cam.getCameraID());
		values.put(KEY_UserEmail, cam.getUserEmail());

		values.put(KEY_AccessMethod, cam.getAccessMethod());
		values.put(KEY_AudioUrl, cam.getAudioUrl());
		values.put(KEY_BaseUrl, cam.getBaseUrl());
		values.put(KEY_BrowserUrl, cam.getBrowserUrl());
		values.put(KEY_CameraImageUrl, cam.getCameraImageUrl());
		values.put(KEY_CameraMake, cam.getCameraMake());
		values.put(KEY_CameraModel, cam.getCameraModel());
		values.put(KEY_CameraPassword, cam.getCameraPassword());
		values.put(KEY_CameraTimeZone, cam.getCameraTimeZone());
		values.put(KEY_CameraUserName, cam.getCameraUserName());
		values.put(KEY_Code, cam.getCode());
		values.put(KEY_H264Url, cam.getH264Url());
		values.put(KEY_HistoryDays, cam.getHistoryDays());
		values.put(KEY_LocalIpPort, cam.getLocalIpPort());
		values.put(KEY_LowResolutionSnapshotUrl, cam.getLowResolutionSnapshotUrl());
		values.put(KEY_MjpgUrl, cam.getMjpgUrl());
		values.put(KEY_MobileUrl, cam.getMobileUrl());
		values.put(KEY_Mpeg4Url, cam.getMpeg4Url());
		values.put(KEY_Name, cam.getName());
		values.put(KEY_OfflineTime, cam.getOfflineTime());
		values.put(KEY_RtspPort, cam.getRtspPort());
		values.put(KEY_RtspUrl, cam.getRtspUrl());
		values.put(KEY_Status, cam.getStatus());
		values.put(KEY_UseCredentials, cam.getUseCredentials());
		values.put(KEY_VideoRecording, cam.getVideoRecording());

		values.put(KEY_AlarmLevel, cam.getAlarmLevel());
		values.put(Key_CameraGroup, cam.getCameraGroup());
		values.put(Key_CameraStatusIntVal, cam.getCameraStatusIntVal());
		values.put(Key_IsMdEnabled, cam.getIsMdEnabledInteger());

		values.put(Key_UtcOffset, cam.getUtcOffset());

		// updating row
		int return_value = db.update(TABLE_Camera, values, KEY_ID + " = ?",
				new String[] { String.valueOf(cam.getID()) });
		db.close();
		return return_value;
	}

	// Deleting single Camera
	public void deleteCamera(Camera cam)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_Camera, KEY_ID + " = ?", new String[] { String.valueOf(cam.getID()) });
		db.close();
	}

	public void deleteCameraForEmail(String Email)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_Camera, " upper(" + KEY_UserEmail + ") = upper(?)",
				new String[] { String.valueOf(Email) });
		db.close();
	}

	// Getting Cameras Count
	public int getCamerasCount()
	{

		String countQuery = "SELECT  * FROM " + TABLE_Camera;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();

		// return count
		int count = cursor.getCount();
		db.close();
		return count;
	}

}
