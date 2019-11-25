package com.maxMustermannGeheim.linkcollection.Utilitys;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.maxMustermannGeheim.linkcollection.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomRecycler<T>{

    // ToDo: ItemTouchHelper https://www.youtube.com/watch?v=dvDTmJtGE_I
    //  holder.layoutId.setTag

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
    private boolean useCustomRipple = true;
    private Context context;
    private RecyclerView recycler;
    private int layoutId;
    private SetItemContent<T> setItemContent;
    private List<T> objectList = new ArrayList<>();
    private int orientation = RecyclerView.VERTICAL;
    private OnLongClickListener<T> onLongClickListener;
    private View.OnLongClickListener longClickListener = view -> {
        if ((lastClickTime > System.currentTimeMillis() - multipleClickDelay) && !isMultiClickEnabled)
            return false;
        lastClickTime = System.currentTimeMillis();
        int index = recycler.getChildAdapterPosition(view);
        onLongClickListener.runOnLongClickListener(this, view, objectList.get(index), index);
        return true;
    };
    private OnClickListener<T> onClickListener;
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
    private GetActiveObjectList<T> getActiveObjectList;
    private boolean hideOverscroll;
    private MyAdapter mAdapter;
//    private boolean dragAndDrop = false;
    private OnDragAndDrop<T> onDragAndDrop;
    private int dividerMargin;
    private OnSwiped<T> onSwiped;
    private Pair<Boolean,Boolean> leftRightSwipe_pair;


    public CustomRecycler(Context context) {
        this.context = context;
    }

//    public static CustomRecycler Builder(Context context) {
//        return new CustomRecycler<>(context);
//    }

//    public static CustomRecycler Builder(Context context, RecyclerView recycler) {
//        CustomRecycler customRecycler = new CustomRecycler<>(context);
//        customRecycler.recycler = recycler;
//        return customRecycler;
//    }

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

    public CustomRecycler<T> setItemLayout(int layoutId) {
        this.layoutId = layoutId;
        return this;
    }

    public int getLayoutId() {
        return layoutId;
    }

    public CustomRecycler<T> setObjectList(Collection<T> objectCollection) {
        if (objectCollection instanceof List)
            this.objectList = (List<T>) objectCollection;
        else
            this.objectList = new ArrayList<>(objectCollection);
        return this;
    }

    public List<T> getObjectList() {
        return objectList;
    }

    public interface GetActiveObjectList<T>{
        List<T> runGetActiveObjectList();
    }

    public CustomRecycler<T> setGetActiveObjectList(GetActiveObjectList<T> getActiveObjectList) {
        this.getActiveObjectList = getActiveObjectList;
        objectList.addAll(getActiveObjectList.runGetActiveObjectList());
        useActiveObjectList = true;
        return this;
    }

    public CustomRecycler<T> setOrientation(ORIENTATION orientation) {
        switch (orientation) {
            case VERTICAL: this.orientation = RecyclerView.VERTICAL; break;
            case HORIZONTAL: this.orientation = RecyclerView.HORIZONTAL; break;
        }
        return this;
    }

    public CustomRecycler<T> deaktivateCustomRipple() {
        this.useCustomRipple = false;
        return this;
    }

    public CustomRecycler<T> hideDivider() {
        this.showDivider = false;
        return this;
    }

    public CustomRecycler<T> removeLastDivider() {
        this.hideLastDivider = true;
        return this;
    }

    public CustomRecycler<T> setSelectionMode(SELECTION_MODE selectionMode) {
        this.selectionMode = selectionMode;
        return this;
    }

    public CustomRecycler<T> setMultipleClickDelay(long multipleClickDelay) {
        this.multipleClickDelay = multipleClickDelay;
        return this;
    }

    public CustomRecycler<T> setMultiClickEnabled(boolean multiClickEnabled) {
        isMultiClickEnabled = multiClickEnabled;
        return this;
    }

    public CustomRecycler<T> addSubOnClickListener(int viewId, OnClickListener<T> onClickListener, boolean ripple) {
        idSubOnClickListenerMap.put(viewId, new Pair<>(onClickListener, ripple));
        return this;
    }

    public CustomRecycler<T> addSubOnClickListener(int viewId, OnClickListener<T> onClickListener) {
        idSubOnClickListenerMap.put(viewId, new Pair<>(onClickListener, false));
        return this;
    }

    public CustomRecycler<T> addSubOnLongClickListener(int viewId, OnClickListener<T> onClickListener, boolean ripple) {
        idSubOnLongClickListenerMap.put(viewId, new Pair<>(onClickListener, ripple));
        return this;
    }

    public CustomRecycler<T> addSubOnLongClickListener(int viewId, OnClickListener<T> onClickListener) {
        idSubOnLongClickListenerMap.put(viewId, new Pair<>(onClickListener, false));
        return this;
    }

    public CustomRecycler<T> setRowOrColumnCount(int rowOrColumnCount) {
        this.rowOrColumnCount = rowOrColumnCount;
        return this;
    }

    public interface SetItemContent<E> {
        void runSetCellContent(View itemView, E e);
    }

    public CustomRecycler<T> setSetItemContent(SetItemContent<T> setItemContent) {
        this.setItemContent = setItemContent;
        return this;
    }

    public interface OnClickListener<T> {
        void runOnClickListener(CustomRecycler<T> customRecycler, View itemView, T t, int index);
    }

    public CustomRecycler<T> setOnClickListener(OnClickListener<T> onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    public interface OnLongClickListener<T> {
        void runOnLongClickListener(CustomRecycler<T> customRecycler, View view, T t, int index);
    }

    public CustomRecycler<T> setOnLongClickListener(OnLongClickListener<T> onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
        return this;
    }

    public CustomRecycler<T> hideOverscroll() {
        hideOverscroll = true;
        return this;
    }

    public CustomRecycler<T> enableDragAndDrop(OnDragAndDrop<T> onDragAndDrop) {
        this.onDragAndDrop = onDragAndDrop;
//        dragAndDrop = true;
        return this;
    }

    public CustomRecycler<T> enableSwiping(OnSwiped<T> onSwiped, boolean start, boolean end) {
        this.onSwiped = onSwiped;
        this.leftRightSwipe_pair = new Pair<>(start, end);
//        dragAndDrop = true;
        return this;
    }

    private void applyTouchActions() {
        int dragFlags = 0;
        int swipeFlags = 0;
        if (onDragAndDrop != null)
            dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        if (onSwiped != null) {
            if (leftRightSwipe_pair.first)
                swipeFlags += ItemTouchHelper.START;
            if (leftRightSwipe_pair.second)
                swipeFlags += ItemTouchHelper.END;
        }
        ItemTouchHelper.Callback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(dragFlags, swipeFlags) {
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
                int index = viewHolder.getAdapterPosition();

                T t = objectList.remove(index);
//                mAdapter.notifyItemRangeChanged(index, 1);
                mAdapter.notifyDataSetChanged();
                onSwiped.runSwyped(objectList, direction, t);
            }
        };
        ItemTouchHelper helper = new ItemTouchHelper(itemTouchHelperCallback);
        helper.attachToRecyclerView(recycler);
    }

    public interface OnDragAndDrop<T> {
        void runOnDragAndDrop(List<T> objectList);
    }

    public interface OnSwiped<T> {
        void runSwyped(List<T> objectList, int direction, T t);
    }

    public CustomRecycler<T> setDividerMargin_inDp(int dividerMargin_inDp) {
        this.dividerMargin = Utility.dpToPx(dividerMargin_inDp);
        return this;
    }


    //  ----- Adapter ----->
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List dataSet;

        public MyAdapter(List list) {
            dataSet = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(layoutId, parent, false);
            v.setId(View.generateViewId());

            if (!idSubOnClickListenerMap.isEmpty()) {
                for (Map.Entry<Integer, Pair<OnClickListener, Boolean>> entry : idSubOnClickListenerMap.entrySet()) {
                    View view = v.findViewById(entry.getKey());
                    view.setOnClickListener(view2 -> {
                        int index = recycler.getChildAdapterPosition(v);
                        entry.getValue().first.runOnClickListener(CustomRecycler.this, v, dataSet.get(index), index);
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
                        entry.getValue().first.runOnClickListener(CustomRecycler.this, v, dataSet.get(index), index);
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

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            setItemContent.runSetCellContent(viewHolder.itemView, (T) dataSet.get(position));
        }

        @Override
        public int getItemCount() {
            return dataSet.size();
        }

        public void removeItemAt(int index) {
            if (dataSet.isEmpty())
                return;
            dataSet.remove(index);
//                notifyDataSetChanged();
            notifyItemRemoved(index);
            notifyItemRangeChanged(index, dataSet.size());
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(View v) {
                super(v);
            }
        }

        public List getDataSet() {
            return dataSet;
        }
    }
    //  <----- Adapter -----


    //  ----- Generate ----->
    public Pair<CustomRecycler, RecyclerView> generatePair() {
        RecyclerView recyclerView = generateRecyclerView();
        return new Pair<>(this, recyclerView);
    }

    public CustomRecycler<T> generate() {
        this.recycler = generateRecyclerView();
        return this;
    }

    public RecyclerView generateRecyclerView() {
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
                        int dividerLeft = parent.getPaddingLeft() + dividerMargin;
                        int dividerRight = parent.getWidth() - parent.getPaddingRight() - dividerMargin;

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

        if (onDragAndDrop != null || onSwiped != null)
            applyTouchActions();

        return recycler;
    }

    public CustomRecycler<T> reload() {
        if (useActiveObjectList) {
            objectList.clear();
            objectList.addAll(getActiveObjectList.runGetActiveObjectList());
        }
        mAdapter.notifyDataSetChanged();
        return this;
    }

    public CustomRecycler<T> reload(List<T> objectList) {
        this.objectList.clear();
        this.objectList.addAll(objectList);
        mAdapter.notifyDataSetChanged();
        return this;
    }

    public RecyclerView update(Integer... index) {
        if (useActiveObjectList) {
            objectList.clear();
            objectList.addAll(getActiveObjectList.runGetActiveObjectList());
        }
        Arrays.asList(index).forEach(mAdapter::notifyItemChanged);
        return recycler;
    }


    public RecyclerView reloadNew() {
        mAdapter = new MyAdapter(objectList);
        this.recycler.setAdapter(mAdapter);
        generateRecyclerView();
        return recycler;
    }
    //  <----- Generate -----


    //  --------------- GoTo --------------->
    public CustomRecycler<T> goTo(T t) {
        int index = objectList.indexOf(t);
        if (index == -1)
            return this;
        scrollTo(index, true);
        return this;
    }
    public CustomRecycler<T> goTo(GoToFilter<T> goToFilter, String search) {
        final T[] currentObject = (T[]) new Object[1];
        CustomList<T> filterdObjectList = new CustomList<>();
        List<T> allObjectList = getObjectList();

        if (search != null) {
            filterdObjectList.clear();
            filterdObjectList.addAll(allObjectList.stream().filter(t -> goToFilter.runGoToFilter(search, t)).collect(Collectors.toList()));
            if (filterdObjectList.isEmpty())
                Toast.makeText(context, "Kein Eintrag für diese Suche", Toast.LENGTH_SHORT).show();
            else if (filterdObjectList.size() == 1) {
                scrollTo(allObjectList.indexOf(filterdObjectList.get(0)), true);
                return this;
            }
        }

        CustomDialog goToDialog = CustomDialog.Builder(context);
        goToDialog
                .setTitle("Gehe Zu")
                .addButton("Zurück", customDialog1 -> {
                    currentObject[0] = filterdObjectList.previous(currentObject[0]);
                    customDialog1.reloadView();
                }, false)
                .addButton("Weiter", customDialog1 -> {
                    currentObject[0] = filterdObjectList.next(currentObject[0]);
                    customDialog1.reloadView();
                }, false)
                .addButton(CustomDialog.BUTTON_TYPE.GO_TO_BUTTON, customDialog1 -> scrollTo(allObjectList.indexOf(currentObject[0]), true))
                .setView(getLayoutId())
                .setEdit(new CustomDialog.EditBuilder().setHint("Filter").setFireActionDirectly(search != null && !search.isEmpty()).setText(search != null ? search : "").allowEmpty()
                        .setOnAction((textInputHelper, textInputLayout, actionId, text1) -> {
                    filterdObjectList.clear();
                    filterdObjectList.addAll(allObjectList.stream().filter(t -> goToFilter.runGoToFilter(text1, t)).collect(Collectors.toList()));
                    if (filterdObjectList.isEmpty())
                        Toast.makeText(context, "Kein Eintrag für diese Suche", Toast.LENGTH_SHORT).show();
                    else if (filterdObjectList.size() == 1) {
                        scrollTo(allObjectList.indexOf(filterdObjectList.get(0)), true);
                        goToDialog.dismiss();
                    } else {
                        currentObject[0] = filterdObjectList.get(0);
                        goToDialog.reloadView();
                    }
                }, Helpers.TextInputHelper.IME_ACTION.SEARCH))
                .setSetViewContent((customDialog1, view1) -> {
                    view1.setBackground(null);
                    View layoutView = customDialog1.findViewById(R.id.dialog_custom_layout_view);
                    if (currentObject[0] == null)
                        layoutView.setVisibility(View.GONE);
                    else{
                        setItemContent.runSetCellContent(layoutView, currentObject[0]);
                        layoutView.setVisibility(View.VISIBLE);
                    }
                })
                .show();
        return this;
    }
    public interface GoToFilter<T>{
        boolean runGoToFilter(String search, T t);
    }
    //  <--------------- GoTo ---------------


    //  --------------- Convenience --------------->
    public CustomRecycler<T> scrollTo(int index, boolean ripple) {
        if (index > objectList.size() - 1)
            return this;

        int firstVisiblePosition = ((LinearLayoutManager) recycler.getLayoutManager()).findFirstVisibleItemPosition();
        int lastVisiblePosition = ((LinearLayoutManager) recycler.getLayoutManager()).findLastVisibleItemPosition();
//        int middlePosition = (lastVisiblePosition + firstVisiblePosition) / 2;
//        int middleHeight = (lastVisiblePosition - firstVisiblePosition) / 2;

//        if (index > middlePosition) {
//            int diff = index - middlePosition;
//            if (index + diff >= objectList.size())
//                recycler.scrollToPosition(objectList.size() - 1);
//            else
//                recycler.scrollToPosition(index + diff);
//            recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
//                @Override
//                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                    recyclerView.getLayoutManager().findViewByPosition(index).setPressed(ripple);
//                    recyclerView.clearOnScrollListeners();
//                    super.onScrolled(recyclerView, dx, dy);
//                }
//            });
//        }
//        else
//            recycler.getLayoutManager().findViewByPosition(index).setPressed(ripple);


//        if (index + middleHeight >= objectList.size()) {
//            middleHeight = 0;
//            recycler.scrollToPosition(objectList.size() - 1);
//        } else
//            recycler.scrollToPosition(index + middleHeight);

        recycler.scrollToPosition(index);
        if (index >= firstVisiblePosition && index <= lastVisiblePosition)
            recycler.getLayoutManager().findViewByPosition(index).setPressed(ripple);
        else
            recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    recyclerView.getLayoutManager().findViewByPosition(index).setPressed(ripple);
                    recyclerView.clearOnScrollListeners();
                    super.onScrolled(recyclerView, dx, dy);
                }
            });
        return this;
    }
    //  <--------------- Convenience ---------------
}
