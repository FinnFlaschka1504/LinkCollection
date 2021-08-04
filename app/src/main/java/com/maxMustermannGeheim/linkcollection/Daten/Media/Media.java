package com.maxMustermannGeheim.linkcollection.Daten.Media;

import com.finn.androidUtilities.CustomList;
import com.finn.androidUtilities.CustomUtility;
import com.finn.androidUtilities.Helpers;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Image;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tree;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Media extends ParentClass_Image {
    private List<String> personIdList = new CustomList<>();
    private List<String> categoryIdList = new CustomList<>();


    //  ------------------------- Constructor ------------------------->
    public Media() {
    }

    public Media(String imagePath) {
        uuid = "media_" + UUID.randomUUID().toString();
        this.setImagePath(imagePath);
    }
    //  <------------------------- Constructor -------------------------


    //  ------------------------- Getter & Setter ------------------------->
    public List<String> getPersonIdList() {
        return personIdList;
    }

    public Media setPersonIdList(CustomList<String> personIdList) {
        this.personIdList = personIdList;
        return this;
    }

    public List<String> getCategoryIdList() {
        return categoryIdList;
    }

    public Media setCategoryIdList(List<String> categoryIdList) {
        this.categoryIdList = categoryIdList;
        return this;
    }
    //  <------------------------- Getter & Setter -------------------------

    //  ------------------------- Convenience ------------------------->
    public CharSequence _getDescription() {
        Helpers.SpannableStringHelper helper = new Helpers.SpannableStringHelper();
        boolean needsLineBreak = false;

        Map<String, MediaPerson> mediaPersonMap = Database.getInstance().mediaPersonMap;
        String persons = getPersonIdList().stream().map(id -> mediaPersonMap.get(id).getName()).collect(Collectors.joining(", "));
        if (CustomUtility.stringExists(persons)) {
            needsLineBreak = true;
            helper.appendItalic("P: ").append(persons);
        }

        if (needsLineBreak) {
            needsLineBreak = false;
            helper.append("\n");
        }
        String categories = getCategoryIdList().stream().map(id -> ParentClass_Tree.findObjectById(CategoriesActivity.CATEGORIES.MEDIA_CATEGORY, id).getName()).collect(Collectors.joining(", "));
        if (CustomUtility.stringExists(categories)) {
            needsLineBreak = true;
            helper.appendItalic("K: ").append(categories);
        }

        if (needsLineBreak) {
            needsLineBreak = false;
            helper.append("\n");
        }
        helper.appendItalic("D: ").append(Utility.formatDate("dd.MM.yyyy", new Date(new File(getImagePath()).lastModified())));
        return helper.get();
    }
    //  <------------------------- Convenience -------------------------
    @Override
    public Media clone() {
        Media clone = (Media) super.clone();
        clone.personIdList = new CustomList<>(personIdList);
        return clone;

    }

}
