package com.maxMustermannGeheim.linkcollection.Utilitys;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ItemTouchHelper.Callback;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.maxMustermannGeheim.linkcollection.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomRecycler<T>{

    // ToDo: ItemTouchHelper https://www.youtube.com/watch?v=dvDTmJtGE_I
    //  holder.itemView.setTag

    public enum ORIENTATION {
        VERTICAL, HORIZONTAL
    }
    public enum SELECTION_MODE {
        SINGLE_SELECTION, MULTI_SELECTION
    }

    private int rowOrColumnCount = 1;
    private long multipleClickDelay = 300;
    private long lastClickTime = System.currentTimeMillis();
    private boolean isMultiClickEnabled = false;
    private boolean showDivider = true;
    private boolean hideLastDivider;
    private boolean useCustomRipple = false;
    private Context context;
    private RecyclerView recycler;
    private int itemView;
    private SetItemContent<T> setItemContent;
    private List objectList = new ArrayList();
    private int orientation = RecyclerView.VERTICAL;
    private OnLongClickListener onLongClickListener;
    private View.OnLongClickListener longClickListener = view -> {
        if ((lastClickTime > System.currentTimeMillis() - multipleClickDelay) && !isMultiClickEnabled)
            return false;
        lastClickTime = System.currentTimeMillis();
        int index = recycler.getChildAdapterPosition(view);
        onLongClickListener.runOnLongClickListener(this, view, objectList.get(index), index);
        return true;
    };
    private OnClickListener onClickListener;
    private View.OnClickListener clickListener = view -> {
        if ((lastClickTime > System.currentTimeMillis() - multipleClickDelay) && !isMultiClickEnabled)
            return;
        lastClickTime = System.currentTimeMillis();
        int index = recycler.getChildAdapterPosition(view);
        onClickListener.runOnClickListener(this, view, objectList.get(index), index);
    };
    private Map<Integer, Pair<OnClickListener, Boolean>> idSubOnClickListenerMap = new HashMap<>();
    private Map<Integer, Pair<OnClickListener, Boolean>> idSubOnLongClickListenerMap = new HashMap<>();
    private SELECTION_MODE selectionMode = SELECTION_MODE.SINGLE_SELECTION;
    private boolean useActiveObjectList;
    private GetActiveObjectList getActiveObjectList;
    private boolean hideOverscroll;
    private MyAdapter mAdapter;
    private boolean dragAndDrop = false;
    private OnDragAndDrop onDragAndDrop;


    public CustomRecycler(Context context) {
        this.context = context;
    }

    public static CustomRecycler Builder(Context context) {
        return new CustomRecycler<>(context);
    }

    public static CustomRecycler Builder(Context context, RecyclerView recycler) {
        CustomRecycler customRecycler = new CustomRecycler<>(context);
        customRecycler.recycler = recycler;
        return customRecycler;
    }

    public CustomRecycler (Context context, RecyclerView recycler) {
        this.context = context;
        this.recycler = recycler;
    }



    public CustomRecycler setRecycler(RecyclerView recycler) {
        this.recycler = recycler;
        return this;
    }

    public RecyclerView getRecycler() {
        return recycler;
    }

    public CustomRecycler setItemLayout(int layoutId) {
        this.itemView = layoutId;
        return this;
    }

    public CustomRecycler setObjectList(List objectList) {
        this.objectList = objectList;
        return this;
    }

    public List getObjectList() {
        return objectList;
    }

    public interface GetActiveObjectList<T>{
        List<T> runGetActiveObjectList();
    }

    public CustomRecycler setGetActiveObjectList(GetActiveObjectList getActiveObjectList) {
        this.getActiveObjectList = getActiveObjectList;
        objectList.addAll(getActiveObjectList.runGetActiveObjectList());
        useActiveObjectList = true;
        return this;
    }

    public CustomRecycler setOrientation(ORIENTATION orientation) {
        switch (orientation) {
            case VERTICAL: this.orientation = RecyclerView.VERTICAL; break;
            case HORIZONTAL: this.orientation = RecyclerView.HORIZONTAL; break;
        }
        return this;
    }

    public CustomRecycler useCustomRipple() {
        this.useCustomRipple = true;
        return this;
    }

    public CustomRecycler hideDivider() {
        this.showDivider = false;
        return this;
    }

    public CustomRecycler removeLastDivider() {
        this.hideLastDivider = true;
        return this;
    }

    public CustomRecycler setSelectionMode(SELECTION_MODE selectionMode) {
        this.selectionMode = selectionMode;
        return this;
    }

    public CustomRecycler setMultipleClickDelay(long multipleClickDelay) {
        this.multipleClickDelay = multipleClickDelay;
        return this;
    }

    public CustomRecycler setMultiClickEnabled(boolean multiClickEnabled) {
        isMultiClickEnabled = multiClickEnabled;
        return this;
    }

    public CustomRecycler addSubOnClickListener(int viewId, OnClickListener onClickListener, boolean ripple) {
        idSubOnClickListenerMap.put(viewId, new Pair<>(onClickListener, ripple));
        return this;
    }

    public CustomRecycler addSubOnClickListener(int viewId, OnClickListener onClickListener) {
        idSubOnClickListenerMap.put(viewId, new Pair<>(onClickListener, true));
        return this;
    }

    public CustomRecycler addSubOnLongClickListener(int viewId, OnClickListener onClickListener, boolean ripple) {
        idSubOnLongClickListenerMap.put(viewId, new Pair<>(onClickListener, ripple));
        return this;
    }

    public CustomRecycler addSubOnLongClickListener(int viewId, OnClickListener onClickListener) {
        idSubOnLongClickListenerMap.put(viewId, new Pair<>(onClickListener, true));
        return this;
    }

    public CustomRecycler setRowOrColumnCount(int rowOrColumnCount) {
        this.rowOrColumnCount = rowOrColumnCount;
        return this;
    }

    public interface SetItemContent<T> {
        void runSetCellContent(View itemView, T t);
    }

    public CustomRecycler setSetItemContent(SetItemContent setItemContent) {
        this.setItemContent = setItemContent;
        return this;
    }

    public interface OnClickListener<T> {
        void runOnClickListener(CustomRecycler customRecycler, View itemView, T t, int index);
    }

    public CustomRecycler setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    public interface OnLongClickListener<T> {
        void runOnLongClickListener(CustomRecycler customRecycler, View view, T t, int index);
    }

    public CustomRecycler setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
        return this;
    }

    public CustomRecycler hideOverscroll() {
        hideOverscroll = true;
        return this;
    }

    public CustomRecycler enableDragAndDrop(OnDragAndDrop onDragAndDrop) {
        this.onDragAndDrop = onDragAndDrop;
        dragAndDrop = true;
        return this;
    }

    private void applyDragAndDrop() {
        ItemTouchHelper.Callback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder dragged, @NonNull RecyclerView.ViewHolder target) {

                int posDragged = dragged.getAdapterPosition();
                int posTarget = target.getAdapterPosition();

                Collections.swap(objectList, posDragged, posTarget);
                mAdapter.notifyItemMoved(posDragged, posTarget);

                onDragAndDrop.runOnDragAndDrop(objectList);

                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                String BREAKPOINT = null;
            }
        };
        ItemTouchHelper helper = new ItemTouchHelper(itemTouchHelperCallback);
        helper.attachToRecyclerView(recycler);
    }

    public interface OnDragAndDrop {
        void runOnDragAndDrop(List objectList);
    }

