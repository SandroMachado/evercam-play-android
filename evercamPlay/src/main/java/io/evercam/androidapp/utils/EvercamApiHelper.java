package io.evercam.androidapp.utils;

import android.content.Context;

import io.evercam.API;

public class EvercamApiHelper
{
    public static void setEvercamDeveloperKeypair(Context context)
    {
        PropertyReader propertyReader = new PropertyReader(context);
        String developerAppKey = propertyReader.getPropertyStr(PropertyReader.KEY_API_KEY);
        String developerAppID = propertyReader.getPropertyStr(PropertyReader.KEY_API_ID);
        API.setDeveloperKeyPair(developerAppKey, developerAppID);
    }
}
