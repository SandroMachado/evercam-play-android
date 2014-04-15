package io.evercam.android.utils;

import io.evercam.android.exceptions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.util.Base64;
import android.util.Log;

public class Commons
{
	static String TAG = "Commons";
	static boolean enableLogs = false;

	public static boolean isOnline(Context ctx)
	{
		try
		{
			if (Constants.isOfflineDebugging)// if the offline debugging is
												// enabled always tell that
												// system is
			{
				return true;
			}
			ConnectivityManager cm = (ConnectivityManager) ctx
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			return cm.getActiveNetworkInfo() != null
					&& cm.getActiveNetworkInfo().isConnectedOrConnecting();

		}
		catch (Exception ex)
		{
			if (enableLogs) Log.e(TAG, ex.toString());
		}
		return false;
	}

	public static Drawable getDrawablefromUrlAuthenticated_Deprecated(String URL, String login,
			String pass) throws Exception
	{
		Drawable rv = null;

		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(URL);
		get.setHeader("Accept", "image/jpeg");
		get.setHeader("User-Agent", "Apache-HttpClient/4.1 (java 1.5)");
		get.setHeader("Authorization", getB64Auth(login, pass));
		Commons.setTimeouts(get.getParams());
		HttpResponse response = client.execute(get);
		HttpEntity entity = response.getEntity();
		InputStream input = entity.getContent();
		if (enableLogs) Log.i(TAG,
				"getDrawablefromUrlAuthenticated. Can read is " + input.available());

		if (entity.getContentType().getValue().contains("html")
				&& !entity.getContentType().getValue().contains("image"))
		{
			if (enableLogs) Log.i(TAG, "Content Type:" + entity.getContentType());
			BufferedReader r = new BufferedReader(new InputStreamReader(input));

			if (enableLogs) Log.i(TAG, "Content Type:" + entity.getContentType());
			String res = null;
			while ((res += r.readLine()) != null)
				;
			if (enableLogs) Log.i(TAG, "Content Data:" + res);
		}
		else
		{
			rv = Drawable.createFromStream(input, "src");
		}

		return rv;

	}

