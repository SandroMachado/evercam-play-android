package io.evercam.androidapp.test.utils;

import android.content.Context;
import android.test.AndroidTestCase;

import io.evercam.androidapp.utils.DataCollector;

public class DataCollectorTest extends AndroidTestCase
{
    Context mContext;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mContext = this.getContext();
    }

    public void testGetCountryCode()
    {
        assertEquals("ie", DataCollector.getCountryCode(mContext));
    }
}
