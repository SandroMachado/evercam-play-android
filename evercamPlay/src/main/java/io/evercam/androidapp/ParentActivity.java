package io.evercam.androidapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.bugsense.trace.BugSenseHandler;

import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.PropertyReader;

public class ParentActivity extends Activity
{
    private PropertyReader propertyReader;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        propertyReader = new PropertyReader(this);

        if(Constants.isAppTrackingEnabled)
        {
            if(propertyReader.isPropertyExist(PropertyReader.KEY_BUG_SENSE))
            {
                String bugSenseCode = propertyReader.getPropertyStr(PropertyReader
                        .KEY_BUG_SENSE);
                BugSenseHandler.initAndStartSession(this,bugSenseCode);
            }
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if(Constants.isAppTrackingEnabled)
        {
            if(propertyReader.isPropertyExist(PropertyReader.KEY_BUG_SENSE))
            {
                BugSenseHandler.startSession(this);
            }
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if(Constants.isAppTrackingEnabled)
        {
            if(propertyReader.isPropertyExist(PropertyReader.KEY_BUG_SENSE))
            {
                BugSenseHandler.closeSession(this);
            }
        }
    }

    public PropertyReader getPropertyReader()
    {
        return propertyReader;
    }
}