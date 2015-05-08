package io.evercam.androidapp.tasks;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.view.View;

import io.evercam.androidapp.MainActivity;
import io.evercam.androidapp.dto.AppUser;

public class CheckKeyExpirationTask extends AsyncTask<Void, Void, Boolean>
{
    private String username;
    private String apiKey;
    private String apiId;
    protected AppUser appUser;
    protected View viewToDismiss;
    protected AlertDialog dialogToDismiss;

    public CheckKeyExpirationTask(String username, String apiKey, String apiId)
    {
        this.username = username;
        this.apiKey = apiKey;
        this.apiId = apiId;
    }

    public CheckKeyExpirationTask(AppUser appUser, View viewToDismiss, AlertDialog dialogToDismiss)
    {
        this.appUser = appUser;
        this.username = appUser.getUsername();
        this.apiKey = appUser.getApiKey();
        this.apiId = appUser.getApiId();
        this.viewToDismiss = viewToDismiss;
        this.dialogToDismiss = dialogToDismiss;
    }

    @Override
    protected Boolean doInBackground(Void... params)
    {
        return MainActivity.isApiKeyExpired(username, apiKey, apiId);
    }
}