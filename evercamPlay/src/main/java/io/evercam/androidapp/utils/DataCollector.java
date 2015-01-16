package io.evercam.androidapp.utils;

import io.evercam.androidapp.R;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

public class DataCollector 
{
	private Context context;
	
	public DataCollector(Context context)
	{
		this.context = context;
	}
	
	/**
	 * Return app version name, could be an empty string
	 */
	public String getAppVersion()
	{
		String version = "";
		PackageInfo packageInfo;
		try 
		{
			packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			version = packageInfo.versionName;
		} catch (NameNotFoundException e) 
		{
			e.printStackTrace();
		}
		return version;
	}
	
	/**
	 * Return device name in format manufacturer + model
	 */
	public static String getDeviceName() 
	{
		  String manufacturer = Build.MANUFACTURER;
		  String model = Build.MODEL;
		  if (model.startsWith(manufacturer)) 
		  {
		    return capitalize(model);
		  } 
		  else 
		  {
		    return capitalize(manufacturer) + " " + model;
		  }
	}
	
	public static String getAndroidVersion()
	{
		return Build.VERSION.RELEASE;
	}
	
	/**
	 * Check if there is any connectivity to a Wifi network
	 * @param context
	 * @param type
	 * @return
	 */
	public boolean isConnectedWifi()
	{
	    NetworkInfo info = getNetworkInfo();
	    return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
	}
	
	/**
	 * Check if there is any connectivity to a mobile network
	 * @param context
	 * @param type
	 * @return
	 */
	public boolean isConnectedMobile()
	{
	    NetworkInfo info = getNetworkInfo();
	    return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
	}
	
	/**
	 * Return '3G' or 'WiFi'
	 */
	public String getNetworkString()
	{
		if(isConnectedWifi())
		{
			return context.getString(R.string.wifi);
		}
		else if(isConnectedMobile())
		{
			return context.getString(R.string.three_g);
		}
		return context.getString(R.string.unknown);
	}
	
	/**
	 * Get the network info
	 * @param context
	 * @return
	 */
	private  NetworkInfo getNetworkInfo()
	{
	    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    return connectivityManager.getActiveNetworkInfo();
	}
	
	private static String capitalize(String s) 
	{
	  if (s == null || s.length() == 0) 
	  {
	    return "";
	  }
	  char first = s.charAt(0);
	  if (Character.isUpperCase(first)) 
	  {
	    return s;
	  } else 
	  {
	    return Character.toUpperCase(first) + s.substring(1);
	  }
	} 
}
