package io.evercam.androidapp.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.http.AndroidHttpClient;
import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.evercam.androidapp.exceptions.ConnectivityException;

public class Commons
{
    static String TAG = "evercamplay-Commons";
    static boolean enableLogs = false;

    public static boolean isOnline(Context ctx)
    {
        try
        {
            ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context
                    .CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo()
                    .isConnectedOrConnecting();

        }
        catch(Exception ex)
        {
            if(enableLogs) Log.e(TAG, ex.toString());
        }
        return false;
    }

    public static int getAppVersionCode(Context context)
    {
        try
        {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context
                    .getPackageName(), 0);
            return packageInfo.versionCode;
        }
        catch(NameNotFoundException e)
        {
            Log.e(TAG, e.toString());
        }
        return 0;
    }

    public static String readRawTextFile(int id, Context ctx)
    {
        InputStream inputStream = ctx.getResources().openRawResource(id);
        InputStreamReader in = new InputStreamReader(inputStream);
        BufferedReader buf = new BufferedReader(in);
        String line;
        StringBuilder text = new StringBuilder();
        try
        {
            while((line = buf.readLine()) != null)
            {
                text.append(line);
            }
        }
        catch(IOException e)
        {
            return null;
        }
        return text.toString();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static String[] joinStringArray(String[]... arrays)
    {
        int size = 0;
        for(String[] array : arrays)
        {
            size += array.length;
        }
        java.util.List list = new java.util.ArrayList(size);
        for(String[] array : arrays)
        {
            list.addAll(java.util.Arrays.asList(array));
        }
        return (String[]) list.toArray(new String[size]);
    }

    public static float calculateTimeDifferenceFrom(Date startTime)
    {
        long timeDifferenceLong = (new Date()).getTime() - startTime.getTime();
        return (float) timeDifferenceLong / 1000;
    }

    public static boolean isLocalIp(String ip)
    {
        if(ip != null && !ip.isEmpty())
        {
            if(ip.matches(Constants.REGULAR_EXPRESSION_LOCAL_IP))
            {
                return true;
            }
        }
        return false;
    }
}
