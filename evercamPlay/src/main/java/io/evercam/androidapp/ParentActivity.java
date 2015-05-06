package io.evercam.androidapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.bugsense.trace.BugSenseHandler;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import io.evercam.androidapp.feedback.MixpanelHelper;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.PropertyReader;

public class ParentActivity extends Activity
{
    private PropertyReader propertyReader;
    private MixpanelHelper mixpanelHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        propertyReader = new PropertyReader(this);

        initBugSense();

        mixpanelHelper = new MixpanelHelper(this, propertyReader);
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

        getMixpanel().flush();
    }

    public PropertyReader getPropertyReader()
    {
        return propertyReader;
    }

    /**
     * @return the Mixpanel helper class
     */
    public MixpanelHelper getMixpanel()
    {
        mixpanelHelper.registerSuperProperty("Client-Type", "Play");

        return mixpanelHelper;
    }

    private void initBugSense()
    {
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
}