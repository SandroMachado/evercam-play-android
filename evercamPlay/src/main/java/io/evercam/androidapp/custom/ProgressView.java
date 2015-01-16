package io.evercam.androidapp.custom;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Movie;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.InputStream;

//This class will play the progress spinner for loading image
public class ProgressView extends ProgressBar
{
    static String TAG = "evercamapp-ProgressView";
    Movie movie;
    InputStream inputStream = null;

    long moviestart = 0;
    Handler handler1 = new Handler();

    public int canvasColor = Color.WHITE;

    public ProgressView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        constructorToDo(context);
    }

    public ProgressView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        constructorToDo(context);
    }

    public ProgressView(Context context)
    {
        super(context);
        constructorToDo(context);
    }

    private void constructorToDo(Context context)
    {
        try
        {
            this.setIndeterminate(true);
        }
        catch(Exception e)
        {
            Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
        }

    }

}
