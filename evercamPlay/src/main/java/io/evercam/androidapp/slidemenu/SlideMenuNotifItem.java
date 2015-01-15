package io.evercam.androidapp.slidemenu;

public class SlideMenuNotifItem
{
	public int id;
	// public Drawable icon;
	public String label;
	public int typeID;
	public boolean IsRead = false;

	public SlideMenuNotifItem()
	{
	}

	public SlideMenuNotifItem(int itemId, String itemLabel, int itemTypeId, boolean itemIsRead)
	{
		id = itemId;
		label = itemLabel;
		typeID = itemTypeId;
		IsRead = itemIsRead;
	}

	@Override
	public String toString()
	{
		return ",Id [" + id + "],Label [" + label + "],TypeId [" + typeID + "],IsRead [" + IsRead
				+ "]";
	}
}