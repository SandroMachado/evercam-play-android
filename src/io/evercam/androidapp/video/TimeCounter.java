package io.evercam.androidapp.video;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.evercam.androidapp.R;
import android.app.Activity;
import android.view.View;
import android.widget.TextView;

public class TimeCounter 
{
	private Activity activity;
	private Thread thread;
	private TextView timeTextView;
	private boolean isStarted = false;
	
	public TimeCounter(Activity activity)
	{
		this.activity = activity;
		this.timeTextView = (TextView)activity.findViewById(R.id.time_text_view);
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
	    		Date now = new Date(System.currentTimeMillis());
	    		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy\nHH:mm:ss");
	    		String timeString = formatter.format(now);
                timeTextView.setText(timeString);
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
                catch (InterruptedException e) 
                {
                    Thread.currentThread().interrupt();
                } 
            }
	    }
	}
}
