package io.evercam.androidapp.custom;

import io.evercam.androidapp.R;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GredientTitleLayout extends RelativeLayout
{

	public GredientTitleLayout(Context context) 
	{
		super(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);   
		inflater.inflate(R.layout.gredient_layout, this);
	}
	
	public void setTitle(String title)
	{
		TextView textView = (TextView) findViewById(R.id.title_text);
		textView.setText(title);
	}

	public void removeGredientShadow()
	{
		RelativeLayout backgroundLayout = (RelativeLayout) findViewById(R.id.gredient_background_layout);
		backgroundLayout.setBackgroundColor(Color.TRANSPARENT);
	}
}
