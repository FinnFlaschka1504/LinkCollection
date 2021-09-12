/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maxMustermannGeheim.linkcollection.Utilities;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroupOverlay;
import android.view.ViewOverlay;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finn.androidUtilities.CustomUtility;

import java.lang.reflect.Field;

import me.zhanghai.android.fastscroll.FastScroller;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;
import me.zhanghai.android.fastscroll.PopupTextProvider;
import me.zhanghai.android.fastscroll.Predicate;

public class FastScrollRecyclerViewHelper implements FastScroller.ViewHelper {
    private static final String TAG = "RecyclerViewHelper";

    @NonNull
    private final RecyclerView mView;
    @Nullable
    FastScroller[] mFastScroller;
    @Nullable
    private final PopupTextProvider mPopupTextProvider;

    @NonNull
    private final Rect mTempRect = new Rect();

    public FastScrollRecyclerViewHelper(@NonNull RecyclerView view, @Nullable PopupTextProvider popupTextProvider) {
        mView = view;
        mPopupTextProvider = popupTextProvider;
    }

    public FastScrollRecyclerViewHelper(@NonNull RecyclerView view, @Nullable FastScroller[] fastScroller, @Nullable PopupTextProvider popupTextProvider) {
        mView = view;
        mFastScroller = fastScroller;
        mPopupTextProvider = popupTextProvider;
    }

