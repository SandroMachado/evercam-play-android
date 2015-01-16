package io.evercam.androidapp.custom;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.evercam.androidapp.R;

public class GradientTitleLayout extends RelativeLayout
{

    public GradientTitleLayout(Context context)
    {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.gradient_layout, this);
    }

    public void setTitle(String title)
    {
        TextView textView = (TextView) findViewById(R.id.title_text);
        textView.setText(title);
    }

    public void showOfflineImage(boolean show)
    {
        ImageView imageView = (ImageView) findViewById(R.id.offline_image_view);
        if(show)
        {
            imageView.setVisibility(View.VISIBLE);
        }
        else
        {
            imageView.setVisibility(View.GONE);
        }
    }

    public void removeGradientShadow()
    {
        RelativeLayout backgroundLayout = (RelativeLayout) findViewById(R.id.gredient_background_layout);
        backgroundLayout.setBackgroundColor(Color.TRANSPARENT);
    }
}
