package com.maxMustermannGeheim.linkcollection.Utilities;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.maxMustermannGeheim.linkcollection.R;

public class MinDimensionLayout extends LinearLayout {

    enum TARGET_MODE {
        MAX, MIN
    }

    private TARGET_MODE targetMode = TARGET_MODE.MAX;
    private int minDimension;

    //  ------------------------- Getter & Setter ------------------------->
    public TARGET_MODE getTargetMode() {
        return targetMode;
    }

    public MinDimensionLayout setTargetMode(TARGET_MODE targetMode) {
        this.targetMode = targetMode;
        return this;
    }

    public int getMinDimension() {
        return minDimension;
    }

    public MinDimensionLayout setMinDimension(int minDimension) {
        this.minDimension = minDimension;
        return this;
    }
    //  <------------------------- Getter & Setter -------------------------

    public MinDimensionLayout(Context context) {
        super(context);
    }

    public MinDimensionLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MinDimensionLayout);
        readAttributes(array);
        array.recycle();
    }

    public MinDimensionLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AspectRatioLayout);
        readAttributes(array);
        array.recycle();
    }

    public MinDimensionLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AspectRatioLayout, defStyleAttr, defStyleRes);
        readAttributes(array);
        array.recycle();
    }

    private void readAttributes(TypedArray array) {
        minDimension = array.getDimensionPixelSize(R.styleable.MinDimensionLayout_minDimension, 1);

        switch (array.getInt(R.styleable.MinDimensionLayout_minMode, 1)) {
            case 0:
                targetMode = TARGET_MODE.MAX;
                break;
            case 1:
                targetMode = TARGET_MODE.MIN;
                break;
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // Iterate through all children, measuring them and computing our dimensions
// from their size.
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChildWithMargins(child, 0, 0, 0, 0);
            }
        }

        super.onMeasure(0, 0);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        double aspectRatio = height == 0 ? 0 : width / (double) height;
        int width_result;
        int height_result;

        switch (targetMode) {
            default:
            case MIN:
                if (Math.min(width, height) >= minDimension) {
                    width_result = width;
                    height_result = height;
                } else if (width < height) {
                    width_result = minDimension;
                    height_result = (int) (minDimension * aspectRatio);
                } else {
                    height_result = minDimension;
                    width_result = (int) (minDimension * aspectRatio);
                }
                break;
            case MAX:
                if (Math.max(width, height) >= minDimension) {
                    width_result = width;
                    height_result = height;
                } else if (width > height) {
                    width_result = minDimension;
                    height_result = (int) (minDimension * aspectRatio);
                } else {
                    height_result = minDimension;
                    width_result = (int) (minDimension / aspectRatio);
                }
                break;
        }

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(width_result, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height_result, MeasureSpec.EXACTLY));
    }



}