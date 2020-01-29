package com.maxMustermannGeheim.linkcollection.Utilities;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerScrollToggle extends RecyclerView {
    private final CustomGridLayoutManager manager = new CustomGridLayoutManager(getContext());
    private boolean verticalScrollEnabled = true;


    public RecyclerScrollToggle(@NonNull Context context) {
        super(context);
    }

    public RecyclerScrollToggle(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerScrollToggle(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

//    //  ------------------------- Scroll ------------------------->
//    public boolean isVerticalScrollEnabled() {
//        return verticalScrollEnabled;
//    }
//
//    public RecyclerScrollToggle setVerticalScrollEnabled(boolean verticalScrollEnabled) {
//        this.verticalScrollEnabled = verticalScrollEnabled;
//        return this;
//    }
//
//    @Override
//    public boolean canScrollVertically(int direction) {
//        return isVerticalScrollEnabled() && super.canScrollVertically(direction);
//    }
//    //  <------------------------- Scroll -------------------------

    public class CustomGridLayoutManager extends LinearLayoutManager {
        private boolean isScrollEnabled = true;

        public CustomGridLayoutManager(Context context) {
            super(context);
        }

        public void setScrollEnabled(boolean flag) {
            this.isScrollEnabled = flag;
        }

        @Override
        public boolean canScrollVertically() {
            //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
            return isScrollEnabled && super.canScrollVertically();
        }
    }

    @Override
    public void setLayoutManager(@Nullable LayoutManager layout) {
        super.setLayoutManager(manager);
    }
}
