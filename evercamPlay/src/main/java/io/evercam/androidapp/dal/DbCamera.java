package io.evercam.androidapp.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.EvercamCamera;

public class DbCamera extends DatabaseMaster
{
    public static final String TABLE_CAMERA = "evercamcamera";
    public static final String KEY_ID = "id";

    private final String TAG = "evercamplay-DbCamera";
    private final String KEY_CAMERA_ID = "cameraId";
    private final String KEY_CAMERA_NAME = "name";
    private final String KEY_OWNER = "owner";
    private final String KEY_REAL_OWNER = "realOwner";
    private final String KEY_CAN_EDIT = "canEdit";
    private final String KEY_CAN_DELETE = "canDelete";
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

    // Fields for edit camera
    private final String KEY_INTERNAL_HOST = "internalHost";
    private final String KEY_EXTERNAL_HOST = "externalHost";
    private final String KEY_INTERNAL_HTTP = "internalHttp";
    private final String KEY_INTERNAL_RTSP = "internalRtsp";
    private final String KEY_EXTERNAL_HTTP = "externalHttp";
    private final String KEY_EXTERNAL_RTSP = "externalRtsp";

    //Thumbnail URL
    private final String KEY_THUMBNAIL_URL = "thumbnailUrl";

    public DbCamera(Context context)
    {
        super(context);
    }

