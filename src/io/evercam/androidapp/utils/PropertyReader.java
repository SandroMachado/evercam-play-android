package io.evercam.androidapp.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class PropertyReader
{
	private Context context;
	private Properties properties;
	private final String TAG = "evercamplay-PropertyReader";
	private final String LOCAL_PROPERTY_FILE = "local.properties";
	public final static String KEY_API_KEY = "ApiKey";
	public final static String KEY_API_ID = "ApiId";
	public static final String KEY_BUG_SENSE = "BugSenseCode";
	public static final String KEY_SENDGRID_USERNAME = "SendgridUsername";
	public static final String KEY_SENDGRID_PASSWORD = "SendgridPassword";

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
		catch (IOException e)
		{
			Log.e(TAG, e.toString());
		}
		return properties;
	}

	public String getPropertyStr(String propertyName)
	{
		if (isPropertyExist(propertyName))
		{
			return properties.getProperty(propertyName).toString();
		}
		else
		{
			return "";
		}
	}

	public boolean isPropertyExist(String key)
	{
		if (properties.containsKey(key))
		{
			return true;
		}
		return false;
	}

}