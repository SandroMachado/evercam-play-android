package io.evercam.androidapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.logentries.android.AndroidLogger;
import com.splunk.mint.Mint;

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
            if(propertyReader.isPropertyExist(PropertyReader.KEY_SPLUNK_MINT))
            {
                Mint.startSession(this);
            }
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if(Constants.isAppTrackingEnabled)
        {
            if(propertyReader.isPropertyExist(PropertyReader.KEY_SPLUNK_MINT))
            {
                Mint.closeSession(this);
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
        mixpanelHelper.registerSuperProperty("Client-Type", "Play-Android");

        return mixpanelHelper;
    }

    private void initBugSense()
    {
        if(Constants.isAppTrackingEnabled)
        {
            if(propertyReader.isPropertyExist(PropertyReader.KEY_SPLUNK_MINT))
            {
                String bugSenseCode = propertyReader.getPropertyStr(PropertyReader
                        .KEY_SPLUNK_MINT);
                Mint.initAndStartSession(this,bugSenseCode);
            }
        }
    }

    public static void sendToMint(Exception e)
    {
        if(Constants.isAppTrackingEnabled)
        {
            Mint.logException(e);
        }
    }

    public void sendToLogentries(AndroidLogger logger, String message)
    {
        if(logger != null)
        {
            logger.info(message);
        }
    }

    public static void sendWithMsgToMint(String messageName, String message, Exception e)
    {
        Mint.logExceptionMessage(messageName, message, e);
    }
}