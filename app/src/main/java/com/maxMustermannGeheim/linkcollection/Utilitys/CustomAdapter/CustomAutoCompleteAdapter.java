package com.maxMustermannGeheim.linkcollection.Utilitys.CustomAdapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
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
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.maxMustermannGeheim.linkcollection.R;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomAutoCompleteAdapter extends ArrayAdapter<ImageAdapterItem> {
    private List<ImageAdapterItem> listFull;


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
            textView.setText(item.getText());

            if (item.getImagePath() == null || item.getImagePath().isEmpty())
                imageView.setVisibility(View.INVISIBLE);
            else {
                imageView.setVisibility(View.VISIBLE);

                Glide
                        .with(getContext())
                        .load("https://image.tmdb.org/t/p/w92/" + item.getImagePath())
//                        .centerCrop()
                        .placeholder(R.drawable.ic_download)
                        .into(imageView);
//                Glide.with(getContext()).load("http://image.tmdb.org/t/p/w92/" + item.getImagePath()).listener(new RequestListener<Drawable>() {
//                    @Override
//                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                        item.setImage(null);
////                        autoCompleteAdapter.notifyDataSetChanged();
//                        return false;
//                    }
//                }).submit();

            }

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
            FilterResults results = new FilterResults();
            List<ImageAdapterItem> suggestions = new ArrayList<>();

            if (constraint == null || constraint.length() == 0)
                suggestions.addAll(listFull);
            else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                suggestions.addAll(listFull.stream().filter(imageAdapterItem -> imageAdapterItem.getText().toLowerCase().contains(filterPattern)).collect(Collectors.toList()));
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
