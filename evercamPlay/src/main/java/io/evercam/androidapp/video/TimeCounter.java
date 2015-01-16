package io.evercam.androidapp.video;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import io.evercam.androidapp.R;

public class TimeCounter
{
    private Activity activity;
    private Thread thread;
    private TextView timeTextView;
    private boolean isStarted = false;
    private String timezone;

    public TimeCounter(Activity activity, String timezone)
    {
        this.activity = activity;
        this.timezone = timezone;
        this.timeTextView = (TextView) activity.findViewById(R.id.time_text_view);
        Runnable countRunnable = new CountRunner();
        thread = new Thread(countRunnable);
    }

    public void start()
    {
        isStarted = true;
        timeTextView.setVisibility(View.VISIBLE);
        if(!thread.isInterrupted())
        {
            thread.start();
        }
    }

    public boolean isStarted()
    {
        return isStarted;
    }

    public void stop()
    {
        timeTextView.setVisibility(View.GONE);
        thread.interrupt();
        thread = null;
    }

    public void updateTime()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                //	    		Date now = new Date(System.currentTimeMillis());
                //	    		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy
                // HH:mm:ss");
                //	    		String timeString = formatter.format(now);
                //	    		timeTextView.setText(timeString);
                org.joda.time.DateTimeZone timeZone = org.joda.time.DateTimeZone.forID(timezone);
                org.joda.time.DateTime dateTime = new org.joda.time.DateTime(timeZone);
                org.joda.time.format.DateTimeFormatter formatter = org.joda.time.format
                        .DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
                String timeAsString = dateTime.toString(formatter);
                timeTextView.setText(timeAsString);

            }
        });
    }

    class CountRunner implements Runnable
    {
        @Override
        public void run()
        {
            while(!Thread.currentThread().isInterrupted())
            {
                try
                {
                    updateTime();
                    Thread.sleep(1000);
                }
                catch(InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
