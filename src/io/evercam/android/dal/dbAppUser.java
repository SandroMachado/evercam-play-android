package io.evercam.android.dal;

import io.evercam.android.dto.AppUser;
import io.evercam.android.utils.Crypto;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class dbAppUser extends DatabaseMaster
{

	// AppUsers table name

	private static final String TABLE_AppUser = "AppUser";

	// AppUsers Table Columns names
	public static final String KEY_ID = "_id";
	public static final String KEY_UserEmail = "UserEmail";
	public static final String KEY_UserPassword = "UserPassword";
	public static final String Key_ApiKey = "ApiKey";
	public static final String KEY_IsActive = "IsActive";
	public static final String KEY_IsDefault = "IsDefault";

	public dbAppUser(Context context)
	{

		super(context);
	}

	// Creating Tables
	public void onCreateCustom(SQLiteDatabase db)
	{
		String CREATE_TABLE_AppUsers = "CREATE TABLE " + TABLE_AppUser + "(" + KEY_ID
				+ " INTEGER PRIMARY KEY autoincrement" + "," + KEY_UserEmail + " TEXT UNIQUE" + ","
				+ KEY_UserPassword + " TEXT" + "," + Key_ApiKey + " TEXT" + "," + KEY_IsActive
				+ " INTEGER" + "," + KEY_IsDefault + " INTEGER"

				+ ")";
		db.execSQL(CREATE_TABLE_AppUsers);
	}

	// Upgrading database
	public void onUpgradeCustom(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_AppUser);

		// Create tables again
		onCreateCustom(db);
	}

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 * 
	 * @throws Exception
	 */

	// Adding new AppUser
	public void addAppUser(AppUser user) throws Exception
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(KEY_UserEmail, user.getUserEmail());
		values.put(KEY_UserPassword, Crypto.encrypt(user.getUserPassword()));
		values.put(Key_ApiKey, user.getApiKey());
		values.put(KEY_IsActive, user.getIsActiveInteger());
		values.put(KEY_IsDefault, user.getIsDefaultInteger());

		// Inserting Row
		db.insert(TABLE_AppUser, null, values);
		db.close(); // Closing database connection

	}

	// Getting single AppUser
	public AppUser getAppUser(String email) throws NumberFormatException, Exception
	{
		AppUser user = null;
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_AppUser, new String[] { KEY_ID, KEY_UserEmail,
				KEY_UserPassword, Key_ApiKey, KEY_IsActive, KEY_IsDefault }, KEY_UserEmail + "=?",
				new String[] { email }, null, null, null, null);
		if (cursor != null)
		{
			if (cursor.moveToFirst())
			{

				user = new AppUser(Integer.parseInt(cursor.getString(0)), cursor.getString(1),
						Crypto.decrypt(cursor.getString(2)), cursor.getString(3),
						Integer.parseInt(cursor.getString(4)),
						Integer.parseInt(cursor.getString(5)));
			}
			cursor.close();
		}
		db.close();

		return user;
	}

	public AppUser getAppUserByID(int _idobj) throws NumberFormatException, Exception
	{
		AppUser user = null;
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_AppUser, new String[] { KEY_ID, KEY_UserEmail,
				KEY_UserPassword, Key_ApiKey, KEY_IsActive, KEY_IsDefault }, KEY_ID + "=?",
				new String[] { _idobj + "" }, null, null, null, null);
		if (cursor != null)
		{
			if (cursor.moveToFirst())
			{

				user = new AppUser(Integer.parseInt(cursor.getString(0)), cursor.getString(1),
						Crypto.decrypt(cursor.getString(2)), cursor.getString(3),
						Integer.parseInt(cursor.getString(4)),
						Integer.parseInt(cursor.getString(5)));
			}
			cursor.close();
		}
		db.close();

		return user;
	}

	public int getMaxID()
	{
		int latestID = 0;
		// Select All Query
		String selectQuery = "SELECT  max(" + KEY_ID + ") FROM " + TABLE_AppUser;// +
																					// " order by "
																					// +
																					// KEY_ID
																					// +
																					// " desc";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		if (cursor.moveToFirst())
		{

			latestID = Integer.parseInt(cursor.getString(0));

		}

		cursor.close();
		db.close();

		return latestID;
	}

	// Getting All AppUsers
	public List<AppUser> getAllAppUsers(int maxRecords) throws NumberFormatException, Exception
	{
		List<AppUser> AppUserList = new ArrayList<AppUser>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_AppUser + " order by " + KEY_UserEmail
				+ " asc";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		int i = 0;
		if (cursor.moveToFirst())
		{

			do
			{
				AppUser user = new AppUser(Integer.parseInt(cursor.getString(0)),
						cursor.getString(1), Crypto.decrypt(cursor.getString(2)),
						cursor.getString(3), Integer.parseInt(cursor.getString(4)),
						Integer.parseInt(cursor.getString(5)));

				// Adding AppUser to list
				AppUserList.add(user);
				i++;
			} while (cursor.moveToNext() && (maxRecords == 0 || i < maxRecords));

		}

		cursor.close();
		db.close();
		return AppUserList;
	}

	public List<AppUser> getAllActiveAppUsers(int maxRecords) throws NumberFormatException,
			Exception
	{

		List<AppUser> AppUserList = new ArrayList<AppUser>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_AppUser + " where " + KEY_IsActive
				+ " = 1 order by " + KEY_ID + " desc";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		int i = 0;
		if (cursor.moveToFirst())
		{

			do
			{
				AppUser user = new AppUser(Integer.parseInt(cursor.getString(0)),
						cursor.getString(1), Crypto.decrypt(cursor.getString(2)),
						cursor.getString(3), Integer.parseInt(cursor.getString(4)),
						Integer.parseInt(cursor.getString(5)));

				// Adding AppUser to list
				AppUserList.add(user);
				i++;
			} while (cursor.moveToNext() && (maxRecords == 0 || i < maxRecords));

		}

		cursor.close();
		db.close();

		return AppUserList;
	}

	// Updating single AppUser
	public int updateAppUser(AppUser user) throws Exception
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_UserEmail, user.getUserEmail());
		values.put(KEY_UserPassword, Crypto.encrypt(user.getUserPassword()));
		values.put(Key_ApiKey, user.getApiKey());
		values.put(KEY_IsActive, user.getIsActiveInteger());
		values.put(KEY_IsDefault, user.getIsDefaultInteger());

		// updating row
		int return_value = db.update(TABLE_AppUser, values, KEY_ID + " = ?",
				new String[] { String.valueOf(user.getID()) });
		db.close();
		return return_value;
	}

	// Updating single AppUser
	public int updateAllIsDefaultFalse() throws Exception
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_IsDefault, 0);
		// updating rows
		int return_value = db.update(TABLE_AppUser, values, KEY_IsDefault + " = ?",
				new String[] { "1" }); // update teh isdefault with 0 at all
										// places where isdefault is 1
		db.close();
		return return_value;
	}

	// Deleting single AppUser
	public void deleteAppUser(AppUser user)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_AppUser, KEY_ID + " = ?", new String[] { String.valueOf(user.getID()) });
		db.close();
	}

	public void deleteAppUserForEmail(String Email)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_AppUser, " upper(" + KEY_UserEmail + ") = upper(?)",
				new String[] { String.valueOf(Email) });
		db.close();
	}

	// Getting AppUsers Count
	public int getAppUsersCount()
	{

		String countQuery = "SELECT  * FROM " + TABLE_AppUser;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);

		// return count
		int count = cursor.getCount();
		cursor.close();
		db.close();
		return count;
	}

	// Getting AppUsers Count
	public int getDefaultUsersCount()
	{

		String countQuery = "SELECT  * FROM " + TABLE_AppUser + " where " + KEY_IsDefault + "=1";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);

		// return count
		int count = cursor.getCount();
		cursor.close();
		db.close();
		return count;
	}

}
