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
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.ViewOverlay;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finn.androidUtilities.CustomList;
import com.finn.androidUtilities.CustomUtility;
import com.finn.androidUtilities.CustomRecycler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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

    int[] scrollRange;
    CustomList<Integer>[] heightList;
    CustomRecycler customRecycler;
    int thumbOffset = 0;
    boolean smoothScroll;
    private Runnable onPreDraw;
    private View thumbView;

    // ToDo: wenn nach ganz untern gescrollt dann letztes element anzeigen
    //  & warum so abgehackt in medien

    /**  <------------------------- Constructor -------------------------  */
    public FastScrollRecyclerViewHelper(@NonNull RecyclerView view, @Nullable PopupTextProvider popupTextProvider) {
        mView = view;
        mPopupTextProvider = popupTextProvider;
    }

    public FastScrollRecyclerViewHelper(@NonNull RecyclerView view, @Nullable FastScroller[] fastScroller, @Nullable PopupTextProvider popupTextProvider) {
        mView = view;
        mFastScroller = fastScroller;
        mPopupTextProvider = popupTextProvider;
    }

    public FastScrollRecyclerViewHelper(CustomRecycler customRecycler, @Nullable FastScroller[] fastScroller, int[] scrollRange, CustomList<Integer>[] heightList, boolean smoothScroll, @Nullable PopupTextProvider popupTextProvider) {
        mView = customRecycler.getRecycler();
        mFastScroller = fastScroller;
        mPopupTextProvider = popupTextProvider;
        this.customRecycler = customRecycler;
        this.scrollRange = scrollRange;
        this.heightList = heightList;
        this.smoothScroll = smoothScroll;
    }

    public FastScrollRecyclerViewHelper(CustomRecycler customRecycler, @Nullable FastScroller[] fastScroller, boolean smoothScroll, @Nullable PopupTextProvider popupTextProvider) {
        mView = customRecycler.getRecycler();
        mFastScroller = fastScroller;
        mPopupTextProvider = popupTextProvider;
        this.customRecycler = customRecycler;
        this.smoothScroll = smoothScroll;
    }
    /**  ------------------------- Constructor ------------------------->  */

    @Override
    public void addOnPreDrawListener(@NonNull Runnable onPreDraw) {
        this.onPreDraw = onPreDraw;
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
        CustomUtility.GenericReturnOnlyInterface<View> tryGetThumbView = () -> {
            if (mFastScroller != null && mFastScroller[0] != null) {
                try {
                    Field mThumbView = FastScroller.class.getDeclaredField("mThumbView");
                    mThumbView.setAccessible(true);
                    thumbView = (View) mThumbView.get(mFastScroller[0]);
                    return thumbView;
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    Log.e(TAG, "addOnTouchEventListener: ", e);
                }
            }
            return null;
        };

        final boolean[] blocked = {false};

        CustomUtility.GenericReturnInterface<MotionEvent, Boolean> processMotionEvent = event -> {
            if (thumbView == null && (thumbView = tryGetThumbView.run()) == null)
                return onTouchEvent.test(event);
            else {
                int[] ints = new int[]{0,0};
                thumbView.getLocationOnScreen(ints);
                float rawX = event.getX();
                float rawY = event.getY();
                boolean isInX = rawX >= ints[0] && rawX <= ints[0] + thumbView.getWidth();
                boolean isInY = rawY >= ints[1] && rawY <= ints[1] + thumbView.getHeight();

                if (!blocked[0] && event.getAction() == MotionEvent.ACTION_UP) {
                    if (!smoothScroll) {
                        startResetThumbOffsetAnimation();
                    }
                    Utility.reflectionCall(mView, "dispatchOnScrollStateChanged", Pair.create(int.class, RecyclerView.SCROLL_STATE_IDLE));
                }

//                if (event.getAction() == MotionEvent.ACTION_DOWN)
//                    Log.d(TAG, String.format("addOnTouchEventListener: %d, %d | %d, %d || %s, %s", (int) rawX, (int) rawY, ints[0], ints[1], isInX, isInY));
                if ((event.getAction() == MotionEvent.ACTION_DOWN && isInX && isInY) || (event.getAction() != MotionEvent.ACTION_DOWN && !blocked[0])) {
                    blocked[0] = false;
                    return onTouchEvent.test(event);
                } else
                    blocked[0] = true;
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

    int lastRange;
    @Override
    public int getScrollRange() {
        if (scrollRange == null) {
            return lastRange = getItemHeight() * getItemCount() - mView.getPaddingBottom() - mView.getPaddingTop();
        } else
            return lastRange = scrollRange[0];
    }

    private int getItemCount() {
        int count;
        if (heightList == null)
            count = mView.getAdapter().getItemCount();
        else
            count = heightList[0].size();
        LinearLayoutManager layoutManager = customRecycler.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager)
            count = (int) Math.ceil((double) count / ((GridLayoutManager) layoutManager).getSpanCount());
        return count;
    }

    int prev;
    @Override
    public int getScrollOffset() {
        LinearLayoutManager layoutManager = customRecycler.getLayoutManager();
        int position = getFirstItemPosition();
        if (position == RecyclerView.NO_POSITION) {
            return 0;
        }
        int firstItemTop = getFirstItemOffset();
//        int columns = 1;
//        if (layoutManager instanceof GridLayoutManager)
//            columns = ((GridLayoutManager) layoutManager).getSpanCount();
//        Log.d(TAG, "getScrollOffset: " + position + " | " + firstItemTop + " | " + columns);
        int sum;
        if (heightList == null)
            sum = getItemHeight() * position;
        else
            sum = heightList[0].subList(0, position).stream().mapToInt(Integer::intValue).sum();
        int i = mView.getPaddingTop() + sum - firstItemTop  + thumbOffset;
//        if (i != prev) {
////            Log.d(TAG, String.format("getScrollOffset: %d", i - prev));
////            Log.d(TAG, String.format("getScrollOffset: %d | %d | %d\n", sum, firstItemTop, thumbOffset));
//            prev = i;
//        }
        return i;
    }

    @Override
    public void scrollTo(int offset) {
        mView.stopScroll();
        int i = 0;
        int size;
        boolean isLast;
//        int initialOffset = offset;
//        int availableDistance = lastRange - mView.getHeight() - initialOffset;
        LinearLayoutManager layoutManager = customRecycler.getLayoutManager();
        if (heightList == null) {
            int columns = 1;
            if (layoutManager instanceof GridLayoutManager)
                columns = ((GridLayoutManager) layoutManager).getSpanCount();

            int itemHeight = getItemHeight();
            size = getItemCount();
            for (; i < size; i++) {
                if (offset - itemHeight < 0)
                    break;
                else
                    offset -= itemHeight;
            }
//            int lastItemOffset = getLastItemOffset();
            isLast = layoutManager.findLastVisibleItemPosition() >= layoutManager.getItemCount() - 1;// && offset > (double) lastItemOffset / 2 && lastItemOffset + availableDistance < itemHeight;// && (double) itemHeight / 2 >  lastItemOffset;// && (double) itemHeight / 2 >  getLastItemOffset();// && offset > itemHeight / 2;
//            Log.d(TAG, String.format("scrollTo: %d | %d | %d | %s", layoutManager.findLastVisibleItemPosition(), size-1, layoutManager.getItemCount() - 1, isLast));
            thumbOffset = smoothScroll || isLast ? 0 : (offset /*+  itemHeight * (i % columns)*/);
            layoutManager.scrollToPositionWithOffset(i * columns, smoothScroll || isLast ? -offset : 0);
//            Log.d(TAG, String.format("scrollTo: %d | %d", i, thumbOffset));
//            if (isLast) {
//                int lastItemOffset = getLastItemOffset();
//            Log.d(TAG, String.format("scrollTo: l:%d | %s | o:%d | h:%d | d:%s | %s | c:%d", lastItemOffset, (double) itemHeight / 2 >  lastItemOffset, offset, itemHeight, availableDistance, lastItemOffset + availableDistance > itemHeight, lastItemOffset + availableDistance));
//            }

        } else {
            size = getItemCount();
            Integer itemHeight = 0;
            for (; i < size; i++) {
                itemHeight = heightList[0].get(i);
                if (offset - itemHeight < 0)
                    break;
                else
                    offset -= itemHeight;
            }
            isLast = layoutManager.findLastVisibleItemPosition() == size - 1;// && offset > itemHeight / 2;


            thumbOffset = smoothScroll || isLast ? 0 : offset;
            layoutManager.scrollToPositionWithOffset(i, smoothScroll || isLast ? -offset : 0);
        }
    }

    private void startResetThumbOffsetAnimation() {
        if (thumbView != null && thumbOffset > 0 && onPreDraw != null) {
            int startOffset = thumbOffset;
            Animation a = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if (interpolatedTime == 1) {
                        thumbOffset = 0;
                    } else {
                        thumbOffset = startOffset - (int) (startOffset * interpolatedTime);
                        onPreDraw.run();
                    }
                }

                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };

            a.setDuration(125);
            thumbView.startAnimation(a);
        }
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

    public int getLastItemOffset() {
        int childCount = mView.getChildCount();
        if (childCount == 0) {
            return RecyclerView.NO_POSITION;
        }
        View itemView = mView.getChildAt(childCount - 1);
        mView.getDecoratedBoundsWithMargins(itemView, mTempRect);
        return mTempRect.bottom - mView.getHeight();
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
