package io.evercam.androidapp;

import android.app.Activity;
import android.os.Bundle;

import android.view.Window;

public class ParentActivity extends Activity
{

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
	}
}