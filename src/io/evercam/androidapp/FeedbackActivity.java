package io.evercam.androidapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class FeedbackActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (this.getActionBar() != null)
		{
			this.getActionBar().setDisplayHomeAsUpEnabled(true);
			this.getActionBar().setIcon(R.drawable.icon_50x50);
		}
		
		setContentView(R.layout.activity_feedback);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.menu_feedback, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		if (id == R.id.action_send) 
		{
			
			return true;
		}
		else if(id == android.R.id.home)
		{
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
