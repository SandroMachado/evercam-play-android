package io.evercam.androidapp;

import io.evercam.androidapp.tasks.ScanForCameraTask;
import io.evercam.androidapp.utils.EvercamApiHelper;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

public class ScanActivity extends Activity
{
	private View scanProgressView;
	private View scanResultView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);
		
		scanProgressView = findViewById(R.id.scan_status_layout);
		scanResultView = findViewById(R.id.scan_result_layout);
		
		startDiscovery();
	}
	
	private void startDiscovery()
	{
		 EvercamApiHelper.setEvercamDeveloperKeypair(this);
		 new ScanForCameraTask(ScanActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	public void showResult(boolean show)
	{
		scanResultView.setVisibility(show ? View.VISIBLE : View.GONE);
		scanProgressView.setVisibility(show ? View.GONE : View.VISIBLE);
	}
}
