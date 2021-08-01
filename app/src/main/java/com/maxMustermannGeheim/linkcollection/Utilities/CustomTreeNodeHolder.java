package com.maxMustermannGeheim.linkcollection.Utilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.finn.androidUtilities.CustomUtility;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tree;
import com.maxMustermannGeheim.linkcollection.R;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

public class CustomTreeNodeHolder extends TreeNode.BaseNodeViewHolder<ParentClass_Tree> {
    TreeNode node;
    AndroidTreeView tView;
    Utility.DoubleGenericInterface<ViewGroup,TreeNode> customLayoutAdjustments;

    public CustomTreeNodeHolder(Context context, AndroidTreeView tView, @Nullable Utility.DoubleGenericInterface<ViewGroup,TreeNode> customLayoutAdjustments) {
        super(context);
        this.tView = tView;
        this.customLayoutAdjustments = customLayoutAdjustments;
    }

    @Override
    public View createNodeView(TreeNode node, ParentClass_Tree parentClass) {
        this.node = node;
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.custom_tree_node_layout, null, false);
        setViewContent(view, node, tView, customLayoutAdjustments);
        return view;
    }

    public static void setViewContent(View view, TreeNode node, AndroidTreeView tView, @Nullable Utility.DoubleGenericInterface<ViewGroup,TreeNode> customLayoutAdjustments) {
        ParentClass_Tree parentClass = (ParentClass_Tree) node.getValue();
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.findViewById(R.id.customTreeNode_inset).setMinimumWidth((node.getLevel() - 1)/*parentClass._getTempLevel()*/ * CustomUtility.dpToPx(24));

        ImageView expandButton = view.findViewById(R.id.customTreeNode_expandButton);
        FrameLayout parent = (FrameLayout) expandButton.getParent();
        if (node.isLeaf()) {
            expandButton.setVisibility(View.GONE);
        } else {
            expandButton.setVisibility(View.VISIBLE);
            expandButton.setImageResource(node.isExpanded() ? R.drawable.ic_remove : R.drawable.ic_add);
            parent.setOnClickListener(v -> {
                if (node.isExpanded())
                    tView.collapseNode(node);
                else
                    tView.expandNode(node);
                expandButton.setImageResource(node.isExpanded() ? R.drawable.ic_remove : R.drawable.ic_add);
            });
        }

        String name = parentClass.getName();
        ((TextView) view.findViewById(R.id.customTreeNode_text_main)).setText(name);

        CheckBox checkBox = view.findViewById(R.id.customTreeNode_checked);
        if (node.isSelectable()) {
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setChecked(node.isSelected());
        } else
            checkBox.setVisibility(View.GONE);
        
        Utility.runDoubleGenericInterface(customLayoutAdjustments, (ViewGroup) view, node);
    }

    public void update() {
        setViewContent(getView(), node, tView, customLayoutAdjustments);
    }
}