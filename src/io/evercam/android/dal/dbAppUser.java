package io.evercam.android.dal;

import io.evercam.android.dto.AppUser;
import io.evercam.android.utils.Crypto;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DbAppUser extends DatabaseMaster
{
	private static final String TABLE_APP_USER = "appuser";

	// Users Table Columns names
	public static final String KEY_ID = "id";
	public static final String KEY_EMAIL = "useremail";
	public static final String KEY_PASSWORD = "userpassword";
	public static final String KEY_API_KEY = "userapikey";
	public static final String KEY_IS_ACTIVE = "isactive";
	public static final String KEY_IS_DEFAULT = "isdefault";

	public DbAppUser(Context context)
	{
		super(context);
	}

	// Creating Tables
	public void onCreateCustom(SQLiteDatabase db)
	{
		String createUserTableQuery = "CREATE TABLE " + TABLE_APP_USER + "(" + KEY_ID
				+ " INTEGER PRIMARY KEY autoincrement" + "," + KEY_EMAIL + " TEXT UNIQUE" + ","
				+ KEY_PASSWORD + " TEXT" + "," + KEY_API_KEY + " TEXT" + "," + KEY_IS_ACTIVE
				+ " INTEGER" + "," + KEY_IS_DEFAULT + " INTEGER"

				+ ")";
		db.execSQL(createUserTableQuery);
	}

	// Upgrading database
	public void onUpgradeCustom(SQLiteDatabase db, int oldVersion, int newVersion)
	{

		db.execSQL("DROP TABLE IF EXISTS " + TABLE_APP_USER);
		onCreateCustom(db);
	}

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 * 
	 * @throws Exception
	 */

	public void addAppUser(AppUser user) throws Exception
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(KEY_EMAIL, user.getUserEmail());
		values.put(KEY_PASSWORD, Crypto.encrypt(user.getUserPassword()));
		values.put(KEY_API_KEY, user.getApiKey());
		values.put(KEY_IS_ACTIVE, user.getIsActiveInteger());
		values.put(KEY_IS_DEFAULT, user.getIsDefaultInteger());

		// Inserting Row
		db.insert(TABLE_APP_USER, null, values);
		db.close(); // Closing database connection

	}

	// Getting single AppUser
	public AppUser getAppUser(String email) throws NumberFormatException, Exception
	{
		AppUser user = null;
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_APP_USER, new String[] { KEY_ID, KEY_EMAIL,
				KEY_PASSWORD, KEY_API_KEY, KEY_IS_ACTIVE, KEY_IS_DEFAULT }, KEY_EMAIL + "=?",
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

	public AppUser getAppUserByID(int userId) throws NumberFormatException, Exception
	{
		AppUser user = null;
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_APP_USER, new String[] { KEY_ID, KEY_EMAIL,
				KEY_PASSWORD, KEY_API_KEY, KEY_IS_ACTIVE, KEY_IS_DEFAULT }, KEY_ID + "=?",
				new String[] { userId + "" }, null, null, null, null);
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

		String selectQuery = "SELECT  max(" + KEY_ID + ") FROM " + TABLE_APP_USER;

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

	public List<AppUser> getAllAppUsers(int maxRecords) throws NumberFormatException, Exception
	{
		List<AppUser> appUserList = new ArrayList<AppUser>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_APP_USER + " order by " + KEY_EMAIL
				+ " asc";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// loop through all rows and adding to list
		int count = 0;
		if (cursor.moveToFirst())
		{

			do
			{
				AppUser user = new AppUser(Integer.parseInt(cursor.getString(0)),
						cursor.getString(1), Crypto.decrypt(cursor.getString(2)),
						cursor.getString(3), Integer.parseInt(cursor.getString(4)),
						Integer.parseInt(cursor.getString(5)));

				appUserList.add(user);
				count++;
			} while (cursor.moveToNext() && (maxRecords == 0 || count < maxRecords));

		}

		cursor.close();
		db.close();
		return appUserList;
	}

	public List<AppUser> getAllActiveAppUsers(int maxRecords) throws NumberFormatException,
			Exception
	{

		List<AppUser> AppUserList = new ArrayList<AppUser>();

		String selectQuery = "SELECT  * FROM " + TABLE_APP_USER + " where " + KEY_IS_ACTIVE
				+ " = 1 order by " + KEY_ID + " desc";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		int count = 0;
		if (cursor.moveToFirst())
		{

			do
			{
				AppUser user = new AppUser(Integer.parseInt(cursor.getString(0)),
						cursor.getString(1), Crypto.decrypt(cursor.getString(2)),
						cursor.getString(3), Integer.parseInt(cursor.getString(4)),
						Integer.parseInt(cursor.getString(5)));

				AppUserList.add(user);
				count++;
			} while (cursor.moveToNext() && (maxRecords == 0 || count < maxRecords));

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
		values.put(KEY_EMAIL, user.getUserEmail());
		values.put(KEY_PASSWORD, Crypto.encrypt(user.getUserPassword()));
		values.put(KEY_API_KEY, user.getApiKey());
		values.put(KEY_IS_ACTIVE, user.getIsActiveInteger());
		values.put(KEY_IS_DEFAULT, user.getIsDefaultInteger());

		// updating row
		int return_value = db.update(TABLE_APP_USER, values, KEY_ID + " = ?",
				new String[] { String.valueOf(user.getId()) });
		db.close();
		return return_value;
	}

	// Updating single AppUser
	public int updateAllIsDefaultFalse() throws Exception
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_IS_DEFAULT, 0);
		// updating rows
		int return_value = db.update(TABLE_APP_USER, values, KEY_IS_DEFAULT + " = ?",
				new String[] { "1" }); // update teh isdefault with 0 at all
										// places where isdefault is 1
		db.close();
		return return_value;
	}

	// Deleting single AppUser
	public void deleteAppUser(AppUser user)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_APP_USER, KEY_ID + " = ?", new String[] { String.valueOf(user.getId()) });
		db.close();
	}

	public void deleteAppUserForEmail(String Email)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_APP_USER, " upper(" + KEY_EMAIL + ") = upper(?)",
				new String[] { String.valueOf(Email) });
		db.close();
	}

	// Getting AppUsers Count
	public int getAppUsersCount()
	{

		String countQuery = "SELECT  * FROM " + TABLE_APP_USER;
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

		String countQuery = "SELECT  * FROM " + TABLE_APP_USER + " where " + KEY_IS_DEFAULT + "=1";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);

		// return count
		int count = cursor.getCount();
		cursor.close();
		db.close();
		return count;
	}

}
