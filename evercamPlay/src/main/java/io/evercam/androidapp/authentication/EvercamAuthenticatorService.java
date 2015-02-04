package io.evercam.androidapp.authentication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class EvercamAuthenticatorService extends Service
{
    @Override
    public IBinder onBind(Intent intent)
    {
        EvercamAuthenticator authenticator = new EvercamAuthenticator(this);
        return authenticator.getIBinder();
    }
}
