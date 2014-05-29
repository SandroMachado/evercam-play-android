package io.evercam.androidapp.dal;

import java.util.ArrayList;

import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.utils.AppData;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DbCamera extends DatabaseMaster
{
	public static final String TABLE_CAMERA = "evercamcamera";
	public static final String KEY_ID = "id";

	private final String TAG = "evercamplay-DbCamera";
	private final String KEY_CAMERA_ID = "cameraId";
	private final String KEY_CAMERA_NAME = "name";
	private final String KEY_OWNER = "owner";
	private final String KEY_USERNAME = "username";
	private final String KEY_PASSWORD = "password";
	private final String KEY_TIMEZONE = "timezone";
	private final String KEY_VENDOR = "vendor";
	private final String KEY_MODEL = "model";
	private final String KEY_MAC = "mac";
	private final String KEY_EXTERNAL_JPG_URL = "externalSnapshotUrl";
	private final String KEY_INTERNAL_JPG_URL = "internalSnapshotUrl";
	private final String KEY_EXTERNAL_RTSP_URL = "externalRtspUrl";
	private final String KEY_INTERNAL_RTSP_URL = "internalRtspUrl";
	private final String KEY_STATUS = "status";
	private final String KEY_HAS_CREDENTIAL = "hasCredential";

	public DbCamera(Context context)
	{
		super(context);
	}

	public void onCreateCustom(SQLiteDatabase db)
	{
		String CREATE_TABLE_Cameras = "CREATE TABLE " + TABLE_CAMERA + "(" + KEY_ID
				+ " INTEGER PRIMARY KEY autoincrement" + "," + KEY_CAMERA_ID + " TEXT NOT NULL"
				+ "," + KEY_CAMERA_NAME + " TEXT NULL" + "," + KEY_OWNER + " TEXT  NOT NULL" + ","
				+ KEY_USERNAME + " TEXT NULL" + "," + KEY_PASSWORD + " TEXT NULL" + ","
				+ KEY_TIMEZONE + " TEXT NULL" + "," + KEY_VENDOR + " TEXT NULL" + "," + KEY_MODEL
				+ " TEXT NULL" + "," + KEY_MAC + " TEXT NULL " + "," + KEY_EXTERNAL_JPG_URL
				+ " TEXT NULL " + "," + KEY_INTERNAL_JPG_URL + " TEXT NULL " + ","
				+ KEY_EXTERNAL_RTSP_URL + " TEXT NULL" + "," + KEY_INTERNAL_RTSP_URL + " TEXT NULL"
				+ "," + KEY_STATUS + " TEXT NULL" + "," + KEY_HAS_CREDENTIAL + " INT NULL" + ","
				+ "CONSTRAINT uniqueCamAndUser UNIQUE (" + KEY_CAMERA_ID + ")" + ")";
		db.execSQL(CREATE_TABLE_Cameras);
	}

	public void onUpgradeCustom(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CAMERA);

		// Create tables again
		onCreateCustom(db);
	}

	public void addCamera(EvercamCamera evercamCamera)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = getContentValueFrom(evercamCamera);

		db.insert(TABLE_CAMERA, null, values);
		db.close();

	}

	// Getting single Camera
	public EvercamCamera getCamera(int id)
	{
		EvercamCamera evercamCamera = null;

		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_CAMERA, new String[] { KEY_ID, KEY_CAMERA_ID,
				KEY_CAMERA_NAME, KEY_OWNER, KEY_USERNAME, KEY_PASSWORD, KEY_TIMEZONE, KEY_VENDOR,
				KEY_MODEL, KEY_MAC, KEY_EXTERNAL_JPG_URL, KEY_INTERNAL_JPG_URL,
				KEY_EXTERNAL_RTSP_URL, KEY_INTERNAL_RTSP_URL, KEY_STATUS, KEY_HAS_CREDENTIAL },
				KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
		{
			if (cursor.moveToFirst())
			{
				evercamCamera = new EvercamCamera();
				evercamCamera.setId(Integer.parseInt(cursor.getString(0)));
				evercamCamera.setCameraId(cursor.getString(1));
				evercamCamera.setName(cursor.getString(2));
				evercamCamera.setOwner(cursor.getString(3));
				evercamCamera.setUsername(cursor.getString(4));
				evercamCamera.setPassword(cursor.getString(5));
				evercamCamera.setTimezone(cursor.getString(6));
				evercamCamera.setVendor(cursor.getString(7));
				evercamCamera.setModel(cursor.getString(8));
				evercamCamera.setMac(cursor.getString(9));
				evercamCamera.setExternalSnapshotUrl(cursor.getString(10));
				evercamCamera.setInternalSnapshotUrl(cursor.getString(11));
				evercamCamera.setExternalRtspUrl(cursor.getString(12));
				evercamCamera.setInternalRtspUrl(cursor.getString(13));
				evercamCamera.setStatus(cursor.getString(14));
				evercamCamera.setHasCredentials(cursor.getInt(15) == 1);
			}
			cursor.close();
		}
		db.close();

		return evercamCamera;
	}

	public ArrayList<EvercamCamera> getAllCameras(int maxRecords)
	{
		String selectQuery = "SELECT  * FROM " + TABLE_CAMERA + " order by " + KEY_ID + " asc";
		return selectCameraListByQuery(selectQuery, maxRecords);
	}

	public ArrayList<EvercamCamera> getCamerasByOwner(String ownerUsername, int maxRecords)
	{
		String selectQuery = "SELECT  * FROM " + TABLE_CAMERA + " where upper(" + KEY_OWNER
				+ ") = upper('" + ownerUsername + "') order by " + KEY_ID + " asc";

		return selectCameraListByQuery(selectQuery, maxRecords);
	}

	public int updateCamera(EvercamCamera evercamCamera)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = getContentValueFrom(evercamCamera);

		// update row
		int return_value = db.update(TABLE_CAMERA, values, KEY_ID + " = ?",
				new String[] { String.valueOf(evercamCamera.getId()) });
		db.close();

		return return_value;
	}

	public void deleteCamera(EvercamCamera evercamCamera)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_CAMERA, KEY_ID + " = ?",
				new String[] { String.valueOf(evercamCamera.getId()) });
		db.close();
	}

	public void deleteCameraByOwner(String ownerUsername)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_CAMERA, " upper(" + KEY_OWNER + ") = upper(?)",
				new String[] { String.valueOf(ownerUsername) });
		db.close();
	}

	private ContentValues getContentValueFrom(EvercamCamera evercamCamera)
	{
		ContentValues values = new ContentValues();

		values.put(KEY_CAMERA_ID, evercamCamera.getCameraId());
		values.put(KEY_OWNER, AppData.defaultUser.getUsername());
		values.put(KEY_CAMERA_NAME, evercamCamera.getName());
		values.put(KEY_USERNAME, evercamCamera.getUsername());
		values.put(KEY_PASSWORD, evercamCamera.getPassword());
		values.put(KEY_VENDOR, evercamCamera.getVendor());
		values.put(KEY_MODEL, evercamCamera.getModel());
		values.put(KEY_TIMEZONE, evercamCamera.getTimezone());
		values.put(KEY_MAC, evercamCamera.getMac());
		values.put(KEY_STATUS, evercamCamera.getStatus());
		values.put(KEY_INTERNAL_JPG_URL, evercamCamera.getInternalSnapshotUrl());
		values.put(KEY_EXTERNAL_JPG_URL, evercamCamera.getExternalSnapshotUrl());
		values.put(KEY_EXTERNAL_RTSP_URL, evercamCamera.getExternalRtspUrl());
		values.put(KEY_INTERNAL_RTSP_URL, evercamCamera.getInternalRtspUrl());
		values.put(KEY_HAS_CREDENTIAL, evercamCamera.getHasCredentialsInt());

		return values;
	}

	private ArrayList<EvercamCamera> selectCameraListByQuery(String query, int maxRecords)
	{
		ArrayList<EvercamCamera> cameraList = new ArrayList<EvercamCamera>();

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		int count = 0;
		if (cursor.moveToFirst())
		{
			do
			{
				EvercamCamera evercamCamera = new EvercamCamera();
				evercamCamera.setId(Integer.parseInt(cursor.getString(0)));
				evercamCamera.setCameraId(cursor.getString(1));
				evercamCamera.setName(cursor.getString(2));
				evercamCamera.setOwner(cursor.getString(3));
				evercamCamera.setUsername(cursor.getString(4));
				evercamCamera.setPassword(cursor.getString(5));
				evercamCamera.setTimezone(cursor.getString(6));
				evercamCamera.setVendor(cursor.getString(7));
				evercamCamera.setModel(cursor.getString(8));
				evercamCamera.setMac(cursor.getString(9));
				evercamCamera.setExternalSnapshotUrl(cursor.getString(10));
				evercamCamera.setInternalSnapshotUrl(cursor.getString(11));
				evercamCamera.setExternalRtspUrl(cursor.getString(12));
				evercamCamera.setInternalRtspUrl(cursor.getString(13));
				evercamCamera.setStatus(cursor.getString(14));
				evercamCamera.setHasCredentials(cursor.getInt(15) == 1);

				cameraList.add(evercamCamera);
				count++;
			} while (cursor.moveToNext() && (maxRecords == 0 || count < maxRecords));
		}

		cursor.close();
		db.close();

		return cameraList;
	}
}
