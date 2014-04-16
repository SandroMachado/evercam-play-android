package io.evercam.android.dal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseMaster extends SQLiteOpenHelper
{
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "evercamdata";
	private Context context = null;

	public DatabaseMaster(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		new DbNotifcation(this.context).onCreateCustom(db);
		new EvercamDbCamera(this.context).onCreateCustom(db);
		new DbAppUser(this.context).onCreateCustom(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{

	}
}
