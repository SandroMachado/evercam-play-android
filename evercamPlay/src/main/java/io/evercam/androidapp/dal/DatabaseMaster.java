package io.evercam.androidapp.dal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseMaster extends SQLiteOpenHelper
{
    // Version 3: Added camera field :has credential
    // Version 4: New constraint camera id + owner
    // Version 5: Added camera fields for internal && external host &
    // port.Remove camera id-user constraint
    // Version 6: Added fields camera owner and can edit.
    // Version 7: Add can delete
    // Version 8: Removed database user table
    // Version 9: Add thumbnail URL in camera object
    private static final String TAG = "evercamplay-DatabaseMaster";
    private static final int DATABASE_VERSION = 9;
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
        new DbCamera(this.context).onCreateCustom(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        new DbCamera(context).onUpgradeCustom(db, oldVersion, newVersion);
    }
}
