package io.evercam.androidapp.cambase;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class CambaseModel
{
	private final String TAG = "evercamplay-CambaseModel";
	private final String URL = "http://www.cambase.io:80/api/v1/cameras";
	private JSONObject jsonObject;

	public CambaseModel(String modelId)
	{
		try
		{
			HttpResponse<JsonNode> response = Unirest.get(URL + '/' + modelId).asJson();
			if (response.getCode() == 200)
			{
				JSONObject modelJsonObject = response.getBody().getObject()
						.getJSONObject("cameras");
				this.jsonObject = modelJsonObject;
			}
			else
			{
				Log.e(TAG, response.getCode() + " " + response.getBody().toString());
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

	public ArrayList<String> getThumnailUrls()
	{
		ArrayList<String> urlArray = new ArrayList<String>();
		if (jsonObject != null)
		{
			try
			{
				JSONArray urlJsonArray = jsonObject.getJSONArray("images");
				if (urlJsonArray.length() > 0)
				{
					for (int index = 0; index < urlJsonArray.length(); index++)
					{
						urlArray.add(urlJsonArray.getJSONObject(index).getString("url"));
					}
				}
				else
				{
					Log.d(TAG, "model url array is empty");
				}
			}
			catch (JSONException e)
			{
				Log.e(TAG, e.toString());
			}
		}
		return urlArray;
	}
}
