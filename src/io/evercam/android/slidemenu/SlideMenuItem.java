package io.evercam.android.slidemenu;

import android.graphics.drawable.Drawable;

public class SlideMenuItem
{
	public int id;
	public Drawable icon;
	public String label;

	public SlideMenuItem()
	{
	}

	public SlideMenuItem(int itemId, Drawable itemIcon, String itemLabel)
	{
		id = itemId;
		;
		icon = itemIcon;
		label = itemLabel;
	}
}