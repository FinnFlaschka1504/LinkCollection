package com.maxMustermannGeheim.linkcollection.Daten;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.allyants.draggabletreeview.DraggableTreeView;
import com.allyants.draggabletreeview.SimpleTreeViewAdapter;
import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomList;
import com.finn.androidUtilities.CustomUtility;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomTreeNodeHolder;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomTreeViewAdapter;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public interface ParentClass_Tree {
    /*
    private List<? extends ParentClass_Tree> children = new ArrayList<>();
    private String parentId;
    */
    //  ------------------------- Tree ------------------------->
    default ParentClass_Tree addChild(ParentClass_Tree child) {
        child.setParentId(((ParentClass) this).getUuid());
        getChildren().add(child);
        return this;
    }

    default ParentClass_Tree addChildren(List<ParentClass_Tree> children) {
        children.forEach(child -> child.setParentId(((ParentClass) this).getUuid()));
        this.getChildren().addAll(children);
        return this;
    }
    
    static TreeNode getFilteredCompleteTree(Context context, CategoriesActivity.CATEGORIES category, String searchQuery,
                                            @Nullable CustomList<String> selectedIds, AndroidTreeView tView, Comparator<ParentClass> comparator,
                                            @Nullable Utility.DoubleGenericInterface<ViewGroup, TreeNode> customLayoutAdjustments){
        TreeNode root = TreeNode.root();

        for (ParentClass parentClass : new CustomList<>(((Map<String, ParentClass>) Utility.getMapFromDatabase(category)).values()).sorted(comparator)) {
            TreeNode childNode = ((ParentClass_Tree) parentClass)._getRecursiveTree(context, searchQuery, selectedIds, tView, comparator, customLayoutAdjustments);
            if (childNode != null)
                root.addChild(childNode);
        }

        return root;
    }

    default TreeNode _getRecursiveTree(Context context, String searchQuery,
                                       @Nullable CustomList<String> selectedIds, AndroidTreeView tView, Comparator<ParentClass> comparator,
                                       @Nullable Utility.DoubleGenericInterface<ViewGroup, TreeNode> customLayoutAdjustments) {
        boolean matches = true;
        TreeNode treeNode = new TreeNode(this)
                .setViewHolder(new CustomTreeNodeHolder(context, tView, customLayoutAdjustments));
        if (CustomUtility.stringExists(searchQuery)) {
            matches = ((ParentClass) this).getName().toLowerCase().contains(searchQuery.toLowerCase());
            if (getChildren().isEmpty() && !matches)
                return null;
            else if (!getChildren().isEmpty()) {
                List<TreeNode> list = getChildren()
                        .stream()
                        .sorted((o1, o2) -> comparator.compare((ParentClass) o1, (ParentClass) o2))
                        .map(parentClass_tree -> parentClass_tree._getRecursiveTree(context, searchQuery, selectedIds, tView, comparator, customLayoutAdjustments))
                        .filter(obj -> !Objects.isNull(obj))
                        .collect(Collectors.toList());

                if (list.isEmpty() && !matches)
                    return null;

                treeNode.addChildren(list);
            }


        } else {
            treeNode
                    .addChildren(getChildren()
                            .stream()
                            .map(parentClass_tree -> parentClass_tree._getRecursiveTree(context, searchQuery, selectedIds, tView, comparator, customLayoutAdjustments))
                            .collect(Collectors.toList()));
        }
        treeNode.setSelectable(selectedIds != null && matches);
        treeNode.setSelected(selectedIds != null && selectedIds.contains(((ParentClass) this).getUuid()));
        return treeNode;
    }

    static CustomUtility.Triple<AndroidTreeView, View,TreeNode> buildTreeView(ViewGroup container, CategoriesActivity.CATEGORIES category,
                                                                              @Nullable CustomList<String> selectedIds, String searchQuery,
                                                                              @Nullable Runnable updateSelectedRecycler, Comparator<ParentClass> comparator, TextView emptyTextView,
                                                                              @Nullable Pair<TreeNode.TreeNodeClickListener, TreeNode.TreeNodeLongClickListener> clickListenerPair,
                                                                              @Nullable Utility.DoubleGenericInterface<ViewGroup, TreeNode> customLayoutAdjustments) {
        Context context = container.getContext();
        container.removeAllViews();

        AndroidTreeView tView = new AndroidTreeView(context);
        tView.setUseAutoToggle(false);
        TreeNode completeTree = getFilteredCompleteTree(context, category, searchQuery, selectedIds, tView, comparator, customLayoutAdjustments);
        tView.setRoot(completeTree);

        if (completeTree.getChildren().isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText(String.format("Keine %s %s", category.getPlural(), Utility.getMapFromDatabase(category).isEmpty() ? "hinzugefügt" : "für diese Suche"));
        } else
            emptyTextView.setVisibility(View.GONE);

        Utility.GenericInterface<TreeNode> updateSelectedList = treeNode -> {
            String uuid = ((ParentClass) treeNode.getValue()).getUuid();
            if (treeNode.isSelected())
                selectedIds.add(uuid);
            else
                selectedIds.remove(uuid);
            CustomUtility.runRunnable(updateSelectedRecycler);
        };

        Utility.DoubleGenericInterface<TreeNode, TreeNode> manageSelection = (root, node) -> {
            for (TreeNode child : root.getChildren()) {
                if (((ParentClass_Tree) child.getValue()).manageSelection(child, node, updateSelectedList)) {
                    break;
                }
            }
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
                        updateSelectedList.run(node);
                    } else
                        manageSelection.run(completeTree, node);

                }
            });

            tView.setDefaultNodeLongClickListener((node, value) -> {
                addNew(context, (ParentClass_Tree) value, searchQuery, category, newObject -> {
//                    selectedIds.add(newObject.getUuid());
//                    completeTree.sel
//                    updateSelectedRecycler.run();
                    CustomUtility.Triple<AndroidTreeView, View,TreeNode> triple = buildTreeView(container, category, selectedIds, searchQuery, updateSelectedRecycler, comparator, emptyTextView, clickListenerPair, null);

                    TreeNode root = triple.third;

                    TreeNode foundTreeNode = Utility.runRecursiveGenericReturnInterface(root, TreeNode.class, (treeNode, recursiveInterface) -> {
                        for (TreeNode child : treeNode.getChildren()) {
                            if (child.getValue() == newObject)
                                return child;
                            else if (!child.isLeaf()) {
                                TreeNode returnNode = recursiveInterface.run(child, recursiveInterface);
                                if (returnNode != null)
                                    return returnNode;
                            }
                        }
                        return null;
                    });

                    manageSelection.run(root, foundTreeNode);

                });
                return true;
            });
        }


