package com.maxMustermannGeheim.linkcollection.Daten;

import java.util.UUID;

public class Genre {
    private String uuid = "genre_" + UUID.randomUUID().toString();

    public Genre(String name) {
        this.name = name;
    }

    public Genre() {
    }

    private String name;

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Genre setName(String name) {
        this.name = name;
        return this;
    }
}
