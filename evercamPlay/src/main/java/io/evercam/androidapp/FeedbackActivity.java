package io.evercam.androidapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import io.evercam.User;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.feedback.FeedbackSender;
import io.evercam.androidapp.utils.Constants;

public class FeedbackActivity extends ParentActivity
{
    private final String TAG = "FeedbackActivity";
    private EditText feedbackEditText;
    private String cameraId;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(this.getActionBar() != null)
        {
            this.getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.activity_feedback);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
        {
            cameraId = bundle.getString(Constants.BUNDLE_KEY_CAMERA_ID);
        }
        feedbackEditText = (EditText) findViewById(R.id.feedback_edit_text);

        fillUserDetail();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        if(!MainActivity.isUserLogged(this))
        {
            finish();
        }
        else
        {
            //Re-fill user details because user account could be changed
            fillUserDetail();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        getMenuInflater().inflate(R.menu.menu_feedback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if(id == R.id.action_send)
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
                        feedbackSender.send(feedbackString, cameraId);
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

    private void showConfirmQuitDialog()
    {
        String feedbackString = feedbackEditText.getText().toString();
        if(!feedbackString.isEmpty())
        {
            CustomedDialog.getConfirmQuitFeedbackDialog(this, new DialogInterface.OnClickListener()
            {
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

    private void fillUserDetail()
    {
        Runnable requestUserRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                AppUser user = AppData.defaultUser;
                if(user != null)
                {
                    try
                    {
                        User evercamUser = new User(user.getUsername());
                        String fullName = evercamUser.getFirstName() + " " + evercamUser
                                .getLastName();
                        String userEmail = evercamUser.getEmail();
                        fill(fullName, userEmail);
                    }
                    catch(Exception e)
                    {
                        EvercamPlayApplication.sendCaughtException(FeedbackActivity.this, e);
                        Log.e(TAG, e.toString());
                    }
                }
            }
        };

        new Thread(requestUserRunnable).start();
    }

    private void fill(final String fullName, final String email)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                final EditText nameEditText = (EditText) findViewById(R.id.feedback_name_edit);
                final EditText emailEditText = (EditText) findViewById(R.id.feedback_email_edit);
                nameEditText.setText(fullName);
                emailEditText.setText(email);
            }
        });
    }
}
