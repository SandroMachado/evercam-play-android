package io.evercam.android.utils;

import io.evercam.android.dto.Camera;
import io.evercam.android.exceptions.*;
import io.evercam.android.utils.Commons;
import io.evercam.android.utils.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
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
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

public class CambaApiManager
{

	static String TAG = "CambaApiManager";
	static boolean enableLogs = false;

	static String debugValues = "";

	// static String CambaApiUrl = "http://www.camba.tv/api/";//
	// Bionic:"http://maxis.bionic-csi.com/api/";

	// static String CambaApiUrl = "http://api.camba.tv/";
	// static String CambaApiCameraUrl = CambaApiUrl + "Camera"; //
	// "http://www.camba.tv/api/camera"
	// static String CambaApiLatestImageUrl = CambaApiUrl + "image/latest/" ;
	// //http://camba.tv/api/image/latest/
	// static String CambaApiTokenUrl = CambaApiUrl + "token"; //
	// "http://www.camba.tv/api/token"
	// static String CambaApiGcmDeviceUrl = CambaApiUrl + "gcmdevice";
	// //http://camba.tv/api/gcmdevice

	static String CambaApiUrl = "http://webapi.camba.tv/v1/";
	static String CambaApiCameraUrl = CambaApiUrl + "cameras"; // "http://www.camba.tv/api/camera"
	static String CambaApiLatestImageUrl = CambaApiUrl + "image/latest/"; // http://camba.tv/api/image/latest/
	static String CambaApiTokenUrl = CambaApiUrl + "auth/token"; // "http://www.camba.tv/api/token"
	static String CambaApiGcmDeviceUrl = CambaApiUrl + "gcmdevice"; // http://camba.tv/api/gcmdevice

	public static Boolean isLoggedIn = false;
	// key to be passed to the camba api

	public static String debug;

