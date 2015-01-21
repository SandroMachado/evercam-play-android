package io.evercam.androidapp.custom;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class CustomScrollView extends ScrollView
{
    private Runnable scrollerTask;
    private int initialPosition;

    private static final String TAG = "evercamplay-CustomScrollView";

    public interface OnScrollStoppedListener
    {
        void onScrollStopped();
    }

    private OnScrollStoppedListener onScrollStoppedListener;

    public CustomScrollView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        scrollerTask = new Runnable()
        {

            @Override
            public void run()
            {
                int newPosition = getScrollY();
                if(initialPosition - newPosition == 0)
                {// has stopped

                    if(onScrollStoppedListener != null)
                    {
                        onScrollStoppedListener.onScrollStopped();
                    }
                }
                else
                {
                    initialPosition = getScrollY();
                    CustomScrollView.this.postDelayed(scrollerTask, 100);
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
        CustomScrollView.this.postDelayed(scrollerTask, 100);
    }

    /**
     * Return bounds with bottom value + 300 in order to load more cameras
     */
    public Rect getLiveBoundsRect()
    {
        Rect scrollViewBounds = new Rect();
        getDrawingRect(scrollViewBounds);
        //		Log.d(TAG, scrollViewBounds.top + " " + scrollViewBounds.bottom + " "
        //				+ scrollViewBounds.left + " " + scrollViewBounds.right);
        Rect rectWithExtension = new Rect(scrollViewBounds.left, scrollViewBounds.top,
                scrollViewBounds.right, scrollViewBounds.bottom + scrollViewBounds.bottom / 4);
        //		Log.d(TAG, "Extended: " + rectWithExtension.top + " " + rectWithExtension.bottom +
        // " "
        //				+ rectWithExtension.left + " " + rectWithExtension.right);
        return rectWithExtension;
    }
}