//  ----- Adapter ----->
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List dataset;
        private List<ViewHolder> viewHolders = new ArrayList<>();


        public MyAdapter(List list) {
            dataset = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(itemView, parent, false);
            v.setId(View.generateViewId());

            if (!idSubOnClickListenerMap.isEmpty()) {
                for (Map.Entry<Integer, Pair<OnClickListener, Boolean>> entry : idSubOnClickListenerMap.entrySet()) {
                    View view = v.findViewById(entry.getKey());
                    view.setOnClickListener(view2 -> {
                        int index = recycler.getChildAdapterPosition(v);
                        entry.getValue().first.runOnClickListener(CustomRecycler.this, v, dataset.get(index), index);
                        view.setFocusable(true);
                        view.setClickable(true);
                        if (entry.getValue().second) {
                            TypedValue outValue = new TypedValue();
                            context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                            view.setBackgroundResource(outValue.resourceId);
                        }
                    });
                }
            }

            if (!idSubOnLongClickListenerMap.isEmpty()) {
                for (Map.Entry<Integer, Pair<OnClickListener, Boolean>> entry : idSubOnLongClickListenerMap.entrySet()) {
                    View view = v.findViewById(entry.getKey());
                    view.setOnLongClickListener(view2 -> {
                        int index = recycler.getChildAdapterPosition(v);
                        entry.getValue().first.runOnClickListener(CustomRecycler.this, v, dataset.get(index), index);
                        view.setFocusable(true);
                        view.setClickable(true);
                        if (entry.getValue().second) {
                            TypedValue outValue = new TypedValue();
                            context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                            view.setBackgroundResource(outValue.resourceId);
                        }
                        return true;
                    });
                }
            }

            if (selectionMode != SELECTION_MODE.MULTI_SELECTION) {
                if (onClickListener != null || onLongClickListener != null) {

                    if (onClickListener != null)
                        v.setOnClickListener(clickListener);

                    if (onLongClickListener != null)
                        v.setOnLongClickListener(longClickListener);

                    if (!useCustomRipple) {
                        v.setFocusable(true);
                        v.setClickable(true);
                        TypedValue outValue = new TypedValue();
                        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                        v.setBackgroundResource(outValue.resourceId);
                    }
                }
            }

            ViewHolder viewHolder = new ViewHolder(v);

            viewHolders.add(viewHolder);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            setItemContent.runSetCellContent(viewHolder.itemView, (T) dataset.get(position));
        }

        @Override
        public int getItemCount() {
            return dataset.size();
        }

        public void removeItemAt(int index) {
            if (dataset.isEmpty())
                return;
            dataset.remove(index);
//                notifyDataSetChanged();
            notifyItemRemoved(index);
            notifyItemRangeChanged(index, dataset.size());
        }

        public void addItem(Object object, int index) {
            dataset.add(index, object);
            notifyItemInserted(index);
            notifyItemRangeChanged(index, dataset.size());
        }

        public void addItem(Object object) {
            dataset.add(object);
            notifyItemInserted(dataset.size() - 1);
            notifyItemRangeChanged(dataset.size() - 1, dataset.size());
        }

        public List<ViewHolder> getViewHolders() {
            return viewHolders;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
//            Map<Integer, View> viewMap = new HashMap<>();

            public ViewHolder(View v) {
                super(v);
//                for (Integer id : viewIdList) {
//                    viewMap.put(id, v.findViewById(id));
//                }
            }
        }

        public List getDataset() {
            return dataset;
        }
    }
