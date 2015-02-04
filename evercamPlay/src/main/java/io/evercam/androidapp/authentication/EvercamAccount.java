package io.evercam.androidapp.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import java.util.ArrayList;

import io.evercam.androidapp.R;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.utils.Constants;

public class EvercamAccount
{
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

        return appUser;
    }

    public ArrayList<AppUser> retrieveUserList()
    {
        ArrayList<AppUser> userList = new ArrayList<>();
        Account[] accounts = mAccountManager.getAccountsByType(mContext.getString(R.string.account_type));
        if(accounts.length > 0)
        {
            for (Account account : accounts)
            {
                AppUser appUser = retrieveUserByEmail(account.name);
                userList.add(appUser);
            }
        }
        return userList;
    }
}
