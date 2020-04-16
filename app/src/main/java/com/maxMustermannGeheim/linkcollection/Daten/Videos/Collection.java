package com.maxMustermannGeheim.linkcollection.Daten.Videos;

import com.finn.androidUtilities.CustomList;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Image;

import java.util.List;
import java.util.UUID;

public class Collection extends ParentClass_Image {
    private List<String> filmIdList = new CustomList<>();
    private String listId;

    public Collection(String name) {
        uuid = "collection_" + UUID.randomUUID().toString();
        this.name = name;
    }

    public Collection() {
    }


    //  ------------------------- Getter & Setter ------------------------->
    public List<String> getFilmIdList() {
        return filmIdList;
    }

    public Collection setFilmIdList(List<String> filmIdList) {
        this.filmIdList = filmIdList;
        return this;
    }

    public String getListId() {
        return listId;
    }

    public Collection setListId(String listId) {
        this.listId = listId;
        return this;
    }
    //  <------------------------- Getter & Setter -------------------------

    @Override
    public Collection clone() {
        return (Collection) super.clone();
    }
}
