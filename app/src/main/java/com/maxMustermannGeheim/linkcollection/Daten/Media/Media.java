package com.maxMustermannGeheim.linkcollection.Daten.Media;

import com.finn.androidUtilities.CustomList;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Image;

import java.util.List;
import java.util.UUID;

public class Media extends ParentClass_Image {
    private List<String> personIdList = new CustomList<>();


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
    //  <------------------------- Getter & Setter -------------------------

    @Override
    public Media clone() {
        Media clone = (Media) super.clone();
        clone.personIdList = new CustomList<>(personIdList);
        return clone;

    }

}
