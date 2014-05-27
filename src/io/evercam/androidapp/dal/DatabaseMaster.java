package io.evercam.androidapp.dal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseMaster extends SQLiteOpenHelper
{
	private static final int DATABASE_VERSION = 3; //version 2: added camera field :has credential
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
		new DbCamera(this.context).onCreateCustom(db);
		new DbAppUser(this.context).onCreateCustom(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		
	}
}
