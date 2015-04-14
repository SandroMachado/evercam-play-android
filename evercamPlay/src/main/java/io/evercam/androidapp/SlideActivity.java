package io.evercam.androidapp;

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

import java.util.ArrayList;
import java.util.List;

import io.evercam.androidapp.utils.Constants;

public class SlideActivity extends ParentActivity implements OnPageChangeListener
{
    private final String TAG = "SlideActivity";

    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;

    private List<View> views;
    private ImageView[] dots;
    private static final int[] pics = {R.drawable.play_page_intro, R.drawable.play_page_feature,
            R.drawable.play_page_next};
    private int currentIndex;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.indexslide);

        initSlideView();
        initDots();
        initLinks();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == Constants.REQUEST_CODE_SIGN_IN || requestCode == Constants
                .REQUEST_CODE_SIGN_UP)
        {
            if(resultCode == Constants.RESULT_TRUE)
            {
                finish();
                startActivity(new Intent(this, MainActivity.class));
            }
        }
    }

    private void initSlideView()
    {
        views = new ArrayList<>();
        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(LayoutParams
                .WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        for(int index = 0; index < pics.length; index++)
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
        loginTextView.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                Intent login = new Intent(SlideActivity.this, LoginActivity.class);
                startActivityForResult(login, Constants.REQUEST_CODE_SIGN_IN);
            }
        });

        signUpTextView.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                Intent signup = new Intent(SlideActivity.this, SignUpActivity.class);
                startActivityForResult(signup, Constants.REQUEST_CODE_SIGN_UP);
            }
        });
    }

    private void initDots()
    {
        LinearLayout dotLayout = (LinearLayout) findViewById(R.id.dot_layout);
        dots = new ImageView[pics.length];

        for(int index = 0; index < pics.length; index++)
        {
            dots[index] = (ImageView) dotLayout.getChildAt(index);
            dots[index].setEnabled(true);
            dots[index].setOnClickListener(new OnClickListener()
            {

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
        if(position < 0 || position >= pics.length)
        {
            return;
        }
        viewPager.setCurrentItem(position);
    }

    private void setCurrentDot(int positon)
    {
        if(positon < 0 || positon > pics.length - 1 || currentIndex == positon)
        {
            return;
        }
        dots[positon].setEnabled(false);
        dots[currentIndex].setEnabled(true);
        currentIndex = positon;
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
            if(views != null)
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