	// query the camba api and get the data of cameras
	public static ArrayList<Camera> getCameraListAndSetKey() throws Exception
	{
		try
		{
			if (enableLogs) Log.i(TAG, "going to login for User[" + AppData.AppUserEmail
					+ "] and Password[" + AppData.AppUserPassword + "].");
			String camXml = null;
			if (Constants.isOfflineDebugging)
			{
				camXml = "<ArrayOfApiCamera xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.datacontract.org/2004/07/StopMotion.Business.Entities\"><ApiCamera><AccessMethod>AirCam</AccessMethod><AudioUrl></AudioUrl><BaseUrl>http://vassily.dvrdns.org:1500/</BaseUrl><BrowserUrl>http://www.camba.tv/cameraview.aspx?c=suwqsmox</BrowserUrl><CameraImageUrl>http://vassily.dvrdns.org:1500/snapshot.cgi</CameraImageUrl><CameraMake>Ubiquiti</CameraMake><CameraModel>Aircam</CameraModel><CameraPassword>syswebman01</CameraPassword><CameraTimeZone>W. Central Africa Standard Time</CameraTimeZone><CameraUserName>admin</CameraUserName><Code>suwqsmox</Code><H264Url>rtsp://vassily.dvrdns.org:1500/live/ch01_0</H264Url><HistoryDays>1</HistoryDays><Id>1285</Id><LocalIpPort i:nil=\"true\" /><LowResolutionSnapshotUrl i:nil=\"true\" /><MjpgUrl></MjpgUrl><MobileUrl>rtsp://vassily.dvrdns.org:554/live/ch01_0</MobileUrl><Mpeg4Url></Mpeg4Url><Name>A new mini</Name><OfflineTime i:nil=\"true\" /><RtspPort>554</RtspPort><RtspUrl i:nil=\"true\" /><Status>Active</Status><UseCredentials>true</UseCredentials><VideoRecording>false</VideoRecording></ApiCamera><ApiCamera><AccessMethod>Manual</AccessMethod><AudioUrl></AudioUrl><BaseUrl>http://93.107.43.164:8087/</BaseUrl><BrowserUrl>http://www.camba.tv/cameraview.aspx?c=lweehyqd</BrowserUrl><CameraImageUrl>http://93.107.43.164:8087/snapshot.jpg</CameraImageUrl><CameraMake>Y-Cam</CameraMake><CameraModel>White S</CameraModel><CameraPassword>mehcam4mehcam</CameraPassword><CameraTimeZone>GMT Standard Time</CameraTimeZone><CameraUserName>admin</CameraUserName><Code>lweehyqd</Code><H264Url></H264Url><HistoryDays>7</HistoryDays><Id>59</Id><LocalIpPort>192.168.3.112:8087</LocalIpPort><LowResolutionSnapshotUrl>http://93.107.43.164:8087/snapshot1.jpg</LowResolutionSnapshotUrl><MjpgUrl>http://93.107.43.164:8087/stream.jpg</MjpgUrl><MobileUrl>rtsp://93.107.43.164:8087/live_3gpp.sdp</MobileUrl><Mpeg4Url>rtsp://93.107.43.164:8087/live_mpeg4.sdp</Mpeg4Url><Name>Aughrim Gate</Name><OfflineTime i:nil=\"true\" /><RtspPort>8087</RtspPort><RtspUrl i:nil=\"true\" /><Status>Paused</Status><UseCredentials>true</UseCredentials><VideoRecording>false</VideoRecording></ApiCamera><ApiCamera><AccessMethod>Manual</AccessMethod><AudioUrl></AudioUrl><BaseUrl>http://93.107.43.164:8082/</BaseUrl><BrowserUrl>http://www.camba.tv/cameraview.aspx?c=esewekxn</BrowserUrl><CameraImageUrl>http://93.107.43.164:8082/snapshot.jpg</CameraImageUrl><CameraMake>Y-Cam</CameraMake><CameraModel>Black SD</CameraModel><CameraPassword>mehcam4mehcam</CameraPassword><CameraTimeZone>GMT Standard Time</CameraTimeZone><CameraUserName>admin</CameraUserName><Code>esewekxn</Code><H264Url></H264Url><HistoryDays>90</HistoryDays><Id>105</Id><LocalIpPort>192.168.3.115:8082</LocalIpPort><LowResolutionSnapshotUrl>http://93.107.43.164:8082/snapshot1.jpg</LowResolutionSnapshotUrl><MjpgUrl>http://93.107.43.164:8082/stream.jpg</MjpgUrl><MobileUrl>rtsp://93.107.43.164:8082/live_3gpp.sdp</MobileUrl><Mpeg4Url>rtsp://93.107.43.164:8082/live_mpeg4.sdp</Mpeg4Url><Name>Aughrim Office</Name><OfflineTime i:nil=\"true\" /><RtspPort>8082</RtspPort><RtspUrl i:nil=\"true\" /><Status>Active</Status><UseCredentials>true</UseCredentials><VideoRecording>false</VideoRecording></ApiCamera></ArrayOfApiCamera>";
				camXml = "<ArrayOfApiCamera xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://schemas.datacontract.org/2004/07/StopMotion.Business.Entities\"><ApiCamera><AccessMethod>Manual</AccessMethod><AlarmLevel>2</AlarmLevel><AudioUrl></AudioUrl><BaseUrl>http://149.5.42.145:11061/</BaseUrl><BrowserUrl>http://camba.tv/cameraview.aspx?c=rbdgmqlw</BrowserUrl><CameraGroup>DCA</CameraGroup><CameraImageUrl>http://149.5.42.145:11061/snapshot.jpg</CameraImageUrl><CameraMake>Y-Cam</CameraMake><CameraModel>Bullet Black</CameraModel><CameraPassword>mehcam4mehcam</CameraPassword><CameraStatusIntVal>2</CameraStatusIntVal><CameraTimeZone>GMT Standard Time</CameraTimeZone><CameraUserName>admin</CameraUserName><Code>rbdgmqlw</Code><H264Url></H264Url><HistoryDays>1</HistoryDays><Id>52</Id><IsMdEnabled>true</IsMdEnabled><LocalIpPort>192.168.0.61:80</LocalIpPort><LowResolutionSnapshotUrl>http://149.5.42.145:11061/snapshot1.jpg</LowResolutionSnapshotUrl><MjpgUrl>http://149.5.42.145:11061/stream.jpg</MjpgUrl><MobileUrl>rtsp://149.5.42.145:11061/live_3gpp.sdp</MobileUrl><Mpeg4Url>rtsp://149.5.42.145:11061/live_mpeg4.sdp</Mpeg4Url><Name>Blainroe Garage</Name><OfflineTime i:nil=\"true\" /><RtspPort>554</RtspPort><RtspUrl i:nil=\"true\" /><Status>Paused</Status><UseCredentials>true</UseCredentials><VideoRecording>false</VideoRecording></ApiCamera><ApiCamera><AccessMethod>Manual</AccessMethod><AlarmLevel>7</AlarmLevel><AudioUrl></AudioUrl><BaseUrl>http://149.5.42.145:8494/</BaseUrl><BrowserUrl>http://camba.tv/cameraview.aspx?c=waphudru</BrowserUrl><CameraGroup>DCA</CameraGroup><CameraImageUrl>http://149.5.42.145:8494/snapshot?reso=1024</CameraImageUrl><CameraMake>Bewan</CameraMake><CameraModel>iCam</CameraModel><CameraPassword>mehcam4mehcam</CameraPassword><CameraStatusIntVal>2</CameraStatusIntVal><CameraTimeZone>GMT Standard Time</CameraTimeZone><CameraUserName>admin</CameraUserName><Code>waphudru</Code><H264Url></H264Url><HistoryDays>1</HistoryDays><Id>32</Id><IsMdEnabled>false</IsMdEnabled><LocalIpPort>192.168.0.55:80</LocalIpPort><LowResolutionSnapshotUrl i:nil=\"true\" /><MjpgUrl>http://149.5.42.145:8494/video?profile=1</MjpgUrl><MobileUrl>http://149.5.42.145:8494/snapshot?reso=1024</MobileUrl><Mpeg4Url></Mpeg4Url><Name>Blainroe Garden</Name><OfflineTime i:nil=\"true\" /><RtspPort>554</RtspPort><RtspUrl i:nil=\"true\" /><Status>Paused</Status><UseCredentials>true</UseCredentials><VideoRecording>false</VideoRecording></ApiCamera><ApiCamera><AccessMethod>Manual</AccessMethod><AlarmLevel>7</AlarmLevel><AudioUrl></AudioUrl><BaseUrl>http://149.5.42.145:97/</BaseUrl><BrowserUrl>http://camba.tv/cameraview.aspx?c=nodorwto</BrowserUrl><CameraGroup>DCA</CameraGroup><CameraImageUrl>http://149.5.42.145:97/image.jpg?cidx=20041304921174479</CameraImageUrl><CameraMake>D-LINK</CameraMake><CameraModel>DCS-900</CameraModel><CameraPassword>Camba123</CameraPassword><CameraStatusIntVal>2</CameraStatusIntVal><CameraTimeZone>GMT Standard Time</CameraTimeZone><CameraUserName>camba</CameraUserName><Code>nodorwto</Code><H264Url>rtsp://149.5.42.145:97/play1.sdp</H264Url><HistoryDays>30</HistoryDays><Id>92</Id><IsMdEnabled>false</IsMdEnabled><LocalIpPort>192.168.0.58:80</LocalIpPort><LowResolutionSnapshotUrl i:nil=\"true\" /><MjpgUrl>http://149.5.42.145:97/video.cgi</MjpgUrl><MobileUrl>rtsp://149.5.42.145:97/play1.sdp</MobileUrl><Mpeg4Url></Mpeg4Url><Name>Cattle Shed2</Name><OfflineTime i:nil=\"true\" /><RtspPort>554</RtspPort><RtspUrl i:nil=\"true\" /><Status>Paused</Status><UseCredentials>true</UseCredentials><VideoRecording>false</VideoRecording></ApiCamera><ApiCamera><AccessMethod>Manual</AccessMethod><AlarmLevel>4</AlarmLevel><AudioUrl></AudioUrl><BaseUrl>http://149.5.36.19:8003/</BaseUrl><BrowserUrl>http://camba.tv/cameraview.aspx?c=hgrqpxnl</BrowserUrl><CameraGroup>DCC</CameraGroup><CameraImageUrl>http://149.5.36.19:8003/snapshot.jpg</CameraImageUrl><CameraMake>Y-Cam</CameraMake><CameraModel>Black S</CameraModel><CameraPassword>mehcam</CameraPassword><CameraStatusIntVal>1</CameraStatusIntVal><CameraTimeZone>GMT Standard Time</CameraTimeZone><CameraUserName>admin</CameraUserName><Code>hgrqpxnl</Code><H264Url></H264Url><HistoryDays>30</HistoryDays><Id>347</Id><IsMdEnabled>true</IsMdEnabled><LocalIpPort>192.168.1.223:80</LocalIpPort><LowResolutionSnapshotUrl>http://149.5.36.19:8003/snapshot1.jpg</LowResolutionSnapshotUrl><MjpgUrl>http://149.5.36.19:8003/stream.jpg</MjpgUrl><MobileUrl>rtsp://149.5.36.19:8003/live_3gpp.sdp</MobileUrl><Mpeg4Url>rtsp://149.5.36.19:8003/live_mpeg4.sdp</Mpeg4Url><Name>First Fl. Front </Name><OfflineTime i:nil=\"true\" /><RtspPort>9003</RtspPort><RtspUrl i:nil=\"true\" /><Status>Active</Status><UseCredentials>true</UseCredentials><VideoRecording>false</VideoRecording></ApiCamera><ApiCamera><AccessMethod>Manual</AccessMethod><AlarmLevel>7</AlarmLevel><AudioUrl></AudioUrl><BaseUrl>http://149.5.41.153:8081/</BaseUrl><BrowserUrl>http://camba.tv/cameraview.aspx?c=awlilqod</BrowserUrl><CameraGroup>DCA</CameraGroup><CameraImageUrl>http://149.5.41.153:8081/snapshot.jpg</CameraImageUrl><CameraMake>Y-Cam</CameraMake><CameraModel>White S</CameraModel><CameraPassword>Camba123</CameraPassword><CameraStatusIntVal>1</CameraStatusIntVal><CameraTimeZone>GMT Standard Time</CameraTimeZone><CameraUserName>camba</CameraUserName><Code>awlilqod</Code><H264Url></H264Url><HistoryDays>30</HistoryDays><Id>254</Id><IsMdEnabled>true</IsMdEnabled><LocalIpPort>192.168.1.206:8081</LocalIpPort><LowResolutionSnapshotUrl>http://149.5.41.153:8081/snapshot1.jpg</LowResolutionSnapshotUrl><MjpgUrl>http://149.5.41.153:8081/stream.jpg</MjpgUrl><MobileUrl>rtsp://149.5.41.153:8081/live_3gpp.sdp</MobileUrl><Mpeg4Url>rtsp://149.5.41.153:8081/live_mpeg4.sdp</Mpeg4Url><Name>Gaol Shop2</Name><OfflineTime i:nil=\"true\" /><RtspPort>8081</RtspPort><RtspUrl i:nil=\"true\" /><Status>Active</Status><UseCredentials>true</UseCredentials><VideoRecording>false</VideoRecording></ApiCamera><ApiCamera><AccessMethod>Manual</AccessMethod><AlarmLevel>7</AlarmLevel><AudioUrl></AudioUrl><BaseUrl>http://149.5.42.145:11066/</BaseUrl><BrowserUrl>http://camba.tv/cameraview.aspx?c=bzaohosg</BrowserUrl><CameraGroup>DCA</CameraGroup><CameraImageUrl>http://149.5.42.145:11066/snapshot.jpg</CameraImageUrl><CameraMake>Y-Cam</CameraMake><CameraModel>White S</CameraModel><CameraPassword>mehcam4mehcam</CameraPassword><CameraStatusIntVal>2</CameraStatusIntVal><CameraTimeZone>GMT Standard Time</CameraTimeZone><CameraUserName>admin</CameraUserName><Code>bzaohosg</Code><H264Url></H264Url><HistoryDays>30</HistoryDays><Id>277</Id><IsMdEnabled>false</IsMdEnabled><LocalIpPort>192.168.0.66:80</LocalIpPort><LowResolutionSnapshotUrl>http://149.5.42.145:11066/snapshot1.jpg</LowResolutionSnapshotUrl><MjpgUrl>http://149.5.42.145:11066/stream.jpg</MjpgUrl><MobileUrl>rtsp://149.5.42.145:11066/live_3gpp.sdp</MobileUrl><Mpeg4Url>rtsp://149.5.42.145:11066/live_mpeg4.sdp</Mpeg4Url><Name>Grain Cam</Name><OfflineTime i:nil=\"true\" /><RtspPort>554</RtspPort><RtspUrl i:nil=\"true\" /><Status>Paused</Status><UseCredentials>true</UseCredentials><VideoRecording>false</VideoRecording></ApiCamera><ApiCamera><AccessMethod>Manual</AccessMethod><AlarmLevel>4</AlarmLevel><AudioUrl></AudioUrl><BaseUrl>http://149.5.36.19:8001/</BaseUrl><BrowserUrl>http://camba.tv/cameraview.aspx?c=gyzsqbde</BrowserUrl><CameraGroup>DCC</CameraGroup><CameraImageUrl>http://149.5.36.19:8001/snapshot.jpg</CameraImageUrl><CameraMake>Y-Cam</CameraMake><CameraModel>Black S</CameraModel><CameraPassword>mehcam</CameraPassword><CameraStatusIntVal>1</CameraStatusIntVal><CameraTimeZone>GMT Standard Time</CameraTimeZone><CameraUserName>admin</CameraUserName><Code>gyzsqbde</Code><H264Url></H264Url><HistoryDays>30</HistoryDays><Id>346</Id><IsMdEnabled>true</IsMdEnabled><LocalIpPort>192.168.1.221:80</LocalIpPort><LowResolutionSnapshotUrl>http://149.5.36.19:8001/snapshot1.jpg</LowResolutionSnapshotUrl><MjpgUrl>http://149.5.36.19:8001/stream.jpg</MjpgUrl><MobileUrl>rtsp://149.5.36.19:8001/live_3gpp.sdp</MobileUrl><Mpeg4Url>rtsp://149.5.36.19:8001/live_mpeg4.sdp</Mpeg4Url><Name>Ground Fl. Front</Name><OfflineTime i:nil=\"true\" /><RtspPort>9002</RtspPort><RtspUrl i:nil=\"true\" /><Status>Active</Status><UseCredentials>true</UseCredentials><VideoRecording>false</VideoRecording></ApiCamera><ApiCamera><AccessMethod>Manual</AccessMethod><AlarmLevel>7</AlarmLevel><AudioUrl></AudioUrl><BaseUrl>http://149.5.42.145:11214/</BaseUrl><BrowserUrl>http://camba.tv/cameraview.aspx?c=qasodset</BrowserUrl><CameraGroup>DCC</CameraGroup><CameraImageUrl>http://149.5.42.145:11214/axis-cgi/jpg/image.cgi?resolution=640x480</CameraImageUrl><CameraMake>Axis</CameraMake><CameraModel>Q Series</CameraModel><CameraPassword>mehcam</CameraPassword><CameraStatusIntVal>1</CameraStatusIntVal><CameraTimeZone>GMT Standard Time</CameraTimeZone><CameraUserName>camba</CameraUserName><Code>qasodset</Code><H264Url>rtsp://149.5.42.145:11214/axis-media/media.amp?resolution=640x360</H264Url><HistoryDays>1</HistoryDays><Id>758</Id><IsMdEnabled>false</IsMdEnabled><LocalIpPort>192.168.0.214:80</LocalIpPort><LowResolutionSnapshotUrl>http://149.5.42.145:11214/axis-cgi/jpg/image.cgi?resolution=cif</LowResolutionSnapshotUrl><MjpgUrl>http://149.5.42.145:11214/axis-cgi/mjpg/video.cgi</MjpgUrl><MobileUrl>rtsp://149.5.42.145:10214/axis-media/media.amp?resolution=QCIF</MobileUrl><Mpeg4Url i:nil=\"true\" /><Name>Herbst Mast PTZ MJ</Name><OfflineTime i:nil=\"true\" /><RtspPort>10214</RtspPort><RtspUrl i:nil=\"true\" /><Status>Active</Status><UseCredentials>true</UseCredentials><VideoRecording>false</VideoRecording></ApiCamera></ArrayOfApiCamera>";
				AppData.AppUserEmail = "marco@herbst.ie";
				AppData.AppUserPassword = "123marco123";

			}
			else
			{
				AppData.cambaApiKey = getCambaKey(AppData.AppUserEmail, AppData.AppUserPassword); // getting
																									// the
																									// camba
																									// key.
																									// If
																									// we
																									// encounter
																									// any
																									// exception,
																									// login
																									// preblem
																									// will
																									// be
																									// highlighted
																									// automatically
				camXml = getCamerasXML();
			}
			if (enableLogs) Log.i(TAG, camXml);
			// if(enableLogs) Log.i(TAG,
			// camXml.substring((camXml.contains("1396")? camXml.indexOf("1396")
			// : 0)));

			ArrayList<Camera> camList = getEventsFromAnXML(camXml);
			return camList;

		}
		catch (CredentialsException e)
		{
			if (enableLogs) Log.e(TAG, e.toString() + ":[:" + e.getServerHtml() + ":]:", e);
			throw e;
		}
		catch (ConnectivityException e)
		{
			if (enableLogs) Log.e(TAG, e.toString() + ":[:" + e.getServerHtml() + ":]:", e);
			throw e;
		}

		catch (UnknownHostException uhe)
		{
			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + uhe.toString(), uhe);
			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity);
		}
		catch (SocketTimeoutException e)
		{
			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + e.toString(), e);
			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity);
		}
		catch (ConnectException e)
		{
			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + e.toString(), e);
			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity);
		}
		catch (ConnectTimeoutException hce)
		{
			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + hce.toString(), hce);
			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity);
		}
		catch (IOException e)
		{
			if (enableLogs) Log.e(TAG, "Login Error: Server returned:" + e.toString(), e);
			throw new ConnectivityException(Constants.ErrorMessageNoConnectivity);
		}

		catch (Exception e)
		{
			if (enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
			throw e;
		}
	}

	// get th cameras xmls exactly from the camba api
	public static String getCamerasXML() throws Exception
	{
		try
		{
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(CambaApiCameraUrl);
			get.setHeader("Accept", "application/xml");
			get.setHeader("User-Agent", "Apache-HttpClient/4.1 (java 1.5)");
			get.setHeader("Authorization",
					getB64AuthCamba(AppData.AppUserEmail, AppData.AppUserPassword));
			Commons.setTimeouts(get.getParams());
			HttpResponse response = client.execute(get);
			String str = HttpUtils.getResponseBody(response);
			return str;
		}
		catch (Exception ex)
		{
			if (enableLogs) Log.e(TAG, ex.toString() + "::" + Log.getStackTraceString(ex));
			throw ex;
		}
	}

	// get the bitmap image from the camba website for a given camera id
	public static Bitmap getCameraLatestBitmap(int cameraID) throws Exception
	{
		String URL = null;
		try
		{
			URL = CambaApiLatestImageUrl + cameraID;
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(URL);
			get.setHeader("Accept", "image/jpeg");
			get.setHeader("User-Agent", "Apache-HttpClient/4.1 (java 1.5)");
			get.setHeader("Authorization",
					getB64AuthCamba(AppData.AppUserEmail, AppData.AppUserPassword));
			Commons.setTimeouts(get.getParams());
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			InputStream input = entity.getContent();
			return BitmapFactory.decodeStream(input);
		}
		catch (Exception ex)
		{
			if (enableLogs) Log.e(TAG,
					ex.toString() + "::URL[" + URL + "]" + Log.getStackTraceString(ex));
			throw ex;
		}
	}

	// get the drawable image from the camba website for a given camera id
	public static Drawable getCameraLatestDrawable(int cameraID) throws Exception
	{
		String URL = null;
		try
		{
			URL = CambaApiLatestImageUrl + cameraID;
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(URL);
			get.setHeader("Accept", "image/jpeg");
			get.setHeader("User-Agent", "Apache-HttpClient/4.1 (java 1.5)");
			get.setHeader("Authorization",
					getB64AuthCamba(AppData.AppUserEmail, AppData.AppUserPassword));
			Commons.setTimeouts(get.getParams());
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			InputStream input = entity.getContent();
			Drawable img = Drawable.createFromStream(input,
					Environment.getExternalStorageDirectory() + "/media/" + cameraID + ".jpg");// cont.getCacheDir()
																								// );
			return img;
		}

		catch (Exception ex)
		{
			if (enableLogs) Log.e(TAG,
					ex.toString() + "::URL[" + URL + "]" + Log.getStackTraceString(ex));
			throw ex;
		}
	}

	// get the bitmap image from the camba website for a given camera id and
	// save it to the cache of the applciation
	public static boolean getCameraLatestImageAndSave(int cameraID, String pathString,
			int timeoutMillis) throws Exception
	{
		String URL = null;
		try
		{
			URL = CambaApiLatestImageUrl + cameraID;
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(URL);
			get.setHeader("Accept", "image/jpeg");
			get.setHeader("User-Agent", "Apache-HttpClient/4.1 (java 1.5)");
			get.setHeader("Authorization",
					getB64AuthCamba(AppData.AppUserEmail, AppData.AppUserPassword));
			Commons.setTimeouts(get.getParams(), timeoutMillis);
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			InputStream input = entity.getContent();

			File file = new File(pathString);
			if (file.exists())
			{
				file.delete();
			}
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);

			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = input.read(bytes)) != -1)
			{
				fos.write(bytes, 0, read);
			}

			input.close();
			fos.flush();
			fos.close();

			return true;
		}
		catch (Exception ex)
		{
			if (enableLogs) Log.e(TAG,
					ex.toString() + "::URL[" + URL + "]" + Log.getStackTraceString(ex));
			throw ex;
		}
	}

	// get the camba key from the camba api. this contains the username and
	// password token and expires in few hours
	public static String getCambaKey(String username, String password) throws Exception,
			IllegalArgumentException
	{
		String serverResponse = null;
		try
		{
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(CambaApiTokenUrl);
			post.setHeader("Accept", "application/xml");
			post.setHeader("User-Agent", "Apache-HttpClient/4.1 (java 1.5)");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("user_name", username));
			nvps.add(new BasicNameValuePair("user_password", password));
			AbstractHttpEntity ent = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);// (nvps,
																				// HTTP.UTF_8);
			ent.setContentType("application/x-www-form-urlencoded; charset=UTF-8");// x-www-form-urlencoded;
																					// charset=UTF-8");
			ent.setContentEncoding("UTF-8");
			post.setEntity(ent);
			Commons.setTimeouts(post.getParams());
			HttpResponse response = client.execute(post);
			serverResponse = HttpUtils.getResponseBody(response);
			if (enableLogs) Log.i(TAG, ":Response getCambaKey:[" + serverResponse + "]");

			if (!(serverResponse + "").contains("<key>")
					|| !(serverResponse + "").contains("</key>"))
			{
				if (enableLogs) Log.i(TAG, "Server Returned [" + serverResponse + "]");
				throw new CredentialsException(null, serverResponse + "");
			}

			int sIndex = serverResponse.indexOf("<key>");
			int eIndex = serverResponse.indexOf("</key>");

			String key = serverResponse.substring(sIndex + 5, eIndex);

			return key;
		}
		catch (CredentialsException ie)
		{
			if (enableLogs) Log.e(
					TAG,
					"Login Error: Server returned:" + ie.toString() + "::"
							+ Log.getStackTraceString(ie));
			throw ie;
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

	private static String getB64AuthCamba(String login, String pass)
			throws IllegalArgumentException, Exception
	{

		String source = login + ":" + pass;
		String ret;
		ret = "Basic "
				+ (AppData.cambaApiKey == null || AppData.cambaApiKey.equals("")
						|| login != AppData.AppUserEmail || pass != AppData.AppUserPassword ? getCambaKey(
						login, pass) : AppData.cambaApiKey);
		// ret="Basic "+
		// Base64.encodeToString(source.getBytes(),Base64.URL_SAFE|Base64.NO_WRAP);
		return ret;
	}

	// list of cameras extracted from the api xml
	private static ArrayList<Camera> getEventsFromAnXML(String XML) throws XmlPullParserException,
			IOException
	{

		ArrayList<Camera> camList = new ArrayList<Camera>();

		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		xpp.setInput(new StringReader(XML));

		Camera obj = new Camera();

		xpp.next();
		int eventType = xpp.getEventType();
		// int count = 0;
		String tagname = "";
		while (eventType != XmlPullParser.END_DOCUMENT)
		{ // count++;

			if (eventType == XmlPullParser.START_TAG)
			{
				if (xpp.getName().equalsIgnoreCase("CameraInfoModel"))
				{
					obj = new Camera();
				}
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
				if (enableLogs) Log.i(TAG, "\"" + tagname + "\":\"" + tagtext + "\"");
				obj.SetCameraProperty(obj, tagname, tagtext);
			}
			else if (eventType == XmlPullParser.END_TAG)
			{
				if (xpp.getName().equalsIgnoreCase("CameraInfoModel"))
				{
					camList.add(obj);
				}
			}
			eventType = xpp.next();
		}

		return camList;
	}

	public static Camera ParseJsonObject(String json)
	{
		Camera obj = new Camera();

		if (enableLogs) Log.i(TAG, " json [" + json + "]");

		json = json.substring(json.indexOf("{") + 1, json.indexOf("}"));

		if (enableLogs) Log.i(TAG, " json inside brackets [" + json + "]");

		json = json.replace("\\/", "/");

		if (enableLogs) Log.i(TAG, " json after correcting slash [" + json + "]");

		String[] tags = json.split(",");

		if (enableLogs) Log.i(TAG, " tags length [" + tags.length + "]");

		for (int i = 0; i < tags.length; i++)
		{
			String tag = tags[i];
			if (enableLogs) Log.i(TAG, " tags [i] [" + tags[i] + "]");
			if (tag.trim().length() > 0)
			{
				int start = tag.indexOf("\"") + 1;
				int end = tag.indexOf("\"", start);
				String tagname = tag.substring(start, end);

				start = tag.indexOf("\"", end + 1) + 1;
				end = tag.indexOf("\"", start);
				String tagtext = tag.substring(start, end);

				obj.SetCameraProperty(obj, tagname, tagtext);

			}
		}

		return obj;

	}

	public static String getUsernamesForGCMDevice(String deviceID) throws Exception
	{
		try
		{
			HttpGet get = new HttpGet(CambaApiGcmDeviceUrl
					+ (deviceID != null && deviceID.length() > 0 ? "/" + deviceID : ""));
			HttpClient client = new DefaultHttpClient();
			get.setHeader("Accept", "application/xml");
			get.setHeader("User-Agent", "Apache-HttpClient/4.1 (java 1.5)");
			get.setHeader("Authorization",
					getB64AuthCamba(AppData.AppUserEmail, AppData.AppUserPassword));
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			AbstractHttpEntity ent = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
			ent.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
			ent.setContentEncoding("UTF-8");
			HttpResponse response = client.execute(get);
			String str = HttpUtils.getResponseBody(response);
			if (enableLogs) Log.i(TAG, "::Response::[" + str + "]::");
			return str;
		}
		catch (Exception ex)
		{
			Log.e(TAG, ex.toString() + "::" + Log.getStackTraceString(ex));
			throw ex;
		}
	}

	// get th cameras xmls exactly from the camba api
	public static boolean registerDeviceForUsername(String UserEmail, String Password,
			String DeviceRegId, String Operation, String BluetoothName, String Manufacturer,
			String Model, String SerialNo, String ImeiNo, String Fingureprint, String MacAddress,
			String AppVersion) throws Exception
	{
		try
		{

			if (enableLogs) Log.i("RegisterTask", "starting method for registration");

			String URL = CambaApiGcmDeviceUrl;
			HttpPost post = new HttpPost(URL); // "/AddOrRemoveGcmDevice"
			HttpClient client = new DefaultHttpClient();
			post.setHeader("Accept", "application/xml");
			post.setHeader("User-Agent", "Apache-HttpClient/4.1 (java 1.5)");
			post.setHeader("Authorization", getB64AuthCamba(UserEmail, Password));
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();

			nvps.add(new BasicNameValuePair("UserEmail", UserEmail));
			nvps.add(new BasicNameValuePair("DeviceRegId", DeviceRegId));
			nvps.add(new BasicNameValuePair("Operation", Operation));
			nvps.add(new BasicNameValuePair("BluetoothName", BluetoothName));
			nvps.add(new BasicNameValuePair("Manufacturer", Manufacturer));
			nvps.add(new BasicNameValuePair("Model", Model));
			nvps.add(new BasicNameValuePair("SerialNo", SerialNo));
			nvps.add(new BasicNameValuePair("ImeiNo", ImeiNo));
			nvps.add(new BasicNameValuePair("Fingureprint", Fingureprint));
			nvps.add(new BasicNameValuePair("MacAddress", MacAddress));
			nvps.add(new BasicNameValuePair("AppVersion", AppVersion));

			AbstractHttpEntity ent = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
			ent.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
			ent.setContentEncoding("UTF-8");
			post.setEntity(ent);
			HttpResponse response = client.execute(post);
			String str = HttpUtils.getResponseBody(response);
			if (enableLogs) Log.i(TAG, "::Response::[" + str + "]");
			Log.e("RegisterTask", "UserEmail[" + UserEmail + "], Password[" + Password
					+ "], DeviceRegId[" + DeviceRegId + "], Operation[" + Operation
					+ "], Bluetooth[" + BluetoothName + "], Manufacturer[" + Manufacturer
					+ "], Model[" + Model + "], Serial[" + SerialNo + "], Imei[" + ImeiNo
					+ "], Fingureprint[" + Fingureprint + "], MacAddress[" + MacAddress
					+ "], AppVersion[" + AppVersion + "],  already[" + str.contains("already")
					+ "], Succeeded[" + str.contains("Succeeded") + "]");

			if (str.contains("Succeeded") || str.contains("already") || str.contains("Success")) return true;
			return false;
		}
		catch (Exception ex)
		{
			if (enableLogs) Log.e("RegisterTask",
					ex.toString() + "::" + Log.getStackTraceString(ex));
			throw ex;
		}
	}

}
