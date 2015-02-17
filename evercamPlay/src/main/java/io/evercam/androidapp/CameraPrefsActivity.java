package io.evercam.androidapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import com.bugsense.trace.BugSenseHandler;

import java.util.ArrayList;

import io.evercam.androidapp.custom.ThemedListPreference;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.DataCollector;
import io.evercam.androidapp.utils.PrefsManager;

public class CameraPrefsActivity extends PreferenceActivity
{
    private static int screenWidth = 0;
    private static final String TAG = "evercamplay-CameraPrefsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(Constants.isAppTrackingEnabled)
        {
            BugSenseHandler.initAndStartSession(this, Constants.bugsense_ApiKey);
        }

        if(this.getActionBar() != null)
        {
            this.getActionBar().setDisplayHomeAsUpEnabled(true);
            this.getActionBar().setIcon(R.drawable.icon_50x50);
        }

        screenWidth = CamerasActivity.readScreenWidth(this);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new MyPreferenceFragment()).commit();
        this.setDefaultKeyMode(DEFAULT_KEYS_DISABLE);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if(Constants.isAppTrackingEnabled)
        {
            BugSenseHandler.startSession(this);
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if(Constants.isAppTrackingEnabled)
        {
            BugSenseHandler.closeSession(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch(item.getItemId())
        {
            case android.R.id.home:

                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        public MyPreferenceFragment()
        {
            // super();
        }

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.main_preference);
            setCameraNumbersForScreen(screenWidth);
            setUpSleepTime();
            fillAbout();
        }

        private void setCameraNumbersForScreen(int screenWidth)
        {
            int maxCamerasPerRow = 3;
            if(screenWidth != 0)
            {
                maxCamerasPerRow = screenWidth / 350;
            }
            if(maxCamerasPerRow == 0)
            {
                maxCamerasPerRow = 1;
            }
            ArrayList<String> cameraNumberArrayList = new ArrayList<String>();
            for(int index = 1; index <= maxCamerasPerRow; index++)
            {
                cameraNumberArrayList.add(String.valueOf(index));
            }
            CharSequence[] charNumberValues = cameraNumberArrayList.toArray(new
                    CharSequence[cameraNumberArrayList.size()]);
            final ThemedListPreference interfaceList = (ThemedListPreference)
                    getPreferenceManager().findPreference(PrefsManager.KEY_CAMERA_PER_ROW);
            interfaceList.setEntries(charNumberValues);
            interfaceList.setEntryValues(charNumberValues);
            interfaceList.setSummary(interfaceList.getValue());
            interfaceList.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    interfaceList.setSummary(newValue.toString());
                    return true;
                }
            });
        }

        private void setUpSleepTime()
        {
            final ThemedListPreference sleepListPrefs = (ThemedListPreference)
                    getPreferenceManager().findPreference(PrefsManager.KEY_AWAKE_TIME);
            sleepListPrefs.setSummary(getSummary(sleepListPrefs.getEntry() + ""));
            sleepListPrefs.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    int index = sleepListPrefs.findIndexOfValue(newValue.toString());
                    String entry = sleepListPrefs.getEntries()[index].toString();
                    sleepListPrefs.setSummary(getSummary(entry));
                    return true;
                }
            });
        }

        private void fillAbout()
        {
            final Preference versionPrefs = getPreferenceManager()
                    .findPreference(PrefsManager.KEY_VERSION);
            final Preference aboutPrefs = getPreferenceManager()
                    .findPreference(PrefsManager.KEY_ABOUT);
            versionPrefs.setSummary(new DataCollector(this.getActivity()).getAppVersion());
            aboutPrefs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.evercam_url)));
                    startActivity(intent);
                    return false;
                }
            });
        }

        private String getSummary(String entry)
        {
            if(entry.equals(getString(R.string.prefs_never)))
            {
                return entry;
            }
            else
            {
                return getString(R.string.summary_awake_time_prefix) + " " + entry + " " +
                        getString(R.string.summary_awake_time_suffix);
            }
        }
    }
}