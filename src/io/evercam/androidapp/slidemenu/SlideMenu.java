/*
 * A sliding menu for Android, very much like the Google+ and Facebook apps have.
 * 
 * Copyright (C) 2012 CoboltForge
 * 
 * Based upon the great work done by stackoverflow user Scirocco (http://stackoverflow.com/a/11367825/361413), thanks a lot!
 * The XML parsing code comes from https://github.com/darvds/RibbonMenu, thanks!
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.evercam.androidapp.slidemenu;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import io.evercam.androidapp.R;
import io.evercam.androidapp.R.color;
import io.evercam.androidapp.dal.*;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.CameraNotification;

public class SlideMenu extends LinearLayout
{

	// keys for saving/restoring instance state
	private final static String KEY_MENUSHOWN = "menuWasShown";
	private final static String KEY_STATUSBARHEIGHT = "statusBarHeight";
	private final static String KEY_SUPERSTATE = "superState";
	private static final String TAG = "SlideMenu";

	// a simple adapter
	private static class SlideMenuAdapter extends ArrayAdapter<SlideMenuItem>
	{
		Activity activity;
		SlideMenuItem[] items;
		Typeface itemFont;

		class MenuItemHolder
		{
			public TextView label;
			public ImageView icon;
		}

		public SlideMenuAdapter(Activity activity, SlideMenuItem[] items, Typeface itemFont)
		{
			super(activity, R.id.menu_label, items);
			this.activity = activity;
			this.items = items;
			this.itemFont = itemFont;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View rowView = convertView;
			if (rowView == null)
			{
				LayoutInflater inflater = activity.getLayoutInflater();
				rowView = inflater.inflate(R.layout.slidemenu_listitem, null);
				MenuItemHolder viewHolder = new MenuItemHolder();
				viewHolder.label = (TextView) rowView.findViewById(R.id.menu_label);
				if (itemFont != null) viewHolder.label.setTypeface(itemFont);
				viewHolder.icon = (ImageView) rowView.findViewById(R.id.menu_icon);
				rowView.setTag(viewHolder);
			}

			MenuItemHolder holder = (MenuItemHolder) rowView.getTag();
			String s = items[position].label;
			holder.label.setText(s);
			holder.icon.setImageDrawable(items[position].icon);

			return rowView;
		}
	}

	private static class SlideMenuNotifAdapter extends ArrayAdapter<SlideMenuNotifItem>
	{
		Activity activity;
		SlideMenuNotifItem[] items;
		Typeface itemFont;

		class MenuItemNotifHolder
		{
			public TextView label;
		}

		public SlideMenuNotifAdapter(Activity activity, SlideMenuNotifItem[] items,
				Typeface itemFont)
		{
			super(activity, R.id.menu_notif_label, items);
			this.activity = activity;
			this.items = items;
			this.itemFont = itemFont;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View rowView = convertView;
			if (rowView == null)
			{
				LayoutInflater inflater = activity.getLayoutInflater();
				rowView = inflater.inflate(R.layout.slidemenu_listnotificationitem, null);
				MenuItemNotifHolder viewHolder = new MenuItemNotifHolder();
				viewHolder.label = (TextView) rowView.findViewById(R.id.menu_notif_label);
				if (itemFont != null) viewHolder.label.setTypeface(itemFont);

				// viewHolder.icon = (ImageView)
				// rowView.findViewById(R.id.menu_notif_icon);
				rowView.setTag(viewHolder);
			}

			MenuItemNotifHolder holder = (MenuItemNotifHolder) rowView.getTag();
			String s = items[position].label;
			holder.label.setText(s);
			// holder.icon.setImageDrawable(items[position].icon);

			Log.i(TAG, items[position].toString());

			if (items[position].IsRead)
			{
				Log.i(TAG, "is read true");
				holder.label.setTextColor(activity.getResources().getColor(
						color.notificationreadcolor));
			}
			else
			{
				Log.i(TAG, "is read false");
				holder.label.setTextColor(activity.getResources().getColor(
						color.notificationunreadcolor));
			}

			return rowView;
		}
	}

	private static boolean menuShown = false;
	private int statusHeight = -1;
	private static View menu;
	private static ViewGroup content;
	private static FrameLayout parent;
	private static int menuSize;
	private Activity activity;
	private Drawable headerImage;
	private String headerText = null;
	private Typeface font;
	private TranslateAnimation slideRightAnim;
	private TranslateAnimation slideMenuLeftAnim;
	private TranslateAnimation slideContentLeftAnim;

	private ArrayList<SlideMenuItem> menuItemList;
	private ArrayList<SlideMenuNotifItem> menuNotifList;
	private SlideMenuInterface.OnSlideMenuItemClickListener callback;

	/**
	 * Constructor used by the inflation apparatus. To be able to use the
	 * SlideMenu, call the {@link #init init()} method.
	 * 
	 * @param context
	 */
	public SlideMenu(Context context)
	{
		super(context);
	}

	/**
	 * Constructor used by the inflation apparatus. To be able to use the
	 * SlideMenu, call the {@link #init init()} method.
	 * 
	 * @param attrs
	 */
	public SlideMenu(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	/**
	 * Constructs a SlideMenu with the given menu XML.
	 * 
	 * @param activity
	 *            The calling activity.
	 * @param menuResource
	 *            Menu resource identifier.
	 * @param cb
	 *            Callback to be invoked on menu item click.
	 * @param slideDuration
	 *            Slide in/out duration in milliseconds.
	 */
	public SlideMenu(Activity activity, int menuResource,
			SlideMenuInterface.OnSlideMenuItemClickListener cb, int slideDuration)
	{
		super(activity);
		init(activity, menuResource, cb, slideDuration);
	}

	/**
	 * Constructs an empty SlideMenu.
	 * 
	 * @param activity
	 *            The calling activity.
	 * @param cb
	 *            Callback to be invoked on menu item click.
	 * @param slideDuration
	 *            Slide in/out duration in milliseconds.
	 */
	public SlideMenu(Activity activity, SlideMenuInterface.OnSlideMenuItemClickListener cb,
			int slideDuration)
	{
		this(activity, 0, cb, slideDuration);
	}

	/**
	 * If inflated from XML, initializes the SlideMenu.
	 * 
	 * @param activity
	 *            The calling activity.
	 * @param menuResource
	 *            Menu resource identifier, can be 0 for an empty SlideMenu.
	 * @param cb
	 *            Callback to be invoked on menu item click.
	 * @param slideDuration
	 *            Slide in/out duration in milliseconds.
	 */
	public void init(Activity activity, int menuResource,
			SlideMenuInterface.OnSlideMenuItemClickListener cb, int slideDuration)
	{

		this.activity = activity;
		this.callback = cb;

		// set size
		menuSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, activity
				.getResources().getDisplayMetrics());

		// create animations accordingly
		slideRightAnim = new TranslateAnimation(-menuSize, 0, 0, 0);
		slideRightAnim.setDuration(slideDuration);
		slideRightAnim.setFillAfter(true);
		slideMenuLeftAnim = new TranslateAnimation(0, -menuSize, 0, 0);
		slideMenuLeftAnim.setDuration(slideDuration * 3 / 2);
		slideMenuLeftAnim.setFillAfter(true);
		slideContentLeftAnim = new TranslateAnimation(menuSize, 0, 0, 0);
		slideContentLeftAnim.setDuration(slideDuration * 3 / 2);
		slideContentLeftAnim.setFillAfter(true);

		// and get our menu
		parseXml(menuResource);

		menuNotifList = new ArrayList<SlideMenuNotifItem>();
	}

	/**
	 * Sets an optional image to be displayed on top of the menu.
	 * 
	 * @param d
	 */
	public void setHeader(Drawable icon, String label)
	{
		headerImage = icon;
		headerText = label;
	}

	/**
	 * Optionally sets the font for the menu items.
	 * 
	 * @param f
	 *            A font.
	 */
	public void setFont(Typeface f)
	{
		font = f;
	}

	/**
	 * Dynamically adds a menu item.
	 * 
	 * @param item
	 */
	public void addMenuItem(SlideMenuItem item)
	{
		menuItemList.add(item);
	}

	public void addMenuNotifItem(SlideMenuNotifItem item)
	{
		menuNotifList.add(item);
	}

	/**
	 * Empties the SlideMenu.
	 */
	public void clearMenuItems()
	{
		menuItemList.clear();
	}

	public void clearMenuNotifItems()
	{
		menuNotifList.clear();
	}

	/**
	 * Slide the menu in.
	 */
	public void show()
	{
		this.show(true);
	}

	/**
	 * Set the menu to shown status without displaying any slide animation.
	 */
	public void setAsShown()
	{
		this.show(false);
	}

	private void show(boolean animate)
	{

		/*
		 * We have to adopt to status bar height in most cases, but not if there
		 * is a support actionbar!
		 */
		try
		{

			Method getSupportActionBar = activity.getClass().getMethod("getSupportActionBar",
					(Class[]) null);
			Object sab = getSupportActionBar.invoke(activity, (Object[]) null);
			sab.toString(); // check for null

			if (android.os.Build.VERSION.SDK_INT >= 11)
			{
				// over api level 11? add the margin
				getStatusbarHeight();
			}
		}
		catch (Exception es)
		{
			// there is no support action bar!
			Log.e(TAG, es.toString(), es);
			getStatusbarHeight();

		}

		// modify content layout params
		try
		{
			content = ((LinearLayout) activity.findViewById(android.R.id.content).getParent());
		}
		catch (ClassCastException e)
		{
			/*
			 * When there is no title bar
			 * (android:theme="@android:style/Theme.NoTitleBar"), the
			 * android.R.id.content FrameLayout is directly attached to the
			 * DecorView, without the intermediate LinearLayout that holds the
			 * titlebar plus content.
			 */
			if (Build.VERSION.SDK_INT < 18) content = (ViewGroup) activity
					.findViewById(android.R.id.content);
			else content = (ViewGroup) activity.findViewById(android.R.id.content).getParent(); // FIXME?
			// what
			// about
			// the
			// corner
			// cases
			// (fullscreen
			// etc)
		}

		FrameLayout.LayoutParams parm = new FrameLayout.LayoutParams(-1, -1, 3);
		parm.setMargins(menuSize, 0, -menuSize, 0);
		content.setLayoutParams(parm);

		// animation for smooth slide-out
		if (animate) content.startAnimation(slideRightAnim);

		// quirk for sony xperia devices on ICS only, shouldn't hurt on others
		if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT <= 15
				&& Build.MANUFACTURER.contains("Sony") && menuShown) content.setX(menuSize);

		// add the slide menu to parent
		// parent = (FrameLayout) content.getParent();
		try
		{
			parent = (FrameLayout) content.getParent();
		}
		catch (ClassCastException e)
		{
			/*
			 * Most probably a LinearLayout, at least on Galaxy S3.
			 * https://github.com/bk138/LibSlideMenu/issues/12
			 */
			LinearLayout realParent = (LinearLayout) content.getParent();
			parent = new FrameLayout(activity);
			realParent.addView(parent, 0); // add FrameLayout to real parent of
											// content
			realParent.removeView(content); // remove content from real parent
			parent.addView(content); // add content to FrameLayout
		}

		LayoutInflater inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		menu = inflater.inflate(R.layout.slidemenu, null);

		FrameLayout.LayoutParams lays = new FrameLayout.LayoutParams(-1, -1, 3);
		lays.setMargins(0, statusHeight, 0, 0);
		menu.setLayoutParams(lays);

		parent.addView(menu);

		// set header
		try
		{
			// if(headerImage != null){
			// ImageView headerIV = (ImageView)
			// activity.findViewById(R.id.menu_header_image);
			// headerIV.setImageDrawable(headerImage);
			// }
			// if(headerText != null && headerText.length() > 0){
			// TextView headerT = (TextView)
			// activity.findViewById(R.id.menu_header_text);
			// headerT.setText(headerText);
			// }
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString(), e);
			// not found
		}

		// connect the menu's listview
		ListView list = (ListView) activity.findViewById(R.id.cams_menu_listItems);
		SlideMenuItem[] items = menuItemList.toArray(new SlideMenuItem[menuItemList.size()]);
		SlideMenuAdapter adap = new SlideMenuAdapter(activity, items, font);
		list.setAdapter(adap);
		list.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{

				if (callback != null) callback.onSlideMenuItemClick(menuItemList.get(position).id);

				hide();
			}
		});

		// slide menu in
		if (animate) menu.startAnimation(slideRightAnim);

		menu.findViewById(R.id.overlay).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v)
			{
				hide();
			}
		});
		enableDisableViewGroup(content, false);

		menuShown = true;

		// Disabled to hide events.
		// showNotifications();

		new RefreshNotificationsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");

		((ImageView) SlideMenu.this.activity.findViewById(R.id.slidemenu_notifLoadingrefresh))
				.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v)
					{
						// TODO Auto-generated method stub
						ShowProgressSpinner();
						new RefreshNotificationsTask().executeOnExecutor(
								AsyncTask.THREAD_POOL_EXECUTOR, "");
					}
				});
	}

	public void showNotifications()
	{
		try
		{
			// connect the menu's listview
			// menuNotifList.add(new
			// SlideMenuNotifItem(449,"item  label",1,true));

			TextView noNotif = (TextView) activity.findViewById(R.id.slidemenu_NoNotif);
			if (menuNotifList.size() > 0)
			{
				noNotif.setVisibility(View.GONE);
			}
			else
			{
				// Disabled to hide events.
				// noNotif.setVisibility(View.VISIBLE);
				return;
			}

			ListView list = (ListView) activity.findViewById(R.id.cams_menu_ListNotif);
			// list.removeAllViewsInLayout();
			SlideMenuNotifItem[] items = menuNotifList.toArray(new SlideMenuNotifItem[menuNotifList
					.size()]);
			SlideMenuNotifAdapter adap = new SlideMenuNotifAdapter(activity, items, font);
			list.setAdapter(adap);
			list.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{

					if (callback != null) callback.onSlideMenuItemClick(menuNotifList.get(position).id);

					hide();
				}
			});
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString(), e);
		}
	}

	private class RefreshNotificationsTask extends AsyncTask<String, String, String>
	{

		@Override
		protected String doInBackground(String... usernames)
		{
			String message = "";

			try
			{
				Log.i(TAG, "Started Task RefreshNotificationsTask");
				this.publishProgress("");
				io.evercam.androidapp.dal.DbNotifcation helper = new DbNotifcation(
						SlideMenu.this.getContext());

				List<CameraNotification> notiflist = helper.getAllCameraNotificationsForEmailID(
						AppData.defaultUser.getEmail(), 100);
				for (CameraNotification notif : notiflist)
				{
					Log.i(TAG, notif.toString());
					SlideMenuNotifItem item = new SlideMenuNotifItem(notif.getID(),
							notif.getAlertMessage(), notif.getAlertTypeID(), notif.getIsRead());
					menuNotifList.add(item);
				}

			}
			catch (Exception e)
			{
				Log.i(TAG, e.toString() + "::" + Log.getStackTraceString(e));
				message = e.toString();
			}
			return message;
		}

		@Override
		protected void onProgressUpdate(String... status)
		{
			try
			{
				ShowProgressSpinner();
				Log.i(TAG, "RefreshNotificationsTask - starting the progress bar");
			}
			catch (Exception e)
			{
				Log.e(TAG, e.toString(), e);
			}
		};

		@Override
		protected void onPostExecute(String result)
		{
			try
			{
				showNotifications();
				HideProgressSpinner();
			}
			catch (Exception e)
			{
				Log.e(TAG, e.toString(), e);
			}
		}

	}

	private void ShowProgressSpinner()
	{
		try
		{
			SlideMenu.this.activity.findViewById(R.id.slidemenu_notifLoadingProgress)
					.setVisibility(View.VISIBLE);
			SlideMenu.this.activity.findViewById(R.id.slidemenu_notifLoadingrefresh).setVisibility(
					View.GONE);
		}
		catch (Exception e)
		{
		}

	}

	private void HideProgressSpinner()
	{
		new AsyncTask<String, String, String>(){
			@Override
			protected String doInBackground(String... params)
			{
				// TODO Auto-generated method stub
				try
				{
					synchronized (this)
					{
						this.wait(500);
					}
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(String result)
			{
				try
				{
					// TODO Auto-generated method stub
					SlideMenu.this.activity.findViewById(R.id.slidemenu_notifLoadingProgress)
							.setVisibility(View.GONE);
					SlideMenu.this.activity.findViewById(R.id.slidemenu_notifLoadingrefresh)
							.setVisibility(View.VISIBLE);
				}
				catch (Exception e)
				{
				}
			}

		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");

	}

	/**
	 * Slide the menu out.
	 */
	public void hide()
	{
		menu.startAnimation(slideMenuLeftAnim);
		parent.removeView(menu);

		content.startAnimation(slideContentLeftAnim);

		FrameLayout.LayoutParams parm = (FrameLayout.LayoutParams) content.getLayoutParams();
		parm.setMargins(0, 0, 0, 0);
		content.setLayoutParams(parm);
		enableDisableViewGroup(content, true);

		menuShown = false;
	}

	private void getStatusbarHeight()
	{
		// Only do this if not already set.
		// Especially when called from within onCreate(), this does not return
		// the true values.
		if (statusHeight == -1)
		{
			Rect r = new Rect();
			Window window = activity.getWindow();
			window.getDecorView().getWindowVisibleDisplayFrame(r);
			statusHeight = r.top;
		}
	}

	// originally:
	// http://stackoverflow.com/questions/5418510/disable-the-touch-events-for-all-the-views
	// modified for the needs here
	private void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled)
	{
		int childCount = viewGroup.getChildCount();
		for (int i = 0; i < childCount; i++)
		{
			View view = viewGroup.getChildAt(i);
			if (view.isFocusable()) view.setEnabled(enabled);
			if (view instanceof ViewGroup)
			{
				enableDisableViewGroup((ViewGroup) view, enabled);
			}
			else if (view instanceof ListView)
			{
				if (view.isFocusable()) view.setEnabled(enabled);
				ListView listView = (ListView) view;
				int listChildCount = listView.getChildCount();
				for (int j = 0; j < listChildCount; j++)
				{
					if (view.isFocusable()) listView.getChildAt(j).setEnabled(false);
				}
			}
		}
	}

	// originally: https://github.com/darvds/RibbonMenu
	// credit where credits due!
	private void parseXml(int menu)
	{

		menuItemList = new ArrayList<SlideMenuItem>();

		try
		{
			XmlResourceParser xpp = activity.getResources().getXml(menu);

			xpp.next();
			int eventType = xpp.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT)
			{

				if (eventType == XmlPullParser.START_TAG)
				{

					String elemName = xpp.getName();

					if (elemName.equals("item"))
					{

						String textId = xpp.getAttributeValue(
								"http://schemas.android.com/apk/res/android", "title");
						String iconId = xpp.getAttributeValue(
								"http://schemas.android.com/apk/res/android", "icon");
						String resId = xpp.getAttributeValue(
								"http://schemas.android.com/apk/res/android", "id");

						SlideMenuItem item = new SlideMenuItem();
						item.id = Integer.valueOf(resId.replace("@", ""));
						item.icon = activity.getResources().getDrawable(
								Integer.valueOf(iconId.replace("@", "")));
						item.label = resourceIdToString(textId);

						menuItemList.add(item);
					}

				}

				eventType = xpp.next();

			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private String resourceIdToString(String text)
	{
		if (!text.contains("@"))
		{
			return text;
		}
		else
		{
			String id = text.replace("@", "");
			return activity.getResources().getString(Integer.valueOf(id));

		}
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state)
	{
		try
		{

			if (state instanceof Bundle)
			{
				Bundle bundle = (Bundle) state;

				statusHeight = bundle.getInt(KEY_STATUSBARHEIGHT);

				if (bundle.getBoolean(KEY_MENUSHOWN)) show(false); // show
																	// without
																	// animation

				super.onRestoreInstanceState(bundle.getParcelable(KEY_SUPERSTATE));

				return;
			}

			super.onRestoreInstanceState(state);

		}
		catch (NullPointerException e)
		{
			// in case the menu was not declared via XML but added from code
		}
	}

	@Override
	protected Parcelable onSaveInstanceState()
	{
		Bundle bundle = new Bundle();
		bundle.putParcelable(KEY_SUPERSTATE, super.onSaveInstanceState());
		bundle.putBoolean(KEY_MENUSHOWN, menuShown);
		bundle.putInt(KEY_STATUSBARHEIGHT, statusHeight);

		return bundle;
	}

}