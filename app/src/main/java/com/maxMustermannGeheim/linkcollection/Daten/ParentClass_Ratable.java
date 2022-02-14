package com.maxMustermannGeheim.linkcollection.Daten;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.finn.androidUtilities.CustomUtility;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomPopupWindow;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;

public class ParentClass_Ratable extends ParentClass {
    private Float rating = -1f;
    private Integer ratingTendency;


    /**  <------------------------- Getter & Setter -------------------------  */
    public Float getRating() {
        return rating;
    }

    public ParentClass_Ratable setRating(Float rating) {
        this.rating = rating;
        return this;
    }

    public Integer getRatingTendency() {
        return ratingTendency;
    }

    public ParentClass_Ratable setRatingTendency(Integer ratingTendency) {
        this.ratingTendency = ratingTendency;
        return this;
    }
    /**  ------------------------- Getter & Setter ------------------------->  */

    public boolean hasRating() {
        return !(rating == -1 || rating == 0);
    }

    public boolean hasRatingTendency() {
        return ratingTendency != null && hasRating();
    }

    public static void showRatingDialog(Context context, ParentClass_Ratable ratable, View anchor, boolean showOldRating, @Nullable Runnable runnable){
        Helpers.RatingHelper ratingHelper = Helpers.RatingHelper.inflate(context);
        if (showOldRating)
            ratingHelper.setRating(ratable.getRating());
        FrameLayout frameLayout = ratingHelper.getLayout();
        frameLayout.setBackground(context.getDrawable(R.drawable.tile_background));
        CustomUtility.setMargins(ratingHelper.getRatingBar(), -1, 10);
        CustomPopupWindow customPopupWindow = CustomPopupWindow.Builder(anchor, frameLayout).setPositionRelativeToAnchor(CustomPopupWindow.POSITION_RELATIVE_TO_ANCHOR.CENTER_VERTICAL).show();

        ratingHelper.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
            ratable.setRating(rating);
            customPopupWindow.dismiss();
            if (runnable != null)
                runnable.run();
        });
    }

    public static void showRatingTendencyDialog(Context context, ParentClass_Ratable ratable, View anchor, @Nullable CustomUtility.GenericInterface<ParentClass_Ratable> onSelected) {
        ViewGroup layout = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.popup_rating_tendency, null);

        View downButton = layout.findViewById(R.id.popup_ratingTendency_down);
        View removeButton = layout.findViewById(R.id.popup_ratingTendency_remove);
        View upButton = layout.findViewById(R.id.popup_ratingTendency_up);

        removeButton.setVisibility(ratable.hasRatingTendency() ? View.VISIBLE : View.GONE);

        CustomPopupWindow customPopupWindow = CustomPopupWindow.Builder(anchor, layout).setPositionRelativeToAnchor(CustomPopupWindow.POSITION_RELATIVE_TO_ANCHOR.TOP_RIGHT).show();

        downButton.setOnClickListener(v -> {
            ratable.setRatingTendency(-1);
            Toast.makeText(context, "Negative Tendenz", Toast.LENGTH_SHORT).show();
            customPopupWindow.dismiss();
            CustomUtility.runGenericInterface(onSelected, ratable);
        });

        removeButton.setOnClickListener(v -> {
            ratable.setRatingTendency(null);
            Toast.makeText(context, "Tendenz Entfernt", Toast.LENGTH_SHORT).show();
            customPopupWindow.dismiss();
            CustomUtility.runGenericInterface(onSelected, ratable);
        });

        upButton.setOnClickListener(v -> {
            ratable.setRatingTendency(1);
            Toast.makeText(context, "Positive Tendenz", Toast.LENGTH_SHORT).show();
            customPopupWindow.dismiss();
            CustomUtility.runGenericInterface(onSelected, ratable);
        });
    }

    public static void applyRatingTendencyIndicator(ImageView imageView, ParentClass_Ratable ratable) {
        Context context = imageView.getContext();
        if (ratable.hasRatingTendency()) {
            imageView.setVisibility(View.VISIBLE);
            if (ratable.getRatingTendency() > 0) {
                imageView.setImageResource(R.drawable.ic_arrow_up);
                imageView.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.colorGreen)));
            } else {
                imageView.setImageResource(R.drawable.ic_arrow_down);
                imageView.setImageTintList(ColorStateList.valueOf(Color.RED));
            }
        } else
            imageView.setVisibility(View.GONE);
    }
}
