package com.maxMustermannGeheim.linkcollection.Daten;

import java.util.UUID;

public class Genre {
    private UUID uuid = UUID.randomUUID();

    public Genre(String name) {
        this.name = name;
    }

    private String name;

    public UUID getUuid() {
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
