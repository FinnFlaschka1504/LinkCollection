package com.maxMustermannGeheim.linkcollection.Daten;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.finn.androidUtilities.CustomUtility;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomPopupWindow;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;

import java.util.Objects;
import java.util.Optional;

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

    public boolean hasRatingTendency(boolean ignoreRating) {
        return ratingTendency != null && (hasRating() || ignoreRating);
    }

    public Optional<Float> _getRatingWithTendency() {
        if (!hasRating())
            return Optional.empty();
        float tendency;
        if (ratingTendency == null)
            tendency = 0;
        else
            tendency = 0.0833f * ratingTendency;
        return Optional.of(rating + tendency);
    }

    public static void showRatingDialog(Context context, ParentClass_Ratable ratable, View anchor, boolean showOldRating, boolean showRatingTendency, @Nullable Runnable runnable){
        Helpers.RatingHelper ratingHelper = Helpers.RatingHelper.inflate(context);
        if (showOldRating)
            ratingHelper.setRating(ratable.getRating());
        ViewGroup frameLayout = ratingHelper.getLayout();
        frameLayout.setBackground(AppCompatResources.getDrawable(context, R.drawable.tile_background));
        CustomUtility.setMargins(ratingHelper.getRatingBar(), -1, 10);

        // ratingTendency
        if (showRatingTendency) {
            FrameLayout ratingTendencyLayout = frameLayout.findViewById(R.id.customRating_ratingTendencyLayout);
            FrameLayout inflate = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.popup_rating_tendency, ratingTendencyLayout);
            View linearLayout = ((ViewGroup) inflate.getChildAt(0)).getChildAt(0);
            CustomUtility.setMargins(linearLayout, 0);
            ParentClass_Ratable.showRatingTendencyDialog(context, ratable, null, (ViewGroup) inflate.getChildAt(0), null);
        }


        CustomPopupWindow customPopupWindow = CustomPopupWindow.Builder(anchor, frameLayout)
                .setPositionRelativeToAnchor(CustomPopupWindow.POSITION_RELATIVE_TO_ANCHOR.CENTER_VERTICAL)
                .setOnDismissListener(customPopupWindow1 -> {
                    if (!ratable.hasRating())
                        ratable.setRatingTendency(null);
                })
                .show();

        ratingHelper.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
            ratable.setRating(rating);
            customPopupWindow.dismiss();
            if (runnable != null)
                runnable.run();
        });
    }

    public static void showRatingTendencyDialog(Context context, ParentClass_Ratable ratable, @Nullable View anchor, @Nullable ViewGroup existingLayout, @Nullable CustomUtility.GenericInterface<ParentClass_Ratable> onSelected) {
        ViewGroup layout;
        if (existingLayout == null)
            layout = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.popup_rating_tendency, null);
        else
            layout = existingLayout;

        View downButton = layout.findViewById(R.id.popup_ratingTendency_down);
        View removeButton = layout.findViewById(R.id.popup_ratingTendency_remove);
        View upButton = layout.findViewById(R.id.popup_ratingTendency_up);
        View removeUp = layout.findViewById(R.id.popup_ratingTendency_remove_up);
        View removeDown = layout.findViewById(R.id.popup_ratingTendency_remove_down);

        Runnable setVisibilities = () -> {
            boolean downVisible = !Objects.equals(ratable.getRatingTendency(), -1);
            boolean upVisible = !Objects.equals(ratable.getRatingTendency(), 1);
            downButton.setVisibility(downVisible ? View.VISIBLE : View.GONE);

            removeButton.setVisibility(ratable.getRatingTendency() != null ? View.VISIBLE : View.GONE);
            removeUp.setVisibility(!upVisible ? View.VISIBLE : View.GONE);
            removeDown.setVisibility(!downVisible ? View.VISIBLE : View.GONE);

            upButton.setVisibility(upVisible ? View.VISIBLE : View.GONE);
            CustomUtility.setMargins(removeButton, -1, -1, upVisible ? 5 : 0, -1);
        };
        setVisibilities.run();


        CustomPopupWindow customPopupWindow;
        if (existingLayout == null)
            customPopupWindow = CustomPopupWindow.Builder(anchor, layout).setPositionRelativeToAnchor(CustomPopupWindow.POSITION_RELATIVE_TO_ANCHOR.CENTER_VERTICAL).show();
        else
            customPopupWindow = null;

        downButton.setOnClickListener(v -> {
            ratable.setRatingTendency(-1);
            Toast.makeText(context, "Negative Tendenz", Toast.LENGTH_SHORT).show();
            if (customPopupWindow != null)
                customPopupWindow.dismiss();
            else
                setVisibilities.run();
            CustomUtility.runGenericInterface(onSelected, ratable);
        });

        removeButton.setOnClickListener(v -> {
            ratable.setRatingTendency(null);
            Toast.makeText(context, "Tendenz Entfernt", Toast.LENGTH_SHORT).show();
            if (customPopupWindow != null)
                customPopupWindow.dismiss();
            else
                setVisibilities.run();
            CustomUtility.runGenericInterface(onSelected, ratable);
        });

        upButton.setOnClickListener(v -> {
            ratable.setRatingTendency(1);
            Toast.makeText(context, "Positive Tendenz", Toast.LENGTH_SHORT).show();
            if (customPopupWindow != null)
                customPopupWindow.dismiss();
            else
                setVisibilities.run();
            CustomUtility.runGenericInterface(onSelected, ratable);
        });
    }

    public static void applyRatingTendencyIndicator(ImageView imageView, ParentClass_Ratable ratable, boolean show, boolean showEditIfUnset) {
        Context context = imageView.getContext();
        if (ratable.hasRatingTendency(showEditIfUnset) && show) {
            imageView.setVisibility(View.VISIBLE);
            if (ratable.getRatingTendency() > 0) {
                imageView.setImageResource(R.drawable.ic_arrow_up);
                imageView.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.colorGreen)));
            } else {
                imageView.setImageResource(R.drawable.ic_arrow_down);
                imageView.setImageTintList(ColorStateList.valueOf(Color.RED));
            }
        } else {
            if (showEditIfUnset) {
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.drawable.ic_edit);
                imageView.setImageTintList(ContextCompat.getColorStateList(imageView.getContext(), R.color.clickable_text_color_normal));
            } else
                imageView.setVisibility(View.GONE);
        }
    }
}
