package io.evercam.androidapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import io.evercam.network.discovery.IpTranslator;

public class NetInfo
{
    private String localIp = EMPTY_IP;
    private String netmaskIp = EMPTY_IP;
    private String gatewayIp = EMPTY_IP;
    public static final String EMPTY_IP = "0.0.0.0";

    public NetInfo(Context context)
    {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(isWifiConnected(context))
        {
            WifiInfo wifiInfo = wifi.getConnectionInfo();
            localIp = IpTranslator.getIpFromIntSigned(wifiInfo.getIpAddress());
            netmaskIp = IpTranslator.getIpFromIntSigned(wifi.getDhcpInfo().netmask);
            gatewayIp = IpTranslator.getIpFromIntSigned(wifi.getDhcpInfo().gateway);
        }
    }

    // Check WiFi connection
    public boolean isWifiConnected(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService
                (Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager
                .TYPE_WIFI);
        if(wifiNetworkInfo.isConnected())
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

    public String getGatewayIp()
    {
        return gatewayIp;
    }
}
