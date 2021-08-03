package com.maxMustermannGeheim.linkcollection.Daten;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomList;
import com.finn.androidUtilities.CustomUtility;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Media.MediaCategory;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomTreeNodeHolder;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    
    public static TreeNode getFilteredCompleteTree(Context context, CategoriesActivity.CATEGORIES category, String searchQuery, 
                                                   @Nullable CustomList<String> selectedIds, AndroidTreeView tView, Comparator<ParentClass_Tree> comparator, 
                                                   @Nullable Utility.DoubleGenericInterface<ViewGroup,TreeNode> customLayoutAdjustments){
        TreeNode root = TreeNode.root();

        for (ParentClass_Tree parentClass : new CustomList<>(((Map<String, ParentClass_Tree>) Utility.getMapFromDatabase(category)).values()).sorted(comparator)) {
            TreeNode childNode = parentClass._getRecursiveTree(context, searchQuery, selectedIds, tView, comparator, customLayoutAdjustments);
            if (childNode != null)
                root.addChild(childNode);
        }

        return root;
    }

    public TreeNode _getRecursiveTree(Context context, String searchQuery, 
                                      @Nullable CustomList<String> selectedIds, AndroidTreeView tView, Comparator<ParentClass_Tree> comparator, 
                                      @Nullable Utility.DoubleGenericInterface<ViewGroup,TreeNode> customLayoutAdjustments) {
        boolean matches = true;
        TreeNode treeNode = new TreeNode(this)
                .setViewHolder(new CustomTreeNodeHolder(context, tView, customLayoutAdjustments));
        if (CustomUtility.stringExists(searchQuery)) {
            matches = name.toLowerCase().contains(searchQuery.toLowerCase());
            if (children.isEmpty() && !matches)
                return null;
            else if (!children.isEmpty()) {
                List<TreeNode> list = children
                        .stream()
                        .sorted(comparator)
                        .map(parentClass_tree -> parentClass_tree._getRecursiveTree(context, searchQuery, selectedIds, tView, comparator, customLayoutAdjustments))
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
                            .map(parentClass_tree -> parentClass_tree._getRecursiveTree(context, searchQuery, selectedIds, tView, comparator, customLayoutAdjustments))
                            .collect(Collectors.toList()));
        }
        treeNode.setSelectable(selectedIds != null && matches);
        treeNode.setSelected(selectedIds != null && selectedIds.contains(uuid));
        return treeNode;
    }

    public static Pair<AndroidTreeView,TreeNode> buildTreeView(ViewGroup container, CategoriesActivity.CATEGORIES category,
                                     @Nullable CustomList<String> selectedIds, String searchQuery,
                                     @Nullable Runnable updateSelectedRecycler, Comparator<ParentClass_Tree> comparator, TextView emptyTextView, 
                                     @Nullable Pair<TreeNode.TreeNodeClickListener, TreeNode.TreeNodeLongClickListener> clickListenerPair, 
                                     @Nullable Utility.DoubleGenericInterface<ViewGroup,TreeNode> customLayoutAdjustments) {
        Context context = container.getContext();
        container.removeAllViews();

        AndroidTreeView tView = new AndroidTreeView(context);
        tView.setUseAutoToggle(false);
        TreeNode completeTree = MediaCategory.getFilteredCompleteTree(context, category, searchQuery, selectedIds, tView, comparator, customLayoutAdjustments);
        tView.setRoot(completeTree);

        if (completeTree.getChildren().isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText(String.format("Keine %s %s", category.getPlural(), Utility.getMapFromDatabase(category).isEmpty() ? "hinzugefügt" : "für diese Suche"));
        } else
            emptyTextView.setVisibility(View.GONE);

        Utility.GenericInterface<TreeNode> updateSelectedList = treeNode -> {
            String uuid = ((ParentClass_Tree) treeNode.getValue()).getUuid();
            if (treeNode.isSelected())
                selectedIds.add(uuid);
            else
                selectedIds.remove(uuid);
            CustomUtility.runRunnable(updateSelectedRecycler);
        };

        if (clickListenerPair != null) {
            tView.setDefaultNodeClickListener(clickListenerPair.first);
            tView.setDefaultNodeLongClickListener(clickListenerPair.second);
        } else if (selectedIds != null){
            tView.setDefaultNodeClickListener((node, value) -> {
                if (node.isSelectable()) {
                    if (node.isSelected()) {
                        node.setSelected(false);
                        ((CustomTreeNodeHolder) node.getViewHolder()).update();
                        updateSelectedList.runGenericInterface(node);
                    } else {
                        for (TreeNode child : completeTree.getChildren()) {
                            if (((ParentClass_Tree) child.getValue()).manageSelection(child, node, updateSelectedList)) {
                                break;
                            }
                        }
                    }

                }
            });

            tView.setDefaultNodeLongClickListener((node, value) -> {
                addNew(context, (ParentClass_Tree) value, searchQuery, category, newObject -> {
                    selectedIds.add(newObject.getUuid());
                    updateSelectedRecycler.run();
                    buildTreeView(container, category, selectedIds, searchQuery, updateSelectedRecycler, comparator, emptyTextView, clickListenerPair, null);
                });
                return true;
            });
        }


//        View view = tView.getView();
////        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        container.addView(view);
        container.addView(tView.getView());
        tView.expandAll();
        updateAll(completeTree);
        return Pair.create(tView, completeTree);
    }

    public static void addNew(Context context, @Nullable ParentClass_Tree parent, String name, CategoriesActivity.CATEGORIES category, Utility.GenericInterface<ParentClass_Tree> onAdded) {
        CustomDialog.Builder(context)
                .setTitle(category.getSingular() + " Hinzufügen")
                .addOptionalModifications(customDialog -> {
                    if (parent == null)
                        customDialog.setText("Mit langem Klicken auf eine Kategorie können Unterkategorien hinzugefügt werden.");
                    else
                        customDialog.setText("Neue Subkategorie zu " + parent.getName() + " hinzufügen");
                })
                .setEdit(new CustomDialog.EditBuilder().setHint(category.getSingular() + " Name").setText(name))
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                    String text = customDialog.getEditText();
                    ParentClass_Tree newObject = (ParentClass_Tree) ParentClass.newCategory(category, text);
                    if (parent != null) {
                        parent.addChild(newObject);
                    } else {
                        ((Map<String, ParentClass>) Utility.getMapFromDatabase(category)).put(newObject.getUuid(), newObject);
                    }
                    Toast.makeText(context, (Database.saveAll_simple() ? "" : "Nichts") + " Gespeichert", Toast.LENGTH_SHORT).show();
                    onAdded.runGenericInterface(newObject);
                })
                .disableLastAddedButton()
                .show();

    }

    private boolean manageSelection(TreeNode root, TreeNode selected, Utility.GenericInterface<TreeNode> changedSelection) {
        if (selected == root) {
            if (!isChildSelected(root)) {
                selected.setSelected(true);
                ((CustomTreeNodeHolder) selected.getViewHolder()).update();
                changedSelection.runGenericInterface(selected);
            }
            return true;
        } else if (root.isLeaf()) {
            return false;
        } else {
            for (TreeNode child : root.getChildren()) {
                if (manageSelection(child, selected, changedSelection)) {
                    if (root.isSelected()) {
                        root.setSelected(false);
                        ((CustomTreeNodeHolder) root.getViewHolder()).update();
                        changedSelection.runGenericInterface(root);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    private boolean isChildSelected(TreeNode root) {
        if (root.isSelected())
            return true;
        return root.getChildren().stream().anyMatch(this::isChildSelected);
    }

    public static void updateAll(TreeNode root){
        if (root.getViewHolder() instanceof CustomTreeNodeHolder)
            ((CustomTreeNodeHolder) root.getViewHolder()).update();
        root.getChildren().forEach(ParentClass_Tree::updateAll);
    }

    // ---------------

    public static void rebuildMap(CategoriesActivity.CATEGORIES category, com.allyants.draggabletreeview.TreeNode root) {
        Map<String, ParentClass> map = (Map<String, ParentClass>) Utility.getMapFromDatabase(category);
        getAll(category).forEach(parentClass_tree -> {
            parentClass_tree.setParentId(null);
            parentClass_tree.getChildren().clear();
        });
        map.clear();

        for (com.allyants.draggabletreeview.TreeNode baseChild : root.getChildren()) {
            ParentClass_Tree data = (ParentClass_Tree) baseChild.getData();
            map.put(data.getUuid(), data);
            Utility.runRecursiveGenericInterface(baseChild, (parentNode, recursiveInterface) -> {
                ArrayList<com.allyants.draggabletreeview.TreeNode> children = parentNode.getChildren();
                if (children.isEmpty())
                    return;
                ParentClass_Tree parentObject = (ParentClass_Tree) parentNode.getData();
                for (com.allyants.draggabletreeview.TreeNode childNode : children) {
                    ParentClass_Tree childObject = (ParentClass_Tree) childNode.getData();
                    parentObject.addChild(childObject);
                    recursiveInterface.run(childNode, recursiveInterface);
                }
            });
        }
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

    public static int getAllCount(TreeNode root, String searchQuery){
        int nodeValue = root.isRoot() || (CustomUtility.stringExists(searchQuery) && !Utility.containsIgnoreCase(((ParentClass_Tree) root.getValue()).getName(), searchQuery)) ? 0 : 1;
        return root.getChildren().stream().mapToInt(treeNode -> getAllCount(treeNode, searchQuery)).sum() + nodeValue;
    }

    public static int getAllCount(CategoriesActivity.CATEGORIES category){
        Collection<ParentClass_Tree> values = (Collection<ParentClass_Tree>) Utility.getMapFromDatabase(category).values();
        return values.stream().mapToInt(ParentClass_Tree::_getAllCount).sum();
    }

    public int _getAllCount() {
        return children.stream().mapToInt(ParentClass_Tree::_getAllCount).sum() + 1;
    }

    public static List<ParentClass_Tree> getAll(CategoriesActivity.CATEGORIES category) {
        Collection<ParentClass_Tree> values = (Collection<ParentClass_Tree>) Utility.getMapFromDatabase(category).values();
        List<ParentClass_Tree> list = CustomUtility.concatenateCollections(values.stream().map(ParentClass_Tree::_getAll).collect(Collectors.toList()));
        return list;
    }

    public List<ParentClass_Tree> _getAll() {
        List<ParentClass_Tree> list = CustomUtility.concatenateCollections(children.stream().map(ParentClass_Tree::_getAll).collect(Collectors.toCollection(CustomList::new)));
        list.add(this);
        return list;
    }
    //  <------------------------- Convenience -------------------------
}
