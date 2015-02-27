package io.evercam.androidapp.custom;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import io.evercam.androidapp.R;
import io.evercam.androidapp.dto.AppUser;

public class CustomAdapter extends ArrayAdapter<AppUser>
{
    private ArrayList<AppUser> appUsers;
    private Activity activity;
    private int itemLayoutId = 0;
    private int emailViewId = 0;
    private int newItemLayoutid = 0;

    AppUser fakeUser = null;

    public CustomAdapter(Activity activity, int itemLayoutId, int newItemLayoutResource,
                         int itemTextViewIdToDislayNameOfUser, ArrayList<AppUser> appUsers)
    {
        super(activity, itemLayoutId, itemTextViewIdToDislayNameOfUser, appUsers);

        this.appUsers = appUsers;

        if(this.appUsers != null && this.appUsers.size() > 0 && this.appUsers.get(this.appUsers
                .size() - 1).getId() == -1)
        {
            fakeUser = this.appUsers.get(this.appUsers.size() - 1);
        }
        else
        {
            this.fakeUser = new AppUser();
            fakeUser.setId(-1);
            this.appUsers.add(fakeUser); // add at the end
        }
        this.activity = activity;
        this.itemLayoutId = itemLayoutId;
        emailViewId = itemTextViewIdToDislayNameOfUser;
        newItemLayoutid = newItemLayoutResource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = convertView;
        final AppUser appUser = appUsers.get(position);

        if(view == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);
            view = ((appUser != fakeUser) ? layoutInflater.inflate(itemLayoutId,
                    null) : layoutInflater.inflate(newItemLayoutid, null));
        }

        if(appUser != null && appUser != fakeUser)
        {
            ((TextView) view.findViewById(emailViewId)).setText(appUser.getEmail());
            TextView usernameTextView = (TextView) view.findViewById(R.id.account_item_username);
            usernameTextView.setText(appUser.getUsername() + (appUser.getIsDefault() ? " - " +
                    "Default" : ""));
        }

        return view;
    }

}