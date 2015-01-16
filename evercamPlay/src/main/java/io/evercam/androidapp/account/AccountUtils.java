package io.evercam.androidapp.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Patterns;

import com.google.android.gms.auth.GoogleAuthUtil;

import java.util.regex.Matcher;

public class AccountUtils
{
    /**
     * Retrieves the user profile information.
     *
     * @param context the context from which to retrieve the user profile
     * @return the user profile
     * @throws Exception
     */
    public static UserProfile getUserProfile(Context context) throws Exception
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ?
                getUserProfileOnIcsDevice(context) : getUserProfileOnGingerbreadDevice(context);
    }

    /**
     * Retrieves the user profile information in a manner supported by
     * Gingerbread devices.
     *
     * @param context the context from which to retrieve the user's email address
     *                and name
     * @return a list of the possible user's email address and name
     */
    private static UserProfile getUserProfileOnGingerbreadDevice(Context context)
    {
        // Other that using Patterns (API level 8) this works on devices down to
        // API level 5
        final Matcher valid_email_address = Patterns.EMAIL_ADDRESS.matcher("");
        final Account[] accounts = AccountManager.get(context).getAccountsByType(GoogleAuthUtil
                .GOOGLE_ACCOUNT_TYPE);
        UserProfile user_profile = new UserProfile();
        // As far as I can tell, there is no way to get the real name or phone
        // number from the Google account
        for(Account account : accounts)
        {
            if(valid_email_address.reset(account.name).matches())
                user_profile.addPossibleEmail(account.name);
        }
        // Gets the phone number of the device is the device has one
        if(context.getPackageManager().hasSystemFeature(Context.TELEPHONY_SERVICE))
        {
            final TelephonyManager telephony = (TelephonyManager) context.getSystemService
                    (Context.TELEPHONY_SERVICE);
            user_profile.addPossiblePhoneNumber(telephony.getLine1Number());
        }

        return user_profile;
    }

    /**
     * Retrieves the user profile information in a manner supported by Ice Cream
     * Sandwich devices.
     *
     * @param context the context from which to retrieve the user's email address
     *                and name
     * @return a list of the possible user's email address and name
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static UserProfile getUserProfileOnIcsDevice(Context context) throws Exception
    {
        final ContentResolver content = context.getContentResolver();
        final Cursor cursor = content.query(
                // Retrieves data rows for the device user's 'profile' contact
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Selects only email addresses or names
                ContactsContract.Contacts.Data.MIMETYPE + "=? OR " + ContactsContract.Contacts
                        .Data.MIMETYPE + "=? OR " + ContactsContract.Contacts.Data.MIMETYPE + "=?" +
                        " OR " + ContactsContract.Contacts.Data.MIMETYPE + "=?",
                new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                        ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE},

                // Show primary rows first. Note that there won't be a primary
                // email address if the
                // user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");

        UserProfile userProfile = getUserProfileOnGingerbreadDevice(context);
        String mime_type;
        while(cursor.moveToNext())
        {
            mime_type = cursor.getString(ProfileQuery.MIME_TYPE);
            if(mime_type.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE))
                userProfile.addPossibleEmail(cursor.getString(ProfileQuery.EMAIL),
                        cursor.getInt(ProfileQuery.IS_PRIMARY_EMAIL) > 0);
            else if(mime_type.equals(ContactsContract.CommonDataKinds.StructuredName
                    .CONTENT_ITEM_TYPE))
                userProfile.addPossibleName(cursor.getString(ProfileQuery.GIVEN_NAME) + " " +
                        cursor.getString(ProfileQuery.FAMILY_NAME));
            else if(mime_type.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE))
                userProfile.addPossiblePhoneNumber(cursor.getString(ProfileQuery.PHONE_NUMBER),
                        cursor.getInt(ProfileQuery.IS_PRIMARY_PHONE_NUMBER) > 0);
            else if(mime_type.equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE))
                userProfile.addPossiblePhoto(Uri.parse(cursor.getString(ProfileQuery.PHOTO)));
        }

        cursor.close();

        return userProfile;
    }
}
