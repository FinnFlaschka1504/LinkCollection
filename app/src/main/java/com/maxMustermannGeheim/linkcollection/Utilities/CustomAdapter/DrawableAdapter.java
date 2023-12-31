package com.maxMustermannGeheim.linkcollection.Utilities.CustomAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.maxMustermannGeheim.linkcollection.R;

import java.util.List;

class SpinnerAdapter extends ArrayAdapter<Integer> {

    Context context;
    List<Integer> drawableList;

    public SpinnerAdapter(Context context, List<Integer> states) {
        super(context, R.layout.list_item_dropdown_image, states);
        this.context = context;
        this.drawableList = states;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.list_item_dropdown_image, parent, false);

        ImageView img = row.findViewById(R.id.listItem_dropDownImage_image);

        img.setBackgroundResource(drawableList.get(position));

        return row;
    }

}