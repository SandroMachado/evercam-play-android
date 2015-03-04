package io.evercam.androidapp.feedback;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class LoadTimeFeedbackItem extends FeedbackItem
{
    private final String TAG = "LoadTimeFeedbackItem";
    public final static String TYPE_REMOTE = "loadFromServer";
    public final static String TYPE_LOCAL = "loadFromDatabase";
    public final static String TYPE_REFRESH = "loadByRefreshing";

    private Float database_load_time;
    private Float evercam_load_time;

    public LoadTimeFeedbackItem(Context context, String username, Float databaseLoadTime, Float evercamLoadTime)
    {
        super(context, username);
        this.database_load_time = databaseLoadTime;
        this.evercam_load_time = evercamLoadTime;
    }

    public String toJson()
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("user", user);
            jsonObject.put("timestamp", timestamp);
            jsonObject.put("database_load_time", database_load_time);
            jsonObject.put("evercam_load_time", evercam_load_time);
            jsonObject.put("network", network);
            jsonObject.put("app_version", app_version);
            jsonObject.put("device", device);
            jsonObject.put("android_version", android_version);
        }
        catch(JSONException e)
        {
            Log.e(TAG, e.toString());
        }
        return jsonObject.toString();
    }
}