	// get the drawable image from the camera with authentication (cookies or
	// http)
	public static Drawable getDrawablefromUrlAuthenticated1(String URL, String login, String pass,
			ArrayList<Cookie> cookies, int timeoutMillies) throws Exception
	{
		Drawable rv = null;

		// Making first image request with authorization header, availabel
		// cookies, and Get method
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(URL);
		if (cookies != null && cookies.size() > 0)
		{
			CookieStore store = new BasicCookieStore();
			for (Cookie c : cookies)
				store.addCookie(c);
			client.setCookieStore(store);
		}
		get.setHeader("Accept", "image/jpeg");
		get.setHeader("User-Agent", "Apache-HttpClient/4.1 (java 1.5)");
		get.setHeader("Authorization", getB64Auth(login, pass));
		Commons.setTimeouts(get.getParams(), timeoutMillies);
		HttpContext httpcontext = new BasicHttpContext();
		HttpResponse response = client.execute(get, httpcontext);
		HttpEntity entity = response.getEntity();
		InputStream input = entity.getContent();
		if (enableLogs) Log.i(TAG,
				"getDrawablefromUrlAuthenticated1. Can read is " + input.available());

		// Checking the response, if image was returned or the html. if html
		// reatunred, that will be to redirect to login page
		if (entity.getContentType().getValue().contains("image")
				&& !entity.getContentType().getValue().contains("html")) // if
																			// the
																			// image
																			// was
																			// returned
																			// i.e.
																			// authentication
																			// approved
		{
			rv = Drawable.createFromStream(input, "src");
			if (enableLogs) Log.i(TAG, "Returning drawabale : " + rv);
			// Log.i(TAG, "Requesting cookies for URL [" + URL +
			// "] image returned");
			return rv;
		}
		else
		// imagge was not retunred. Rather login html page was returned.
		{
			// Log.i(TAG, "Requesting cookies for URL [" + URL + "], login [" +
			// login + "], pass [" + pass + "]");
			// Getting redirected URL (Login URL)
			HttpUriRequest currentReq = (HttpUriRequest) httpcontext
					.getAttribute(ExecutionContext.HTTP_REQUEST);
			HttpHost currentHost = (HttpHost) httpcontext
					.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
			String currentUrl = (currentReq.getURI().isAbsolute()) ? currentReq.getURI().toString()
					: (currentHost.toURI() + currentReq.getURI());

			if (enableLogs) Log.i(TAG, "=Content Type:" + entity.getContentType() + ":New URL:"
					+ currentUrl + ":OLD URL:" + URL);

			// Adding new Cookies
			if (cookies == null) cookies = new ArrayList<Cookie>();
			for (Cookie c : client.getCookieStore().getCookies())
			{
				Log.i(TAG,
						" cookie.getName() [" + c.getName() + "] , cookie.getValue() ["
								+ c.getValue() + "] , cokie.toString() [" + c.toString()
								+ "] , URL [" + URL + "]");
				if (cookies.contains(c)) cookies.remove(c); // remove old
															// cookies with old
															// values
				cookies.add(c); // add new cookies
			}

			// Getting Response stream and read the html that was returned
			BufferedReader r = new BufferedReader(new InputStreamReader(input));
			String responseHtml = "";
			String line = r.readLine();
			while (line != null)
			{
				responseHtml += line;
				line = r.readLine();
			}
			input.close();
			if (enableLogs) Log.i(TAG, "=Content Data:" + responseHtml + ":user:" + login
					+ ":Password:" + pass + ":");

			// Getting Form String

			// Making the second post request with usernames and password to get
			// authenticated on login url.
			HttpPost post = GetPostMethod(currentUrl, responseHtml, login, pass);// new
																					// HttpGet(URL);
			if (cookies != null && cookies.size() > 0) // adding cookies to
														// store
			{
				CookieStore store = new BasicCookieStore();
				for (Cookie c : cookies)
				{
					store.addCookie(c);
					if (enableLogs) Log.i(TAG,
							"cookie:Name:" + c.getName() + ":Vaue:" + c.getValue() + ":tostring:"
									+ c.toString() + ":");
				}
				client.setCookieStore(store);
			}
			post.setHeader("Accept", "image/jpeg");
			post.setHeader("User-Agent", "Apache-HttpClient/4.1 (java 1.5)");
			post.setHeader("Authorization", getB64Auth(login, pass));
			Commons.setTimeouts(post.getParams(), timeoutMillies);
			httpcontext = new BasicHttpContext();
			response = client.execute(post, httpcontext);
			entity = response.getEntity();
			input = entity.getContent();

			// Checking response of second request that was with username and
			// password
			if (entity.getContentType().getValue().contains("html")
					&& !entity.getContentType().getValue().contains("image")) // html
																				// has
																				// been
																				// returned.
																				// We
																				// may
																				// need
																				// to
																				// make
																				// a
																				// request
																				// with
																				// get
																				// method
																				// on
																				// image
																				// url.
			{
				r = new BufferedReader(new InputStreamReader(input));
				responseHtml = "";
				line = r.readLine();
				while (line != null)
				{
					responseHtml += line;
					line = r.readLine();
				}
				input.close();

				if (enableLogs) Log.i(TAG, "Content Type:" + entity.getContentType());
				if (enableLogs) Log.i(TAG, "Content Data:" + responseHtml);

				// making a request with Get Method at image url
				get = new HttpGet(URL);
				if (cookies != null && cookies.size() > 0)
				{

					CookieStore store = new BasicCookieStore();
					for (Cookie c : cookies)
					{
						store.addCookie(c);
						Log.i(TAG, " cookie.getName() [" + c.getName() + "] , cookie.getValue() ["
								+ c.getValue() + "] , cokie.toString() [" + c.toString()
								+ "] , URL [" + URL + "]");
					}
					client.setCookieStore(store);
				}
				get.setHeader("Accept", "image/jpeg");
				get.setHeader("User-Agent", "Apache-HttpClient/4.1 (java 1.5)");
				get.setHeader("Authorization", getB64Auth(login, pass));
				Commons.setTimeouts(get.getParams(), timeoutMillies);
				httpcontext = new BasicHttpContext();
				response = client.execute(get, httpcontext);
				entity = response.getEntity();
				input = entity.getContent();
				if (enableLogs) Log.i(TAG,
						"3rd request for getting image with Get method having input stream size "
								+ input.available());

				if (entity.getContentType().getValue().contains("html")
						&& !entity.getContentType().getValue().contains("image")) // html
																					// has
																					// been
																					// returned.
																					// We
																					// may
																					// need
																					// to
																					// make
																					// a
																					// request
																					// with
																					// get
																					// method
																					// on
																					// image
																					// url.
				{
					r = new BufferedReader(new InputStreamReader(input));
					responseHtml = "";
					line = r.readLine();
					while (line != null)
					{
						responseHtml += line + "\r\n";
						line = r.readLine();
					}
					input.close();
					if (enableLogs) Log.i(TAG,
							"3rd request for getting image was unsuccessful. Server returned ["
									+ responseHtml + "]");
					throw new ConnectivityException("Server did not return the image.",
							responseHtml);
				}
				else
				{
					rv = Drawable.createFromStream(input, "src");
				}
			}
			else
			// image has been retunred. This will be reutned.
			{
				if (enableLogs) Log.i(TAG,
						"3rd request for getting image was successful Get method having input stream size "
								+ input.available());
				rv = Drawable.createFromStream(input, "src");
			}
		}

		return rv;

	}

