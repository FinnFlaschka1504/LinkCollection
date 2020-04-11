package com.maxMustermannGeheim.linkcollection.Daten.Videos;

import com.finn.androidUtilities.CustomList;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Image;

import java.util.UUID;

public class Collection extends ParentClass_Image {
    private CustomList<String> filmIdList = new CustomList<>();


    public Collection(String name) {
        uuid = "collection_" + UUID.randomUUID().toString();
        this.name = name;
    }

    public Collection() {
    }

    public CustomList<String> getFilmIdList() {
        return filmIdList;
    }

    public Collection setFilmIdList(CustomList<String> filmIdList) {
        this.filmIdList = filmIdList;
        return this;
    }

    @Override
    public Collection clone() {
        return (Collection) super.clone();
    }
}
