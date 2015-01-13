package io.evercam.androidapp;

import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.feedback.FeedbackSender;
import io.evercam.androidapp.utils.Constants;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class FeedbackActivity extends Activity 
{
	private final String TAG = "evercamplay-FeedbackActivity";
	private EditText feedbackEditText;
	private String cameraId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		if (this.getActionBar() != null)
		{
			this.getActionBar().setDisplayHomeAsUpEnabled(true);
			this.getActionBar().setIcon(R.drawable.icon_50x50);
		}
		
		setContentView(R.layout.activity_feedback);
		
		Bundle bundle = getIntent().getExtras();
		if(bundle != null)
		{
			cameraId = bundle.getString(Constants.BUNDLE_KEY_CAMERA_ID);
		}
		feedbackEditText = (EditText) findViewById(R.id.feedback_edit_text);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.menu_feedback, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		int id = item.getItemId();
		if (id == R.id.action_send) 
		{
			final String feedbackString = feedbackEditText.getText().toString().trim();
			if(feedbackString.isEmpty())
			{
				//Do nothing
			}
			else
			{
				feedbackEditText.setText("");
				CustomToast.showInCenterLong(this, R.string.msg_feedback_sent);
				new Thread(new Runnable()
				{
					@Override
					public void run() 
					{
						FeedbackSender feedbackSender = new FeedbackSender(FeedbackActivity.this);
						feedbackSender.send(feedbackString,cameraId);
					}
				}).start();
				finish();
			}
			
			return true;
		}
		else if(id == android.R.id.home)
		{
			showConfirmQuitDialog();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() 
	{
		showConfirmQuitDialog();
	}

//	public void sendFeedback()
//	{
//		String feedbackString = feedbackEditText.getText().toString();
//		if(feedbackString.isEmpty())
//		{
//			//Do nothing
//		}
//		else
//		{
//			CustomToast.showInCenter(this, R.string.msg_feedback_sent);
//			FeedbackSender feedbackSender = new FeedbackSender(this);
//			feedbackSender.send(feedbackString);
//		}
//	}
	
	private void showConfirmQuitDialog()
	{
		String feedbackString = feedbackEditText.getText().toString();
		if(!feedbackString.isEmpty())
		{
			CustomedDialog.getConfirmQuitFeedbackDialog(this,
					new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					finish();
				}
			}).show();
		}
		else
		{
			finish();
		}
	}
}
