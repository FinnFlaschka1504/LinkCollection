package com.maxMustermannGeheim.linkcollection.Daten;

import java.util.UUID;

public class Darsteller {
    private UUID uuid = UUID.randomUUID();

    private String name;

    public Darsteller(String name) {
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Darsteller setName(String name) {
        this.name = name;
        return this;
    }
}
