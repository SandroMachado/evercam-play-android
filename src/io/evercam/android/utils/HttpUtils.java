package io.evercam.android.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.protocol.HTTP;

import android.net.ParseException;
import android.util.Log;

public class HttpUtils
{

	static String TAG = "HttpUtils";
	static boolean enableLogs = false;

	public static String getResponseBody(HttpResponse response)
	{

		String response_text = null;

		HttpEntity entity = null;

		try
		{

			entity = response.getEntity();

			response_text = _getResponseBody(entity);

		}
		catch (ParseException e)
		{

			if (enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));

		}
		catch (IOException e)
		{
			if (enableLogs) Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
			if (entity != null)
			{

				try
				{

					entity.consumeContent();

				}
				catch (IOException e1)
				{

				}

			}

		}

		return response_text;

	}

	public static String _getResponseBody(final HttpEntity entity) throws IOException,
			ParseException
	{

		if (entity == null)
		{
			throw new IllegalArgumentException("HTTP entity may not be null");
		}

		InputStream instream = entity.getContent();

		if (instream == null)
		{
			return "";
		}

		if (entity.getContentLength() > Integer.MAX_VALUE)
		{
			throw new IllegalArgumentException(

			"HTTP entity too large to be buffered in memory");
		}

		String charset = getContentCharSet(entity);

		if (charset == null)
		{

			charset = HTTP.DEFAULT_CONTENT_CHARSET;

		}

		Reader reader = new InputStreamReader(instream, charset);

		BufferedReader r = new BufferedReader(reader);
		StringBuilder buffer = new StringBuilder();

		try
		{

			char[] tmp = new char[1024];

			int l;

			while ((l = reader.read(tmp)) != -1)
			{

				buffer.append(tmp, 0, l);

			}

		} finally
		{

			reader.close();

		}

		return buffer.toString();

	}

	public static String getContentCharSet(final HttpEntity entity) throws ParseException
	{

		if (entity == null)
		{
			throw new IllegalArgumentException("HTTP entity may not be null");
		}

		String charset = null;

		if (entity.getContentType() != null)
		{

			HeaderElement values[] = entity.getContentType().getElements();

			if (values.length > 0)
			{

				NameValuePair param = values[0].getParameterByName("charset");

				if (param != null)
				{

					charset = param.getValue();
				}
			}
		}

		return charset;
	}

}
