package com.maxMustermannGeheim.linkcollection.Daten.Media;

import com.finn.androidUtilities.CustomList;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;

import java.util.UUID;

public class Media extends ParentClass {
    private String path;
    private CustomList<MediaPerson> personList = new CustomList<>();

    public Media() {
    }

    public Media(String name) {
        uuid = "media_" + UUID.randomUUID().toString();
        this.name = name;
    }

}
