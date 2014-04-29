package io.evercam.androidapp.utils;

//package io.evercam.androidapp.utils;
//
//import io.evercam.androidapp.dto.Camera;
//import io.evercam.androidapp.exceptions.*;
//import io.evercam.androidapp.utils.Commons;
//import io.evercam.androidapp.utils.Constants;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.StringReader;
//import java.net.ConnectException;
//import java.net.SocketTimeoutException;
//import java.net.UnknownHostException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.conn.ConnectTimeoutException;
//import org.apache.http.entity.AbstractHttpEntity;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.message.BasicNameValuePair;
//import org.apache.http.protocol.HTTP;
//import org.xmlpull.v1.XmlPullParser;
//import org.xmlpull.v1.XmlPullParserException;
//import org.xmlpull.v1.XmlPullParserFactory;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.drawable.Drawable;
//import android.os.Environment;
//import android.util.Log;
//
//public class CambaApiManager
//{
//
//	static String TAG = "CambaApiManager";
//	static boolean enableLogs = false;
//
//	static String debugValues = "";
//
//	// static String CambaApiUrl = "http://www.camba.tv/api/";//
//	// Bionic:"http://maxis.bionic-csi.com/api/";
//
//	// static String CambaApiUrl = "http://api.camba.tv/";
//	// static String CambaApiCameraUrl = CambaApiUrl + "Camera"; //
//	// "http://www.camba.tv/api/camera"
//	// static String CambaApiLatestImageUrl = CambaApiUrl + "image/latest/" ;
//	// //http://camba.tv/api/image/latest/
//	// static String CambaApiTokenUrl = CambaApiUrl + "token"; //
//	// "http://www.camba.tv/api/token"
//	// static String CambaApiGcmDeviceUrl = CambaApiUrl + "gcmdevice";
//	// //http://camba.tv/api/gcmdevice
//
//	static String CambaApiUrl = "http://webapi.camba.tv/v1/";
//	static String CambaApiCameraUrl = CambaApiUrl + "cameras"; // "http://www.camba.tv/api/camera"
//	static String CambaApiLatestImageUrl = CambaApiUrl + "image/latest/"; // http://camba.tv/api/image/latest/
//	static String CambaApiTokenUrl = CambaApiUrl + "auth/token"; // "http://www.camba.tv/api/token"
//	static String CambaApiGcmDeviceUrl = CambaApiUrl + "gcmdevice"; // http://camba.tv/api/gcmdevice
//
//	public static Boolean isLoggedIn = false;
//	// key to be passed to the camba api
//
//	public static String debug;
//
//	// get the bitmap image from the camba website for a given camera id
//	public static Bitmap getCameraLatestBitmap(int cameraID) throws Exception
//	{
//		String URL = null;
//		try
//		{
//			URL = CambaApiLatestImageUrl + cameraID;
//			HttpClient client = new DefaultHttpClient();
//			HttpGet get = new HttpGet(URL);
//			get.setHeader("Accept", "image/jpeg");
//			get.setHeader("User-Agent", "Apache-HttpClient/4.1 (java 1.5)");
//			get.setHeader("Authorization",
//					getB64AuthCamba(AppData.AppUserEmail, AppData.AppUserPassword));
//			Commons.setTimeouts(get.getParams());
//			HttpResponse response = client.execute(get);
//			HttpEntity entity = response.getEntity();
//			InputStream input = entity.getContent();
//			return BitmapFactory.decodeStream(input);
//		}
//		catch (Exception ex)
//		{
//			if (enableLogs) Log.e(TAG,
//					ex.toString() + "::URL[" + URL + "]" + Log.getStackTraceString(ex));
//			throw ex;
//		}
//	}
//
//	// get the drawable image from the camba website for a given camera id
//	public static Drawable getCameraLatestDrawable(int cameraID) throws Exception
//	{
//		String URL = null;
//		try
//		{
//			URL = CambaApiLatestImageUrl + cameraID;
//			HttpClient client = new DefaultHttpClient();
//			HttpGet get = new HttpGet(URL);
//			get.setHeader("Accept", "image/jpeg");
//			get.setHeader("User-Agent", "Apache-HttpClient/4.1 (java 1.5)");
//			get.setHeader("Authorization",
//					getB64AuthCamba(AppData.AppUserEmail, AppData.AppUserPassword));
//			Commons.setTimeouts(get.getParams());
//			HttpResponse response = client.execute(get);
//			HttpEntity entity = response.getEntity();
//			InputStream input = entity.getContent();
//			Drawable img = Drawable.createFromStream(input,
//					Environment.getExternalStorageDirectory() + "/media/" + cameraID + ".jpg");// cont.getCacheDir()
//																								// );
//			return img;
//		}
//
//		catch (Exception ex)
//		{
//			if (enableLogs) Log.e(TAG,
//					ex.toString() + "::URL[" + URL + "]" + Log.getStackTraceString(ex));
//			throw ex;
//		}
//	}
//
//	// get the bitmap image from the camba website for a given camera id and
//	// save it to the cache of the applciation
//	public static boolean getCameraLatestImageAndSave(int cameraID, String pathString,
//			int timeoutMillis) throws Exception
//	{
//		String URL = null;
//		try
//		{
//			URL = CambaApiLatestImageUrl + cameraID;
//			HttpClient client = new DefaultHttpClient();
//			HttpGet get = new HttpGet(URL);
//			get.setHeader("Accept", "image/jpeg");
//			get.setHeader("User-Agent", "Apache-HttpClient/4.1 (java 1.5)");
//			get.setHeader("Authorization",
//					getB64AuthCamba(AppData.AppUserEmail, AppData.AppUserPassword));
//			Commons.setTimeouts(get.getParams(), timeoutMillis);
//			HttpResponse response = client.execute(get);
//			HttpEntity entity = response.getEntity();
//			InputStream input = entity.getContent();
//
//			File file = new File(pathString);
//			if (file.exists())
//			{
//				file.delete();
//			}
//			file.createNewFile();
//			FileOutputStream fos = new FileOutputStream(file);
//
//			int read = 0;
//			byte[] bytes = new byte[1024];
//			while ((read = input.read(bytes)) != -1)
//			{
//				fos.write(bytes, 0, read);
//			}
//
//			input.close();
//			fos.flush();
//			fos.close();
//
//			return true;
//		}
//		catch (Exception ex)
//		{
//			if (enableLogs) Log.e(TAG,
//					ex.toString() + "::URL[" + URL + "]" + Log.getStackTraceString(ex));
//			throw ex;
//		}
//	}
//
//	// get the camba key from the camba api. this contains the username and
//	// password token and expires in few hours
//	public static String getCambaKey(String username, String password) throws Exception,
//			IllegalArgumentException
//	{
//		String serverResponse = null;
//		try
//		{
//			HttpClient client = new DefaultHttpClient();
//			HttpPost post = new HttpPost(CambaApiTokenUrl);
//			post.setHeader("Accept", "application/xml");
//			post.setHeader("User-Agent", "Apache-HttpClient/4.1 (java 1.5)");
//			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
//			nvps.add(new BasicNameValuePair("user_name", username));
//			nvps.add(new BasicNameValuePair("user_password", password));
//			AbstractHttpEntity ent = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);// (nvps,
//																				// HTTP.UTF_8);
//			ent.setContentType("application/x-www-form-urlencoded; charset=UTF-8");// x-www-form-urlencoded;
//																					// charset=UTF-8");
//			ent.setContentEncoding("UTF-8");
//			post.setEntity(ent);
//			Commons.setTimeouts(post.getParams());
//			HttpResponse response = client.execute(post);
//			serverResponse = HttpUtils.getResponseBody(response);
//			if (enableLogs) Log.i(TAG, ":Response getCambaKey:[" + serverResponse + "]");
//
//			if (!(serverResponse + "").contains("<key>")
//					|| !(serverResponse + "").contains("</key>"))
//			{
//				if (enableLogs) Log.i(TAG, "Server Returned [" + serverResponse + "]");
//				throw new CredentialsException(null, serverResponse + "");
//			}
//
//			int sIndex = serverResponse.indexOf("<key>");
//			int eIndex = serverResponse.indexOf("</key>");
//
//			String key = serverResponse.substring(sIndex + 5, eIndex);
//
//			return key;
//		}
//		catch (CredentialsException ie)
//		{
//			if (enableLogs) Log.e(
//					TAG,
//					"Login Error: Server returned:" + ie.toString() + "::"
//							+ Log.getStackTraceString(ie));
//			throw ie;
//		}
//		catch (UnknownHostException uhe)
//		{
//			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + uhe.toString(), uhe);
//			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity, serverResponse);
//		}
//		catch (SocketTimeoutException e)
//		{
//			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + e.toString(), e);
//			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity, serverResponse);
//		}
//		catch (ConnectException e)
//		{
//			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + e.toString(), e);
//			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity, serverResponse);
//		}
//		catch (ConnectTimeoutException hce)
//		{
//			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + hce.toString(), hce);
//			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity, serverResponse);
//		}
//		catch (IOException e)
//		{
//			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + e.toString(), e);
//			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity, serverResponse);
//		}
//		catch (Exception ex)
//		{
//			if (enableLogs) Log.e(TAG, ex.toString() + "::" + Log.getStackTraceString(ex));
//			throw ex;
//		}
//	}
//
//	private static String getB64AuthCamba(String login, String pass)
//			throws IllegalArgumentException, Exception
//	{
//
//		String source = login + ":" + pass;
//		String ret;
//		ret = "Basic "
//				+ (AppData.cambaApiKey == null || AppData.cambaApiKey.equals("")
//						|| login != AppData.AppUserEmail || pass != AppData.AppUserPassword ? getCambaKey(
//						login, pass) : AppData.cambaApiKey);
//		// ret="Basic "+
//		// Base64.encodeToString(source.getBytes(),Base64.URL_SAFE|Base64.NO_WRAP);
//		return ret;
//	}
//
//	// list of cameras extracted from the api xml
//	private static ArrayList<Camera> getEventsFromAnXML(String XML) throws XmlPullParserException,
//			IOException
//	{
//
//		ArrayList<Camera> camList = new ArrayList<Camera>();
//
//		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//		factory.setNamespaceAware(true);
//		XmlPullParser xpp = factory.newPullParser();
//		xpp.setInput(new StringReader(XML));
//
//		Camera obj = new Camera();
//
//		xpp.next();
//		int eventType = xpp.getEventType();
//		// int count = 0;
//		String tagname = "";
//		while (eventType != XmlPullParser.END_DOCUMENT)
//		{ // count++;
//
//			if (eventType == XmlPullParser.START_TAG)
//			{
//				if (xpp.getName().equalsIgnoreCase("CameraInfoModel"))
//				{
//					obj = new Camera();
//				}
//				if (eventType != XmlPullParser.TEXT)
//				{
//					tagname = (xpp.getName() + "").toUpperCase(Locale.ENGLISH);
//				}
//
//			}
//			else if (eventType == XmlPullParser.TEXT)// && xpp.getName() != null
//														// && xpp.getText() !=
//														// null)
//			{
//				String tagtext = xpp.getText() + "";
//				if (enableLogs) Log.i(TAG, "\"" + tagname + "\":\"" + tagtext + "\"");
//				obj.SetCameraProperty(obj, tagname, tagtext);
//			}
//			else if (eventType == XmlPullParser.END_TAG)
//			{
//				if (xpp.getName().equalsIgnoreCase("CameraInfoModel"))
//				{
//					camList.add(obj);
//				}
//			}
//			eventType = xpp.next();
//		}
//
//		return camList;
//	}
//
//	public static Camera ParseJsonObject(String json)
//	{
//		Camera obj = new Camera();
//
//		if (enableLogs) Log.i(TAG, " json [" + json + "]");
//
//		json = json.substring(json.indexOf("{") + 1, json.indexOf("}"));
//
//		if (enableLogs) Log.i(TAG, " json inside brackets [" + json + "]");
//
//		json = json.replace("\\/", "/");
//
//		if (enableLogs) Log.i(TAG, " json after correcting slash [" + json + "]");
//
//		String[] tags = json.split(",");
//
//		if (enableLogs) Log.i(TAG, " tags length [" + tags.length + "]");
//
//		for (int i = 0; i < tags.length; i++)
//		{
//			String tag = tags[i];
//			if (enableLogs) Log.i(TAG, " tags [i] [" + tags[i] + "]");
//			if (tag.trim().length() > 0)
//			{
//				int start = tag.indexOf("\"") + 1;
//				int end = tag.indexOf("\"", start);
//				String tagname = tag.substring(start, end);
//
//				start = tag.indexOf("\"", end + 1) + 1;
//				end = tag.indexOf("\"", start);
//				String tagtext = tag.substring(start, end);
//
//				obj.SetCameraProperty(obj, tagname, tagtext);
//
//			}
//		}
//
//		return obj;
//
//	}
//
//	public static String getUsernamesForGCMDevice(String deviceID) throws Exception
//	{
//		try
//		{
//			HttpGet get = new HttpGet(CambaApiGcmDeviceUrl
//					+ (deviceID != null && deviceID.length() > 0 ? "/" + deviceID : ""));
//			HttpClient client = new DefaultHttpClient();
//			get.setHeader("Accept", "application/xml");
//			get.setHeader("User-Agent", "Apache-HttpClient/4.1 (java 1.5)");
//			get.setHeader("Authorization",
//					getB64AuthCamba(AppData.AppUserEmail, AppData.AppUserPassword));
//			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
//			AbstractHttpEntity ent = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
//			ent.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
//			ent.setContentEncoding("UTF-8");
//			HttpResponse response = client.execute(get);
//			String str = HttpUtils.getResponseBody(response);
//			if (enableLogs) Log.i(TAG, "::Response::[" + str + "]::");
//			return str;
//		}
//		catch (Exception ex)
//		{
//			Log.e(TAG, ex.toString() + "::" + Log.getStackTraceString(ex));
//			throw ex;
//		}
//	}
//
//	// get th cameras xmls exactly from the camba api
//	public static boolean registerDeviceForUsername(String UserEmail, String Password,
//			String DeviceRegId, String Operation, String BluetoothName, String Manufacturer,
//			String Model, String SerialNo, String ImeiNo, String Fingureprint, String MacAddress,
//			String AppVersion) throws Exception
//	{
//		try
//		{
//
//			if (enableLogs) Log.i("RegisterTask", "starting method for registration");
//
//			String URL = CambaApiGcmDeviceUrl;
//			HttpPost post = new HttpPost(URL); // "/AddOrRemoveGcmDevice"
//			HttpClient client = new DefaultHttpClient();
//			post.setHeader("Accept", "application/xml");
//			post.setHeader("User-Agent", "Apache-HttpClient/4.1 (java 1.5)");
//			post.setHeader("Authorization", getB64AuthCamba(UserEmail, Password));
//			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
//
//			nvps.add(new BasicNameValuePair("UserEmail", UserEmail));
//			nvps.add(new BasicNameValuePair("DeviceRegId", DeviceRegId));
//			nvps.add(new BasicNameValuePair("Operation", Operation));
//			nvps.add(new BasicNameValuePair("BluetoothName", BluetoothName));
//			nvps.add(new BasicNameValuePair("Manufacturer", Manufacturer));
//			nvps.add(new BasicNameValuePair("Model", Model));
//			nvps.add(new BasicNameValuePair("SerialNo", SerialNo));
//			nvps.add(new BasicNameValuePair("ImeiNo", ImeiNo));
//			nvps.add(new BasicNameValuePair("Fingureprint", Fingureprint));
//			nvps.add(new BasicNameValuePair("MacAddress", MacAddress));
//			nvps.add(new BasicNameValuePair("AppVersion", AppVersion));
//
//			AbstractHttpEntity ent = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
//			ent.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
//			ent.setContentEncoding("UTF-8");
//			post.setEntity(ent);
//			HttpResponse response = client.execute(post);
//			String str = HttpUtils.getResponseBody(response);
//			if (enableLogs) Log.i(TAG, "::Response::[" + str + "]");
//			Log.e("RegisterTask", "UserEmail[" + UserEmail + "], Password[" + Password
//					+ "], DeviceRegId[" + DeviceRegId + "], Operation[" + Operation
//					+ "], Bluetooth[" + BluetoothName + "], Manufacturer[" + Manufacturer
//					+ "], Model[" + Model + "], Serial[" + SerialNo + "], Imei[" + ImeiNo
//					+ "], Fingureprint[" + Fingureprint + "], MacAddress[" + MacAddress
//					+ "], AppVersion[" + AppVersion + "],  already[" + str.contains("already")
//					+ "], Succeeded[" + str.contains("Succeeded") + "]");
//
//			if (str.contains("Succeeded") || str.contains("already") || str.contains("Success")) return true;
//			return false;
//		}
//		catch (Exception ex)
//		{
//			if (enableLogs) Log.e("RegisterTask",
//					ex.toString() + "::" + Log.getStackTraceString(ex));
//			throw ex;
//		}
//	}
//
// }