    public void onCreateCustom(SQLiteDatabase db)
    {
        String CREATE_TABLE_Cameras = "CREATE TABLE " + TABLE_CAMERA + "(" + KEY_ID + " INTEGER " +
                "PRIMARY KEY autoincrement" + "," + KEY_CAMERA_ID + " TEXT NOT NULL" + "," +
                "" + KEY_CAMERA_NAME + " TEXT NULL" + "," + KEY_OWNER + " TEXT  NOT NULL" + "," +
                "" + KEY_USERNAME + " TEXT NULL" + "," + KEY_PASSWORD + " TEXT NULL" + "," +
                "" + KEY_TIMEZONE + " TEXT NULL" + "," + KEY_VENDOR + " TEXT NULL" + "," +
                "" + KEY_MODEL + " TEXT NULL" + "," + KEY_MAC + " TEXT NULL " + "," +
                "" + KEY_EXTERNAL_JPG_URL + " TEXT NULL " + "," + KEY_INTERNAL_JPG_URL + " TEXT " +
                "NULL " + "," + KEY_EXTERNAL_RTSP_URL + " TEXT NULL" + "," +
                "" + KEY_INTERNAL_RTSP_URL + " TEXT NULL" + "," + KEY_STATUS + " TEXT NULL" + "," +
                "" + KEY_HAS_CREDENTIAL + " INTEGER NULL" + "," + KEY_INTERNAL_HOST + " TEXT " +
                "NULL" + "," + KEY_EXTERNAL_HOST + " TEXT NULL" + "," +
                "" + KEY_INTERNAL_HTTP + " INTEGER NULL" + "," + KEY_EXTERNAL_HTTP + " INTEGER " +
                "NULL" + "," + KEY_INTERNAL_RTSP + " INTEGER NULL" + "," +
                "" + KEY_EXTERNAL_RTSP + " INTEGER NULL" + "," + KEY_THUMBNAIL_URL + " TEXT NULL" +
                "," + KEY_REAL_OWNER + " TEXT NULL" + "," + KEY_CAN_EDIT + " TEXT NULL" + "," + KEY_CAN_DELETE + " TEXT NULL" + ")";
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

        Cursor cursor = db.query(TABLE_CAMERA, new String[]{KEY_ID, KEY_CAMERA_ID,
                KEY_CAMERA_NAME, KEY_OWNER, KEY_USERNAME, KEY_PASSWORD, KEY_TIMEZONE, KEY_VENDOR,
                KEY_MODEL, KEY_MAC, KEY_EXTERNAL_JPG_URL, KEY_INTERNAL_JPG_URL,
                KEY_EXTERNAL_RTSP_URL, KEY_INTERNAL_RTSP_URL, KEY_STATUS, KEY_HAS_CREDENTIAL,
                KEY_INTERNAL_HOST, KEY_EXTERNAL_HOST, KEY_INTERNAL_HTTP, KEY_EXTERNAL_HTTP,
                KEY_INTERNAL_RTSP, KEY_EXTERNAL_RTSP, KEY_THUMBNAIL_URL, KEY_REAL_OWNER, KEY_CAN_EDIT,
                KEY_CAN_DELETE}, KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null,
                null, null);
        if(cursor != null)
        {
            if(cursor.moveToFirst())
            {
                evercamCamera = new EvercamCamera();
                evercamCamera = getCameraFromCursor(cursor, evercamCamera);
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
        String selectQuery = "SELECT  * FROM " + TABLE_CAMERA + " where upper(" + KEY_OWNER + ") " +
                "= upper('" + ownerUsername + "') order by " + KEY_ID + " asc";

        return selectCameraListByQuery(selectQuery, maxRecords);
    }

    public int updateCamera(EvercamCamera evercamCamera)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = getContentValueFrom(evercamCamera);

        // update row
        int return_value = db.update(TABLE_CAMERA, values, KEY_ID + " = ?",
                new String[]{String.valueOf(evercamCamera.getId())});
        db.close();

        return return_value;
    }

    public void deleteCamera(EvercamCamera evercamCamera)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CAMERA, KEY_ID + " = ?", new String[]{String.valueOf(evercamCamera.getId
                ())});
        db.close();
    }

    public void deleteCamera(String cameraId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CAMERA, KEY_ID + " = ?", new String[]{cameraId});
        db.close();
    }

    public void deleteCameraByOwner(String ownerUsername)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CAMERA, " upper(" + KEY_OWNER + ") = upper(?)",
                new String[]{String.valueOf(ownerUsername)});
        db.close();
    }

    private ContentValues getContentValueFrom(EvercamCamera evercamCamera)
    {
        ContentValues values = new ContentValues();

        values.put(KEY_CAMERA_ID, evercamCamera.getCameraId());
        if(AppData.defaultUser != null)
        {
            values.put(KEY_OWNER, AppData.defaultUser.getUsername());
        }
        else
        {
            // If default owner not exists, save username as an empty string.
            values.put(KEY_OWNER, "");
        }
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
        values.put(KEY_INTERNAL_HOST, evercamCamera.getInternalHost());
        values.put(KEY_EXTERNAL_HOST, evercamCamera.getExternalHost());
        values.put(KEY_INTERNAL_HTTP, evercamCamera.getInternalHttp());
        values.put(KEY_EXTERNAL_HTTP, evercamCamera.getExternalHttp());
        values.put(KEY_INTERNAL_RTSP, evercamCamera.getInternalRtsp());
        values.put(KEY_EXTERNAL_RTSP, evercamCamera.getExternalRtsp());
        values.put(KEY_THUMBNAIL_URL, evercamCamera.getThumbnailUrl());
        values.put(KEY_REAL_OWNER, evercamCamera.getRealOwner());
        values.put(KEY_CAN_EDIT, evercamCamera.getCanEditInt());
        values.put(KEY_CAN_DELETE, evercamCamera.getCanDeleteInt());

        return values;
    }

    private ArrayList<EvercamCamera> selectCameraListByQuery(String query, int maxRecords)
    {
        ArrayList<EvercamCamera> cameraList = new ArrayList<EvercamCamera>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        int count = 0;
        if(cursor.moveToFirst())
        {
            do
            {
                EvercamCamera evercamCamera = new EvercamCamera();
                evercamCamera = getCameraFromCursor(cursor, evercamCamera);

                cameraList.add(evercamCamera);
                count++;
            } while(cursor.moveToNext() && (maxRecords == 0 || count < maxRecords));
        }

        cursor.close();
        db.close();

        return cameraList;
    }

    private EvercamCamera getCameraFromCursor(Cursor cursor, EvercamCamera evercamCamera)
    {
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
        evercamCamera.setInternalHost(cursor.getString(16));
        evercamCamera.setExternalHost(cursor.getString(17));
        evercamCamera.setInternalHttp(cursor.getInt(18));
        evercamCamera.setExternalHttp(cursor.getInt(19));
        evercamCamera.setInternalRtsp(cursor.getInt(20));
        evercamCamera.setExternalRtsp(cursor.getInt(21));
        evercamCamera.setThumbnailUrl(cursor.getString(22));
        evercamCamera.setRealOwner(cursor.getString(23));
        evercamCamera.setCanEdit(cursor.getInt(24) == 1);
        evercamCamera.setCanDelete(cursor.getInt(25) == 1);

        return evercamCamera;
    }
}