//        View view = tView.getView();
////        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        container.addView(view);
        View view = tView.getView();
        container.addView(view);
        tView.expandAll();
        updateAll(completeTree);
        return CustomUtility.Triple.create(tView, view, completeTree);
    }

    static void addNew(Context context, @Nullable ParentClass_Tree parent, String name, CategoriesActivity.CATEGORIES category, Utility.GenericInterface<ParentClass_Tree> onAdded) {
        CustomDialog.Builder(context)
                .setTitle(category.getSingular() + " Hinzufügen")
                .addOptionalModifications(customDialog -> {
                    if (parent == null)
                        customDialog.setText("Mit langem Klicken auf eine Kategorie können Unterkategorien hinzugefügt werden.");
                    else
                        customDialog.setText("Neue Subkategorie zu " + ((ParentClass) parent).getName() + " hinzufügen");
                })
                .setEdit(new CustomDialog.EditBuilder().setHint(category.getSingular() + " Name").setText(name).setValidation((validator, text) -> {
                    if (Utility.findObjectByName(category, text) != null)
                        validator.setInvalid(category.getSingular() + " bereits vorhanden");
                }))
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                    String text = customDialog.getEditText();
                    ParentClass_Tree newObject = (ParentClass_Tree) ParentClass.newCategory(category, text);
                    if (parent != null) {
                        parent.addChild(newObject);
                    } else {
                        ((Map<String, ParentClass>) Utility.getMapFromDatabase(category)).put(((ParentClass) newObject).getUuid(), (ParentClass) newObject);
                    }
                    Toast.makeText(context, (Database.saveAll_simple() ? "" : "Nichts") + " Gespeichert", Toast.LENGTH_SHORT).show();
                    onAdded.run(newObject);
                })
                .disableLastAddedButton()
                .show();

    }

    default boolean manageSelection(TreeNode root, TreeNode selected, Utility.GenericInterface<TreeNode> changedSelection) {
        if (selected == root) {
            if (!isChildSelected(root)) {
                selected.setSelected(true);
                ((CustomTreeNodeHolder) selected.getViewHolder()).update();
                changedSelection.run(selected);
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
                        changedSelection.run(root);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    default boolean isChildSelected(TreeNode root) {
        if (root.isSelected())
            return true;
        return root.getChildren().stream().anyMatch(this::isChildSelected);
    }

    static void updateAll(TreeNode root){
        if (root.getViewHolder() instanceof CustomTreeNodeHolder)
            ((CustomTreeNodeHolder) root.getViewHolder()).update();
        root.getChildren().forEach(ParentClass_Tree::updateAll);
    }

    // ---------------

    static void rebuildMap(CategoriesActivity.CATEGORIES category, com.allyants.draggabletreeview.TreeNode root) {
        Map<String, ParentClass> map = (Map<String, ParentClass>) Utility.getMapFromDatabase(category);
        getAll(category).forEach(parentClass_tree -> {
            parentClass_tree.setParentId(null);
            parentClass_tree.getChildren().clear();
        });
        map.clear();

        for (com.allyants.draggabletreeview.TreeNode baseChild : root.getChildren()) {
            ParentClass_Tree data = (ParentClass_Tree) baseChild.getData();
            map.put(((ParentClass) data).getUuid(), (ParentClass) data);
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

    static void showReorderTreeDialog(Context context, CategoriesActivity.CATEGORIES category, @Nullable CustomUtility.GenericInterface<CustomDialog> onSaved) {
        // ToDo: herausfinden warum Elemente verschwinden wenn ganz am Rand losgelassen
        CustomDialog.Builder(context)
                .setTitle(category.getSingular() + " Baum Bearbeiten")
                .setView(R.layout.dialog_edit_tree)
                .setDimensionsFullscreen()
                .disableScroll()
                .setSetViewContent((customDialog, view, reload) -> {
                    DraggableTreeView treeView = view.findViewById(R.id.dialog_editTree_treeView);
                    try {
                        Field sideMargin = DraggableTreeView.class.getDeclaredField("sideMargin");
                        sideMargin.setAccessible(true);
                        sideMargin.set(treeView, 35);
                    } catch (NoSuchFieldException | IllegalAccessException ignored) {}

                    com.maxMustermannGeheim.linkcollection.Utilities.CustomList<? extends ParentClass> list = new com.maxMustermannGeheim.linkcollection.Utilities.CustomList<>(Utility.getMapFromDatabase(category).values()).sorted((o1, o2) ->  o1.getName().compareTo(o2.getName()));

                    com.allyants.draggabletreeview.TreeNode root = new com.allyants.draggabletreeview.TreeNode(context);
                    customDialog.setPayload(root);

                    for (ParentClass object : list) {
                        Utility.runRecursiveGenericInterface(Pair.create(root, (ParentClass_Tree) object), (pair, recursiveInterface) -> {
                            com.allyants.draggabletreeview.TreeNode childNode = new com.allyants.draggabletreeview.TreeNode(pair.second);
                            pair.first.addChild(childNode);
                            if (!pair.second.getChildren().isEmpty())
                                pair.second.getChildren()
                                        .stream().sorted((o1, o2) -> ((ParentClass) o1).getName().compareTo(((ParentClass) o2).getName()))
                                        .forEach(childObject -> recursiveInterface.run(Pair.create(childNode, childObject), recursiveInterface));
                        });
                    }

                    SimpleTreeViewAdapter adapter = new CustomTreeViewAdapter(context, root);
//                    TextView textView = new TextView(context);
//                    textView.setText("Test");
//                    adapter.setPlaceholder(textView);
                    treeView.setAdapter(adapter);
                })
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                    com.allyants.draggabletreeview.TreeNode root = (com.allyants.draggabletreeview.TreeNode) customDialog.getPayload();
                    ParentClass_Tree.rebuildMap(category, root);
                    Database.saveAll(context);
                    CustomUtility.runGenericInterface(onSaved, customDialog);
                })
                .show();
    }
    //  <------------------------- Tree -------------------------


    //  ------------------------- Getter & Setter ------------------------->
    default List<ParentClass_Tree> getChildren() {
        try {
            Field children = getClass().getDeclaredField("children");
            children.setAccessible(true);
            return (List<ParentClass_Tree>) children.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
        }
        return null;
    }

    default ParentClass_Tree setChildren(List<? extends ParentClass_Tree> children) {
        try {
            Field field = getClass().getDeclaredField("children");
            field.setAccessible(true);
            field.set(this, children);
        } catch (NoSuchFieldException | IllegalAccessException e) {
        }
        return this;
    }

    default String getParentId() {
        try {
            Field children = getClass().getDeclaredField("parentId");
            children.setAccessible(true);
            return (String) children.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
        }
        return null;
    }

    default ParentClass_Tree setParentId(String parentId) {
        try {
            Field field = getClass().getDeclaredField("parentId");
            field.setAccessible(true);
            field.set(this, parentId);
        } catch (NoSuchFieldException | IllegalAccessException e) {
        }
        return this;
    }

//    static public String getName(ParentClass_Tree parentClass_tree) {
//        try {
//            Field children = parentClass_tree.getClass().getDeclaredField("name");
//            children.setAccessible(true);
//            return (String) children.get(parentClass_tree);
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//        }
//        return null;
//    }
//
//    static public ParentClass_Tree setName(ParentClass_Tree parentClass_tree, String parentId) {
//        try {
//            Field field = parentClass_tree.getClass().getDeclaredField("name");
//            field.setAccessible(true);
//            field.set(parentClass_tree, parentId);
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//        }
//        return parentClass_tree;
//    }
//
//    default public String getUuid() {
//        try {
//            Field children = getClass().getDeclaredField("uuid");
//            children.setAccessible(true);
//            return (String) children.get(this);
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//        }
//        return null;
//    }
//
//    default public ParentClass_Tree setUuid(String parentId) {
//        try {
//            Field field = getClass().getDeclaredField("uuid");
//            field.setAccessible(true);
//            field.set(this, parentId);
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//        }
//        return this;
//    }
    //  <------------------------- Getter & Setter -------------------------

    //  ------------------------- Convenience ------------------------->
    static ParentClass findObjectById(CategoriesActivity.CATEGORIES category, String id) {
        Collection<ParentClass> values = (Collection<ParentClass>) Utility.getMapFromDatabase(category).values();
        for (ParentClass parentClass : values) {
            ParentClass_Tree child;
            if ((child = ((ParentClass_Tree) parentClass).findObjectById(id)) != null)
                return (ParentClass) child;
        }
        return null;
    }

    default ParentClass_Tree findObjectById(String id) {
        if (((ParentClass) this).getUuid().equals(id))
            return this;
        for (ParentClass_Tree parentClass : getChildren()) {
            ParentClass_Tree child;
            if ((child = parentClass.findObjectById(id)) != null)
                return child;
        }
        return null;
    }

    static ParentClass_Tree findObjectByName(CategoriesActivity.CATEGORIES category, String name, boolean ignoreCase) {
        Collection<ParentClass> values = (Collection<ParentClass>) Utility.getMapFromDatabase(category).values();
        for (ParentClass parentClass : values) {
            ParentClass_Tree child;
            if ((child = ((ParentClass_Tree) parentClass).findObjectByName(name, ignoreCase)) != null)
                return child;
        }
        return null;
    }

    default ParentClass_Tree findObjectByName(String name, boolean ignoreCase) {
        if (ignoreCase ? (((ParentClass) this).getName().equalsIgnoreCase(name)) : (((ParentClass) this).getName().equals(name)))
            return this;
        for (ParentClass_Tree parentClass : getChildren()) {
            ParentClass_Tree child;
            if ((child = parentClass.findObjectByName(name, ignoreCase)) != null)
                return child;
        }
        return null;
    }

    // ---------------

    static int getAllCount(TreeNode root, String searchQuery){
        int nodeValue = root.isRoot() || (CustomUtility.stringExists(searchQuery) && !Utility.containsIgnoreCase(((ParentClass) root.getValue()).getName(), searchQuery)) ? 0 : 1;
        return root.getChildren().stream().mapToInt(treeNode -> getAllCount(treeNode, searchQuery)).sum() + nodeValue;
    }

    static int getAllCount(CategoriesActivity.CATEGORIES category){
        Collection<ParentClass> values = (Collection<ParentClass>) Utility.getMapFromDatabase(category).values();
        return values.stream().mapToInt(value -> ((ParentClass_Tree) value)._getAllCount()).sum();
    }

    default int _getAllCount() {
        return getChildren().stream().mapToInt(ParentClass_Tree::_getAllCount).sum() + 1;
    }

    // ---------------

    static List<ParentClass_Tree> getAll(CategoriesActivity.CATEGORIES category) {
        Collection<ParentClass> values = (Collection<ParentClass>) Utility.getMapFromDatabase(category).values();
        List<ParentClass_Tree> list = CustomUtility.concatenateCollections(values.stream().map(parentClass -> ((ParentClass_Tree) parentClass)._getAll()).collect(Collectors.toList()));
        return list;
    }

    default List<ParentClass_Tree> _getAll() {
        List<ParentClass_Tree> list = CustomUtility.concatenateCollections(getChildren().stream().map(ParentClass_Tree::_getAll).collect(Collectors.toCollection(CustomList::new)));
        list.add(this);
        return list;
    }

    // ---------------

    default <R> R reduce(R initialValue, CustomUtility.DoubleGenericReturnInterface<R, ParentClass_Tree, R> reducer) {
        final R[] value = (R[]) new Object[] {initialValue};
        _getAll().stream().forEach(parentClass_tree -> {
            value[0] = reducer.run(initialValue, parentClass_tree);
        });

        return value[0];
    }

    //  <------------------------- Convenience -------------------------
}
