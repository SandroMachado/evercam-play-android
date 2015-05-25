package io.evercam.androidapp.feedback;

import android.content.Context;

import io.evercam.androidapp.utils.PropertyReader;
import io.keen.client.android.AndroidKeenClientBuilder;
import io.keen.client.java.KeenClient;
import io.keen.client.java.KeenProject;

public class KeenHelper
{
    public static KeenClient getClient(Context context)
    {
        try
        {
            PropertyReader propertyReader = new PropertyReader(context);
            KeenClient client = new AndroidKeenClientBuilder(context).build();
            KeenProject keenProject = new KeenProject(propertyReader.getPropertyStr(PropertyReader.KEY_KEEN_PROJECT_ID), propertyReader.getPropertyStr(PropertyReader.KEY_KEEN_WRITE_KEY), propertyReader.getPropertyStr(PropertyReader.KEY_KEEN_READ_KEY));

            client.setDefaultProject(keenProject);
            return client;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
