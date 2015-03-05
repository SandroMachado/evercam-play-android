package io.evercam.androidapp.feedback;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.evercam.androidapp.utils.Constants;
import io.keen.client.java.KeenClient;

public class StreamFeedbackItem extends FeedbackItem
{
    private final static String TAG = "StreamFeedbackItem";
    public final static String TYPE_JPG = "jpg";
    public final static String TYPE_RTSP = "rtsp";

    //Camera stream details;
    private String url = "";
    private String type = "";
    private String camera_id = "";
    private Boolean is_success;
    private Float load_time;

    public StreamFeedbackItem(Context context, String username, Boolean isSuccess)
    {
        super(context, username);
        this.is_success = isSuccess;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public void setCameraId(String cameraId)
    {
        this.camera_id = cameraId;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return type;
    }

    public void setLoadTime(Float loadTime)
    {
        this.load_time = loadTime;
    }

    public Float getLoad_time()
    {
        return load_time;
    }

    public Boolean getIs_success()
    {
        return is_success;
    }

    public String getUrl()
    {
        return url;
    }

    public String getCamera_id()
    {
        return camera_id;
    }

    public String toJson()
    {
        try
        {
            JSONObject jsonObject = getBaseJsonObject();
            jsonObject.put("camera_id", camera_id);
            jsonObject.put("url", url);
            jsonObject.put("is_success", is_success);
            jsonObject.put("load_time", load_time);
            jsonObject.put("type", type);
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
        event.put("camera_id", camera_id);
        event.put("url", url);
        event.put("is_success", is_success);
        event.put("load_time", load_time);
        event.put("type", type);
        return event;
    }

    public void sendToKeenIo(final KeenClient client)
    {
        final FeedbackItem feedbackItem = this;
        new Thread(new Runnable()
        {

            @Override
            public void run()
            {
                client.addEvent(Constants.KEEN_COLLECTION_STREAM_LOADING_TIME,
                        feedbackItem.toHashMap());
            }
        }).start();
    }
}
