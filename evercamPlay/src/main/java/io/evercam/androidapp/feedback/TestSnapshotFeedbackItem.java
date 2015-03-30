package io.evercam.androidapp.feedback;

import android.content.Context;

import java.util.HashMap;

import io.evercam.androidapp.utils.Constants;
import io.keen.client.java.KeenClient;

public class TestSnapshotFeedbackItem extends FeedbackItem
{
    private String snapshot_url = "";
    private String cam_username = "";
    private String cam_password = "";
    private boolean is_success;
    private boolean is_port_opened;

    public TestSnapshotFeedbackItem(Context context, String username, boolean isSuccess, boolean isPortOpened)
    {
        super(context, username);
        this.is_success = isSuccess;
        this.is_port_opened = isPortOpened;
    }

    public TestSnapshotFeedbackItem setSnapshot_url(String snapshot_url)
    {
        this.snapshot_url = snapshot_url;
        return this;
    }

    public TestSnapshotFeedbackItem setCam_username(String cam_username)
    {
        this.cam_username = cam_username;
        return this;
    }

    public TestSnapshotFeedbackItem setCam_password(String cam_password)
    {
        this.cam_password = cam_password;
        return this;
    }

    @Override
    public HashMap<String, Object> toHashMap()
    {
        HashMap<String, Object> map = super.toHashMap();
        map.put("is_success", is_success);
        map.put("is_port_opened", is_port_opened);
        map.put("snapshot_url", snapshot_url);
        map.put("cam_username", cam_username);
        map.put("cam_password", cam_password);
        return map;
    }

    @Override
    public void sendToKeenIo(final KeenClient client)
    {
        final FeedbackItem feedbackItem = this;
        new Thread(new Runnable()
        {

            @Override
            public void run()
            {
                client.addEvent(Constants.KEEN_COLLECTION_TEST_SNAPSHOT,
                        feedbackItem.toHashMap());
            }
        }).start();
    }
}
