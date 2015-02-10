package io.evercam.androidapp.dto;

import java.util.ArrayList;

public final class AppData
{
    public static ArrayList<EvercamCamera> evercamCameraList = new ArrayList<EvercamCamera>();
    public static ArrayList<AppUser> appUsers;

    public static AppUser defaultUser = null;

    public static void reset()
    {
        evercamCameraList.clear();
        appUsers = null;
        defaultUser = null;
    }
}
