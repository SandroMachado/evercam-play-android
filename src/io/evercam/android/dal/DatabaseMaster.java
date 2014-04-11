package io.evercam.android.dal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseMaster extends SQLiteOpenHelper
{

	// All Static variables
	// Database Version

	/*
	 * version 1 Tables creation
	 */

	private static final int DATABASE_VERSION = 17;

	// Database Name
	private static final String DATABASE_NAME = "CambaData";
	private Context context = null;

	public DatabaseMaster(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		new dbNotifcation(this.context).onCreateCustom(db);
		new dbCamera(this.context).onCreateCustom(db);
		new dbAppUser(this.context).onCreateCustom(db);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		if (oldVersion < 14)
		{
			new dbNotifcation(context).onUpgradeCustom(db, oldVersion, newVersion); // new
																					// version
																					// =
																					// 14
		}
		if (oldVersion < 17)
		{
			new dbCamera(context).onUpgradeCustom(db, oldVersion, newVersion); // new
																				// version
																				// =
																				// 17
		}
		if (newVersion < 14)
		{
			new dbAppUser(context).onUpgradeCustom(db, oldVersion, newVersion); // new
																				// version
																				// =
																				// 14
		}
	}
}
