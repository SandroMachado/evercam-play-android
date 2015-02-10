package io.evercam.androidapp.test.account;

import android.content.Context;
import android.test.AndroidTestCase;

import io.evercam.androidapp.account.AccountUtils;
import io.evercam.androidapp.account.UserProfile;

/**
 * Tests of load user saved account details from the device.
 */
public class AccountUtilsTest extends AndroidTestCase
{
    Context context;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        context = this.getContext();
    }

    public void testRetrieveAccountDetails() throws Exception
    {
        UserProfile profile = AccountUtils.getUserProfile(context);
        String primaryEmail = profile.primaryEmail();
        String savedEmail = profile.possibleEmails().get(0);
        String name = profile.possibleNames().get(0);
        assertEquals(null, primaryEmail);
        assertEquals("kangtadlt@gmail.com", savedEmail);
        assertEquals("Liuting Du", name);
    }
}
