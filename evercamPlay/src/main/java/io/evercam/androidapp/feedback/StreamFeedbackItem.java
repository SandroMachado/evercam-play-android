package io.evercam.androidapp.feedback;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import io.evercam.androidapp.utils.DataCollector;

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
        super(context,username);
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
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("camera_id", camera_id);
            jsonObject.put("user", user);
            jsonObject.put("timestamp", timestamp);
            jsonObject.put("url", url);
            jsonObject.put("is_success", is_success);
            jsonObject.put("load_time", load_time);
            jsonObject.put("network", network);
            jsonObject.put("app_version", app_version);
            jsonObject.put("device", device);
            jsonObject.put("android_version", android_version);
            jsonObject.put("type", type);
        }
        catch(JSONException e)
        {
            Log.e(TAG, e.toString());
        }
        return jsonObject.toString();
    }
}
