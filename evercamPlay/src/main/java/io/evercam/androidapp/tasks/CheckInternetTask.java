package io.evercam.androidapp.tasks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;

public class CheckInternetTask extends AsyncTask<Void, Void, Boolean>
{
    private Context context;
    private final String TAG = "evercamplay-CheckInternetTask";
    private final String HOSTNAME_GOOGLE = "www.google.com";

    public CheckInternetTask(Context context)
    {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Void... params)
    {
        if(hasActiveNetwork())
        {
            return true;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean hasNetwork)
    {
        super.onPostExecute(hasNetwork);
    }

    public boolean hasActiveNetwork()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService
                (Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getActiveNetworkInfo() != null)
        {
            // try
            // {
            // InetAddress.getByName(HOSTNAME_GOOGLE);
            // return true;
            // }
            // catch (UnknownHostException e)
            // {
            // Log.e(TAG, e.getMessage());
            // }
            // return false;
            return true;
        }
        else
        {
            return false;
        }
    }
}
