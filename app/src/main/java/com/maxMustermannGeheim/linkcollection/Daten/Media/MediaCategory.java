package com.maxMustermannGeheim.linkcollection.Daten.Media;

import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tree;

import java.util.UUID;

public class MediaCategory extends ParentClass_Tree {
    public MediaCategory() {
    }

    public MediaCategory(String name) {
        uuid = "mediaCategory_" + UUID.randomUUID().toString();
        this.name = name;
    }

    public static ParentClass_Tree findObjectById(String id) {
        return findObjectById(CategoriesActivity.CATEGORIES.MEDIA_CATEGORY, id);
    }
}
