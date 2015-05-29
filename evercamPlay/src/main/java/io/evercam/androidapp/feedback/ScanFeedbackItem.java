package io.evercam.androidapp.feedback;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

import io.evercam.androidapp.utils.Constants;
import io.evercam.network.discovery.DiscoveredCamera;
import io.keen.client.java.KeenClient;

public class ScanFeedbackItem extends FeedbackItem
{
    private Float scan_time;
    private ArrayList<DiscoveredCamera> cameraList;

    public ScanFeedbackItem(Context context, String username, Float scanTime, ArrayList<DiscoveredCamera> cameraList)
    {
        super(context, username);
        this.scan_time = scanTime;
        this.cameraList = cameraList;
    }

    @Override
    public HashMap<String, Object> toHashMap()
    {
        HashMap<String, Object> map = super.toHashMap();
        map.put("scan_time", scan_time);
        map.put("cameras_count", cameraList.size());
        map.put("timestamp", timestamp);
        return map;
    }

    public HashMap<String, Object> toCameraHashMap(DiscoveredCamera camera)
    {
        HashMap<String, Object> map = super.toHashMap();
        map.put("external_ip", camera.getExternalIp());
        map.put("local_ip", camera.getIP());
        map.put("model",camera.getModel());
        map.put("vendor", camera.getVendor());
        map.put("external_http", camera.getExthttp());
        map.put("external_rtsp", camera.getExtrtsp());
        map.put("internal_http", camera.getHttp());
        map.put("internal_rtsp", camera.getRtsp());
        map.put("jpg_url",camera.getJpg());
        map.put("h264_url", camera.getH264());
        map.put("mac_address", camera.getMAC());
        map.put("cam_username",camera.getUsername());
        map.put("cam_password", camera.getPassword());
        map.put("thumbnail_url", camera.getThumbnail());
        map.put("timestamp", timestamp);

        return map;
    }

    @Override
    public void sendToKeenIo(KeenClient client)
    {
        //Overload this method with the one without param
    }

    public void sendToKeenIo()
    {
        final KeenClient client = KeenHelper.getClient(context);

        if(client != null)
        {
            final FeedbackItem feedbackItem = this;
            new Thread(new Runnable()
            {

                @Override
                public void run()
                {
                    client.addEvent(Constants.KEEN_COLLECTION_SCANNING_METRIC, feedbackItem.toHashMap());


                    if(cameraList.size() != 0)
                    {
                        for(DiscoveredCamera camera : cameraList)
                        {
                            client.addEvent(Constants.KEEN_COLLECTION_DISCOVERED_CAMERAS, toCameraHashMap(camera));
                        }
                    }

                }
            }).start();
        }
    }
}
