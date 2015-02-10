package io.evercam.androidapp.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.content.Context;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.ArrayList;

import io.evercam.androidapp.R;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.PrefsManager;

public class EvercamAccount
{
    private final String TAG = "evercamplay-EvercamAccount";
    private Context mContext;
    private final AccountManager mAccountManager;

    public EvercamAccount(Context context)
    {
        mContext = context;
        mAccountManager = AccountManager.get(mContext);
    }

    public void add(AppUser newUser)
    {
        final Account account = getAccountByEmail(newUser.getEmail());

        mAccountManager.addAccountExplicitly(account, null, null);
        mAccountManager.setAuthToken(account, Constants.KEY_API_KEY, newUser.getApiKey());
        mAccountManager.setAuthToken(account, Constants.KEY_API_ID, newUser.getApiId());
        mAccountManager.setUserData(account, Constants.KEY_USERNAME, newUser.getUsername());
        mAccountManager.setUserData(account, Constants.KEY_COUNTRY, newUser.getCountry());
    }

    public void remove(final String email, AccountManagerCallback<Boolean> callback)
    {
        final Account account = getAccountByEmail(email);
        mAccountManager.removeAccount(account, callback, null);

        //If removing default user, update the shared preference as well
        String defaultEmail = PrefsManager.getUserEmail(mContext);
        if(TextUtils.equals(defaultEmail, email))
        {
            PrefsManager.removeUserEmail(PreferenceManager.getDefaultSharedPreferences(mContext));
            AppData.defaultUser = null;
        }
    }

    public Account getAccountByEmail(String email)
    {
        return new Account(email, mContext.getString(R.string.account_type));
    }

    public AppUser retrieveUserByEmail(String email)
    {
        Account account = getAccountByEmail(email);
        String apiKey = mAccountManager.peekAuthToken(account, Constants.KEY_API_KEY);
        String apiId = mAccountManager.peekAuthToken(account, Constants.KEY_API_ID);
        String username = mAccountManager.getUserData(account, Constants.KEY_USERNAME);
        String country = mAccountManager.getUserData(account, Constants.KEY_COUNTRY);

        AppUser appUser = new AppUser();
        appUser.setEmail(email);
        appUser.setApiKey(apiKey);
        appUser.setApiId(apiId);
        appUser.setUsername(username);
        appUser.setCountry(country);

        if(TextUtils.equals(PrefsManager.getUserEmail(mContext), email))
        {
            appUser.setIsDefault(true);
        }

        return appUser;
    }

    public ArrayList<AppUser> retrieveUserList()
    {
        ArrayList<AppUser> userList = new ArrayList<>();

        String defaultEmail = PrefsManager.getUserEmail(mContext);
        boolean defaultUserMatched = false;
        Account[] accounts = mAccountManager.getAccountsByType(mContext.getString(R.string
                .account_type));
        if(accounts.length > 0)
        {
            for(Account account : accounts)
            {
                AppUser appUser = retrieveUserByEmail(account.name);
                if(defaultEmail != null)
                {
                    if(TextUtils.equals(defaultEmail, appUser.getEmail()))
                    {
                        appUser.setIsDefault(true);
                        AppData.defaultUser = appUser;
                        defaultUserMatched = true;
                    }
                }
                userList.add(appUser);
            }

            //If default user doesn't exist, set the first one in user list as default
            if(!defaultUserMatched)
            {
                AppUser newDefaultUser = userList.get(0);
                newDefaultUser.setIsDefault(true);
                PrefsManager.saveUserEmail(mContext, newDefaultUser.getEmail());
                AppData.defaultUser = newDefaultUser;
            }
        }
        return userList;
    }
}
