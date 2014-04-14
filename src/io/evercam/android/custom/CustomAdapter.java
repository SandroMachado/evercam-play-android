package io.evercam.android.custom;

import io.evercam.android.dto.AppUser;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomAdapter extends ArrayAdapter<AppUser>
{
	private ArrayList<AppUser> entries;
	private Activity activity;
	private int itemlayoutid = 0;
	private int textviewid = 0;
	private int newItemLayoutid = 0;

	AppUser fakeUser = null;

	public CustomAdapter(Activity a, int LayoutID, int newItemLayoutResource,
			int itemTextViewIdToDislayNameOfUser, ArrayList<AppUser> entries)
	{
		super(a, LayoutID, itemTextViewIdToDislayNameOfUser, entries);

		this.entries = entries;

		if (this.entries != null && this.entries.size() > 0
				&& this.entries.get(this.entries.size() - 1).getId() == -1)
		{
			fakeUser = this.entries.get(this.entries.size() - 1);
		}
		else
		{
			this.fakeUser = new AppUser(-1, "", "", "", "","","",false);
			this.entries.add(fakeUser); // add at the end
		}
		this.activity = a;
		itemlayoutid = LayoutID;
		textviewid = itemTextViewIdToDislayNameOfUser;
		newItemLayoutid = newItemLayoutResource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View v = convertView;
		final AppUser custom = entries.get(position);

		if (v == null)
		{
			LayoutInflater vi = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = ((custom != fakeUser) ? vi.inflate(itemlayoutid, null) : vi.inflate(
					newItemLayoutid, null));
		}

		if (custom != null && custom != fakeUser)
		{
			((TextView) v.findViewById(textviewid)).setText(custom.getEmail()
					+ (custom.getIsDefault() ? " - Default" : ""));
		}

		return v;
	}

}