	public static HttpPost GetPostMethod(String URL, String formString, String login,
			String password) throws Exception
	{
		HttpPost post = new HttpPost(URL);
		if (enableLogs) Log.i(TAG, "URL[" + URL + "]");

		int si = 0; // starting index
		int ei = 0; // ending index

		si = formString.indexOf("<form");
		if (enableLogs) Log.i(TAG, "si[" + si + "]");
		ei = formString.indexOf("/form>");
		if (si > 0 && ei <= 0) ei = formString.indexOf("/>");
		if (enableLogs) Log.i(TAG, "ei[" + ei + "]");

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		if (si <= 0 && ei <= 0) throw new Exception("Form not found in server response.");

		formString = formString.substring(si, ei + 6);

		if (enableLogs) Log.i(TAG, "si:ei[" + si + ":" + ei + "]");
		if (enableLogs) Log.i(TAG, "formString.length()[" + formString.length() + "]");

		si = formString.indexOf("<input"); // from 0
		if (enableLogs) Log.i(TAG, "si-<input[" + si + "]");
		ei = formString.indexOf("/>", si);
		if (enableLogs) Log.i(TAG, "ei-/>[" + ei + "]");
		if (si <= 0 && ei <= 0) throw new Exception("No Input field found in form.");

		while (si > 0 && ei > 0 && si < formString.length() && ei < formString.length())
		{

			String input = formString.substring(si, ei + 2);
			if (enableLogs) Log.i(TAG, "input[" + input + "]");

			int s = 0, e = 0;
			String id = "", value = "", type = "";

			s = input.indexOf(" id=");
			s = input.indexOf("\"", s) + 1; // index of first double quote after
											// id tag + 1
			e = input.indexOf("\"", s); // index of last quote of the id tag
			id = input.substring(s, e); // getting the id value
			if (enableLogs) Log.i(TAG, "id[" + id + "], s [" + s + "], e [" + e + "]");

			s = input.indexOf(" type=");
			s = input.indexOf("\"", s) + 1;
			e = input.indexOf("\"", s);
			if (s >= 0 && e >= 0 && s < input.length() && e < input.length() && s != e)
			{
				type = input.substring(s, e);
				if (enableLogs) Log.i(TAG, "type[" + type + "], s [" + s + "], e [" + e + "]");
			}

			s = input.indexOf(" value=");
			s = input.indexOf("\"", s) + 1;
			e = input.indexOf("\"", s);
			if (s >= 0 && e >= 0 && s < input.length() && e < input.length() && s != e)
			{
				value = input.substring(s, e);
				if (enableLogs) Log.i(TAG, "value[" + value + "], s [" + s + "], e [" + e + "]");
			}

			if (type.equals("text")
					&& (id.contains("user") || id.contains("name") || id.contains("login"))) value = login;

			if (type.equals("password")
					&& (id.contains("password") || id.contains("pwd") || id.contains("pass"))) value = password;

			pairs.add(new BasicNameValuePair(id, value));
			if (enableLogs) Log.i(TAG, "id:value[" + id + ":" + value + "]");

			si = formString.indexOf("<input", ei);
			if (enableLogs) Log.i(TAG, "si-<input[" + si + "]");
			ei = formString.indexOf("/>", si);
			if (enableLogs) Log.i(TAG, "ei-/>[" + ei + "]");

		}

		si = formString.indexOf("<form");
		ei = formString.indexOf(">", si);
		String formHeader = (si >= 0 && ei >= 0 ? formString.substring(si, ei) : "");
		if (enableLogs) Log.e(TAG, "--" + formHeader);
		if (formHeader.contains("multipart"))
		{
			MultipartEntity pentity = new MultipartEntity();
			for (NameValuePair pair : pairs)
			{
				pentity.addPart(pair.getName(), new StringBody(pair.getValue()));
			}
			post.setEntity(pentity);
			if (enableLogs) Log.e(TAG, "multipart-" + pentity.toString());
		}
		else
		{
			post.setEntity(new UrlEncodedFormEntity(pairs));
		}
		if (enableLogs) Log
				.i(TAG, "pairs[" + pairs.size() + ":" + pairs.toArray().toString() + "]");

		return post;
	}

