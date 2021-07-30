package com.maxMustermannGeheim.linkcollection.Utilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.finn.androidUtilities.CustomUtility;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tree;
import com.maxMustermannGeheim.linkcollection.R;
import com.unnamed.b.atv.model.TreeNode;

public class CustomTreeNodeHolder extends TreeNode.BaseNodeViewHolder<ParentClass_Tree> {
    TreeNode node;

    public CustomTreeNodeHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(TreeNode node, ParentClass_Tree parentClass) {
        this.node = node;
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.custom_tree_node_layout, null, false);
        setViewContent(view);
        return view;
    }

    private void setViewContent(View view) {
        ParentClass_Tree parentClass = (ParentClass_Tree) node.getValue();
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        TextView textMain = view.findViewById(R.id.customTreeNode_text_main);
        view.findViewById(R.id.customTreeNode_inset).setMinimumWidth((node.getLevel() - 1)/*parentClass._getTempLevel()*/ * CustomUtility.dpToPx(20));
        String name = parentClass.getName();
        ((TextView) view.findViewById(R.id.customTreeNode_text_main)).setText(name);

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.customTreeNode_checked);
        if (node.isSelectable()) {
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setChecked(node.isSelected());
        } else
            checkBox.setVisibility(View.GONE);
    }

    public void update() {
        setViewContent(getView());
    }

//    public static class CustomTreeNode {
//        private int level;
//        @DrawableRes public Integer icon;
//        public String text;
//        public String subText;
//
//        public static CustomTreeNode create(int level) {
//            return new CustomTreeNode().setLevel(level);
//        }
//
////        public static TreeNode newNode(Context context, String text) {
////            CustomTreeNode nodeItem = new CustomTreeNode();
////            nodeItem.text = text;
////            return new TreeNode(nodeItem).setViewHolder(new CustomTreeNodeHolder(context));
////        }
////
////        public static TreeNode newNode(Context context, String text, String subText) {
////            CustomTreeNode nodeItem = new CustomTreeNode();
////            nodeItem.text = text;
////            nodeItem.subText = subText;
////            return new TreeNode(nodeItem).setViewHolder(new CustomTreeNodeHolder(context));
////        }
//
//        public TreeNode build(Context context) {
//            return new TreeNode(this).setViewHolder(new CustomTreeNodeHolder(context));
//        }
//
//
//        //  ------------------------- Getter & Setter ------------------------->
//        public int getLevel() {
//            return level;
//        }
//
//        public CustomTreeNode setLevel(int level) {
//            this.level = level;
//            return this;
//        }
//
//        public int getIcon() {
//            return icon;
//        }
//
//        public CustomTreeNode setIcon(@DrawableRes int icon) {
//            this.icon = icon;
//            return this;
//        }
//
//        public String getText() {
//            return text;
//        }
//
//        public CustomTreeNode setText(String text) {
//            this.text = text;
//            return this;
//        }
//
//        public String getSubText() {
//            return subText;
//        }
//
//        public CustomTreeNode setSubText(String subText) {
//            this.subText = subText;
//            return this;
//        }
//        //  <------------------------- Getter & Setter -------------------------
//
//    }
}