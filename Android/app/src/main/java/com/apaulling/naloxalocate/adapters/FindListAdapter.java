package com.apaulling.naloxalocate.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.apaulling.naloxalocate.R;

import java.util.List;

/**
 * Created by psdco on 11/12/2016.
 */

public class FindListAdapter extends ArrayAdapter<NearbyUser> {

    public FindListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public FindListAdapter(Context context, int resource, List<NearbyUser> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = View.inflate(getContext(), R.layout.activity_find_list_item, null);
        }

        NearbyUser user = getItem(position);
        String distance = String.format("%.2f km", user.getDistance());

        // Set an index number
        ((TextView) view.findViewById(R.id.index_number_text)).setText((position + 1) + ". ");
        // Add the distance
        ((TextView) view.findViewById(R.id.distance_text)).setText(distance);

        return view;
    }
}
