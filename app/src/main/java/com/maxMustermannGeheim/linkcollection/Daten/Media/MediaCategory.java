package com.maxMustermannGeheim.linkcollection.Daten.Media;

import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MediaCategory extends ParentClass implements ParentClass_Tree {
    private List<MediaCategory> children = new ArrayList<>();
    private String parentId;

    public MediaCategory() {
    }

    public MediaCategory(String name) {
        uuid = "mediaCategory_" + UUID.randomUUID().toString();
        this.name = name;
    }

//    @Override
//    public List<MediaCategory> getChildren() {
//        return (List<MediaCategory>) ParentClass_Tree.super.getChildren();
//    }

    //    public static ParentClass_Tree findObjectById(String id) {
//        return findObjectById(CategoriesActivity.CATEGORIES.MEDIA_CATEGORY, id);
//    }
}
