package com.maxMustermannGeheim.linkcollection.Daten;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.finn.androidUtilities.CustomRecycler;
import com.finn.androidUtilities.CustomUtility;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomPopupWindow;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;

public class ParentClass_Ratable extends ParentClass {
    private Float rating = -1f;

    public Float getRating() {
        return rating;
    }

    public ParentClass_Ratable setRating(Float rating) {
        this.rating = rating;
        return this;
    }

    public boolean hasRating() {
        return !(rating == -1 || rating == 0);
    }

    public interface ShowRatingDialog<Object extends ParentClass_Ratable, Anchor extends View, Recycler extends CustomRecycler> {
        void runShowRatingDialog(Object object, Anchor anchor, Recycler recycler);
    }

    public static void showRatingDialog(Context context, ParentClass_Ratable object, View anchor, boolean showOldRating, @Nullable CustomRecycler customRecycler, @Nullable Runnable runnable){
        Helpers.RatingHelper ratingHelper = Helpers.RatingHelper.inflate(context);
        if (showOldRating)
            ratingHelper.setRating(object.getRating());
        FrameLayout frameLayout = ratingHelper.getLayout();
        frameLayout.setBackground(context.getDrawable(R.drawable.tile_background));
        CustomUtility.setMargins(ratingHelper.getRatingBar(), -1, 10);
        CustomPopupWindow customPopupWindow = CustomPopupWindow.Builder(anchor, frameLayout).setPositionRelativeToAnchor(CustomPopupWindow.POSITION_RELATIVE_TO_ANCHOR.CENTER_VERTICAL).show();

        ratingHelper.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
            object.setRating(rating);
            if (runnable != null)
                runnable.run();
            if (customRecycler != null)
                customRecycler.reload();
            customPopupWindow.dismiss();
        });

    }
}
