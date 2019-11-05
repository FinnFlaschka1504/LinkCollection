package com.maxMustermannGeheim.linkcollection.Daten.Videos;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;

import java.util.UUID;

public class Genre extends ParentClass {

    public Genre(String name) {
        uuid = "genre_" + UUID.randomUUID().toString();
        this.name = name;
    }

    public Genre() {
    }

}
