package io.evercam.androidapp.feedback;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.evercam.androidapp.utils.Constants;
import io.keen.client.java.KeenClient;

public class LoadTimeFeedbackItem extends FeedbackItem
{
    private final String TAG = "LoadTimeFeedbackItem";
    public final static String TYPE_REMOTE = "loadFromServer";
    public final static String TYPE_LOCAL = "loadFromDatabase";
    public final static String TYPE_REFRESH = "loadByRefreshing";

    private Float database_load_time;
    private Float evercam_load_time;

    public LoadTimeFeedbackItem(Context context, String username, Float databaseLoadTime,
                                Float evercamLoadTime)
    {
        super(context, username);
        this.database_load_time = databaseLoadTime;
        this.evercam_load_time = evercamLoadTime;
    }

    public String toJson()
    {
        JSONObject jsonObject;
        try
        {
            jsonObject = getBaseJsonObject();
            jsonObject.put("database_load_time", database_load_time);
            jsonObject.put("evercam_load_time", evercam_load_time);
            return jsonObject.toString();
        }
        catch(JSONException e)
        {
            Log.e(TAG, e.toString());
        }
        return "";
    }

    @Override
    public HashMap<String, Object> toHashMap()
    {
        HashMap<String, Object> event = super.toHashMap();
        event.put("database_load_time", database_load_time);
        event.put("evercam_load_time", evercam_load_time);
        return event;
    }

    @Override
    public void sendToKeenIo(final KeenClient client)
    {
        if(client != null)
        {
            final FeedbackItem feedbackItem = this;
            new Thread(new Runnable()
            {

                @Override
                public void run()
                {
                    client.addEvent(Constants.KEEN_COLLECTION_LIST_LOADING_TIME, feedbackItem.toHashMap());

                }
            }).start();
        }
    }
}