	public static String getB64Auth(String login, String pass)
	{
		String source = login + ":" + pass;
		return "Basic "
				+ Base64.encodeToString(source.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
	}

	public static void setTimeouts(HttpParams params)
	{
		params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, Constants.httptimeout);
		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, Constants.httptimeout);
		params.setLongParameter(ConnManagerPNames.TIMEOUT, Constants.httptimeout);
	}

	public static void setTimeouts(HttpParams params, int millis)
	{
		params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, millis);
		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, millis);
		params.setLongParameter(ConnManagerPNames.TIMEOUT, millis);
	}

	public static Drawable DownlaodDrawableSync(URL url, int timeoutMillies) throws IOException
	{
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoInput(true);
		connection.setConnectTimeout(timeoutMillies);
		connection.setReadTimeout(timeoutMillies);
		connection.connect();
		InputStream input = connection.getInputStream();
		return Drawable.createFromStream(input, "src");
	}

//	private static String getB64AuthKey(String login, String pass) throws IllegalArgumentException,
//			Exception
//	{
//
//		String source = login + ":" + pass;
//		String ret;
//		ret = Base64.encodeToString(source.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
//		return ret;
//	}

//	public static void setDefaultUserForApp(Context cont, String email, String password,
//			String apiKey, boolean clearCamsList)
//	{
//		if (email != null && email.length() == 0) email = null;
//
//		if (password != null && password.length() == 0) email = null;
//
//		if (apiKey != null && apiKey.length() == 0) apiKey = null;
//
//		AppData.AppUserEmail = email;
//		AppData.AppUserPassword = password;
//		AppData.cambaApiKey = apiKey;
//		if (AppData.camesList != null && clearCamsList) AppData.camesList.clear();
//
//		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(cont);
//		SharedPreferences.Editor editor = sharedPrefs.edit();
//		editor.putString("AppUserEmail", email);
//		editor.putString("AppUserPassword", password);
//		editor.commit();
//	}

	public static String readRawTextFile(int id, Context ctx)
	{
		InputStream inputStream = ctx.getResources().openRawResource(id);
		InputStreamReader in = new InputStreamReader(inputStream);
		BufferedReader buf = new BufferedReader(in);
		String line;
		StringBuilder text = new StringBuilder();
		try
		{
			while ((line = buf.readLine()) != null)
				text.append(line);
		}
		catch (IOException e)
		{
			return null;
		}
		return text.toString();
	}

	public static void copyFile(File src, File dst) throws IOException
	{
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0)
		{
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	// public static Drawable GetImageFromCache(Context context, String
	// fileName)
	// {
	// Drawable d = null;
	// try{
	// String filePath = context.getCacheDir().getAbsolutePath() + "/" +
	// fileName;
	// if(new File(filePath).exists())
	// {
	// d = Drawable.createFromPath(filePath);
	// if(d == null)
	// new File(filePath).delete();
	// }
	//
	// if(d == null)
	// {
	// filePath = context.getExternalFilesDir(null) + "/" + fileName;
	// if(new File(filePath).exists())
	// {
	// d = Drawable.createFromPath(filePath);
	// if(d == null)
	// new File(filePath).delete();
	// }
	// }
	//
	// }
	// catch(OutOfMemoryError e)
	// {
	// }
	// catch(Exception e)
	// {
	// }
	//
	// return d;
	// }
	//
	// public static void SaveImageToCache(Context context,Drawable d, String
	// fileName)
	// {
	// if(d == null)
	// return;
	// try
	// {
	// String extCachePath = context.getExternalFilesDir(null) + "/" + fileName
	// + ".jpg";
	// File extfile= new File(extCachePath);
	// if (d!= null)
	// {
	// Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
	// if(extfile.exists())
	// extfile.delete();
	// extfile.createNewFile();
	// FileOutputStream fos = new FileOutputStream(extfile);
	// bitmap.compress(CompressFormat.PNG, 0, fos);
	// fos.close();
	// }
	// }
	// catch(Exception e)
	// {}
	//
	// String pathString = context.getCacheDir() + "/" + fileName + ".jpg";
	// File file= new File(pathString);
	//
	// if (d!= null)
	// {
	// Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
	//
	// if(file.exists())
	// {
	// file.delete();
	// }
	// file.createNewFile();
	// FileOutputStream fos = new FileOutputStream(file);
	//
	// bitmap.compress(CompressFormat.PNG, 0, fos);
	//
	// fos.close();
	// }
	//
	//
	// if(file.exists() && file.length() > 0)
	// {
	// return pathString;
	// }
	// else if(file.exists())
	// {
	// file.delete();
	// if(enableLogs) Log.e(TAG, "File Error" + "::" +
	// "Unable to get the full file for the camera [" + cam.getCameraID() + ":"
	// + cam.getName() +"]. File Deleted. File was empty.");
	// return null;
	// }
	// else
	// {
	// if(enableLogs) Log.e(TAG, "File Error" + "::" +
	// "Unable to get the full file for the camera [" + cam.getCameraID() + ":"
	// + cam.getName() +"]");
	// return null;
	// }
	//
	// }
	//

}
