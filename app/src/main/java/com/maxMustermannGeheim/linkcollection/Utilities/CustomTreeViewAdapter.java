package com.maxMustermannGeheim.linkcollection.Utilities;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.allyants.draggabletreeview.SimpleTreeViewAdapter;
import com.allyants.draggabletreeview.TreeNode;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tree;

public class CustomTreeViewAdapter extends SimpleTreeViewAdapter {
    public CustomTreeViewAdapter(Context context, TreeNode root) {
        super(context, root);
    }

    public View createTreeView(Context context, TreeNode node, Object data, int level, boolean hasChildren) {
        View view = View.inflate(context, com.allyants.draggabletreeview.R.layout.tree_view_item, (ViewGroup)null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        view.setLayoutParams(layoutParams);
        TextView textView = (TextView)view.findViewById(com.allyants.draggabletreeview.R.id.textView);
        textView.setText(((ParentClass) data).getName());
        return view;
    }
}
