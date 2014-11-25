package io.evercam.androidapp.utils;

import org.xmlpull.v1.XmlPullParserException;

import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.ImageRecord;
import io.evercam.androidapp.exceptions.*;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;

import java.io.IOException;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

public class CambaRecordingApiManager
{

	static String TAG = "CambaApiManager";
	static boolean enableLogs = false;

	static String debugValues = "";

	static String CambaRecordingApiBaseUrl = "http://webapi.camba.tv/";
	static String CambaRecordingApi_GetMonthDays = "GetMonthDays";
	static String CambaRecordingApi_GetDayHours = "GetDayHours";
	static String CambaRecordingApi_GetCameraInfoOptimized = "GetCameraInfoOptimized";
	static String CambaRecordingApi_GetSnapshotsOptimized = "GetSnapshotsOptimized";

	public static String debug;

	public static final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";

	public static String GetGetMonthDays1(int cameraId, int keepHistoryDays, String timeZone,
			int month, int year, String cameraGroup, int status) throws Exception,
			IllegalArgumentException
	{
		String serverResponse = null;
		try
		{
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(CambaRecordingApiBaseUrl + "/"
					+ CambaRecordingApi_GetMonthDays);
			post.setHeader("Accept", "application/xml");
			post.setHeader("User-Agent", "Apache-HttpClient/4.1 (java 1.5)");

			post.setHeader("Host", "localhost");

			List<NameValuePair> nvps = new ArrayList<NameValuePair>();

			nvps.add(new BasicNameValuePair("cameraId", cameraId + ""));
			nvps.add(new BasicNameValuePair("keepHistoryDays", keepHistoryDays + ""));
			nvps.add(new BasicNameValuePair("timeZone", timeZone + ""));
			nvps.add(new BasicNameValuePair("month", month + ""));
			nvps.add(new BasicNameValuePair("year", year + ""));
			nvps.add(new BasicNameValuePair("cameraGroup", cameraGroup + ""));
			nvps.add(new BasicNameValuePair("status", status + ""));

			AbstractHttpEntity ent = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
			ent.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
			ent.setContentEncoding("UTF-8");
			post.setEntity(ent);
			Commons.setTimeouts(post.getParams());
			HttpResponse response = client.execute(post);
			serverResponse = HttpUtils.getResponseBody(response);
			if (enableLogs) Log.i(TAG, ":Response getCambaKey:[" + serverResponse + "]");

			return serverResponse;

		}
		catch (UnknownHostException uhe)
		{
			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + uhe.toString(), uhe);
			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity, serverResponse);
		}
		catch (SocketTimeoutException e)
		{
			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + e.toString(), e);
			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity, serverResponse);
		}
		catch (ConnectException e)
		{
			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + e.toString(), e);
			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity, serverResponse);
		}
		catch (ConnectTimeoutException hce)
		{
			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + hce.toString(), hce);
			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity, serverResponse);
		}
		catch (IOException e)
		{
			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + e.toString(), e);
			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity, serverResponse);
		}
		catch (Exception ex)
		{
			if (enableLogs) Log.e(TAG, ex.toString() + "::" + Log.getStackTraceString(ex));
			throw ex;
		}
	}

	// list of cameras extracted from the api xml
	private static int getMonthDaysListFromXML(String XML, ArrayList<Boolean> list)
			throws XmlPullParserException, IOException
	{
		int totalActiveDaysCount = 0;

		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		xpp.setInput(new StringReader(XML));

		xpp.next();
		int eventType = xpp.getEventType();
		// int count = 0;
		String tagname = "";
		while (eventType != XmlPullParser.END_DOCUMENT)
		{ // count++;

			if (eventType == XmlPullParser.START_TAG)
			{
				if (eventType != XmlPullParser.TEXT)
				{
					tagname = (xpp.getName() + "").toUpperCase(Locale.ENGLISH);
				}
			}
			else if (eventType == XmlPullParser.TEXT)// && xpp.getName() != null
														// && xpp.getText() !=
														// null)
			{
				String tagtext = xpp.getText() + "";
				if (tagname.equalsIgnoreCase("int"))
				{
					list.set(Integer.parseInt(tagtext), true);
					totalActiveDaysCount++;
				}

			}
			eventType = xpp.next();
		}

		return totalActiveDaysCount;
	}

	public static String GetCameraInfoOptimized(int cameraId, int status, Boolean isMdEnabled,
			int alarmLevel, String cameraTimeZone, String cameraGroup, String from, String to,
			String playFromDt) throws Exception, IllegalArgumentException
	{
		String serverResponse = null;
		try
		{
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(CambaRecordingApiBaseUrl + "/"
					+ CambaRecordingApi_GetCameraInfoOptimized);
			post.setHeader("Accept", "application/xml");
			post.setHeader("User-Agent", "Apache-HttpClient/4.1 (java 1.5)");

			post.setHeader("Host", "localhost");

			List<NameValuePair> nvps = new ArrayList<NameValuePair>();

			nvps.add(new BasicNameValuePair("cameraId", cameraId + ""));
			nvps.add(new BasicNameValuePair("status", status + ""));
			nvps.add(new BasicNameValuePair("isMdEnabled", isMdEnabled + ""));
			nvps.add(new BasicNameValuePair("alarmLevel", alarmLevel + ""));
			nvps.add(new BasicNameValuePair("cameraTimeZone", cameraTimeZone + ""));
			nvps.add(new BasicNameValuePair("cameraGroup", cameraGroup + ""));
			nvps.add(new BasicNameValuePair("from", from + ""));
			nvps.add(new BasicNameValuePair("to", to + ""));
			nvps.add(new BasicNameValuePair("playFromDt", playFromDt + ""));

			AbstractHttpEntity ent = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
			ent.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
			ent.setContentEncoding("UTF-8");
			post.setEntity(ent);
			Commons.setTimeouts(post.getParams());
			HttpResponse response = client.execute(post);
			serverResponse = HttpUtils.getResponseBody(response);

			int startIndex = serverResponse.indexOf("<HourServerIp>");
			if (startIndex <= 0) return null;

			int endIndex = serverResponse.indexOf("</HourServerIp>");
			return serverResponse.substring(startIndex + 14, endIndex);

			// return serverResponse;

		}
		catch (UnknownHostException uhe)
		{
			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + uhe.toString(), uhe);
			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity, serverResponse);
		}
		catch (SocketTimeoutException e)
		{
			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + e.toString(), e);
			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity, serverResponse);
		}
		catch (ConnectException e)
		{
			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + e.toString(), e);
			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity, serverResponse);
		}
		catch (ConnectTimeoutException hce)
		{
			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + hce.toString(), hce);
			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity, serverResponse);
		}
		catch (IOException e)
		{
			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + e.toString(), e);
			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity, serverResponse);
		}
		catch (Exception ex)
		{
			if (enableLogs) Log.e(TAG, ex.toString() + "::" + Log.getStackTraceString(ex));
			throw ex;
		}
	}

	public static boolean GetSnapshotsOptimized(ArrayList<ImageRecord> imageList,
			String cameraCode, String fromDate, String toDate, int iteration, int limit)
			throws Exception, IllegalArgumentException
	{
		boolean returnValue = false;
		if (imageList == null) imageList = new ArrayList<ImageRecord>();
		String serverResponse = null;
		try
		{
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(CambaRecordingApiBaseUrl + "v1/cameras/" + cameraCode
					+ "/snapshots/" + fromDate + "/" + toDate + "/" + iteration + "/" + limit);
			get.setHeader("Accept", "application/xml");
			get.setHeader("User-Agent", "Apache-HttpClient/4.1 (java 1.5)");

			get.setHeader("Authorization", "Basic " + AppData.defaultUser.getApiKey());

			// get.setHeader("Host", "localhost");

			Commons.setTimeouts(get.getParams());
			HttpResponse response = client.execute(get);
			String XML = HttpUtils.getResponseBody(response);

			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(new StringReader(XML));

			ImageRecord obj = new ImageRecord();

			xpp.next();
			int eventType = xpp.getEventType();
			// int count = 0;
			String tagname = "";
			while (eventType != XmlPullParser.END_DOCUMENT)
			{ // count++;

				if (eventType == XmlPullParser.START_TAG)
				{
					if (xpp.getName().toUpperCase(Locale.ENGLISH)
							.equalsIgnoreCase("SnapshotInfoModel"))
					{
						obj = new ImageRecord();
					}
					if (eventType != XmlPullParser.TEXT)
					{
						tagname = (xpp.getName() + "").toUpperCase(Locale.ENGLISH);
					}

				}
				else if (eventType == XmlPullParser.TEXT)// && xpp.getName() !=
															// null &&
															// xpp.getText() !=
															// null)
				{
					String tagtext = xpp.getText() + "";
					Log.i(TAG, "\"" + tagname + "\":\"" + tagtext + "\"");
					obj.SetCameraProperty(obj, tagname, tagtext);
				}
				else if (eventType == XmlPullParser.END_TAG)
				{
					if (xpp.getName().toUpperCase(Locale.ENGLISH)
							.equalsIgnoreCase("SnapshotInfoModel"))
					{
						imageList.add(obj);
						returnValue = true;
					}
				}
				eventType = xpp.next();
			}

			return returnValue;
		}
		catch (UnknownHostException uhe)
		{
			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + uhe.toString(), uhe);
			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity, serverResponse);
		}
		catch (SocketTimeoutException e)
		{
			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + e.toString(), e);
			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity, serverResponse);
		}
		catch (ConnectException e)
		{
			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + e.toString(), e);
			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity, serverResponse);
		}
		catch (ConnectTimeoutException hce)
		{
			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + hce.toString(), hce);
			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity, serverResponse);
		}
		catch (IOException e)
		{
			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + e.toString(), e);
			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity, serverResponse);
		}
		catch (Exception ex)
		{
			if (enableLogs) Log.e(TAG, ex.toString() + "::" + Log.getStackTraceString(ex));
			throw ex;
		}
	}

}