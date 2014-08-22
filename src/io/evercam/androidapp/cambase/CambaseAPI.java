package io.evercam.androidapp.cambase;

public class CambaseAPI
{
	public static String getSmallImageUrl(String url)
	{
		String original = "/original/";
		String small = "/small/";
		return url.replace(original, small);
	}
}
