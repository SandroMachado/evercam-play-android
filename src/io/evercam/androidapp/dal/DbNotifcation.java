package io.evercam.androidapp.dal;

import io.evercam.androidapp.dto.CameraNotification;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DbNotifcation extends DatabaseMaster
{

	// CameraNotifications table name
	private static final String TABLE_CameraNotifications = "CameraNotifications";

	// CameraNotifications Table Columns names
	public static final String KEY_ID = "_id";
	public static final String KEY_CameraID = "CameraID";
	public static final String KEY_UserEmail = "UserEmail";
	public static final String KEY_AlertTypeID = "AlertTypeID";
	public static final String KEY_AlertTypeText = "AlertTypeText";
	public static final String KEY_AlertMessage = "AlertMessage";
	public static final String KEY_AlertTime = "AlertTime";
	public static final String KEY_SnapUrls = "EventUrl";
	public static final String KEY_RecordingViewURL = "RecordingViewURL";
	public static final String KEY_IsRead = "IsRead";

	public DbNotifcation(Context context)
	{
		super(context);
	}

	// Creating Tables
	public void onCreateCustom(SQLiteDatabase db)
	{
		String CREATE_TABLE_CameraNotifications = "CREATE TABLE " + TABLE_CameraNotifications + "("
				+ KEY_ID + " INTEGER PRIMARY KEY autoincrement" + "," + KEY_CameraID + " INTEGER"
				+ "," + KEY_UserEmail + " TEXT" + "," + KEY_AlertTypeID + " INTEGER" + ","
				+ KEY_AlertTypeText + " TEXT" + "," + KEY_AlertMessage + " TEXT" + ","
				+ KEY_AlertTime
				+ " INTEGER" // yyyyMMddHHmmss
				+ "," + KEY_SnapUrls + " TEXT" + "," + KEY_RecordingViewURL + " TEXT" + ","
				+ KEY_IsRead + " INTEGER" + ")";
		db.execSQL(CREATE_TABLE_CameraNotifications);
	}

	// Upgrading database
	public void onUpgradeCustom(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CameraNotifications);

		// Create tables again
		onCreateCustom(db);
	}

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 */

	// Adding new CameraNotification
	public void addCameraNotification(CameraNotification notif)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(KEY_CameraID, notif.getCameraID());
		values.put(KEY_UserEmail, notif.getUserEmail());
		values.put(KEY_AlertTypeID, notif.getAlertTypeID());
		values.put(KEY_AlertTypeText, notif.getAlertTypeText());
		values.put(KEY_AlertMessage, notif.getAlertMessage());
		values.put(KEY_AlertTime, notif.getAlertTimeInteger());
		values.put(KEY_SnapUrls, notif.getSnapUrls());
		values.put(KEY_RecordingViewURL, notif.getRecordingViewURL());
		values.put(KEY_IsRead, notif.getIsReadInteger());

		// Inserting Row
		db.insert(TABLE_CameraNotifications, null, values);
		db.close(); // Closing database connection

	}

	public int getCameraNotificationCount(CameraNotification notif)
	{
		int count = 0;

		String selectQuery = "SELECT  count(*) FROM " + TABLE_CameraNotifications + " where 1=1  "
				+ " and " + KEY_CameraID + " = " + notif.getCameraID() + " and " + KEY_UserEmail
				+ " = '" + notif.getUserEmail() + "'" + " and " + KEY_AlertTypeID + " = "
				+ notif.getAlertTypeID() + " and " + KEY_AlertMessage + " = '"
				+ notif.getAlertMessage() + "'" + " and " + KEY_AlertTime + " = "
				+ notif.getAlertTimeInteger();

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		int i = 0;
		if (cursor.moveToFirst())
		{

			count = Integer.parseInt(cursor.getString(0));

		}

		cursor.close();
		db.close(); // Closing database connection

		return count;

	}

	// Getting single CameraNotification
	public CameraNotification getCameraNotification(int id)
	{
		CameraNotification notif = null;

		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_CameraNotifications, new String[] { KEY_ID, KEY_CameraID,
				KEY_UserEmail, KEY_AlertTypeID, KEY_AlertTypeText, KEY_AlertMessage, KEY_AlertTime,
				KEY_SnapUrls, KEY_RecordingViewURL, KEY_IsRead }, KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
		{
			if (cursor.moveToFirst())
			{

				notif = new CameraNotification(Integer.parseInt(cursor.getString(0)),
						Integer.parseInt(cursor.getString(1)), cursor.getString(2),
						Integer.parseInt(cursor.getString(3)), cursor.getString(4),
						cursor.getString(5), Long.parseLong(cursor.getString(6)),
						cursor.getString(7), cursor.getString(8), Integer.parseInt(cursor
								.getString(9)));
			}
			cursor.close();
		}
		db.close();

		return notif;
	}

	public int getMaxID()
	{
		int latestID = 0;
		// Select All Query
		String selectQuery = "SELECT  max(" + KEY_ID + ") FROM " + TABLE_CameraNotifications;// +
																								// " order by "
																								// +
																								// KEY_ID
																								// +
																								// " desc";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		int i = 0;
		if (cursor.moveToFirst())
		{

			latestID = Integer.parseInt(cursor.getString(0));

		}

		cursor.close();
		db.close();

		return latestID;
	}

	// Getting All CameraNotifications
	public List<CameraNotification> getAllCameraNotifications(int maxRecords)
	{
		List<CameraNotification> CameraNotificationList = new ArrayList<CameraNotification>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_CameraNotifications + " order by " + KEY_ID
				+ " desc";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		int i = 0;
		if (cursor.moveToFirst())
		{

			do
			{
				CameraNotification notif = new CameraNotification(Integer.parseInt(cursor
						.getString(0)), Integer.parseInt(cursor.getString(1)), cursor.getString(2),
						Integer.parseInt(cursor.getString(3)), cursor.getString(4),
						cursor.getString(5), Long.parseLong(cursor.getString(6)),
						cursor.getString(7), cursor.getString(8), Integer.parseInt(cursor
								.getString(9)));

				// Adding CameraNotification to list
				CameraNotificationList.add(notif);
				i++;
			} while (cursor.moveToNext() && (maxRecords == 0 || i < maxRecords));

		}

		cursor.close();
		db.close();
		return CameraNotificationList;
	}

	public List<CameraNotification> getAllCameraNotificationsForEmailID(String Email, int maxRecords)
	{
		List<CameraNotification> CameraNotificationList = new ArrayList<CameraNotification>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_CameraNotifications + " where upper("
				+ KEY_UserEmail + ") = upper('" + Email + "') order by " + KEY_ID + " desc";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		int i = 0;
		if (cursor.moveToFirst())
		{

			do
			{
				CameraNotification notif = new CameraNotification(Integer.parseInt(cursor
						.getString(0)), Integer.parseInt(cursor.getString(1)), cursor.getString(2),
						Integer.parseInt(cursor.getString(3)), cursor.getString(4),
						cursor.getString(5), Long.parseLong(cursor.getString(6)),
						cursor.getString(7), cursor.getString(8), Integer.parseInt(cursor
								.getString(9)));

				// Adding CameraNotification to list
				CameraNotificationList.add(notif);
				i++;
			} while (cursor.moveToNext() && (maxRecords == 0 || i < maxRecords));

		}

		cursor.close();
		db.close();

		return CameraNotificationList;
	}

	// Updating single CameraNotification
	public int updateCameraNotification(CameraNotification notif)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_CameraID, notif.getCameraID());
		values.put(KEY_UserEmail, notif.getUserEmail());
		values.put(KEY_AlertTypeID, notif.getAlertTypeID());
		values.put(KEY_AlertTypeText, notif.getAlertTypeText());
		values.put(KEY_AlertMessage, notif.getAlertMessage());
		values.put(KEY_AlertTime, notif.getAlertTimeInteger());
		values.put(KEY_SnapUrls, notif.getSnapUrls());
		values.put(KEY_RecordingViewURL, notif.getRecordingViewURL());
		values.put(KEY_IsRead, notif.getIsReadInteger());

		// updating row
		int return_value = db.update(TABLE_CameraNotifications, values, KEY_ID + " = ?",
				new String[] { String.valueOf(notif.getID()) });
		db.close();
		return return_value;
	}

	// Deleting single CameraNotification
	public void deleteCameraNotification(CameraNotification notif)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_CameraNotifications, KEY_ID + " = ?",
				new String[] { String.valueOf(notif.getID()) });
		db.close();
	}

	public void deleteCameraNotificationForEmail(String Email)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_CameraNotifications, " upper(" + KEY_UserEmail + ") = upper(?)",
				new String[] { String.valueOf(Email) });
		db.close();
	}

	// Getting CameraNotifications Count
	public int getCameraNotificationsCount()
	{

		String countQuery = "SELECT  * FROM " + TABLE_CameraNotifications;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();

		// return count
		int count = cursor.getCount();
		db.close();
		return count;
	}

}
