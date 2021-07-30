package com.maxMustermannGeheim.linkcollection.Daten;

import android.content.Context;
import android.view.ViewGroup;

import com.finn.androidUtilities.CustomUtility;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Media.MediaCategory;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomList;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomTreeNodeHolder;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ParentClass_Tree extends ParentClass {
//    private int tempLevel;
    private List<ParentClass_Tree> children = new ArrayList<>();
    private String parentId;

    //  ------------------------- Tree ------------------------->
    public ParentClass_Tree addChild(ParentClass_Tree child) {
        child.parentId = uuid;
        children.add(child);
        return this;
    }

    public ParentClass_Tree addChildren(List<ParentClass_Tree> children) {
        children.forEach(child -> child.parentId = uuid);
        this.children.addAll(children);
        return this;
    }
    
    public static TreeNode getFilteredCompleteTree(Context context, String searchQuery, CustomList<String> selectedIds){
        Database database = Database.getInstance();
        TreeNode root = TreeNode.root();

        for (MediaCategory mediaCategory : database.mediaCategoryMap.values()) {
            TreeNode childNode = mediaCategory._getRecursiveTree(context, searchQuery, selectedIds);
            if (childNode != null)
                root.addChild(childNode);
        }

        return root;
    }

    public TreeNode _getRecursiveTree(Context context, String searchQuery, CustomList<String> selectedIds) {
        boolean matches = true;
        TreeNode treeNode = new TreeNode(this)
                .setViewHolder(new CustomTreeNodeHolder(context));
        if (CustomUtility.stringExists(searchQuery)) {
            matches = name.toLowerCase().contains(searchQuery.toLowerCase());
            if (children.isEmpty() && !matches)
                return null;
            else if (!children.isEmpty()) {
                List<TreeNode> list = children
                        .stream()
                        .map(parentClass_tree -> parentClass_tree._getRecursiveTree(context, searchQuery, selectedIds))
                        .filter(obj -> !Objects.isNull(obj))
                        .collect(Collectors.toList());

                if (list.isEmpty() && !matches)
                    return null;

                treeNode.addChildren(list);
            }


        } else {
            treeNode
                    .addChildren(children
                            .stream()
                            .map(parentClass_tree -> parentClass_tree._getRecursiveTree(context, searchQuery, selectedIds))
                            .collect(Collectors.toList()));
        }
        treeNode.setSelectable(matches);
        treeNode.setSelected(selectedIds.contains(uuid));
        return treeNode;
    }

    public static void buildTreeView(ViewGroup container, CustomList<String> selectedIds, String searchQuery, Runnable onSelectionChange) {
        Context context = container.getContext();
        container.removeAllViews();

        TreeNode completeTree = MediaCategory.getFilteredCompleteTree(context, searchQuery, selectedIds);
        AndroidTreeView tView = new AndroidTreeView(context, completeTree);
        tView.setUseAutoToggle(false);

        tView.setDefaultNodeClickListener((node, value) -> {
            if (node.isSelectable()) {
                node.setSelected(!node.isSelected());
                ((CustomTreeNodeHolder) node.getViewHolder()).update();
            }
        });
        tView.setDefaultNodeLongClickListener((node, value) -> {
            if (node.isSelectable()) {
                node.setSelected(!node.isSelected());
                ((CustomTreeNodeHolder) node.getViewHolder()).update();
            }
            return true;
        });

        container.addView(tView.getView());
        tView.expandAll();
    }
    //  <------------------------- Tree -------------------------


    //  ------------------------- Getter & Setter ------------------------->
    public List<ParentClass_Tree> getChildren() {
        return children;
    }

    public ParentClass_Tree setChildren(List<ParentClass_Tree> children) {
        this.children = children;
        return this;
    }

    public String getParentId() {
        return parentId;
    }

    public ParentClass_Tree setParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }
    //  <------------------------- Getter & Setter -------------------------

    //  ------------------------- Convenience ------------------------->
    public static ParentClass_Tree findObjectById(CategoriesActivity.CATEGORIES category, String id) {
        Collection<ParentClass_Tree> values = (Collection<ParentClass_Tree>) Utility.getMapFromDatabase(category).values();
        for (ParentClass_Tree parentClass : values) {
            ParentClass_Tree child;
            if ((child = parentClass.findObjectById(id)) != null)
                return child;
        }
        return null;
    }

    private ParentClass_Tree findObjectById(String id) {
        if (uuid.equals(id))
            return this;
        for (ParentClass_Tree parentClass : children) {
            ParentClass_Tree child;
            if ((child = parentClass.findObjectById(id)) != null)
                return child;
        }
        return null;
    }
    //  <------------------------- Convenience -------------------------
}
