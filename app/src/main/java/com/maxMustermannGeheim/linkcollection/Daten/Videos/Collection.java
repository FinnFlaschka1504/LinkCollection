package com.maxMustermannGeheim.linkcollection.Daten.Videos;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Image;

import java.util.UUID;

public class Collection extends ParentClass_Image {
    public Collection(String name) {
        uuid = "collection_" + UUID.randomUUID().toString();
        this.name = name;
    }

    public Collection() {
    }
}
