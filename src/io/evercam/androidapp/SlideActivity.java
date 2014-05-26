package io.evercam.androidapp;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SlideActivity extends Activity implements OnPageChangeListener
{
	private final String TAG = "evercamplay-SlideActivity";

	private ViewPager viewPager;
	private ViewPagerAdapter viewPagerAdapter;

	private List<View> views;
	private ImageView[] dots;
	private static final int[] pics = { R.drawable.play_page_intro, R.drawable.play_page_feature, R.drawable.play_page_next };
	private int currentIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.indexslide);

		initSlideView();
		initDots();
		initLinks();
	}

	private void initSlideView()
	{
		views = new ArrayList<View>();
		LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		for (int index = 0; index < pics.length; index++)
		{
			ImageView imageView = new ImageView(this);
			imageView.setLayoutParams(mParams);
			imageView.setImageResource(pics[index]);
			views.add(imageView);
		}
		viewPager = (ViewPager) findViewById(R.id.page);

		viewPagerAdapter = new ViewPagerAdapter(views);
		viewPager.setAdapter(viewPagerAdapter);

		viewPager.setOnPageChangeListener(this);
	}

	private void initLinks()
	{
		TextView loginTextView = (TextView) findViewById(R.id.text_login);
		TextView signUpTextView = (TextView) findViewById(R.id.text_signup);
		loginTextView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				Intent login = new Intent(SlideActivity.this, LoginActivity.class);
				startActivity(login);
			}
		});

		signUpTextView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				Intent signup = new Intent(SlideActivity.this, SignUpActivity.class);
				startActivity(signup);
			}
		});
	}

	private void initDots()
	{
		LinearLayout dotLayout = (LinearLayout) findViewById(R.id.dot_layout);
		dots = new ImageView[pics.length];

		for (int index = 0; index < pics.length; index++)
		{
			dots[index] = (ImageView) dotLayout.getChildAt(index);
			dots[index].setEnabled(true);
			dots[index].setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View view)
				{
					int position = (Integer) view.getTag();
					setCurrentView(position);
					setCurrentDot(position);
				}

			});
			dots[index].setTag(index);
		}
		currentIndex = 0;
		dots[currentIndex].setEnabled(false);
	}

	private void setCurrentView(int position)
	{
		if (position < 0 || position >= pics.length)
		{
			return;
		}
		viewPager.setCurrentItem(position);
	}

	private void setCurrentDot(int positon)
	{
		if (positon < 0 || positon > pics.length - 1 || currentIndex == positon)
		{
			return;
		}
		dots[positon].setEnabled(false);
		dots[currentIndex].setEnabled(true);
		currentIndex = positon;
	}

	@Override
	public void onBackPressed()
	{
		// Not allowed to go back.
	}

	@Override
	public void onPageScrollStateChanged(int arg0)
	{
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2)
	{
	}

	@Override
	public void onPageSelected(int position)
	{
		setCurrentDot(position);
	}

	private class ViewPagerAdapter extends PagerAdapter
	{
		private List<View> views;

		public ViewPagerAdapter(List<View> views)
		{
			this.views = views;
		}

		@Override
		public void destroyItem(View view, int position, Object arg2)
		{
			((ViewPager) view).removeView(views.get(position));
		}

		@Override
		public int getCount()
		{
			if (views != null)
			{
				return views.size();
			}
			return 0;
		}

		@Override
		public Object instantiateItem(View view, int position)
		{
			((ViewPager) view).addView(views.get(position), 0);
			return views.get(position);
		}

		@Override
		public boolean isViewFromObject(View view, Object object)
		{
			return (view == object);
		}
	}
}