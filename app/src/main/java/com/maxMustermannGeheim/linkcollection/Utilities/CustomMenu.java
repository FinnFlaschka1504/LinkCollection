package com.maxMustermannGeheim.linkcollection.Utilities;

import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.finn.androidUtilities.CustomUtility;
import com.maxMustermannGeheim.linkcollection.R;

import java.util.ArrayList;
import java.util.List;

public class CustomMenu {
    private PopupWindow popupWindow;
    private AppCompatActivity context;
    private View anchor;
    private List<MenuItem> menus = new ArrayList<>();
    private OnClickListener onClickListener;
    public interface OnClickListener {
        void runOnClickListener(CustomRecycler customRecycler, View itemView, MenuItem item, int index);
    }
    private CustomRecycler customRecycler;
    private boolean dismissOnClick;
    private static boolean dismissAllOnClick;
    private static boolean dismissAll_onLongClick = true;
    private MenuItem parent;


    public static CustomMenu Builder(AppCompatActivity context, View anchor) {
        return Builder(context).setAnchor(anchor);
    }

    public static CustomMenu Builder(AppCompatActivity context){
        return new CustomMenu().setContext(context);
    }

    public CustomMenu setContext(AppCompatActivity context) {
        this.context = context;
        return this;
    }

    public AppCompatActivity getContext() {
        return context;
    }

    public PopupWindow getPopupWindow() {
        return popupWindow;
    }

    public CustomMenu setPopupWindow(PopupWindow popupWindow) {
        this.popupWindow = popupWindow;
        return this;
    }

    public CustomMenu setAnchor(View anchor) {
        this.anchor = anchor;
        return this;
    }

    public CustomMenu setMenus(SetMenus setMenus) {
        ArrayList<MenuItem> items = new ArrayList<>();
        setMenus.runSetMenus(this, items);
        this.menus = items;
        items.forEach(item -> item.setParent(this));
        return this;
    }

    public interface SetMenus {
        void runSetMenus(CustomMenu customMenu, List<MenuItem> items);
    }

    public List<MenuItem> getMenus() {
        return menus;
    }

    public CustomMenu setDynamicSubMenus(List<MenuItem> menuItems, SetDynamicSubMenu setDynamicSubMenu, OnClickListener onClickListener) {
        for (MenuItem item : menuItems) {
            List<MenuItem> subItems = new ArrayList<>();
            setDynamicSubMenu.runSetDynamicSubMenu(item, subItems);
            item.setSubMenus(this, (customMenu, items) -> items.addAll(subItems));
            item.getChild().setOnClickListener(onClickListener);
        }
        return this;
    }

    public interface SetDynamicSubMenu {
        void runSetDynamicSubMenu(MenuItem item, List<MenuItem> subItems);
    }

    public CustomMenu setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    public CustomMenu dismissOnClick() {
        dismissOnClick = true;
        return this;
    }

    public CustomMenu dismissAllOnClick() {
        dismissAllOnClick = true;
        return this;
    }

    public CustomMenu disable_dismissAll_onLongClick() {
        dismissAll_onLongClick = false;
        return this;
    }


    public CustomMenu show() {
        customRecycler = buildRecyclerView();

        popupWindow = CustomPopupWindow.Builder(anchor, customRecycler.generateRecyclerView()).show_popupWindow();
        return this;
    }

