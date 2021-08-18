package com.maxMustermannGeheim.linkcollection.Utilities.CustomAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.finn.androidUtilities.CustomUtility;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomAutoCompleteAdapter extends ArrayAdapter<ImageAdapterItem> {
    private List<ImageAdapterItem> listFull;
    private CharSequence searchQuery = "";
    private boolean imagesEnabled = true;


    public CustomAutoCompleteAdapter(@NonNull Context context, @NonNull List<ImageAdapterItem> objects) {
        super(context, 0, objects);
        listFull = new ArrayList<>(objects);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return filter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_dropdown_image, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.listItem_dropDownImage_text);
        ImageView imageView = convertView.findViewById(R.id.listItem_dropDownImage_image);

        ImageAdapterItem item = getItem(position);

        if (item != null) {
            textView.setText(Helpers.SpannableStringHelper.highlightText(searchQuery.toString(), item.getText()));

            if (imagesEnabled) {
                if (!CustomUtility.stringExists(item.getImagePath()))
                    imageView.setVisibility(View.INVISIBLE);
                else {
                    imageView.setVisibility(View.VISIBLE);

                    Glide
                            .with(getContext())
                            .load("https://image.tmdb.org/t/p/w92/" + item.getImagePath())
//                        .centerCrop()
                            .placeholder(R.drawable.ic_download)
                            .into(imageView);
                }
            } else
                imageView.setVisibility(View.GONE);

//            if (item.getImage() == null)
//                imageView.setImageResource(R.drawable.ic_download);
//            else
//                imageView.setImageBitmap(item.getImage());
        }

        return convertView;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            searchQuery = constraint;
            FilterResults results = new FilterResults();
            List<ImageAdapterItem> suggestions = new ArrayList<>();

            if (constraint == null || constraint.length() == 0)
                suggestions.addAll(listFull);
            else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                suggestions.addAll(listFull.stream().filter(imageAdapterItem -> {
                    if (imageAdapterItem.getText().toLowerCase().contains(filterPattern))
                        return true;
                    if (Utility.containsIgnoreCase(imageAdapterItem.getAlias(), filterPattern))
                        return true;
                    return false;
                }).collect(Collectors.toList()));
            }

            results.values = suggestions;
            results.count = suggestions.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((ImageAdapterItem) resultValue).getText();
        }
    };
}
