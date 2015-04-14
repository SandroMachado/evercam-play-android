package io.evercam.androidapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.PrefsManager;

// 	This activity verifies the login and requests the cams data from the api 
public class ReleaseNotesActivity extends ParentActivity
{
    public String TAG = "ReleaseNotesActivity";
    private Button btnReleaseNotes;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.release_notes_activity_layout);

        TextView textViewNotes = (TextView) findViewById(R.id.txtreleasenotes);
        btnReleaseNotes = (Button) findViewById(R.id.btn_release_notes_ok);

        textViewNotes.setPadding(25, 14, 14, 14);

        btnReleaseNotes.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onNotesRead();
            }
        });

        String data = Commons.readRawTextFile(R.raw.release_notes, this);
        textViewNotes.setText(Html.fromHtml(data));
        Linkify.addLinks(textViewNotes, Linkify.EMAIL_ADDRESSES);

    }

    private void onNotesRead()
    {
        int versionCode = Commons.getAppVersionCode(this);
        PrefsManager.setReleaseNotesShown(this, versionCode);

        Intent act = new Intent(ReleaseNotesActivity.this, MainActivity.class);
        startActivity(act);
        ReleaseNotesActivity.this.finish();
    }

    @Override
    public void onWindowFocusChanged(boolean hasfocus)
    {
        ScrollView svreleasenotes = (ScrollView) findViewById(R.id.svreleasenotes);
        svreleasenotes.getLayoutParams().height = svreleasenotes.getMeasuredHeight() -
                btnReleaseNotes.getMeasuredHeight();
    }

    @Override
    public void onBackPressed()
    {
        onNotesRead();
    }
}