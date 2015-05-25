package io.evercam.androidapp.feedback;

import android.content.Context;

import java.util.HashMap;

import io.evercam.androidapp.utils.Constants;
import io.keen.client.java.KeenClient;

public class NewCameraFeedbackItem extends FeedbackItem
{
    private String camera_id;
    private boolean from_discovery = false;

    public NewCameraFeedbackItem(Context context, String username, String cameraId)
    {
        super(context, username);
        this.camera_id = cameraId;
    }

    public void setIsFromDiscovery(boolean isFromDiscovery)
    {
        this.from_discovery = isFromDiscovery;
    }

    @Override
    public HashMap<String, Object> toHashMap()
    {
        HashMap<String, Object> map = super.toHashMap();
        map.put("from", FROM_ANDROID);
        map.put("camera_id", camera_id);
        map.put("from_discovery", from_discovery);

        return map;
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
                    client.addEvent(Constants.KEEN_COLLECTION_NEW_CAMERA, feedbackItem.toHashMap());
                }
            }).start();
        }
    }
}
