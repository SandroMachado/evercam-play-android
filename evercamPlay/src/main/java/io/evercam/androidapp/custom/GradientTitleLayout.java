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
    private final String TAG = "GradientTitleLayout";

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

    public ImageView getOfflineImageView()
    {
        return (ImageView) findViewById(R.id.offline_image_view);
    }

    public void showOfflineIcon(boolean show, boolean isFloat)
    {
        ImageView imageView = (ImageView) findViewById(R.id.offline_image_view);
        ImageView floatImageView = (ImageView) findViewById(R.id.float_image_view);

        if(show)
        {
            if(isFloat)
            {
                floatImageView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.INVISIBLE);
            }
            else
            {
                floatImageView.setVisibility(View.INVISIBLE);
                imageView.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            imageView.setVisibility(View.INVISIBLE);
            floatImageView.setVisibility(View.INVISIBLE);
        }
    }

    public void removeGradientShadow()
    {
        RelativeLayout backgroundLayout = (RelativeLayout) findViewById(R.id
                .gredient_background_layout);
        backgroundLayout.setBackgroundColor(Color.TRANSPARENT);
    }
}
