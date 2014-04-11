package io.evercam.android;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey = "", // This is required for backward compatibility but
// not used
formUri = "http://www.bugsense.com/api/acra?api_key="
		+ io.evercam.android.utils.Constants.bugsense_ApiKey)
public class EvercamApp extends Application
{

	@Override
	public void onCreate()
	{
		super.onCreate();

		// The following line triggers the initialization of ACRA
		ACRA.init(this);
	}

}
