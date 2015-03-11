package io.evercam.androidapp.feedback;

import android.content.Context;

import io.evercam.androidapp.utils.Constants;
import io.keen.client.android.AndroidKeenClientBuilder;
import io.keen.client.java.KeenClient;
import io.keen.client.java.KeenProject;

public class KeenHelper
{
    public static KeenClient getClient(Context context)
    {
        KeenClient client = new AndroidKeenClientBuilder(context).build();
        KeenProject keenProject = new KeenProject(Constants.KEEN_PROJECT_ID, Constants.KEEN_WRITE_KEY,
                Constants.KEEN_READ_KEY);
        client.setDefaultProject(keenProject);
        return  client;
    }
}
