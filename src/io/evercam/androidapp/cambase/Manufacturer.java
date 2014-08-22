package io.evercam.androidapp.cambase;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class Manufacturer
{
	private final String TAG = "evercamplay-Manufacturer";
	private final String URL = "http://www.cambase.io:80/api/v1/manufacturers";
	private JSONObject jsonObject;

	public Manufacturer(String id)
	{
		try
		{
			HttpResponse<JsonNode> response = Unirest.get(URL + '/' + id).asJson();
			if (response.getCode() == 200)
			{
				JSONObject manufactureObject = response.getBody().getObject();
				this.jsonObject = manufactureObject.getJSONObject("data");
			}
		}
		
		catch (UnirestException e)
		{
			Log.e(TAG, e.toString());
		}
		catch (JSONException e)
		{
			Log.e(TAG, e.toString());
		}
	}

	public String getLogoUrl()
	{
		if (jsonObject != null)
		{
			try
			{
				return jsonObject.getString("logo");
			}
			catch (JSONException e)
			{
				Log.e(TAG, e.toString());
			}
		}
		return "";
	}
}
