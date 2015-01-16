package io.evercam.androidapp.account;

import android.provider.ContactsContract;

/**
 * Contacts user profile query interface.
 */
public interface ProfileQuery
{
    /**
     * The set of columns to extract from the profile query results
     */
    String[] PROJECTION = {ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.IS_PRIMARY, ContactsContract.CommonDataKinds
            .StructuredName.FAMILY_NAME, ContactsContract.CommonDataKinds.StructuredName
            .GIVEN_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.IS_PRIMARY, ContactsContract.CommonDataKinds
            .Photo.PHOTO_URI, ContactsContract.Contacts.Data.MIMETYPE};

    /**
     * Column index for the email address in the profile query results
     */
    int EMAIL = 0;
    /**
     * Column index for the primary email address indicator in the profile query
     * results
     */
    int IS_PRIMARY_EMAIL = 1;
    /**
     * Column index for the family name in the profile query results
     */
    int FAMILY_NAME = 2;
    /**
     * Column index for the given name in the profile query results
     */
    int GIVEN_NAME = 3;
    /**
     * Column index for the phone number in the profile query results
     */
    int PHONE_NUMBER = 4;
    /**
     * Column index for the primary phone number in the profile query results
     */
    int IS_PRIMARY_PHONE_NUMBER = 5;
    /**
     * Column index for the photo in the profile query results
     */
    int PHOTO = 6;
    /**
     * Column index for the MIME type in the profile query results
     */
    int MIME_TYPE = 7;
}