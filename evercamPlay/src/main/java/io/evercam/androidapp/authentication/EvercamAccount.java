package io.evercam.androidapp.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;

import io.evercam.androidapp.R;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;

public class EvercamAccount
{
    public static final String KEY_USERNAME = "username";
    public static final String KEY_COUNTRY = "country";
    public static final String KEY_API_KEY = "apiKey";
    public static final String KEY_API_ID = "apiId";
    public static final String KEY_IS_DEFAULT = "isDefault";
    public static final String KEY_FIRSTNAME = "firstName";
    public static final String KEY_LASTNAME = "lastName";
    private final String TAG = "EvercamAccount";
    private final String TRUE = "true";

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
        mAccountManager.setAuthToken(account, KEY_API_KEY, newUser.getApiKey());
        mAccountManager.setAuthToken(account, KEY_API_ID, newUser.getApiId());
        mAccountManager.setUserData(account, KEY_USERNAME, newUser.getUsername());
        mAccountManager.setUserData(account, KEY_COUNTRY, newUser.getCountry());
        mAccountManager.setUserData(account, KEY_FIRSTNAME, newUser.getFirstName());
        mAccountManager.setUserData(account, KEY_LASTNAME, newUser.getLastName());
        mAccountManager.setUserData(account, KEY_IS_DEFAULT, String.valueOf(newUser.getIsDefault
                ()));

        //Always set the new user as default user
        updateDefaultUser(newUser.getEmail());
    }

    public void remove(final String email, AccountManagerCallback<Boolean> callback)
    {
        final Account account = getAccountByEmail(email);

        String isDefaultString = mAccountManager.getUserData(account, KEY_IS_DEFAULT);
        //If removing default user, clear the static user object
        if(isDefaultString.equals(TRUE))
        {
            AppData.defaultUser = null;
        }

        mAccountManager.removeAccount(account, callback, null);
    }

    public Account getAccountByEmail(String email)
    {
        return new Account(email, mContext.getString(R.string.account_type));
    }

    public AppUser retrieveUserByEmail(String email)
    {
        Account account = getAccountByEmail(email);
        return retrieveUserDetailFromAccount(account);
    }

    public ArrayList<AppUser> retrieveUserList()
    {
        ArrayList<AppUser> userList = new ArrayList<>();

        Account[] accounts = mAccountManager.getAccountsByType(mContext.getString(R.string
                .account_type));
        int defaultCount = 0;

        if(accounts.length > 0)
        {
            for(Account account : accounts)
            {
                AppUser appUser = retrieveUserByEmail(account.name);
                if(appUser.getIsDefault())
                {
                    defaultCount++;
                }
                userList.add(appUser);
            }

            //If default user doesn't exist, or more than 1, reset default user
            if(defaultCount != 1)
            {
                AppUser newDefaultUser = userList.get(0);
                String defaultUserEmail = newDefaultUser.getEmail();
                updateDefaultUser(defaultUserEmail);
                AppData.appUsers = retrieveUserList();
                return AppData.appUsers;
            }
        }

        AppData.appUsers = userList;
        return userList;
    }

    public AppUser retrieveUserDetailFromAccount(Account account)
    {
        //Start to sync camera list
        startSync(account);

        String apiKey = mAccountManager.peekAuthToken(account, KEY_API_KEY);
        String apiId = mAccountManager.peekAuthToken(account, KEY_API_ID);
        String username = mAccountManager.getUserData(account, KEY_USERNAME);
        String country = mAccountManager.getUserData(account, KEY_COUNTRY);
        String firstName = mAccountManager.getUserData(account, KEY_FIRSTNAME);
        String lastName = mAccountManager.getUserData(account, KEY_LASTNAME);

        String isDefaultString = mAccountManager.getUserData(account, KEY_IS_DEFAULT);

        AppUser appUser = new AppUser();
        appUser.setEmail(account.name);
        appUser.setApiKeyPair(apiKey, apiId);
        appUser.setUsername(username);
        appUser.setCountry(country);
        appUser.setFirstName(firstName);
        appUser.setLastName(lastName);

        if(isDefaultString != null && isDefaultString.equals(TRUE))
        {
            appUser.setIsDefault(true);
            AppData.defaultUser = appUser;
        }

        return appUser;
    }

    public AppUser getDefaultUser()
    {
        ArrayList<AppUser> userList = retrieveUserList();

        if(userList.size() > 0)
        {
            for(AppUser appUser : userList)
            {
                if(appUser.getIsDefault())
                {
                    return appUser;
                }
            }
        }
        return null;
    }

    public void updateDefaultUser(String defaultEmail)
    {
        Account[] accounts = mAccountManager.getAccountsByType(mContext.getString(R.string
                .account_type));

        if(accounts.length > 0)
        {
            for(Account account : accounts)
            {
                String email = account.name;
                if(email.equals(defaultEmail))
                {
                    mAccountManager.setUserData(account, KEY_IS_DEFAULT, TRUE);
                    AppData.defaultUser = retrieveUserByEmail(email);
                }
                else
                {
                    mAccountManager.setUserData(account, KEY_IS_DEFAULT, "");
                }
            }
        }
    }

    private void startSync(Account account)
    {
        final int SYNC_INTERVAL = 60;
        ContentResolver.setSyncAutomatically(account, mContext.getString(R.string
                .content_provider_authorities), true);
        ContentResolver.addPeriodicSync(account, mContext.getString(R.string
                .content_provider_authorities), Bundle.EMPTY, SYNC_INTERVAL);
    }
}