//  <----- Adapter -----


//  ----- Generate ----->
    public Pair<CustomRecycler, RecyclerView> generatePair() {
        RecyclerView recyclerView = generate();
        return new Pair<>(this, recyclerView);
    }

    public CustomRecycler generateCustomRecycler() {
        this.recycler = generate();
        return this;
    }

    public RecyclerView generate() {
        if (this.recycler == null)
            recycler = new RecyclerView(context);

        RecyclerView.LayoutManager layoutManager;
        if (rowOrColumnCount > 1)
            layoutManager = new GridLayoutManager(context, rowOrColumnCount,orientation, false);
        else
            layoutManager = new LinearLayoutManager(context, orientation, false);
        recycler.setLayoutManager(layoutManager);

        mAdapter = new MyAdapter(objectList);
        recycler.setAdapter(mAdapter);

        if (showDivider) {
            Drawable mDivider = ContextCompat.getDrawable(context, R.drawable.divider);
            DividerItemDecoration dividerItemDecoration;
            if (hideLastDivider) {
                dividerItemDecoration = new DividerItemDecoration(context, orientation) {
                    @Override
                    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
                        int dividerLeft = parent.getPaddingLeft();
                        int dividerRight = parent.getWidth() - parent.getPaddingRight();

                        int childCount = parent.getChildCount();
                        for (int i = 0; i <= childCount - 2; i++) {
                            View child = parent.getChildAt(i);

                            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                            int dividerTop = child.getBottom() + params.bottomMargin;
                            int dividerBottom = dividerTop + mDivider.getIntrinsicHeight();

                            mDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
                            mDivider.draw(canvas);
                        }
                    }
                };
            }
            else {
                dividerItemDecoration = new DividerItemDecoration(recycler.getContext(),
                        ((LinearLayoutManager) layoutManager).getOrientation());
                dividerItemDecoration.setDrawable(ContextCompat.getDrawable(context, R.drawable.divider));
            }
            recycler.addItemDecoration(dividerItemDecoration);
        }

        if (hideOverscroll)
            recycler.setOverScrollMode(View.OVER_SCROLL_NEVER);

        if (dragAndDrop)
            applyDragAndDrop();

        return recycler;
    }

    public RecyclerView update() {
        if (useActiveObjectList)
            objectList.addAll(getActiveObjectList.runGetActiveObjectList());
        mAdapter.notifyDataSetChanged();
        return recycler;
    }

    public RecyclerView reload() {
        if (useActiveObjectList) {
            objectList.clear();
            objectList.addAll(getActiveObjectList.runGetActiveObjectList());
        }
        mAdapter.notifyDataSetChanged();
        return recycler;
    }

    public RecyclerView reload(List objectList) {
        mAdapter.dataset = objectList;
        mAdapter.notifyDataSetChanged();
        return recycler;
    }

    public RecyclerView update(Integer... index) {
        if (useActiveObjectList)
            objectList.addAll(getActiveObjectList.runGetActiveObjectList());
        Arrays.asList(index).forEach(mAdapter::notifyItemChanged);
        return recycler;
    }


    public RecyclerView reloadNew() {
        mAdapter = new MyAdapter(objectList);
        this.recycler.setAdapter(mAdapter);
        generate();
        return recycler;
    }
//  <----- Generate -----

}
