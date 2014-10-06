package io.evercam.androidapp.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class CustomScrollView extends ScrollView
{

	private Runnable scrollerTask;
	private int initialPosition;

	private int newCheck = 100;
	private static final String TAG = "evercamplay-CustomScrollView";

	public interface OnScrollStoppedListener
	{
		void onScrollStopped();
	}

	private OnScrollStoppedListener onScrollStoppedListener;

	public CustomScrollView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		scrollerTask = new Runnable(){

			public void run()
			{

				int newPosition = getScrollY();
				if (initialPosition - newPosition == 0)
				{// has stopped

					if (onScrollStoppedListener != null)
					{
						onScrollStoppedListener.onScrollStopped();
					}
				}
				else
				{
					initialPosition = getScrollY();
					CustomScrollView.this.postDelayed(scrollerTask, newCheck);
				}
			}
		};
	}

	public void setOnScrollStoppedListener(CustomScrollView.OnScrollStoppedListener listener)
	{
		onScrollStoppedListener = listener;
	}

	public void startScrollerTask()
	{
		initialPosition = getScrollY();
		CustomScrollView.this.postDelayed(scrollerTask, newCheck);
	}
}