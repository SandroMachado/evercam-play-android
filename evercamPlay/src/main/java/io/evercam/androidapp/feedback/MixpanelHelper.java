package io.evercam.androidapp.feedback;

import android.content.Context;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.utils.PropertyReader;

public class MixpanelHelper
{
    private MixpanelAPI mixpanel;
    private Context context;

    public MixpanelHelper(Context context, PropertyReader propertyReader)
    {
        this.context = context;

        initMixpanel(context, propertyReader);
    }

    public MixpanelHelper(Context context)
    {
        this.context = context;

        PropertyReader propertyReader = new PropertyReader(context);
        initMixpanel(context, propertyReader);
    }

    private void initMixpanel(Context context, PropertyReader propertyReader)
    {
        if(propertyReader.isPropertyExist(PropertyReader.KEY_MIXPANEL))
        {
            final String MIXPANEL_TOKEN = propertyReader.getPropertyStr(PropertyReader
                    .KEY_MIXPANEL);
            mixpanel = MixpanelAPI.getInstance(context, MIXPANEL_TOKEN);
        }
    }

    /**
     * Inform Mixpanel library to send whatever events are still unsent
     */
    public void flush()
    {
        if(mixpanel != null)
        {
            mixpanel.flush();
        }
    }

    public void sendEvent(int eventNameId, JSONObject eventJsonObject)
    {
        if(mixpanel != null)
        {
            if(AppData.defaultUser != null)
            {
                mixpanel.identify(AppData.defaultUser.getUsername());
            }

            String eventName = context.getString(eventNameId);
            mixpanel.track(eventName, eventJsonObject);
        }
    }

    public void identifyUser(String username)
    {
        if(mixpanel != null)
        {
            mixpanel.identify(username);
        }
    }

    public void identifyNewUser(AppUser user)
    {
        if(mixpanel != null)
        {
            mixpanel.getPeople().identify(user.getUsername());
            mixpanel.getPeople().set("$email", user.getEmail());
            mixpanel.getPeople().set("$first_name", user.getFirstName());
            mixpanel.getPeople().set("$last_name", user.getLastName());
            mixpanel.getPeople().set("Username", user.getUsername());
        }
    }

    public void registerSuperProperty(String propertyName, String propertyValue)
    {
        if(mixpanel != null)
        {
            JSONObject props = new JSONObject();
            try
            {
                props.put(propertyName, propertyValue);
                mixpanel.registerSuperPropertiesOnce(props);
            }
            catch(JSONException e)
            {
                e.printStackTrace();
            }
        }
    }
}
