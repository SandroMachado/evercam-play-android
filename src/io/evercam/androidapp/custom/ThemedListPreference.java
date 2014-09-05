package io.evercam.androidapp.custom;

import io.evercam.androidapp.R;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ThemedListPreference extends ListPreference implements OnItemClickListener {

    public static final String TAG = "ThemedListPreference";

    private int mClickedDialogEntryIndex;

    private CharSequence mDialogTitle;

    public ThemedListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThemedListPreference(Context context) {
        super(context);
    }

    @Override
    protected View onCreateDialogView() {
        // inflate custom layout with custom title & listview
        View view = View.inflate(getContext(), R.layout.custom_list_preference, null);

        mDialogTitle = getDialogTitle();
        if(mDialogTitle == null) mDialogTitle = getTitle();
        TextView titleTextView = ((TextView) view.findViewById(R.id.dialog_title));
        titleTextView.setText(mDialogTitle);

        ListView list = (ListView) view.findViewById(android.R.id.list);
        // note the layout we're providing for the ListView entries
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                getContext(), R.layout.list_radio_button,
                getEntries());

        list.setAdapter(adapter);
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list.setItemChecked(findIndexOfValue(getValue()), true);
        list.setOnItemClickListener(this);

        return view;
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        mClickedDialogEntryIndex = position;
        ThemedListPreference.this.onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
        getDialog().dismiss();
    }

    @Override
	protected void onPrepareDialogBuilder(Builder builder)
	{
      // adapted from ListPreference
      if (getEntries() == null || getEntryValues() == null) {
          // throws exception
          super.onPrepareDialogBuilder(builder);
          return;
      }

      mClickedDialogEntryIndex = findIndexOfValue(getValue());

      // .setTitle(null) to prevent default (blue)
      // title+divider from showing up
      builder.setTitle(null);

      builder.setPositiveButton(null, null);
	}

	@Override
    protected void onDialogClosed(boolean positiveResult) {
            // adapted from ListPreference
        super.onDialogClosed(positiveResult);

        if (positiveResult && mClickedDialogEntryIndex >= 0
                && getEntryValues() != null) {
            String value = getEntryValues()[mClickedDialogEntryIndex]
                    .toString();
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
    }
}
