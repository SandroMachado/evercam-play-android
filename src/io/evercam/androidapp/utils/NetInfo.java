package io.evercam.androidapp.utils;

import io.evercam.network.ipscan.IpTranslator;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;

public class NetInfo
{
	private String localIp = EMPTY_IP;
	private String netmaskIp = EMPTY_IP;
	public static final String EMPTY_IP = "0.0.0.0";
	
	public NetInfo(Context context)
	{
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (isWifiConnected(context))
		{
			WifiInfo wifiInfo = wifi.getConnectionInfo();
			localIp = IpTranslator.getIpFromIntSigned(wifiInfo.getIpAddress());
			netmaskIp = IpTranslator.getIpFromIntSigned(wifi.getDhcpInfo().netmask);
		}
	}
	
	// Check WiFi connection
	public boolean isWifiConnected(Context context)
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetworkInfo.isConnected())
		{
			return true;
		}

		return false;
	}

	public String getLocalIp()
	{
		return localIp;
	}

	public String getNetmaskIp()
	{
		return netmaskIp;
	}
}
