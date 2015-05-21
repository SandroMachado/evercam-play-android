package io.evercam.androidapp.test.utils;

import android.content.Context;
import android.test.AndroidTestCase;

import io.evercam.androidapp.utils.Commons;

public class CommonsTest extends AndroidTestCase
{
    Context mContext;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mContext = this.getContext();
    }

//    public void testIsLocalIp()
//    {
//        final String LOCAL_IP = "192.168.44.5";
//        final String SECOND_LOCAL_IP = "172.16.0.26";
//        final String EXTERNAL_IP = "89.101.133.66";
//        final String DOMAIN_NAME = "hello.com";
//        assertTrue(Commons.isLocalIP(LOCAL_IP));
//        assertTrue(Commons.isLocalIP(SECOND_LOCAL_IP));
//        assertFalse(Commons.isLocalIP(EXTERNAL_IP));
//        assertFalse(Commons.isLocalIP(DOMAIN_NAME));
//    }
}
