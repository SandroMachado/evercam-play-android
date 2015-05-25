package io.evercam.androidapp.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader
{
    private Context context;
    private Properties properties;
    private final String TAG = "PropertyReader";
    private final String LOCAL_PROPERTY_FILE = "local.properties";
    public static final String KEY_SPLUNK_MINT = "SplunkApiKey";
    public static final String KEY_SENDGRID_USERNAME = "SendgridUsername";
    public static final String KEY_SENDGRID_PASSWORD = "SendgridPassword";
    public static final String KEY_LOGENTRIES_TOKEN = "LogentriesToken";

    public static final String KEY_KEEN_PROJECT_ID = "KeenProjectId";
    public static final String KEY_KEEN_WRITE_KEY = "KeenWriteKey";
    public static final String KEY_KEEN_READ_KEY = "KeenReadKey";

    public static final String KEY_MIXPANEL = "MixpanelToken";

    public PropertyReader(Context context)
    {
        this.context = context;
        properties = new Properties();
        properties = getProperties(LOCAL_PROPERTY_FILE);
    }

    private Properties getProperties(String fileName)
    {
        try
        {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(fileName);
            properties.load(inputStream);
        }
        catch(IOException e)
        {
            Log.e(TAG, e.toString());
        }
        catch(Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
        return properties;
    }

    public String getPropertyStr(String propertyName)
    {
        if(properties != null && isPropertyExist(propertyName))
        {
            return properties.getProperty(propertyName);
        }
        else
        {
            return "";
        }
    }

    public boolean isPropertyExist(String key)
    {
        if(properties != null)
        {
            if(properties.containsKey(key))
            {
                return true;
            }
        }
        return false;
    }
}