    @Override
    public void addOnPreDrawListener(@NonNull Runnable onPreDraw) {
        mView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                onPreDraw.run();
            }
        });
    }

    @Override
    public void addOnScrollChangedListener(@NonNull Runnable onScrollChanged) {
        mView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                onScrollChanged.run();
            }
        });
    }

    @Override
    public void addOnTouchEventListener(@NonNull Predicate<MotionEvent> onTouchEvent) {
//        try {
//            ViewGroupOverlay overlay = mView.getOverlay();
//            Field mOverlayViewGroup = ViewOverlay.class.getDeclaredField("mOverlayViewGroup");
//            mOverlayViewGroup.setAccessible(true);
//            Object overlayViewGroup = mOverlayViewGroup.get(overlay);
//            Field mChildren = overlayViewGroup.getClass().getDeclaredField("mChildren");
//            mChildren.setAccessible(true);
//            View[] children = (View[]) mChildren.get(overlayViewGroup);
//            View child = children[1];
//            String BREAKPOINT = null;
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//        }
        final View[] thumbView = {null};
        CustomUtility.GenericReturnOnlyInterface<View> tryGetThumbView = () -> {
            if (mFastScroller != null && mFastScroller[0] != null) {
                try {
                    Field mThumbView = FastScroller.class.getDeclaredField("mThumbView");
                    mThumbView.setAccessible(true);
                    thumbView[0] = (View) mThumbView.get(mFastScroller[0]);
                    return thumbView[0];
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    Log.e(TAG, "addOnTouchEventListener: ", e);
                }
            }
            return null;
        };

        final boolean[] blocked = {false};

        CustomUtility.GenericReturnInterface<MotionEvent, Boolean> processMotionEvent = event -> {
            if (thumbView[0] == null && (thumbView[0] = tryGetThumbView.run()) == null)
                return onTouchEvent.test(event);
            else {
                int[] ints = new int[]{0,0};
                thumbView[0].getLocationOnScreen(ints);
                float rawX = event.getX();
                float rawY = event.getY();
                boolean isInX = rawX >= ints[0] && rawX <= ints[0] + thumbView[0].getWidth();
                boolean isInY = rawY >= ints[1] && rawY <= ints[1] + thumbView[0].getHeight();
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    Log.d(TAG, String.format("addOnTouchEventListener: %d, %d | %d, %d || %s, %s", (int) rawX, (int) rawY, ints[0], ints[1], isInX, isInY));
                if ((event.getAction() == MotionEvent.ACTION_DOWN && isInX && isInY) || (event.getAction() != MotionEvent.ACTION_DOWN && !blocked[0])) {
                    blocked[0] = false;
                    return onTouchEvent.test(event);
                } else
                    blocked[0] = true;
                String BREAKPOINT = null;
                return false;
            }
        };

        mView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent event) {
                return processMotionEvent.run(event);
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent event) {
                processMotionEvent.run(event);
            }
        });
    }

    @Override
    public int getScrollRange() {
        int itemCount = getItemCount();
        if (itemCount == 0) {
            return 0;
        }
        int itemHeight = getItemHeight();
        if (itemHeight == 0) {
            return 0;
        }
        return mView.getPaddingTop() + itemCount * itemHeight + mView.getPaddingBottom();
    }

    @Override
    public int getScrollOffset() {
        int firstItemPosition = getFirstItemPosition();
        if (firstItemPosition == RecyclerView.NO_POSITION) {
            return 0;
        }
        int itemHeight = getItemHeight();
        int firstItemTop = getFirstItemOffset();
        return mView.getPaddingTop() + firstItemPosition * itemHeight - firstItemTop;
    }

    @Override
    public void scrollTo(int offset) {
        // Stop any scroll in progress for RecyclerView.
        mView.stopScroll();
        offset -= mView.getPaddingTop();
        int itemHeight = getItemHeight();
        // firstItemPosition should be non-negative even if paddingTop is greater than item height.
        int firstItemPosition = Math.max(0, offset / itemHeight);
        int firstItemTop = firstItemPosition * itemHeight - offset;
        scrollToPositionWithOffset(firstItemPosition, firstItemTop);
    }

    @Nullable
    @Override
    public String getPopupText() {
        PopupTextProvider popupTextProvider = mPopupTextProvider;
        if (popupTextProvider == null) {
            RecyclerView.Adapter<?> adapter = mView.getAdapter();
            if (adapter instanceof PopupTextProvider) {
                popupTextProvider = (PopupTextProvider) adapter;
            }
        }
        if (popupTextProvider == null) {
            return null;
        }
        int position = getFirstItemAdapterPosition();
        if (position == RecyclerView.NO_POSITION) {
            return null;
        }
        return popupTextProvider.getPopupText(position);
    }

    private int getItemCount() {
        LinearLayoutManager linearLayoutManager = getVerticalLinearLayoutManager();
        if (linearLayoutManager == null) {
            return 0;
        }
        int itemCount = linearLayoutManager.getItemCount();
        if (itemCount == 0) {
            return 0;
        }
        if (linearLayoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) linearLayoutManager;
            itemCount = (itemCount - 1) / gridLayoutManager.getSpanCount() + 1;
        }
        return itemCount;
    }

    private int getItemHeight() {
        if (mView.getChildCount() == 0) {
            return 0;
        }
        View itemView = mView.getChildAt(0);
        mView.getDecoratedBoundsWithMargins(itemView, mTempRect);
        return mTempRect.height();
    }

    public int getFirstItemPosition() {
        int position = getFirstItemAdapterPosition();
        LinearLayoutManager linearLayoutManager = getVerticalLinearLayoutManager();
        if (linearLayoutManager == null) {
            return RecyclerView.NO_POSITION;
        }
        if (linearLayoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) linearLayoutManager;
            position /= gridLayoutManager.getSpanCount();
        }
        return position;
    }

    private int getFirstItemAdapterPosition() {
        if (mView.getChildCount() == 0) {
            return RecyclerView.NO_POSITION;
        }
        View itemView = mView.getChildAt(0);
        LinearLayoutManager linearLayoutManager = getVerticalLinearLayoutManager();
        if (linearLayoutManager == null) {
            return RecyclerView.NO_POSITION;
        }
        return linearLayoutManager.getPosition(itemView);
    }

    public int getFirstItemOffset() {
        if (mView.getChildCount() == 0) {
            return RecyclerView.NO_POSITION;
        }
        View itemView = mView.getChildAt(0);
        mView.getDecoratedBoundsWithMargins(itemView, mTempRect);
        return mTempRect.top;
    }

    private void scrollToPositionWithOffset(int position, int offset) {
        LinearLayoutManager linearLayoutManager = getVerticalLinearLayoutManager();
        if (linearLayoutManager == null) {
            return;
        }
        if (linearLayoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) linearLayoutManager;
            position *= gridLayoutManager.getSpanCount();
        }
        // LinearLayoutManager actually takes offset from paddingTop instead of top of RecyclerView.
        offset -= mView.getPaddingTop();
        linearLayoutManager.scrollToPositionWithOffset(position, offset);
    }

    @Nullable
    private LinearLayoutManager getVerticalLinearLayoutManager() {
        RecyclerView.LayoutManager layoutManager = mView.getLayoutManager();
        if (!(layoutManager instanceof LinearLayoutManager)) {
            return null;
        }
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
        if (linearLayoutManager.getOrientation() != RecyclerView.VERTICAL) {
            return null;
        }
        return linearLayoutManager;
    }
}
