package io.evercam.android.custom;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Movie;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ProgressBar;

//This class will play the progress spinner for loading image
public class ProgressView extends ProgressBar
{ // View

	static String TAG = "ProgressView";
	Movie movie;
	InputStream is = null;

	long moviestart = 0;
	Handler handler1 = new Handler();

	public int CanvasColor = Color.WHITE;

	public ProgressView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		ConstructorToDo(context);
	}

	public ProgressView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		ConstructorToDo(context);
	}

	public ProgressView(Context context)
	{
		super(context);
		ConstructorToDo(context);
	}

	private void ConstructorToDo(Context context)
	{
		try
		{

			this.setIndeterminate(true);

		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
		}

	}

}