    public CustomRecycler<MenuItem> buildRecyclerView() {
        return new CustomRecycler<MenuItem>(context)
                .setItemLayout(R.layout.popup_standard_list)
                .setObjectList(menus)
                .hideDivider()
                .hideOverscroll()
                .setSetItemContent((customRecycler, itemView, item, index) -> {
                    ((TextView) itemView.findViewById(R.id.popup_standardList_text)).setText(item.getName());

                    ImageView iconView = itemView.findViewById(R.id.popup_standardList_icon);
                    if (item.icon != -1) {
                        iconView.setVisibility(View.VISIBLE);
                        iconView.setImageResource(item.icon);
                    } else
                        iconView.setVisibility(View.GONE);

                    if (onClickListener == null && item.getChild() != null) {
                        View popup_standardList_sub = itemView.findViewById(R.id.popup_standardList_sub);
                        popup_standardList_sub.setVisibility(View.VISIBLE);
                        popup_standardList_sub.setClickable(false);
                        popup_standardList_sub.setForeground(null);
                    } else if (onClickListener != null && item.getChild() != null) {
                        CustomUtility.setMargins(iconView, CustomUtility.dpToPx(13), -1, -1, -1);
                        CustomUtility.setMargins(itemView.findViewById(R.id.popup_standardList_text), CustomUtility.dpToPx(13), -1, -1, -1);
                        itemView.findViewById(R.id.popup_standardList_sub).setVisibility(View.VISIBLE);
                        itemView.findViewById(R.id.popup_standardList_divider).setVisibility(View.VISIBLE);
                    }
                })
                .setOnClickListener((customRecycler1, itemView, item, index) -> {
                    if (onClickListener == null && item.getChild() != null) {
                        item.getChild().setAnchor(itemView).show();
                    } else if (onClickListener != null) {
                        onClickListener.runOnClickListener(customRecycler1, itemView, item, index);

                        if (dismissOnClick || dismissAllOnClick) {
                            dismissPopupWindow(this);
                        }
                    }
                })
                .setOnLongClickListener((customRecycler1, view, item, index) -> {
                    if (dismissAll_onLongClick) {
                        dismissAllOnClick = true;
                        dismissPopupWindow(item.getParent());
                    }
                })
                .addSubOnClickListener(R.id.popup_standardList_sub, (customRecycler, itemView, item, index) -> {
                    item.getChild().setAnchor(itemView).show();
                }, false)
                .addSubOnClickListener(R.id.popup_standardList_sub_layout, (customRecycler, itemView, item, index) -> {
                    View popup_standardList_sub = itemView.findViewById(R.id.popup_standardList_sub);
                    popup_standardList_sub.setPressed(true);
                    popup_standardList_sub.callOnClick();
                }, false);
    }

    private void dismissPopupWindow(CustomMenu customMenu) {
        customMenu.dismiss();

        if (dismissAllOnClick && customMenu.parent != null)
            dismissPopupWindow(customMenu.parent.parent);
        else
            dismissAllOnClick = false;
    }

    public CustomMenu dismiss() {
        popupWindow.dismiss();
        return this;
    }

    public static class MenuItem {
        String name;
        CustomMenu parent;
        CustomMenu child;
        int icon = -1;
//        OnClickListener<MenuItem> onClickListener;
//        public interface OnClickListener<T> {
//            void runOnClickListener(CustomRecycler customRecycler, View itemView, T t, int index);
//        }
        private Object content;

        public MenuItem(String name) {
            this.name = name;
        }

        public MenuItem(String name, Object content) {
            this.name = name;
            this.content = content;
        }

        public MenuItem(String name, Object content, int icon) {
            this.name = name;
            this.content = content;
            this.icon = icon;
        }

        public MenuItem setName(String name) {
            this.name = name;
            return this;
        }

        public String getName() {
            return name;
        }

        public MenuItem setSubMenus(CustomMenu parent, SetSubMenus setSubMenus) {
            List<MenuItem> subMenus = new ArrayList<>();
            child = CustomMenu.Builder(parent.context);
            child.parent = this;
            setSubMenus.runSetSubMenus(child, subMenus);
            subMenus.forEach(item -> item.setParent(parent));
            child.setMenus((customMenu, items) -> items.addAll(subMenus));
            return this;
        }
        public interface SetSubMenus {
            void runSetSubMenus(CustomMenu customMenu, List<MenuItem> items);
        }

//        public List<MenuItem> getSubMenus() {
//            return subMenus;
//        }

        public Object getContent() {
            return content;
        }

        public MenuItem setContent(Object content) {
            this.content = content;
            return this;
        }

        public CustomMenu getParent() {
            return parent;
        }

        public MenuItem setParent(CustomMenu parent) {
            this.parent = parent;
            return this;
        }

        public CustomMenu getChild() {
            return child;
        }

        //        public MenuItem setOnClickListener(OnClickListener<MenuItem> onClickListener) {
//            this.onClickListener = onClickListener;
//            return this;
//        }
    }
}
