package io.evercam.androidapp.custom;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import io.evercam.androidapp.R;
import io.evercam.androidapp.dto.EvercamCamera;

/**
 * The array adapter that updates items in live view drop down list
 */
public class CameraListAdapter extends ArrayAdapter<String>
{
    private final String TAG = "CameraListAdapter";
    private ArrayList<EvercamCamera> cameraList;

    public CameraListAdapter(Context context, int resource, int textViewResourceId,
                             String[] objects, ArrayList<EvercamCamera> cameraList)
    {
        super(context, resource, textViewResourceId, objects);
        this.cameraList = cameraList;
    }

    /**
     * Remove offline icon for selected camera that is showing as title
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = super.getView(position, convertView, parent);
        ImageView offlineIcon = (ImageView) view.findViewById(R.id.spinner_offline_icon);
        offlineIcon.setVisibility(View.GONE);

        //Make spinner text fit landscape too
        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.list_spinner_layout);
        layout.setPadding(10, 0, 10, 0);

        return view;
    }

    /**
     * Only show offline icon for camera that is offline in dropdown list.
     */
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        View view = super.getDropDownView(position, convertView, parent);
        ImageView offlineIcon = (ImageView) view.findViewById(R.id.spinner_offline_icon);
        EvercamCamera evercamCamera = cameraList.get(position);

        if(evercamCamera.isOffline())
        {
            offlineIcon.setVisibility(View.VISIBLE);
        }
        else
        {
            offlineIcon.setVisibility(View.GONE);
        }

        return view;
    }
}
